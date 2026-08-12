package com.jdte.common.greenhouse;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixPatternRecipeValidatorTest {
    @Test
    void acceptsOnlyTheExactSingleInputRecipeAndCountsOccupiedIngredients() {
        List<ItemStack> grid = emptyGrid();
        grid.set(0, new ItemStack(Items.WHEAT));
        grid.set(4, new ItemStack(Items.WHEAT));
        grid.set(8, new ItemStack(Items.WHEAT));
        ItemStack output = new ItemStack(Items.RED_DYE, 6);

        var result = GreenhouseMatrixPatternRecipeValidator.validate(
                grid, output, output.copy(), List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY), 19);

        assertTrue(result.isPresent());
        assertEquals(3L, result.orElseThrow().inputAmount());
        assertEquals(6L, result.orElseThrow().outputAmount());
        assertEquals(19, result.orElseThrow().sourceIndex());
    }

    @Test
    void rejectsTwoDifferentInputItems() {
        List<ItemStack> grid = emptyGrid();
        grid.set(0, new ItemStack(Items.WHEAT));
        grid.set(1, new ItemStack(Items.CARROT));

        assertTrue(GreenhouseMatrixPatternRecipeValidator.validate(
                grid, new ItemStack(Items.RED_DYE), new ItemStack(Items.RED_DYE), emptyGrid(), 0).isEmpty());
    }

    @Test
    void rejectsInputWhoseComponentsDiffer() {
        List<ItemStack> grid = emptyGrid();
        grid.set(0, new ItemStack(Items.WHEAT));
        ItemStack named = new ItemStack(Items.WHEAT);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("forged"));
        grid.set(1, named);

        assertTrue(GreenhouseMatrixPatternRecipeValidator.validate(
                grid, new ItemStack(Items.RED_DYE), new ItemStack(Items.RED_DYE), emptyGrid(), 0).isEmpty());
    }

    @Test
    void rejectsClaimedOutputWithForgedItemOrCount() {
        List<ItemStack> grid = emptyGrid();
        grid.set(0, new ItemStack(Items.WHEAT));
        ItemStack real = new ItemStack(Items.RED_DYE, 6);

        assertTrue(GreenhouseMatrixPatternRecipeValidator.validate(
                grid, new ItemStack(Items.BLUE_DYE, 6), real, emptyGrid(), 0).isEmpty());
        assertTrue(GreenhouseMatrixPatternRecipeValidator.validate(
                grid, new ItemStack(Items.RED_DYE, 99), real, emptyGrid(), 0).isEmpty());
    }

    @Test
    void rejectsRecipesThatReturnContainersOrOtherRemainders() {
        List<ItemStack> grid = emptyGrid();
        grid.set(0, new ItemStack(Items.WHEAT));
        List<ItemStack> remaining = emptyGrid();
        remaining.set(0, new ItemStack(Items.BUCKET));

        assertTrue(GreenhouseMatrixPatternRecipeValidator.validate(
                grid, new ItemStack(Items.RED_DYE), new ItemStack(Items.RED_DYE), remaining, 0).isEmpty());
    }

    @Test
    void rejectsEmptyAndNonThreeByThreeInputs() {
        assertTrue(GreenhouseMatrixPatternRecipeValidator.validate(
                emptyGrid(), new ItemStack(Items.RED_DYE), new ItemStack(Items.RED_DYE), emptyGrid(), 0).isEmpty());
        assertTrue(GreenhouseMatrixPatternRecipeValidator.validate(
                List.of(new ItemStack(Items.WHEAT)), new ItemStack(Items.RED_DYE),
                new ItemStack(Items.RED_DYE), List.of(ItemStack.EMPTY), 0).isEmpty());
    }

    private static List<ItemStack> emptyGrid() {
        return new ArrayList<>(List.of(
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY));
    }
}
