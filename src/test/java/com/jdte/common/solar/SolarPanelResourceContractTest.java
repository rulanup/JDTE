package com.jdte.common.solar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolarPanelResourceContractTest {
    private static final List<String> IDS = List.of(
            "concentrated_solar_panel", "singularity_solar_panel", "stellar_fusion_solar_panel",
            "dimensional_collapse_solar_panel", "creative_solar_panel");
    private static final List<String> RECIPES = List.of(
            "concentrated_solar_panel", "singularity_solar_panel", "stellar_fusion_solar_panel",
            "dimensional_collapse_solar_panel");

    @Test
    void everyPanelHasRenderableBlockStateModelItemModelAndLootTable() {
        for (String id : IDS) {
            assertNotNull(json("assets/jdte/blockstates/" + id + ".json"), id + " blockstate");
            assertNotNull(json("assets/jdte/models/block/" + id + ".json"), id + " block model");
            assertNotNull(json("assets/jdte/models/item/" + id + ".json"), id + " item model");
            assertNotNull(json("data/jdte/loot_table/blocks/" + id + ".json"), id + " loot table");
        }
    }

    @Test
    void survivalRecipesAreEightToOneAndCreativeHasNoRecipe() {
        for (String id : RECIPES) {
            JsonObject recipe = json("data/jdte/recipe/" + id + ".json").getAsJsonObject();
            assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
            JsonArray pattern = recipe.getAsJsonArray("pattern");
            assertEquals(List.of("AAA", "ABA", "AAA"), pattern.asList().stream()
                    .map(element -> element.getAsString()).toList());
            assertEquals(1, recipe.getAsJsonObject("result").get("count").getAsInt());
        }
        assertNull(resource("data/jdte/recipe/creative_solar_panel.json"));
    }

    @Test
    void allSurvivalRecipesAreConditionalOnJustDynaThings() {
        for (String id : RECIPES) {
            JsonObject recipe = json("data/jdte/recipe/" + id + ".json").getAsJsonObject();
            JsonArray conditions = recipe.getAsJsonArray("neoforge:conditions");
            assertNotNull(conditions, id);
            assertTrue(conditions.asList().stream().anyMatch(element ->
                    "neoforge:mod_loaded".equals(element.getAsJsonObject().get("type").getAsString())
                            && "justdynathings".equals(element.getAsJsonObject().get("modid").getAsString())), id);
        }
        JsonObject recipe = json("data/jdte/recipe/concentrated_solar_panel.json").getAsJsonObject();
        assertEquals("justdynathings:eclipse_alloy_solar_panel",
                recipe.getAsJsonObject("key").getAsJsonObject("A").get("item").getAsString());
    }

    @Test
    void bothLanguageFilesContainEveryPanelName() {
        JsonObject en = json("assets/jdte/lang/en_us.json").getAsJsonObject();
        JsonObject zh = json("assets/jdte/lang/zh_cn.json").getAsJsonObject();
        for (String id : IDS) {
            assertTrue(en.has("block.jdte." + id));
            assertTrue(zh.has("block.jdte." + id));
        }
    }

    private static JsonObject json(String path) {
        return JsonParser.parseReader(new InputStreamReader(resource(path), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static InputStream resource(String path) {
        return SolarPanelResourceContractTest.class.getClassLoader().getResourceAsStream(path);
    }
}
