package com.jdte.client;

import com.jdte.common.jei.JDTEJeiPlugin;
import net.minecraft.resources.ResourceLocation;
import com.jdte.common.utils.LootDropInfo;

import java.util.List;
import java.util.Map;

public final class LootFabricatorLootClientCache {
    private static Map<ResourceLocation, List<LootDropInfo>> drops = Map.of();
    private static boolean synced;

    private LootFabricatorLootClientCache() { }

    public static void set(Map<ResourceLocation, List<LootDropInfo>> syncedDrops) {
        Map<ResourceLocation, List<LootDropInfo>> updated = Map.copyOf(syncedDrops);
        if (synced && drops.equals(updated)) return;
        drops = updated;
        synced = true;
        JDTEJeiPlugin.refreshLootFabricatorRecipes();
    }

    public static Map<ResourceLocation, List<LootDropInfo>> get() { return drops; }
    public static boolean isSynced() { return synced; }
}
