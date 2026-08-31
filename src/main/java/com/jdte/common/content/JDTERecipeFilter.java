package com.jdte.common.content;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/** Applies the content policy to a prepared recipe resource map. */
public final class JDTERecipeFilter {
    private JDTERecipeFilter() {
    }

    /**
     * Removes disabled recipe resources in place.  RecipeManager receives a
     * mutable map from its reload listener; keeping this helper generic makes
     * the policy independently testable without loading the recipe framework.
     */
    public static <T> void filterDisabledRecipes(Map<ResourceLocation, T> recipes,
                                                 JDTEContentControl control) {
        if (recipes == null || control == null) {
            return;
        }
        recipes.entrySet().removeIf(entry -> !control.isRecipeEnabled(entry.getKey()));
    }
}
