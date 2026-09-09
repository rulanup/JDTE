package com.jdte.common.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MobLootPreviewTest {
    private static final ResourceKey<LootTable> ZOMBIE = EntityType.ZOMBIE.getDefaultLootTable();
    private static final ResourceLocation EGG = ResourceLocation.parse("minecraft:zombie_spawn_egg");

    @Test
    void rebuildReflectsInPlaceTableEditsWithoutReReadingUnchangedResources() {
        LootTable table = LootTable.lootTable().withPool(LootPool.lootPool().name("original")
                .add(LootItem.lootTableItem(Items.ROTTEN_FLESH))).build();
        Map<ResourceKey<LootTable>, LootTable> tables = Map.of(ZOMBIE, table);
        assertEquals(List.of(ResourceLocation.parse("minecraft:rotten_flesh")), ids(preview(tables)));

        table.removePool("original");
        table.addPool(LootPool.lootPool().name("scripted")
                .add(LootItem.lootTableItem(Items.DIAMOND)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3)))).build());
        List<LootDropInfo> changed = preview(tables);
        assertEquals(List.of(new LootDropInfo(ResourceLocation.parse("minecraft:diamond"), 3, 3, "")), changed);

        table.removePool("scripted");
        assertTrue(preview(tables).isEmpty(), "An empty live table must not resurrect original JSON drops");
    }

    @Test
    void nestedRuntimeTablesRefreshAndCyclesTerminate() {
        ResourceKey<LootTable> nested = ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.parse("jdte:test_nested"));
        Map<ResourceKey<LootTable>, LootTable> tables = new HashMap<>();
        tables.put(ZOMBIE, LootTable.lootTable().withPool(LootPool.lootPool()
                .add(NestedLootTable.lootTableReference(nested))).build());
        tables.put(nested, LootTable.lootTable().withPool(LootPool.lootPool()
                .add(LootItem.lootTableItem(Items.EMERALD))).build());
        assertEquals(List.of(ResourceLocation.parse("minecraft:emerald")), ids(preview(tables)));
        tables.put(nested, LootTable.lootTable().withPool(LootPool.lootPool()
                .add(NestedLootTable.lootTableReference(ZOMBIE))).build());
        assertTrue(preview(tables).isEmpty());
    }

    @Test
    void eachReferencedTableIsEncodedOncePerSnapshot() {
        Map<ResourceKey<LootTable>, Integer> calls = new HashMap<>();
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        MobLootSpawnEggHelper.getLootDropsBySpawnEgg(registries, key -> {
            calls.merge(key, 1, Integer::sum);
            return LootTable.EMPTY;
        }, key -> fail("Empty table is serializable"));
        assertFalse(calls.isEmpty());
        assertTrue(calls.values().stream().allMatch(count -> count == 1));
    }

    @Test
    void nestedNumberProvidersDoNotAbortLootSynchronization() {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        LootTable table = LootTable.DIRECT_CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registries),
                JsonParser.parseString("""
                        {"pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:diamond",
                          "functions":[{"function":"minecraft:set_count","count":{
                            "type":"minecraft:uniform","min":{"type":"minecraft:uniform","min":0,"max":2},
                            "max":3}}]}]}]}
                        """)).getOrThrow();
        List<LootDropInfo> drops = assertDoesNotThrow(() -> preview(Map.of(ZOMBIE, table)));
        assertEquals(List.of(ResourceLocation.parse("minecraft:diamond")), ids(drops));
        assertEquals(3, drops.getFirst().maxCount());
    }

    private static List<LootDropInfo> preview(Map<ResourceKey<LootTable>, LootTable> tables) {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        return MobLootSpawnEggHelper.getLootDropsBySpawnEgg(registries,
                key -> tables.getOrDefault(key, LootTable.EMPTY),
                key -> MobLootPreviewTest.<JsonElement>unexpectedFallback(key)).get(EGG);
    }

    private static <T> T unexpectedFallback(ResourceLocation key) {
        return fail("Runtime table must encode without raw resources: " + key);
    }

    private static List<ResourceLocation> ids(List<LootDropInfo> drops) {
        return drops.stream().map(LootDropInfo::itemId).toList();
    }
}
