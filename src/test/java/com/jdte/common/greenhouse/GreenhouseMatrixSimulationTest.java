package com.jdte.common.greenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixSimulationTest {
    @Test
    void computesHarvestsForCompleteTicksFromA1024MultiplierBatch() {
        GreenhouseMatrixProductionProfile profile = new GreenhouseMatrixProductionProfile(
                GreenhouseMatrixProductionProfile.MachineKind.NORMAL,
                new ItemStack(Items.WHEAT_SEEDS), 64, "minecraft:wheat", 1L,
                1, 1, 0, false, false, 10, 1, 0, 0,
                false, false, 4_096, 512L);
        GreenhouseMatrixSimulation simulation = new GreenhouseMatrixSimulation();
        simulation.beginRebuild(List.of(BlockPos.ZERO), ignored -> List.of(profile));
        simulation.rebuildStep(1);

        simulation.advanceWork(1_020L, 4_096L * 51L, Long.MAX_VALUE);

        assertEquals(127L, simulation.totalPendingHarvests());
    }

    @Test
    void rebuildIsBoundedAndStableSettlementDoesNotResolveMembers() {
        List<BlockPos> members = new ArrayList<>();
        for (int index = 0; index < 3_000; index++) members.add(new BlockPos(index, 0, 0));
        GreenhouseMatrixProductionProfile profile = new GreenhouseMatrixProductionProfile(
                GreenhouseMatrixProductionProfile.MachineKind.NORMAL,
                new ItemStack(Items.WHEAT_SEEDS), 64, "minecraft:wheat", 1L,
                1, 1, 0, false, false, 10, 1, 0, 0,
                false, false, 4_096, 512L);
        AtomicInteger resolutions = new AtomicInteger();
        GreenhouseMatrixSimulation simulation = new GreenhouseMatrixSimulation();
        simulation.beginRebuild(members, ignored -> {
            resolutions.incrementAndGet();
            return List.of(profile);
        });

        simulation.rebuildStep(64);
        assertEquals(64, resolutions.get());
        assertTrue(simulation.rebuilding());
        while (simulation.rebuilding()) simulation.rebuildStep(64);
        assertEquals(3_000, resolutions.get());
        assertEquals(1, simulation.groupCount());

        resolutions.set(0);
        simulation.advanceWork(20L, Long.MAX_VALUE, Long.MAX_VALUE);

        assertEquals(0, resolutions.get());
        assertFalse(simulation.rebuilding());
        assertEquals(7_500L, simulation.totalPendingHarvests());
    }
}
