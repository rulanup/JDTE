package com.jdte.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JDTEFumoResourcesTest {
    private static final Map<String, String> FUMOS = Map.of(
            "xingye_fumo", "星野玩偶",
            "cainiaoyo_fumo", "云游玩玩偶",
            "maiqilin_fumo", "麦淇淋玩偶");
    private static final Set<String> FACINGS = Set.of("facing=north", "facing=east", "facing=south", "facing=west");

    @Test
    void blockstatesMapEachFacingToItsModelRotation() throws IOException {
        for (String id : FUMOS.keySet()) {
            JsonObject variants = readGenerated("assets/jdte/blockstates/" + id + ".json").getAsJsonObject("variants");
            assertEquals(FACINGS, variants.keySet(), id + " facing variants");
            Set<Integer> rotations = FACINGS.stream()
                    .map(key -> variants.getAsJsonObject(key).get("y").getAsInt())
                    .collect(Collectors.toSet());
            assertEquals(Set.of(0, 90, 180, 270), rotations, id + " model rotations");
            for (String facing : Set.of("north", "east", "south", "west")) {
                assertEquals("jdte:block/" + id, variants.getAsJsonObject("facing=" + facing)
                        .get("model").getAsString(), id + " facing " + facing);
            }
        }
    }

    @Test
    void itemModelsUseTheirBlockModelsAsParents() throws IOException {
        for (String id : FUMOS.keySet()) {
            assertEquals("jdte:block/" + id,
                    readGenerated("assets/jdte/models/item/" + id + ".json").get("parent").getAsString(), id);
        }
    }

    @Test
    void lootTablesDropTheirOwnBlocks() throws IOException {
        for (String id : FUMOS.keySet()) {
            JsonObject entry = readGenerated("data/jdte/loot_table/blocks/" + id + ".json")
                    .getAsJsonArray("pools").get(0).getAsJsonObject()
                    .getAsJsonArray("entries").get(0).getAsJsonObject();
            assertEquals("jdte:" + id, entry.get("name").getAsString(), id);
        }
    }

    @Test
    void bothLanguagesNameAllFumoBlocks() throws IOException {
        JsonObject english = readProjectFile("src/main/resources/assets/jdte/lang/en_us.json");
        JsonObject chinese = readProjectFile("src/main/resources/assets/jdte/lang/zh_cn.json");
        for (Map.Entry<String, String> entry : FUMOS.entrySet()) {
            String key = "block.jdte." + entry.getKey();
            assertTrue(english.has(key), "Missing English name for " + entry.getKey());
            assertTrue(chinese.has(key), "Missing Chinese name for " + entry.getKey());
            assertEquals(entry.getValue(), chinese.get(key).getAsString());
        }
    }

    @Test
    void sourceModelsReferenceOnlyExistingJdteTextures() throws IOException {
        for (String id : FUMOS.keySet()) {
            JsonObject model = readProjectFile("src/main/resources/assets/jdte/models/block/" + id + ".json");
            for (var texture : model.getAsJsonObject("textures").entrySet()) {
                String reference = texture.getValue().getAsString();
                assertTrue(reference.startsWith("jdte:block/"), id + " references " + reference);
                Path textureFile = findProjectFile("src/main/resources/assets/jdte/textures/"
                        + reference.substring("jdte:".length()) + ".png");
                assertTrue(Files.isRegularFile(textureFile), id + " missing texture " + textureFile);
            }
        }
    }

    private static JsonObject readGenerated(String relativePath) throws IOException {
        return readProjectFile("src/generated/resources/" + relativePath);
    }

    private static JsonObject readProjectFile(String relativePath) throws IOException {
        Path path = findProjectFile(relativePath);
        assertTrue(Files.isRegularFile(path), "Missing resource: " + path);
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static Path findProjectFile(String relativePath) {
        Path relative = Path.of(relativePath);
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(relative);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return relative;
    }
}