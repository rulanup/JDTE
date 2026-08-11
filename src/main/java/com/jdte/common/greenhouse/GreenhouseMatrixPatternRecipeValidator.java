package com.jdte.common.greenhouse;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/** Security boundary that accepts only an exact, single-item 3x3 crafting recipe. */
public final class GreenhouseMatrixPatternRecipeValidator {
    private static final int CRAFTING_GRID_SLOTS = 9;

    private GreenhouseMatrixPatternRecipeValidator() {
    }

    public static Optional<GreenhouseMatrixCraftingRecipe> validate(
            List<ItemStack> grid,
            ItemStack claimedOutput,
            ItemStack recomputedOutput,
            List<ItemStack> remainingItems,
            int sourceIndex) {
        if (grid == null || grid.size() != CRAFTING_GRID_SLOTS
                || remainingItems == null
                || claimedOutput == null || claimedOutput.isEmpty()
                || recomputedOutput == null || recomputedOutput.isEmpty()
                || sourceIndex < 0) {
            return Optional.empty();
        }
        if (!ItemStack.isSameItemSameComponents(claimedOutput, recomputedOutput)
                || claimedOutput.getCount() != recomputedOutput.getCount()) {
            return Optional.empty();
        }
        for (ItemStack remainder : remainingItems) {
            if (remainder != null && !remainder.isEmpty()) return Optional.empty();
        }

        ItemStack input = ItemStack.EMPTY;
        long inputAmount = 0L;
        for (ItemStack stack : grid) {
            if (stack == null || stack.isEmpty()) continue;
            if (input.isEmpty()) {
                input = stack.copyWithCount(1);
            } else if (!ItemStack.isSameItemSameComponents(input, stack)) {
                return Optional.empty();
            }
            try {
                inputAmount = Math.addExact(inputAmount, 1L);
            } catch (ArithmeticException exception) {
                return Optional.empty();
            }
        }
        if (input.isEmpty() || inputAmount <= 0L || recomputedOutput.getCount() <= 0) return Optional.empty();
        return Optional.of(new GreenhouseMatrixCraftingRecipe(
                input, inputAmount, recomputedOutput, recomputedOutput.getCount(), sourceIndex));
    }
}
