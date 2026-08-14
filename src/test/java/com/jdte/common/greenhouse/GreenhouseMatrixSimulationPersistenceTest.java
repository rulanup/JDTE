package com.jdte.common.greenhouse;

import com.jdte.common.recipes.GreenhouseCropResolver;
import com.jdte.common.recipes.GreenhouseRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreenhouseMatrixSimulationPersistenceTest {
    private static final ResourceLocation WATER_ID = ResourceLocation.withDefaultNamespace("water");

    @Test
    void restoresFixedPointWorkWhenProfilesAreRebuiltAfterLoad() {
        RegistryAccess.Frozen registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        GreenhouseMatrixProductionProfile profile = new GreenhouseMatrixProductionProfile(
                GreenhouseMatrixProductionProfile.MachineKind.NORMAL,
                new ItemStack(Items.WHEAT_SEEDS), 1, "minecraft:wheat", WATER_ID,
                GreenhouseCropResolver.cacheGeneration(),
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

        GreenhouseMatrixProductionGroup restoredGroup = restored.groups().iterator().next();
        assertEquals(WATER_ID, restoredGroup.profile().fluid());
        assertEquals(512L, restoredGroup.workRemainder());
    }

    @Test
    void restoresLegacyProfilesWithTheDefaultFluid() {
        RegistryAccess.Frozen registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        GreenhouseMatrixProductionProfile profile = new GreenhouseMatrixProductionProfile(
                GreenhouseMatrixProductionProfile.MachineKind.NORMAL,
                new ItemStack(Items.WHEAT_SEEDS), 1, "minecraft:wheat", GreenhouseRecipe.DEFAULT_FLUID,
                GreenhouseCropResolver.cacheGeneration(),
                1, 1, 0, false, false, 10, 1, 0, 0,
                false, false, 4_096, 512L);
        GreenhouseMatrixSimulation original = new GreenhouseMatrixSimulation();
        original.beginRebuild(List.of(BlockPos.ZERO), ignored -> List.of(profile));
        original.rebuildStep(1);
        original.advanceWork(1L, 4_096L, Long.MAX_VALUE);

        CompoundTag saved = original.save(registries);
        ListTag groups = saved.getList("groups", CompoundTag.TAG_COMPOUND);
        groups.getCompound(0).remove("fluid");
        GreenhouseMatrixSimulation restored = new GreenhouseMatrixSimulation();
        restored.load(saved, registries, ignored -> { });
        restored.beginRebuild(List.of(BlockPos.ZERO), ignored -> List.of(profile));
        restored.rebuildStep(1);

        GreenhouseMatrixProductionGroup restoredGroup = restored.groups().iterator().next();
        assertEquals(GreenhouseRecipe.DEFAULT_FLUID, restoredGroup.profile().fluid());
        assertEquals(512L, restoredGroup.workRemainder());
    }
}
