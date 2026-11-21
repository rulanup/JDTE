package com.jdte.common.recipes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;

public final class MachineRecipeHelper {
    private static RecipeMap syncedRecipes = RecipeMap.EMPTY;

    private MachineRecipeHelper() {}

    public static RecipeMap recipes(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().getRecipeManager().recipeMap();
        }
        return level == null ? RecipeMap.EMPTY : syncedRecipes;
    }

    public static RecipeMap clientRecipes() {
        return syncedRecipes;
    }

    public static void setClientRecipes(RecipeMap recipes) {
        syncedRecipes = recipes;
    }
}
