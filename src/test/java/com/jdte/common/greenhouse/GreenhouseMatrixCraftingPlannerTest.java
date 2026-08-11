package com.jdte.common.greenhouse;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixCraftingPlannerTest {
    @Test
    void givesCompleteRoundsToEveryPatternWithoutPerCraftIteration() {
        List<GreenhouseMatrixCraftingRecipe> recipes = List.of(
                recipe(2L, Items.RED_DYE, 6L, 0),
                recipe(3L, Items.BLUE_DYE, 8L, 1));

        GreenhouseMatrixCraftingPlanner.Plan plan = GreenhouseMatrixCraftingPlanner.plan(5_000_001L, recipes, 0);

        assertTrue(plan.valid());
        assertEquals(1_000_000L, plan.allocations().get(0).crafts());
        assertEquals(1_000_000L, plan.allocations().get(1).crafts());
        assertEquals(1L, plan.remainingInput());
        assertEquals(0, plan.nextCursor());
    }

    @Test
    void rotatesTheSingleRemainderCraftBetweenEqualCostPatterns() {
        List<GreenhouseMatrixCraftingRecipe> recipes = List.of(
                recipe(2L, Items.RED_DYE, 1L, 0),
                recipe(2L, Items.BLUE_DYE, 1L, 1));

        GreenhouseMatrixCraftingPlanner.Plan first = GreenhouseMatrixCraftingPlanner.plan(6L, recipes, 0);
        GreenhouseMatrixCraftingPlanner.Plan second = GreenhouseMatrixCraftingPlanner.plan(6L, recipes, first.nextCursor());

        assertEquals(List.of(2L, 1L), first.allocations().stream().map(GreenhouseMatrixCraftingPlanner.Allocation::crafts).toList());
        assertEquals(1, first.nextCursor());
        assertEquals(List.of(1L, 2L), second.allocations().stream().map(GreenhouseMatrixCraftingPlanner.Allocation::crafts).toList());
        assertEquals(0, second.nextCursor());
    }

    @Test
    void refusesToPlanRecipesWhoseInputComponentsDiffer() {
        ItemStack namedEssence = new ItemStack(Items.WHEAT);
        namedEssence.set(DataComponents.CUSTOM_NAME, Component.literal("named essence"));
        List<GreenhouseMatrixCraftingRecipe> recipes = List.of(
                recipe(1L, Items.RED_DYE, 1L, 0),
                new GreenhouseMatrixCraftingRecipe(namedEssence, 1L, new ItemStack(Items.BLUE_DYE), 1L, 1));

        GreenhouseMatrixCraftingPlanner.Plan plan = GreenhouseMatrixCraftingPlanner.plan(2L, recipes, 0);

        assertFalse(plan.valid());
        assertTrue(plan.allocations().isEmpty());
    }

    @Test
    void rejectsOutputMultiplicationOverflowInsteadOfWrapping() {
        GreenhouseMatrixCraftingRecipe recipe = recipe(1L, Items.RED_DYE, 2L, 0);

        GreenhouseMatrixCraftingPlanner.Plan plan = GreenhouseMatrixCraftingPlanner.plan(Long.MAX_VALUE, List.of(recipe), 0);

        assertFalse(plan.valid());
        assertTrue(plan.allocations().isEmpty());
    }

    private static GreenhouseMatrixCraftingRecipe recipe(long input, net.minecraft.world.item.Item output,
                                                          long outputCount, int sourceIndex) {
        return new GreenhouseMatrixCraftingRecipe(
                new ItemStack(Items.WHEAT), input, new ItemStack(output), outputCount, sourceIndex);
    }
}
