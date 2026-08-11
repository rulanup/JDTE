package com.jdte.common.greenhouse;

import net.minecraft.world.item.ItemStack;

/** A server-validated, single-input crafting pattern understood without loading AE2 classes. */
public record GreenhouseMatrixCraftingRecipe(ItemStack input, long inputAmount,
                                             ItemStack output, long outputAmount,
                                             int sourceIndex) {
    public GreenhouseMatrixCraftingRecipe {
        if (input == null || input.isEmpty()) throw new IllegalArgumentException("input must not be empty");
        if (output == null || output.isEmpty()) throw new IllegalArgumentException("output must not be empty");
        if (inputAmount <= 0L) throw new IllegalArgumentException("inputAmount must be positive");
        if (outputAmount <= 0L) throw new IllegalArgumentException("outputAmount must be positive");
        if (sourceIndex < 0) throw new IllegalArgumentException("sourceIndex must not be negative");
        input = input.copyWithCount(1);
        output = output.copyWithCount(1);
    }

    @Override
    public ItemStack input() {
        return input.copy();
    }

    @Override
    public ItemStack output() {
        return output.copy();
    }
}
