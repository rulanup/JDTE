package com.jdte.common.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseEdelwoodRecipeResourceTest {
    private static final String RESOURCE =
            "data/jdte/recipe/greenhouse/forbidden_arcanus_edelwood.json";

    @Test
    void definesConditionalEdelwoodLogOutput() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(RESOURCE)) {
            assertNotNull(input, "missing " + RESOURCE);
            JsonObject recipe = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                    .getAsJsonObject();

            assertEquals("forbidden_arcanus:growing_edelwood",
                    recipe.getAsJsonObject("seed").get("item").getAsString());
            assertEquals("forbidden_arcanus:growing_edelwood", recipe.get("display_block").getAsString());
            assertTrue(recipe.getAsJsonArray("outputs").asList().stream().anyMatch(output ->
                    "forbidden_arcanus:edelwood_log".equals(
                            output.getAsJsonObject().get("id").getAsString())
                            && output.getAsJsonObject().get("count").getAsInt() == 4));
            assertEquals("neoforge:mod_loaded",
                    recipe.getAsJsonArray("neoforge:conditions").get(0)
                            .getAsJsonObject().get("type").getAsString());
            assertEquals("forbidden_arcanus",
                    recipe.getAsJsonArray("neoforge:conditions").get(0)
                            .getAsJsonObject().get("modid").getAsString());
        }
    }
}
