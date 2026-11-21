package com.jdte.client;

import com.jdte.client.jei.JDTEJeiPlugin;
import net.minecraft.resources.Identifier;

import java.util.Map;

public final class SpawnEggRecipeClientCache {
    private static Map<Identifier, Identifier> recipes = Map.of();
    private static boolean synced;

    private SpawnEggRecipeClientCache() {
    }

    public static void set(Map<Identifier, Identifier> syncedRecipes) {
        Map<Identifier, Identifier> updated = Map.copyOf(syncedRecipes);
        if (synced && recipes.equals(updated)) {
            return;
        }
        recipes = updated;
        synced = true;
        if (net.neoforged.fml.ModList.get().isLoaded("jei")) {
            JDTEJeiPlugin.refreshSpawnEggRecipes();
        }
    }

    public static Map<Identifier, Identifier> get() {
        return recipes;
    }

    public static boolean isSynced() {
        return synced;
    }
}
