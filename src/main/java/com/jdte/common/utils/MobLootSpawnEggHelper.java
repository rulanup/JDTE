package com.jdte.common.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jdte.common.integrations.DraconicEvolutionIntegration;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.ModList;

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
    private static final Map<ResourceManager, Map<ResourceLocation, List<LootDropInfo>>> LOOT_DROP_CACHE = new WeakHashMap<>();

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

    public static Map<ResourceLocation, List<LootDropInfo>> getLootDropsBySpawnEgg(ResourceManager resources) {
        synchronized (LOOT_DROP_CACHE) {
            return LOOT_DROP_CACHE.computeIfAbsent(resources, MobLootSpawnEggHelper::buildLootDropsBySpawnEgg);
        }
    }

    private static Map<ResourceLocation, List<LootDropInfo>> buildLootDropsBySpawnEgg(ResourceManager resources) {
        Map<ResourceLocation, List<LootDropInfo>> result = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof SpawnEggItem spawnEgg)) continue;
            ItemStack eggStack = new ItemStack(spawnEgg);
            Map<ResourceLocation, LootDropInfo> possibleDrops = new HashMap<>();
            collectLootTableDrops(resources, spawnEgg.getType(eggStack).getDefaultLootTable().location(),
                    possibleDrops, new HashSet<>(), "");
            if (ModList.get().isLoaded("draconicevolution")) {
                DraconicEvolutionIntegration.addLootFabricatorPreviewDrops(spawnEgg.getType(eggStack), possibleDrops);
            }
            addVanillaBossPreviewDrops(spawnEgg.getType(eggStack), possibleDrops);
            List<LootDropInfo> drops = possibleDrops.values().stream()
                    .sorted(java.util.Comparator.comparing(drop -> drop.itemId().toString()))
                    .toList();
            result.put(BuiltInRegistries.ITEM.getKey(item), drops);
        }
        return Map.copyOf(result);
    }

    public static void invalidate(ResourceManager resources) {
        synchronized (CACHE) {
            CACHE.remove(resources);
        }
        synchronized (LOOT_DROP_CACHE) {
            LOOT_DROP_CACHE.remove(resources);
        }
    }

    private static void addVanillaBossPreviewDrops(EntityType<?> entityType, Map<ResourceLocation, LootDropInfo> drops) {
        if (entityType == EntityType.WITHER) {
            ResourceLocation netherStar = BuiltInRegistries.ITEM.getKey(Items.NETHER_STAR);
            drops.put(netherStar, new LootDropInfo(netherStar, 1, 1, ""));
        }
    }

    private static void collectLootTableDrops(ResourceManager resources, ResourceLocation tableId,
                                              Map<ResourceLocation, LootDropInfo> output,
                                              Set<ResourceLocation> visitedTables, String inheritedChance) {
        if (!visitedTables.add(tableId)) return;
        ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(
                tableId.getNamespace(), "loot_table/" + tableId.getPath() + ".json");
        resources.getResource(resourceId).ifPresent(resource -> {
            try (Reader reader = resource.openAsReader()) {
                collectJsonDrops(resources, JsonParser.parseReader(reader), output, visitedTables, inheritedChance);
            } catch (IOException | RuntimeException ignored) {
            }
        });
    }

    private static void collectJsonDrops(ResourceManager resources, JsonElement element,
                                         Map<ResourceLocation, LootDropInfo> output,
                                         Set<ResourceLocation> visitedTables, String inheritedChance) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            boolean competingEntries = array.size() > 1;
            array.forEach(child -> collectJsonDrops(resources, child, output, visitedTables,
                    competingEntries ? mergeChance(inheritedChance, "conditional") : inheritedChance));
            return;
        }
        if (!element.isJsonObject()) return;

        JsonObject object = element.getAsJsonObject();
        String chance = mergeChance(inheritedChance, readChance(object));
        ResourceLocation type = getResourceLocation(object, "type");
        if (type != null && type.getPath().equals("item")) {
            ResourceLocation itemId = getResourceLocation(object, "name");
            if (itemId != null) addDrop(output, itemId, readCountRange(object), chance);
        } else if (type != null && type.getPath().equals("tag")) {
            ResourceLocation tagId = getResourceLocation(object, "name");
            if (tagId != null) {
                int[] range = readCountRange(object);
                BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tagId)).ifPresent(holders ->
                        holders.forEach(holder -> addDrop(output, BuiltInRegistries.ITEM.getKey(holder.value()), range, chance)));
            }
        } else if (type != null && type.getPath().equals("loot_table")) {
            ResourceLocation nested = getResourceLocation(object, "value");
            if (nested == null) nested = getResourceLocation(object, "name");
            if (nested != null) collectLootTableDrops(resources, nested, output, visitedTables, chance);
        }

        object.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("conditions") && !entry.getKey().equals("functions"))
                .forEach(entry -> collectJsonDrops(resources, entry.getValue(), output, visitedTables, chance));
    }

    private static void addDrop(Map<ResourceLocation, LootDropInfo> output, ResourceLocation itemId, int[] range, String chance) {
        output.merge(itemId, new LootDropInfo(itemId, range[0], range[1], chance), (left, right) ->
                new LootDropInfo(itemId, Math.min(left.minCount(), right.minCount()),
                        Math.max(left.maxCount(), right.maxCount()), mergeChance(left.chanceLabel(), right.chanceLabel())));
    }

    private static int[] readCountRange(JsonObject entry) {
        JsonElement functionsElement = entry.get("functions");
        if (!(functionsElement instanceof JsonArray functions)) return new int[]{1, 1};
        for (JsonElement functionElement : functions) {
            if (!functionElement.isJsonObject()) continue;
            JsonObject function = functionElement.getAsJsonObject();
            ResourceLocation functionType = getResourceLocation(function, "function");
            if (functionType == null || !functionType.getPath().equals("set_count")) continue;
            JsonElement count = function.get("count");
            if (count == null) continue;
            if (count.isJsonPrimitive() && count.getAsJsonPrimitive().isNumber()) {
                int value = Math.max(1, count.getAsInt());
                return new int[]{value, value};
            }
            if (count.isJsonObject()) {
                JsonObject range = count.getAsJsonObject();
                int min = range.has("min") ? Math.max(1, range.get("min").getAsInt()) : 1;
                int max = range.has("max") ? Math.max(min, range.get("max").getAsInt()) : min;
                return new int[]{min, max};
            }
        }
        return new int[]{1, 1};
    }

    private static String readChance(JsonObject object) {
        JsonElement conditionsElement = object.get("conditions");
        if (!(conditionsElement instanceof JsonArray conditions) || conditions.isEmpty()) return "";
        for (JsonElement conditionElement : conditions) {
            if (!conditionElement.isJsonObject()) continue;
            JsonObject condition = conditionElement.getAsJsonObject();
            ResourceLocation conditionType = getResourceLocation(condition, "condition");
            if (conditionType != null && conditionType.getPath().equals("random_chance") && condition.has("chance")) {
                return formatChance(condition.get("chance").getAsDouble());
            }
        }
        return "conditional";
    }

    private static String formatChance(double chance) {
        double percent = Math.clamp(chance, 0.0D, 1.0D) * 100.0D;
        return percent == Math.rint(percent) ? Integer.toString((int) percent) + "%" : String.format(java.util.Locale.ROOT, "%.2f%%", percent);
    }

    private static String mergeChance(String left, String right) {
        if (left == null || left.isEmpty()) return right == null ? "" : right;
        if (right == null || right.isEmpty() || left.equals(right)) return left;
        return "conditional";
    }

    private static Map<Item, ItemStack> buildRecipes(ResourceManager resources) {
        Map<Item, List<ItemStack>> candidates = new HashMap<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof SpawnEggItem spawnEgg)) {
                continue;
            }

            ItemStack eggStack = new ItemStack(spawnEgg);
            if (spawnEgg.getType(eggStack) == EntityType.WITHER
                    || spawnEgg.getType(eggStack) == EntityType.ENDER_DRAGON
                    || spawnEgg.getType(eggStack) == EntityType.ELDER_GUARDIAN) {
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
