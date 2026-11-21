package com.jdte.client;

import com.jdte.client.jei.JDTEJeiPlugin;
import net.minecraft.resources.Identifier;
import com.jdte.common.utils.LootDropInfo;

import java.util.List;
import java.util.Map;

public final class LootFabricatorLootClientCache {
    private static Map<Identifier, List<LootDropInfo>> drops = Map.of();
    private static boolean synced;

    private LootFabricatorLootClientCache() { }

    public static void set(Map<Identifier, List<LootDropInfo>> syncedDrops) {
        Map<Identifier, List<LootDropInfo>> updated = Map.copyOf(syncedDrops);
        if (synced && drops.equals(updated)) return;
        drops = updated;
        synced = true;
        if (net.neoforged.fml.ModList.get().isLoaded("jei")) {
            JDTEJeiPlugin.refreshLootFabricatorRecipes();
        }
    }

    public static Map<Identifier, List<LootDropInfo>> get() { return drops; }
    public static boolean isSynced() { return synced; }
}
