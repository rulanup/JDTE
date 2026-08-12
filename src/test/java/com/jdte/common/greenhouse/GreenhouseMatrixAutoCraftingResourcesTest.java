package com.jdte.common.greenhouse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseMatrixAutoCraftingResourcesTest {
    private static final String ID = "greenhouse_matrix_auto_crafting";

    @Test
    void blockResourcesResolveToTheRegisteredIdentity() {
        JsonObject blockState = json("assets/jdte/blockstates/" + ID + ".json");
        assertEquals("jdte:block/" + ID,
                blockState.getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString());
        JsonObject blockModel = json("assets/jdte/models/block/" + ID + ".json");
        assertEquals("jdte:block/greenhouse_matrix_component", blockModel.get("parent").getAsString());
        assertEquals("minecraft:block/crafting_table_front",
                blockModel.getAsJsonObject("textures").get("all").getAsString());
        assertEquals("jdte:block/" + ID,
                json("assets/jdte/models/item/" + ID + ".json").get("parent").getAsString());
        JsonObject loot = json("data/jdte/loot_table/blocks/" + ID + ".json");
        assertEquals("jdte:" + ID, loot.getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString());
    }

    @Test
    void recipeLoadsOnlyWithAe2AndUsesThePatternProvider() {
        JsonObject recipe = json("data/jdte/recipe/" + ID + ".json");
        assertEquals("minecraft:crafting_shapeless", recipe.get("type").getAsString());
        JsonArray ingredients = recipe.getAsJsonArray("ingredients");
        assertTrue(ingredients.asList().stream().anyMatch(element ->
                "jdte:greenhouse_matrix_casing".equals(element.getAsJsonObject().get("item").getAsString())));
        assertTrue(ingredients.asList().stream().anyMatch(element ->
                "ae2:pattern_provider".equals(element.getAsJsonObject().get("item").getAsString())));
        assertTrue(recipe.getAsJsonArray("neoforge:conditions").asList().stream().anyMatch(element ->
                "neoforge:mod_loaded".equals(element.getAsJsonObject().get("type").getAsString())
                        && "ae2".equals(element.getAsJsonObject().get("modid").getAsString())));
        assertEquals("jdte:" + ID, recipe.getAsJsonObject("result").get("id").getAsString());
    }

    @Test
    void miningTagAndBothLanguagesExposeTheFeature() throws Exception {
        var resources = Collections.list(GreenhouseMatrixAutoCraftingResourcesTest.class.getClassLoader()
                .getResources("data/minecraft/tags/block/mineable/pickaxe.json"));
        assertTrue(resources.stream().map(url -> {
            try (InputStream stream = url.openStream()) {
                return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                        .getAsJsonObject().getAsJsonArray("values");
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }).anyMatch(values -> values.asList().stream()
                .anyMatch(element -> ("jdte:" + ID).equals(element.getAsString()))));
        for (String language : new String[]{"en_us", "zh_cn"}) {
            JsonObject lang = json("assets/jdte/lang/" + language + ".json");
            assertTrue(lang.has("block.jdte." + ID));
            assertTrue(lang.has("jdte.screen.greenhouse_matrix.pattern_page"));
            assertTrue(lang.has("jdte.screen.greenhouse_matrix.invalid_pattern"));
            assertTrue(lang.has("jdte.screen.greenhouse_matrix.error.8"));
        }
    }

    private static JsonObject json(String path) {
        InputStream stream = GreenhouseMatrixAutoCraftingResourcesTest.class.getClassLoader().getResourceAsStream(path);
        assertNotNull(stream, path);
        return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
    }
}
