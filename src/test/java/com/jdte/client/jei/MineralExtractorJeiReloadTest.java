package com.jdte.client.jei;

import com.jdte.client.MineralSurveyClientCache;
import com.jdte.client.jei.mineralextractor.MineralExtractorJeiRecipe;
import com.jdte.common.minerals.MineralEntry;
import com.jdte.common.minerals.MineralSurveyData;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MineralExtractorJeiReloadTest {
    private final JDTEJeiPlugin plugin = new JDTEJeiPlugin();

    @AfterEach
    void detachRuntime() {
        plugin.onRuntimeUnavailable();
        MineralSurveyClientCache.set(List.of());
    }

    @Test
    void changingSurveysReplacesVisibleRecipesAndEmptySnapshotsRemoveThem() {
        Set<MineralExtractorJeiRecipe> visible = Collections.newSetFromMap(new IdentityHashMap<>());
        IRecipeManager manager = (IRecipeManager) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{IRecipeManager.class}, (proxy, method, args) -> {
                    if (method.getName().equals("addRecipes")) visible.addAll((List<MineralExtractorJeiRecipe>) args[1]);
                    else if (method.getName().equals("hideRecipes")) visible.removeAll((List<?>) args[1]);
                    else throw new AssertionError("Unexpected JEI operation: " + method);
                    return null;
                });
        IJeiRuntime runtime = (IJeiRuntime) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{IJeiRuntime.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getRecipeManager")) return manager;
                    throw new AssertionError("Unexpected runtime operation: " + method);
                });

        MineralSurveyClientCache.set(List.of(survey("minecraft:plains", List.of(entry("minecraft:iron_ore", 10L)))));
        plugin.onRuntimeAvailable(runtime);
        assertEquals(1, visible.size());
        MineralExtractorJeiRecipe original = visible.iterator().next();
        assertEquals(ResourceLocation.parse("minecraft:plains"), original.biomeId());
        assertEquals(1, original.minerals().size());
        assertTrue(original.minerals().getFirst().stack().is(Items.IRON_ORE));

        MineralSurveyClientCache.set(List.of(survey("minecraft:plains", List.of(entry("minecraft:diamond_ore", 20L)))));
        assertEquals(1, visible.size());
        assertFalse(visible.contains(original));
        MineralExtractorJeiRecipe edited = visible.iterator().next();
        assertEquals(original.id(), edited.id(), "Biome recipes retain stable id");
        assertTrue(edited.minerals().getFirst().stack().is(Items.DIAMOND_ORE));

        // Paging test: 18 entries produce 2 pages
        List<MineralEntry> eighteenEntries = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            eighteenEntries.add(entry("minecraft:iron_ore", 10L));
        }
        MineralSurveyClientCache.set(List.of(survey("minecraft:plains", eighteenEntries)));
        assertEquals(2, visible.size(), "18 minerals should be split into 2 pages of 16 and 2");

        // Clear cache
        MineralSurveyClientCache.set(List.of());
        assertTrue(visible.isEmpty());
    }

    private static MineralSurveyData survey(String biome, List<MineralEntry> entries) {
        return MineralSurveyData.create(
                1L,
                ResourceLocation.parse(biome),
                ResourceLocation.parse("minecraft:overworld"),
                entries);
    }

    private static MineralEntry entry(String ore, long weight) {
        return new MineralEntry(
                ResourceLocation.parse(ore),
                weight,
                -64,
                320,
                8,
                MineralEntry.Confidence.EXACT);
    }
}
