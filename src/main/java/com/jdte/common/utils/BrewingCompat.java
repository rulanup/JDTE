package com.jdte.common.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;

import java.util.List;

/** Forge 1.20.1 equivalent of the 1.21 PotionBrewing service. */
public final class BrewingCompat {
    public static final BrewingCompat INSTANCE = new BrewingCompat();

    private BrewingCompat() {
    }

    public boolean isInput(ItemStack stack) {
        return BrewingRecipeRegistry.isValidInput(stack);
    }

    public boolean isIngredient(ItemStack stack) {
        return BrewingRecipeRegistry.isValidIngredient(stack);
    }

    public boolean hasMix(ItemStack input, ItemStack ingredient) {
        return !BrewingRecipeRegistry.getOutput(input, ingredient).isEmpty();
    }

    public ItemStack mix(ItemStack ingredient, ItemStack input) {
        return BrewingRecipeRegistry.getOutput(input, ingredient).copy();
    }

    public List<IBrewingRecipe> getRecipes() {
        return BrewingRecipeRegistry.getRecipes();
    }
}
