package com.jdte.common.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedJdtMachineResourceTest {
    private static final List<MachineResource> MACHINES = List.of(
            new MachineResource("extended_generator", "justdirethings:generatort1", false,
                    JDTEBlocks.EXTENDED_GENERATOR),
            new MachineResource("extended_fluid_generator", "justdirethings:generatorfluidt1", false,
                    JDTEBlocks.EXTENDED_FLUID_GENERATOR),
            new MachineResource("extended_experience_holder", "justdirethings:experienceholder", true,
                    JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER),
            new MachineResource("extended_energy_transmitter", "justdirethings:energytransmitter", true,
                    JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER)
    );

    @Test
    void recipesUpgradeTheMatchingJdtMachines() throws IOException {
        for (MachineResource machine : MACHINES) {
            JsonObject recipe = readJson("data/jdte/recipe/" + machine.id() + ".json");

            assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString(), machine.id());
            assertEquals("jdte:" + machine.id(),
                    recipe.getAsJsonObject("result").get("id").getAsString(), machine.id());
            assertEquals(1, recipe.getAsJsonObject("result").get("count").getAsInt(), machine.id());
            assertTrue(keyItems(recipe).contains(machine.jdtBlockId()), machine.id());
            assertTrue(keyItems(recipe).contains("jdte:extended_upgrade"), machine.id());
            assertIngredientUsed(recipe, machine.jdtBlockId());
            assertIngredientUsed(recipe, "jdte:extended_upgrade");
        }
    }

    @Test
    void blockstatesAndModelsReferenceTheMatchingExtendedModels() throws IOException {
        for (MachineResource machine : MACHINES) {
            String expectedModel = "jdte:block/" + machine.id();
            JsonObject blockstate = readJson("assets/jdte/blockstates/" + machine.id() + ".json");
            JsonObject variants = blockstate.getAsJsonObject("variants");

            assertNotNull(variants, machine.id());
            assertEquals(machine.hasFacingVariants()
                            ? Set.of("facing=down", "facing=east", "facing=north",
                                    "facing=south", "facing=west", "facing=up")
                            : Set.of(""),
                    variants.keySet(), machine.id());
            for (Map.Entry<String, JsonElement> variant : variants.entrySet()) {
                assertEquals(expectedModel,
                        variant.getValue().getAsJsonObject().get("model").getAsString(), machine.id());
            }

            JsonObject blockModel = readJson("assets/jdte/models/block/" + machine.id() + ".json");
            assertEquals(machine.jdtBlockId().replace("justdirethings:", "justdirethings:block/"),
                    blockModel.get("parent").getAsString(), machine.id());

            JsonObject itemModel = readJson("assets/jdte/models/item/" + machine.id() + ".json");
            assertEquals(expectedModel, itemModel.get("parent").getAsString(), machine.id());
        }
    }

    @Test
    void registrationsAndTranslationsCoverAllFourMachines() throws IOException {
        JsonObject english = readJson("assets/jdte/lang/en_us.json");
        JsonObject chinese = readJson("assets/jdte/lang/zh_cn.json");

        for (MachineResource machine : MACHINES) {
            ResourceLocation expectedId = ResourceLocation.fromNamespaceAndPath("jdte", machine.id());
            assertEquals(expectedId, machine.block().getId(), machine.id());
            assertEquals(expectedId, BuiltInRegistries.BLOCK.getKey(machine.block().get()), machine.id());

            String translationKey = "block.jdte." + machine.id();
            assertTrue(english.has(translationKey), machine.id());
            assertTrue(chinese.has(translationKey), machine.id());
            assertTrue(!english.get(translationKey).getAsString().isBlank(), machine.id());
            assertTrue(!chinese.get(translationKey).getAsString().isBlank(), machine.id());
        }
    }

    private static List<String> keyItems(JsonObject recipe) {
        return recipe.getAsJsonObject("key").entrySet().stream()
                .map(entry -> entry.getValue().getAsJsonObject().get("item").getAsString())
                .toList();
    }

    private static void assertIngredientUsed(JsonObject recipe, String itemId) {
        String symbol = recipe.getAsJsonObject("key").entrySet().stream()
                .filter(entry -> itemId.equals(entry.getValue().getAsJsonObject().get("item").getAsString()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing recipe key for " + itemId));
        String pattern = recipe.getAsJsonArray("pattern").asList().stream()
                .map(JsonElement::getAsString)
                .reduce("", String::concat);
        assertTrue(pattern.contains(symbol), "Recipe pattern does not use " + itemId);
    }

    private static JsonObject readJson(String path) throws IOException {
        ClassLoader classLoader = ExtendedJdtMachineResourceTest.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(path)) {
            assertNotNull(input, "Missing classpath resource: " + path);
            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        }
    }

    private record MachineResource(
            String id,
            String jdtBlockId,
            boolean hasFacingVariants,
            DeferredHolder<Block, ? extends Block> block
    ) {
    }
}
