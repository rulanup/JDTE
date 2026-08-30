package com.jdte.common.upgrades;

import com.jdte.common.integrations.ae2.AE2CraftingReadNetwork;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AECraftingReadUpgradeTest {
    private static final GlobalPos TARGET = GlobalPos.of(
            ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                    ResourceLocation.withDefaultNamespace("overworld")),
            net.minecraft.core.BlockPos.ZERO);

    @Test
    void facadeResolvesBindingAndRejectsUnavailableStates() {
        AE2CraftingReadNetwork.clearCacheForTests();
        ItemStack upgrade = new ItemStack(Items.STICK);
        AtomicInteger reads = new AtomicInteger();
        AE2CraftingReadNetwork.TargetResolver resolver = target -> {
            reads.incrementAndGet();
            return new AE2CraftingReadNetwork.NetworkState("grid", true, true, false,
                    List.of(new AE2CraftingReadNetwork.CraftingCpuSnapshot(true, true)));
        };
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask((GlobalPos) null, 1, resolver));
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(TARGET, 1, resolver));
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(TARGET, 2, resolver));
        assertTrue(reads.get() >= 1);

        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(TARGET, 3,
                ignored -> new AE2CraftingReadNetwork.NetworkState("grid", false, true, false, List.of())));
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(upgrade, 4,
                ignored -> new AE2CraftingReadNetwork.NetworkState("grid", true, false, false, List.of())));
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(upgrade, 5,
                ignored -> new AE2CraftingReadNetwork.NetworkState("grid", true, true, true, List.of())));
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(upgrade, 6,
                ignored -> new AE2CraftingReadNetwork.NetworkState("grid", true, true, false, List.of())));
    }

    @Test
    void bindingGridIdentityAndExpiryInvalidateCache() {
        AE2CraftingReadNetwork.clearCacheForTests();
        ItemStack upgrade = new ItemStack(Items.STICK);
        AtomicInteger reads = new AtomicInteger();
        Object[] grid = {"one"};
        AE2CraftingReadNetwork.TargetResolver resolver = ignored -> {
            reads.incrementAndGet();
            return new AE2CraftingReadNetwork.NetworkState(grid[0], true, true, false,
                    List.of(new AE2CraftingReadNetwork.CraftingCpuSnapshot(true, true)));
        };
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(TARGET, 10, resolver));
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(TARGET, 11, resolver));
        grid[0] = "two";
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(TARGET, 12, resolver));
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(TARGET, 18, resolver));
        assertTrue(reads.get() >= 3);

        ItemStack other = upgrade.copy();
        AE2CraftingReadNetwork.bind(other,
                GlobalPos.of(TARGET.dimension(), net.minecraft.core.BlockPos.ZERO.offset(1, 0, 0)));
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(GlobalPos.of(TARGET.dimension(), net.minecraft.core.BlockPos.ZERO.offset(1, 0, 0)), 19, resolver));
    }
}
