package com.jdte.common.greenhouse;

import com.jdte.common.recipes.GreenhouseCropDefinition;
import com.jdte.common.recipes.GreenhouseRecipe;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GreenhouseMatrixProductionGroupTest {
    private static final ResourceLocation WATER_ID = ResourceLocation.withDefaultNamespace("water");
    private static final ResourceLocation LAVA_ID = ResourceLocation.withDefaultNamespace("lava");

    @Test
    void mergesThreeThousandIdenticalLanesIntoOneGroup() {
        GreenhouseMatrixProductionProfile profile = profile(new ItemStack(Items.WHEAT_SEEDS), 64, 1, 0);
        Map<GreenhouseMatrixProductionProfile, GreenhouseMatrixProductionGroup> groups = new HashMap<>();

        IntStream.range(0, 3_000).forEach(ignored ->
                groups.computeIfAbsent(profile, GreenhouseMatrixProductionGroup::new).addUnit());

        assertEquals(1, groups.size());
        assertEquals(3_000, groups.get(profile).units());
    }

    @Test
    void doesNotMergeDifferentComponentsCountsOrMachineSettings() {
        ItemStack ordinary = new ItemStack(Items.WHEAT_SEEDS);
        ItemStack named = ordinary.copy();
        named.set(DataComponents.CUSTOM_NAME, Component.literal("different"));

        assertNotEquals(profile(ordinary, 64, 1, 0), profile(named, 64, 1, 0));
        assertNotEquals(profile(ordinary, 64, 1, 0), profile(ordinary, 32, 1, 0));
        assertNotEquals(profile(ordinary, 64, 1, 0), profile(ordinary, 64, 2, 0));
        assertNotEquals(profile(ordinary, 64, 1, 0), profile(ordinary, 64, 1, 1));
        assertNotEquals(profile(ordinary, 64, 1, 0, WATER_ID),
                profile(ordinary, 64, 1, 0, LAVA_ID));
    }

    @Test
    void definitionKeyIncludesTheRecipeFluid() {
        GreenhouseCropDefinition water = definition(WATER_ID);
        GreenhouseCropDefinition lava = definition(LAVA_ID);

        assertNotEquals(GreenhouseMatrixProductionProfile.definitionKey(water),
                GreenhouseMatrixProductionProfile.definitionKey(lava));
    }

    private static GreenhouseMatrixProductionProfile profile(ItemStack seed, int templates,
                                                              int selectedMultiplier, int fortune) {
        return profile(seed, templates, selectedMultiplier, fortune, GreenhouseRecipe.DEFAULT_FLUID);
    }

    private static GreenhouseMatrixProductionProfile profile(ItemStack seed, int templates,
                                                              int selectedMultiplier, int fortune,
                                                              ResourceLocation fluid) {
        return new GreenhouseMatrixProductionProfile(
                GreenhouseMatrixProductionProfile.MachineKind.NORMAL,
                seed, templates, "minecraft:wheat", fluid, 7L,
                selectedMultiplier, 1, fortune, false, false,
                10, 1, 2, 1, true, true, 4_096, 512L);
    }

    private static GreenhouseCropDefinition definition(ResourceLocation fluid) {
        return new GreenhouseCropDefinition(List.of(new ItemStack(Items.WHEAT)),
                ResourceLocation.withDefaultNamespace("wheat"),
                ResourceLocation.withDefaultNamespace("wheat"), false, 4_096, fluid, 10);
    }
}
