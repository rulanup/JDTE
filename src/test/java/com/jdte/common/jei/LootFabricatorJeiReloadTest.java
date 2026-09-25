package com.jdte.client.jei;

import com.jdte.client.LootFabricatorLootClientCache;
import com.jdte.client.jei.lootfabricator.LootFabricatorJeiRecipe;
import com.jdte.common.utils.LootDropInfo;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LootFabricatorJeiReloadTest {
    private final JDTEJeiPlugin plugin = new JDTEJeiPlugin();

    @AfterEach
    void detachRuntime() {
        plugin.onRuntimeUnavailable();
        LootFabricatorLootClientCache.set(Map.of());
    }

    @Test
    void changingDropsReplacesVisibleRecipesAndEmptySnapshotsRemoveThem() {
        Set<LootFabricatorJeiRecipe> visible = Collections.newSetFromMap(new IdentityHashMap<>());
        IRecipeManager manager = (IRecipeManager) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{IRecipeManager.class}, (proxy, method, args) -> {
                    if (method.getName().equals("addRecipes")) visible.addAll((List<LootFabricatorJeiRecipe>) args[1]);
                    else if (method.getName().equals("hideRecipes")) visible.removeAll((List<?>) args[1]);
                    else throw new AssertionError("Unexpected JEI operation: " + method);
                    return null;
                });
        IJeiRuntime runtime = (IJeiRuntime) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{IJeiRuntime.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getRecipeManager")) return manager;
                    throw new AssertionError("Unexpected runtime operation: " + method);
                });
        LootFabricatorLootClientCache.set(snapshot("minecraft:rotten_flesh", 1));
        plugin.onRuntimeAvailable(runtime);
        assertEquals(1, visible.size());
        LootFabricatorJeiRecipe original = visible.iterator().next();
        assertTrue(original.possibleDrops().getFirst().stack().is(Items.ROTTEN_FLESH));

        LootFabricatorLootClientCache.set(snapshot("minecraft:diamond", 3));
        assertEquals(1, visible.size());
        assertFalse(visible.contains(original));
        LootFabricatorJeiRecipe edited = visible.iterator().next();
        assertEquals(original.id(), edited.id(), "Bookmarks retain the same recipe ID");
        assertTrue(edited.possibleDrops().getFirst().stack().is(Items.DIAMOND));
        assertEquals(3, edited.possibleDrops().getFirst().maxCount());

        LootFabricatorLootClientCache.set(snapshot("minecraft:diamond", 3));
        assertEquals(Set.of(edited), visible, "Unchanged packets must retain registered recipe objects");
        LootFabricatorLootClientCache.set(Map.of());
        assertTrue(visible.isEmpty());
        LootFabricatorLootClientCache.set(snapshot("minecraft:rotten_flesh", 1));
        assertEquals(1, visible.size());
        assertTrue(visible.iterator().next().possibleDrops().getFirst().stack().is(Items.ROTTEN_FLESH));
    }

    private static Map<ResourceLocation, List<LootDropInfo>> snapshot(String drop, int count) {
        return Map.of(ResourceLocation.parse("minecraft:zombie_spawn_egg"),
                List.of(new LootDropInfo(ResourceLocation.parse(drop), count, count, "")));
    }
}
