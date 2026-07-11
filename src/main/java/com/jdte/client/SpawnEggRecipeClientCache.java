package com.jdte.client;

import com.jdte.common.jei.JDTEJeiPlugin;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public final class SpawnEggRecipeClientCache {
    private static Map<ResourceLocation, ResourceLocation> recipes = Map.of();
    private static boolean synced;

    private SpawnEggRecipeClientCache() {
    }

    public static void set(Map<ResourceLocation, ResourceLocation> syncedRecipes) {
        Map<ResourceLocation, ResourceLocation> updated = Map.copyOf(syncedRecipes);
        if (synced && recipes.equals(updated)) {
            return;
        }
        recipes = updated;
        synced = true;
        JDTEJeiPlugin.refreshSpawnEggRecipes();
    }

    public static Map<ResourceLocation, ResourceLocation> get() {
        return recipes;
    }

    public static boolean isSynced() {
        return synced;
    }
}
