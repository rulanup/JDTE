package com.jdte.common.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseRecipeFluidTest {

    @Test
    void decodesLegacyFluidDefaultAndExplicitFluid() {
        GreenhouseRecipe legacyRecipe = GreenhouseRecipe.Serializer.create().codec().codec()
                .parse(com.jdte.test.TestWorldRegistries.load().createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString("""
                        {
                          "seed": "minecraft:wheat_seeds",
                          "outputs": [{ "id": "minecraft:wheat", "count": 2 }],
                          "display_block": "minecraft:wheat",
                          "growth_work": 20,
                          "time_fluid": 100
                        }
                        """))
                .getOrThrow();
        GreenhouseRecipe waterRecipe = GreenhouseRecipe.Serializer.create().codec().codec()
                .parse(com.jdte.test.TestWorldRegistries.load().createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString("""
                        {
                          "seed": "minecraft:wheat_seeds",
                          "outputs": [{ "id": "minecraft:wheat", "count": 2 }],
                          "display_block": "minecraft:wheat",
                          "growth_work": 20,
                          "fluid": "minecraft:water",
                          "time_fluid": 100
                        }
                        """))
                .getOrThrow();

        assertEquals(GreenhouseRecipe.DEFAULT_FLUID, legacyRecipe.fluid());
        assertEquals(Identifier.withDefaultNamespace("water"), waterRecipe.fluid());
    }

    @Test
    void rejectsUnknownFluidDuringDecode() {
        assertDecodeRejected("examplemod:missing_source");
    }

    @Test
    void rejectsEmptyFluidDuringDecode() {
        assertDecodeRejected("minecraft:empty");
    }

    @Test
    void rejectsFlowingFluidDuringDecode() {
        assertDecodeRejected("minecraft:flowing_water");
    }

    private static void assertDecodeRejected(String fluid) {
        DataResult<GreenhouseRecipe> result = GreenhouseRecipe.Serializer.create().codec().codec()
                .parse(com.jdte.test.TestWorldRegistries.load().createSerializationContext(JsonOps.INSTANCE), recipeJson(fluid));

        assertTrue(result.error().isPresent(), () -> "expected decode rejection for " + fluid);
    }

    private static JsonElement recipeJson(String fluid) {
        return JsonParser.parseString("""
                {
                  "seed": "minecraft:wheat_seeds",
                  "outputs": [{ "id": "minecraft:wheat", "count": 2 }],
                  "display_block": "minecraft:wheat",
                  "growth_work": 20,
                  "fluid": "%s",
                  "time_fluid": 100
                }
                """.formatted(fluid));
    }
}
