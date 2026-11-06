package com.jdte.gametest;
import com.jdte.common.integrations.ae2.AE2CraftingReadNetwork;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;


import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class AECraftingReadCacheGameTests {
    @GameTest(template="mineral_empty") public static void fiveTickCacheExpiresWithoutReadingCpuEveryTick(GameTestHelper h) { var scenario = new Scenario(); scenario.reset(); scenario.fiveTickCacheExpiresWithoutReadingCpuEveryTick(); h.succeed(); }
    @GameTest(template="mineral_empty") public static void unloadedTargetInvalidatesAnOtherwiseActiveCache(GameTestHelper h) { var scenario = new Scenario(); scenario.reset(); scenario.unloadedTargetInvalidatesAnOtherwiseActiveCache(); h.succeed(); }
    @GameTest(template="mineral_empty") public static void inactiveAndBootingNetworksFailClosed(GameTestHelper h) { var scenario = new Scenario(); scenario.reset(); scenario.inactiveAndBootingNetworksFailClosed(); h.succeed(); }
    @GameTest(template="mineral_empty") public static void replacementGridDoesNotReusePreviousGridDecision(GameTestHelper h) { var scenario = new Scenario(); scenario.reset(); scenario.replacementGridDoesNotReusePreviousGridDecision(); h.succeed(); }
    @GameTest(template="mineral_empty") public static void independentServersDoNotShareCacheDecisions(GameTestHelper h) { var scenario = new Scenario(); scenario.reset(); scenario.independentServersDoNotShareCacheDecisions(); h.succeed(); }
    @GameTest(template="mineral_empty") public static void explicitUnlinkInvalidationClearsActiveDecision(GameTestHelper h) { var scenario = new Scenario(); scenario.reset(); scenario.explicitUnlinkInvalidationClearsActiveDecision(); h.succeed(); }
    private static void assertTrue(boolean value) { if(!value) throw new IllegalStateException("Expected active crafting decision"); }
    private static void assertFalse(boolean value) { if(value) throw new IllegalStateException("Expected paused crafting decision"); }
    private static void assertEquals(int expected,int actual) { if(expected!=actual) throw new IllegalStateException("CPU reads: expected "+expected+", got "+actual); }
    private static class Scenario {
    private final GlobalPos target = GlobalPos.of(Level.OVERWORLD,BlockPos.ZERO);
    private final Object scope = new Object(), grid = new Object();
    void reset() { AE2CraftingReadNetwork.clearCacheForTests(); }
    private AE2CraftingReadNetwork.NetworkState state(Object identity,boolean loaded,boolean active,boolean booting,boolean busy) {
        return new AE2CraftingReadNetwork.NetworkState(identity,loaded,active,booting,
                List.of(new AE2CraftingReadNetwork.CraftingCpuSnapshot(busy,busy)));
    }
    void fiveTickCacheExpiresWithoutReadingCpuEveryTick() {
        var reads = new AtomicInteger();
        Iterable<AE2CraftingReadNetwork.CraftingCpuSnapshot> cpus = () -> { reads.incrementAndGet();return List.of(new AE2CraftingReadNetwork.CraftingCpuSnapshot(true,true)).iterator(); };
        AE2CraftingReadNetwork.TargetResolver resolver = ignored -> new AE2CraftingReadNetwork.NetworkState(grid,true,true,false,cpus);
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,0,resolver));
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,4,resolver));assertEquals(1,reads.get());
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,5,resolver));assertEquals(2,reads.get());
    }
    void unloadedTargetInvalidatesAnOtherwiseActiveCache() {
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,0,x->state(grid,true,true,false,true)));
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,1,x->state(grid,false,true,false,true)));
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,2,x->state(grid,true,true,false,false)));
    }
    void inactiveAndBootingNetworksFailClosed() {
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,0,x->state(grid,true,false,false,true)));
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,1,x->state(grid,true,true,true,true)));
    }
    void replacementGridDoesNotReusePreviousGridDecision() {
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,0,x->state(grid,true,true,false,true)));
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,1,x->state(new Object(),true,true,false,false)));
    }
    void independentServersDoNotShareCacheDecisions() {
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,0,x->state(grid,true,true,false,true)));
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(new Object(),target,1,x->state(grid,true,true,false,false)));
    }
    void explicitUnlinkInvalidationClearsActiveDecision() {
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,0,x->state(grid,true,true,false,true)));
        AE2CraftingReadNetwork.invalidate(scope,target);
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(scope,target,1,x->state(grid,true,true,false,false)));
    }
}
}
