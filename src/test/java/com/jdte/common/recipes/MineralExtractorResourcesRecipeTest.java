package com.jdte.common.recipes;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineralExtractorResourcesRecipeTest {

    @Test
    void decodesDistinctFortuneAndAccelerationFluids() {
        MineralExtractorResourcesRecipe recipe = new MineralExtractorResourcesRecipe.Serializer().codec().codec()
                .parse(JsonOps.INSTANCE, JsonParser.parseString("""
                        {
                          "fortune_fluid": "minecraft:lava",
                          "acceleration_fluid": "minecraft:water"
                        }
                        """))
                .getOrThrow();

        assertEquals(ResourceLocation.withDefaultNamespace("lava"), recipe.fortuneFluid());
        assertEquals(ResourceLocation.withDefaultNamespace("water"), recipe.accelerationFluid());
    }

    @Test
    void rejectsMatchingFortuneAndAccelerationFluids() {
        DataResult<MineralExtractorResourcesRecipe> result = new MineralExtractorResourcesRecipe.Serializer().codec().codec()
                .parse(JsonOps.INSTANCE, JsonParser.parseString("""
                        {
                          "fortune_fluid": "minecraft:water",
                          "acceleration_fluid": "minecraft:water"
                        }
                        """));

        assertTrue(result.error().isPresent());
        assertTrue(result.error().orElseThrow().message().contains("must differ"));
    }
}
