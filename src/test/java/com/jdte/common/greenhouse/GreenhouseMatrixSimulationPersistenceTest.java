package com.jdte.common.greenhouse;

import com.jdte.common.recipes.GreenhouseCropResolver;
import com.jdte.common.recipes.GreenhouseCropDefinition;
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
    private static final String LEGACY_DEFAULT_FLUID_KEY =
            "minecraft:wheat|minecraft:wheat|false|4096|10|static";

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
    void migratesARealLegacyDefinitionKeyWithoutMatchingAnotherDefinition() {
        RegistryAccess.Frozen registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        GreenhouseCropDefinition definition = definition(ResourceLocation.withDefaultNamespace("wheat"));
        GreenhouseMatrixProductionProfile profile = profile(definition);
        GreenhouseMatrixSimulation original = new GreenhouseMatrixSimulation();
        original.beginRebuild(List.of(BlockPos.ZERO), ignored -> List.of(profile));
        original.rebuildStep(1);
        original.advanceWork(1L, 4_096L, Long.MAX_VALUE);

        CompoundTag saved = original.save(registries);
        ListTag groups = saved.getList("groups", CompoundTag.TAG_COMPOUND);
        CompoundTag legacyGroup = groups.getCompound(0);
        legacyGroup.remove("fluid");
        legacyGroup.putString("definitionKey", LEGACY_DEFAULT_FLUID_KEY);
        GreenhouseMatrixSimulation restored = new GreenhouseMatrixSimulation();
        restored.load(saved, registries, ignored -> { });

        GreenhouseMatrixProductionProfile otherProfile = profile(
                definition(ResourceLocation.withDefaultNamespace("carrots")));
        restored.beginRebuild(List.of(BlockPos.ZERO), ignored -> List.of(otherProfile));
        restored.rebuildStep(1);
        assertEquals(0L, restored.groups().iterator().next().workRemainder());

        restored.beginRebuild(List.of(BlockPos.ZERO), ignored -> List.of(profile));
        restored.rebuildStep(1);

        GreenhouseMatrixProductionGroup restoredGroup = restored.groups().iterator().next();
        assertEquals(GreenhouseRecipe.DEFAULT_FLUID, restoredGroup.profile().fluid());
        assertEquals(512L, restoredGroup.workRemainder());
    }

    private static GreenhouseCropDefinition definition(ResourceLocation block) {
        return new GreenhouseCropDefinition(List.of(), block, block, false, 4_096,
                GreenhouseRecipe.DEFAULT_FLUID, 10);
    }

    private static GreenhouseMatrixProductionProfile profile(GreenhouseCropDefinition definition) {
        return new GreenhouseMatrixProductionProfile(
                GreenhouseMatrixProductionProfile.MachineKind.NORMAL,
                new ItemStack(Items.WHEAT_SEEDS), 1,
                GreenhouseMatrixProductionProfile.definitionKey(definition), definition.fluid(),
                GreenhouseCropResolver.cacheGeneration(),
                1, 1, 0, false, false, 10, 1, 0, 0,
                false, false, 4_096, 512L);
    }
}
