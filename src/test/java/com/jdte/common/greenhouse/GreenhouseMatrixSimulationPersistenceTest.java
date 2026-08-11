package com.jdte.common.greenhouse;

import com.jdte.common.recipes.GreenhouseCropResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreenhouseMatrixSimulationPersistenceTest {
    @Test
    void restoresFixedPointWorkWhenProfilesAreRebuiltAfterLoad() {
        RegistryAccess.Frozen registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        GreenhouseMatrixProductionProfile profile = new GreenhouseMatrixProductionProfile(
                GreenhouseMatrixProductionProfile.MachineKind.NORMAL,
                new ItemStack(Items.WHEAT_SEEDS), 1, "minecraft:wheat", GreenhouseCropResolver.cacheGeneration(),
                1, 1, 0, false, false, 10, 1, 0, 0,
                false, false, 4_096, 512L);
        GreenhouseMatrixSimulation original = new GreenhouseMatrixSimulation();
        original.beginRebuild(List.of(BlockPos.ZERO), ignored -> List.of(profile));
        original.rebuildStep(1);
        original.advanceWork(1L, 4_096L, Long.MAX_VALUE);

        CompoundTag saved = original.save(registries);
        GreenhouseMatrixSimulation restored = new GreenhouseMatrixSimulation();
        restored.load(saved, registries, ignored -> { });
        restored.beginRebuild(List.of(BlockPos.ZERO), ignored -> List.of(profile));
        restored.rebuildStep(1);

        assertEquals(512L, restored.groups().iterator().next().workRemainder());
    }
}
