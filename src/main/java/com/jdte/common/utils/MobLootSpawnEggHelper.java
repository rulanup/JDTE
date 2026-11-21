package com.jdte.common.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootTable;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;

public final class MobLootSpawnEggHelper {
    public static final int LIFE_FLUID_COST = 64_000;
    public static final int ENERGY_COST = 100_000;

    private static final Map<ResourceManager, Map<Item, ItemStack>> CACHE = new WeakHashMap<>();
    private static final Map<ResourceManager, Map<Identifier, List<LootDropInfo>>> LOOT_DROP_CACHE = new WeakHashMap<>();

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

    public static Map<Identifier, Identifier> getRecipeIds(ResourceManager resources) {
        Map<Identifier, Identifier> result = new HashMap<>();
        getRecipes(resources).forEach((drop, egg) -> result.put(
                BuiltInRegistries.ITEM.getKey(drop),
                BuiltInRegistries.ITEM.getKey(egg.getItem())));
        return Map.copyOf(result);
    }

    public static Map<Identifier, List<LootDropInfo>> getLootDropsBySpawnEgg(ResourceManager resources) {
        synchronized (LOOT_DROP_CACHE) {
            return LOOT_DROP_CACHE.computeIfAbsent(resources, MobLootSpawnEggHelper::buildLootDropsBySpawnEgg);
        }
    }

    /**
     * Builds the JEI preview from the server's live loot-table registry. LootJS and
     * similar reload hooks can mutate compiled tables after the raw resource JSON
     * has been read, so the resource-manager cache is intentionally bypassed here.
     */
    public static Map<Identifier, List<LootDropInfo>> getLootDropsBySpawnEgg(ServerLevel level) {
        if (level == null || level.getServer() == null) return Map.of();
        var registries = level.getServer().reloadableRegistries();
        return getLootDropsBySpawnEgg(registries.lookup(), registries::getLootTable,
                id -> readLootTableJson(level.getServer().getResourceManager(), id));
    }

    private static Map<Identifier, List<LootDropInfo>> buildLootDropsBySpawnEgg(ResourceManager resources) {
        return buildLootDropsBySpawnEgg(id -> readLootTableJson(resources, id));
    }

    static Map<Identifier, List<LootDropInfo>> getLootDropsBySpawnEgg(
            HolderLookup.Provider registries, Function<ResourceKey<LootTable>, LootTable> tables,
            Function<Identifier, JsonElement> fallback) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        // Shared nested tables are encoded only once per synchronization. Nothing
        // survives this snapshot, so an in-place script edit is visible on reload.
        Map<Identifier, Optional<JsonElement>> encoded = new HashMap<>();
        return buildLootDropsBySpawnEgg(id -> encoded.computeIfAbsent(id, key -> {
            try {
                LootTable table = tables.apply(ResourceKey.create(Registries.LOOT_TABLE, key));
                var result = LootTable.DIRECT_CODEC.encodeStart(ops, table);
                if (result.result().isPresent()) return result.result();
            } catch (RuntimeException ignored) {
                // A custom non-serializable table should not hide other previews.
            }
            return Optional.ofNullable(fallback.apply(key));
        }).orElse(null));
    }

    private static Map<Identifier, List<LootDropInfo>> buildLootDropsBySpawnEgg(
            Function<Identifier, JsonElement> tables) {
        Map<Identifier, List<LootDropInfo>> result = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof SpawnEggItem spawnEgg)) continue;
            ItemStack eggStack = new ItemStack(spawnEgg);
            EntityType<?> entityType = spawnEgg.getType(eggStack);
            Map<Identifier, LootDropInfo> possibleDrops = new HashMap<>();
            if (entityType == null) continue;
            entityType.getDefaultLootTable().ifPresent(table ->
                    collectLootTableDrops(tables, table.identifier(), possibleDrops, new HashSet<>(), ""));
            addVanillaBossPreviewDrops(entityType, possibleDrops);
            result.put(BuiltInRegistries.ITEM.getKey(item), possibleDrops.values().stream()
                    .sorted(java.util.Comparator.comparing(drop -> drop.itemId().toString()))
                    .toList());
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

    private static void addVanillaBossPreviewDrops(EntityType<?> entityType, Map<Identifier, LootDropInfo> drops) {
        if (entityType == EntityType.WITHER) {
            Identifier netherStar = BuiltInRegistries.ITEM.getKey(Items.NETHER_STAR);
            drops.put(netherStar, new LootDropInfo(netherStar, 1, 1, ""));
        }
    }

    private static void collectLootTableDrops(Function<Identifier, JsonElement> tables, Identifier tableId,
                                              Map<Identifier, LootDropInfo> output,
                                              Set<Identifier> visitedTables, String inheritedChance) {
        if (!visitedTables.add(tableId)) return;
        JsonElement json = tables.apply(tableId);
        if (json != null) {
            collectJsonDrops(json, output, visitedTables, tables, inheritedChance);
        }
    }

    private static JsonElement readLootTableJson(ResourceManager resources, Identifier tableId) {
        Identifier resourceId = Identifier.fromNamespaceAndPath(
                tableId.getNamespace(), "loot_table/" + tableId.getPath() + ".json");
        return resources.getResource(resourceId).map(resource -> {
            try (Reader reader = resource.openAsReader()) {
                return JsonParser.parseReader(reader);
            } catch (IOException | RuntimeException ignored) {
                return null;
            }
        }).orElse(null);
    }

    private static void collectJsonDrops(JsonElement element,
                                         Map<Identifier, LootDropInfo> output,
                                         Set<Identifier> visitedTables,
                                         Function<Identifier, JsonElement> nestedResolver,
                                         String inheritedChance) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            boolean competingEntries = array.size() > 1;
            array.forEach(child -> collectJsonDrops(child, output, visitedTables, nestedResolver,
                    competingEntries ? mergeChance(inheritedChance, "conditional") : inheritedChance));
            return;
        }
        if (!element.isJsonObject()) return;

        JsonObject object = element.getAsJsonObject();
        String chance = mergeChance(inheritedChance, readChance(object));
        Identifier type = getResourceLocation(object, "type");
        if (type != null && type.getPath().equals("item")) {
            Identifier itemId = getResourceLocation(object, "name");
            if (itemId != null) addDrop(output, itemId, readCountRange(object), chance);
        } else if (type != null && type.getPath().equals("tag")) {
            Identifier tagId = getResourceLocation(object, "name");
            if (tagId != null) {
                int[] range = readCountRange(object);
                BuiltInRegistries.ITEM.get(TagKey.create(Registries.ITEM, tagId)).ifPresent(holders ->
                        holders.forEach(holder -> addDrop(output, BuiltInRegistries.ITEM.getKey(holder.value()), range, chance)));
            }
        } else if (type != null && type.getPath().equals("loot_table")) {
            Identifier nested = getResourceLocation(object, "value");
            if (nested == null) nested = getResourceLocation(object, "name");
            if (nested != null && visitedTables.add(nested)) {
                JsonElement nestedJson = nestedResolver.apply(nested);
                if (nestedJson != null) {
                    collectJsonDrops(nestedJson, output, visitedTables, nestedResolver, chance);
                }
            }
        }

        object.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("conditions") && !entry.getKey().equals("functions"))
                .forEach(entry -> collectJsonDrops(entry.getValue(), output, visitedTables, nestedResolver, chance));
    }

    private static void addDrop(Map<Identifier, LootDropInfo> output, Identifier itemId, int[] range, String chance) {
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
            Identifier functionType = getResourceLocation(function, "function");
            if (functionType == null || !functionType.getPath().equals("set_count")) continue;
            JsonElement count = function.get("count");
            if (count == null) continue;
            if (count.isJsonPrimitive() && count.getAsJsonPrimitive().isNumber()) {
                int value = Math.max(1, count.getAsInt());
                return new int[]{value, value};
            }
            if (count.isJsonObject()) {
                JsonObject range = count.getAsJsonObject();
                int min = readCountBound(range, "min", 0);
                int max = Math.max(min, readCountBound(range, "max", 0));
                return new int[]{min, max};
            }
        }
        return new int[]{1, 1};
    }

    private static int readCountBound(JsonElement provider, String bound, int depth) {
        if (provider == null || depth >= 16) return 1;
        if (provider.isJsonPrimitive() && provider.getAsJsonPrimitive().isNumber()) {
            return Math.max(1, provider.getAsInt());
        }
        if (provider instanceof JsonObject object) {
            if (object.has(bound)) return readCountBound(object.get(bound), bound, depth + 1);
            if (object.has("value")) return readCountBound(object.get("value"), bound, depth + 1);
        }
        // Context-dependent providers cannot be evaluated in a static preview.
        return 1;
    }

    private static String readChance(JsonObject object) {
        JsonElement conditionsElement = object.get("conditions");
        if (!(conditionsElement instanceof JsonArray conditions) || conditions.isEmpty()) return "";
        for (JsonElement conditionElement : conditions) {
            if (!conditionElement.isJsonObject()) continue;
            JsonObject condition = conditionElement.getAsJsonObject();
            Identifier conditionType = getResourceLocation(condition, "condition");
            if (conditionType != null && conditionType.getPath().equals("random_chance") && condition.has("chance")) {
                JsonElement chance = condition.get("chance");
                return chance.isJsonPrimitive() && chance.getAsJsonPrimitive().isNumber()
                        ? formatChance(chance.getAsDouble()) : "conditional";
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
            EntityType<?> entityType = spawnEgg.getType(eggStack);
            if (entityType == null || entityType == EntityType.WITHER
                    || entityType == EntityType.ENDER_DRAGON || entityType == EntityType.ELDER_GUARDIAN) {
                continue;
            }
            if (isInfusionBlacklisted(spawnEgg, eggStack)) {
                continue;
            }
            Set<Item> possibleDrops = new HashSet<>();
            entityType.getDefaultLootTable().ifPresent(table ->
                    collectLootTableItems(resources, table.identifier(), possibleDrops, new HashSet<>()));
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

    /**
     * Whether a spawn egg must be excluded from the Infusion Machine's dynamic spawn egg recipes.
     * Entries in the configured {@code infusion.spawnEggModBlacklist} are mod ids (namespaces), but
     * full item or entity resource location ids are accepted too. This prevents the Infusion Machine
     * from generating recipes that turn mob loot into the blacklisted mod's spawn eggs.
     */
    private static boolean isInfusionBlacklisted(SpawnEggItem spawnEgg, ItemStack eggStack) {
        List<? extends String> blacklist = JDTEConfig.COMMON.infusionSpawnEggModBlacklist.get();
        if (blacklist.isEmpty()) {
            return false;
        }
        Identifier itemId = BuiltInRegistries.ITEM.getKey(spawnEgg);
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(spawnEgg.getType(eggStack));
        for (String entry : blacklist) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            String trimmed = entry.trim();
            if (trimmed.contains(":")) {
                if (trimmed.equals(itemId.toString()) || trimmed.equals(entityId.toString())) {
                    return true;
                }
            } else if (trimmed.equals(itemId.getNamespace()) || trimmed.equals(entityId.getNamespace())) {
                return true;
            }
        }
        return false;
    }

    private static void collectLootTableItems(ResourceManager resources, Identifier tableId,
                                              Set<Item> output, Set<Identifier> visitedTables) {
        if (!visitedTables.add(tableId)) {
            return;
        }

        Identifier resourceId = Identifier.fromNamespaceAndPath(
                tableId.getNamespace(), "loot_table/" + tableId.getPath() + ".json");
        resources.getResource(resourceId).ifPresent(resource -> {
            try (Reader reader = resource.openAsReader()) {
                collectJsonItems(resources, JsonParser.parseReader(reader), output, visitedTables);
            } catch (IOException | RuntimeException ignored) {
            }
        });
    }

    private static void collectJsonItems(ResourceManager resources, JsonElement element,
                                         Set<Item> output, Set<Identifier> visitedTables) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            array.forEach(child -> collectJsonItems(resources, child, output, visitedTables));
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject object = element.getAsJsonObject();
        Identifier type = getResourceLocation(object, "type");
        if (type != null && type.getPath().equals("item")) {
            Identifier itemId = getResourceLocation(object, "name");
            if (itemId != null) {
                BuiltInRegistries.ITEM.getOptional(itemId).ifPresent(output::add);
            }
        } else if (type != null && type.getPath().equals("tag")) {
            Identifier tagId = getResourceLocation(object, "name");
            if (tagId != null) {
                BuiltInRegistries.ITEM.get(TagKey.create(Registries.ITEM, tagId))
                        .ifPresent(holders -> holders.forEach(holder -> output.add(holder.value())));
            }
        } else if (type != null && type.getPath().equals("loot_table")) {
            Identifier nestedTable = getResourceLocation(object, "value");
            if (nestedTable == null) {
                nestedTable = getResourceLocation(object, "name");
            }
            if (nestedTable != null) {
                collectLootTableItems(resources, nestedTable, output, visitedTables);
            }
        }

        object.entrySet().forEach(entry -> collectJsonItems(resources, entry.getValue(), output, visitedTables));
    }

    private static Identifier getResourceLocation(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            return null;
        }
        return Identifier.tryParse(value.getAsString());
    }
}
