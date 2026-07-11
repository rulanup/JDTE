package com.jdte.common.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.entity.EntityType;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class MobLootSpawnEggHelper {
    public static final int LIFE_FLUID_COST = 64_000;
    public static final int ENERGY_COST = 100_000;

    private static final Map<ResourceManager, Map<Item, ItemStack>> CACHE = new WeakHashMap<>();

    private MobLootSpawnEggHelper() {
    }

    public static ItemStack findUniqueSpawnEgg(ServerLevel level, ItemStack input) {
        if (input.isEmpty() || input.getMaxStackSize() <= 1 || input.getCount() < input.getMaxStackSize()) {
            return ItemStack.EMPTY;
        }

        Map<Item, ItemStack> recipes = getRecipes(level.getServer().getResourceManager());
        ItemStack output = recipes.get(input.getItem());
        return output == null ? ItemStack.EMPTY : output.copy();
    }

    public static Map<Item, ItemStack> getRecipes(ResourceManager resources) {
        synchronized (CACHE) {
            return CACHE.computeIfAbsent(resources, MobLootSpawnEggHelper::buildRecipes);
        }
    }

    public static Map<ResourceLocation, ResourceLocation> getRecipeIds(ResourceManager resources) {
        Map<ResourceLocation, ResourceLocation> result = new HashMap<>();
        getRecipes(resources).forEach((drop, egg) -> result.put(
                BuiltInRegistries.ITEM.getKey(drop),
                BuiltInRegistries.ITEM.getKey(egg.getItem())));
        return Map.copyOf(result);
    }

    public static void invalidate(ResourceManager resources) {
        synchronized (CACHE) {
            CACHE.remove(resources);
        }
    }

    private static Map<Item, ItemStack> buildRecipes(ResourceManager resources) {
        Map<Item, List<ItemStack>> candidates = new HashMap<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof SpawnEggItem spawnEgg)) {
                continue;
            }

            ItemStack eggStack = new ItemStack(spawnEgg);
            if (spawnEgg.getType(eggStack) == EntityType.WITHER
                    || spawnEgg.getType(eggStack) == EntityType.ENDER_DRAGON) {
                continue;
            }
            Set<Item> possibleDrops = new HashSet<>();
            collectLootTableItems(resources, spawnEgg.getType(eggStack).getDefaultLootTable().location(), possibleDrops, new HashSet<>());
            for (Item drop : possibleDrops) {
                if (drop.getDefaultMaxStackSize() > 1) {
                    candidates.computeIfAbsent(drop, ignored -> new ArrayList<>()).add(eggStack.copy());
                }
            }
        }

        Map<Item, ItemStack> uniqueRecipes = new HashMap<>();
        candidates.forEach((drop, eggs) -> {
            if (eggs.size() == 1) {
                uniqueRecipes.put(drop, eggs.getFirst());
            }
        });
        return uniqueRecipes;
    }

    private static void collectLootTableItems(ResourceManager resources, ResourceLocation tableId,
                                              Set<Item> output, Set<ResourceLocation> visitedTables) {
        if (!visitedTables.add(tableId)) {
            return;
        }

        ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(
                tableId.getNamespace(), "loot_table/" + tableId.getPath() + ".json");
        resources.getResource(resourceId).ifPresent(resource -> {
            try (Reader reader = resource.openAsReader()) {
                collectJsonItems(resources, JsonParser.parseReader(reader), output, visitedTables);
            } catch (IOException | RuntimeException ignored) {
            }
        });
    }

    private static void collectJsonItems(ResourceManager resources, JsonElement element,
                                         Set<Item> output, Set<ResourceLocation> visitedTables) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            array.forEach(child -> collectJsonItems(resources, child, output, visitedTables));
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject object = element.getAsJsonObject();
        ResourceLocation type = getResourceLocation(object, "type");
        if (type != null && type.getPath().equals("item")) {
            ResourceLocation itemId = getResourceLocation(object, "name");
            if (itemId != null) {
                BuiltInRegistries.ITEM.getOptional(itemId).ifPresent(output::add);
            }
        } else if (type != null && type.getPath().equals("tag")) {
            ResourceLocation tagId = getResourceLocation(object, "name");
            if (tagId != null) {
                BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tagId))
                        .ifPresent(holders -> holders.forEach(holder -> output.add(holder.value())));
            }
        } else if (type != null && type.getPath().equals("loot_table")) {
            ResourceLocation nestedTable = getResourceLocation(object, "value");
            if (nestedTable == null) {
                nestedTable = getResourceLocation(object, "name");
            }
            if (nestedTable != null) {
                collectLootTableItems(resources, nestedTable, output, visitedTables);
            }
        }

        object.entrySet().forEach(entry -> collectJsonItems(resources, entry.getValue(), output, visitedTables));
    }

    private static ResourceLocation getResourceLocation(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            return null;
        }
        return ResourceLocation.tryParse(value.getAsString());
    }
}
