package com.jdte.common.recipes;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;

interface MachineRecipe extends Recipe<CraftingInput> {
    @Override default PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
    @Override default RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }
    @Override default boolean showNotification() { return false; }
    @Override default String group() { return ""; }
}
