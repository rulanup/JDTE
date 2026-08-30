package com.jdte.common.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jdte.setup.JDTEItems;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AEExtractionUpgradeResourceTest {
    @Test
    void recipesModelTranslationsAndGuideTextHonorTheAeExtractionContract() throws IOException {
        JsonObject craft = read("src/main/resources/data/jdte/recipe/ae_extraction_upgrade.json");
        assertEquals("minecraft:crafting_shapeless", craft.get("type").getAsString());
        Set<String> ingredients = new HashSet<>();
        for (var element : craft.getAsJsonArray("ingredients")) {
            ingredients.add(element.getAsJsonObject().get("item").getAsString());
        }
        assertEquals(Set.of("ae2:wireless_receiver", "ae2:export_bus", "jdte:capacity_upgrade"), ingredients);
        assertEquals("jdte:ae_extraction_upgrade", craft.getAsJsonObject("result").get("id").getAsString());
        assertAe2Condition(craft);

        JsonObject apply = read("src/main/resources/data/jdte/recipe/ae_extraction_upgrade_apply.json");
        assertEquals("jdte:ae_extraction_smithing", apply.get("type").getAsString());
        assertAe2Condition(apply);

        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/models/item/ae_extraction_upgrade.json")));
        assertTrue(Files.isRegularFile(source("src/main/resources/assets/jdte/textures/item/ae_extraction_upgrade.png")));
        assertEquals(16, javax.imageio.ImageIO.read(source("src/main/resources/assets/jdte/textures/item/ae_extraction_upgrade.png").toFile()).getWidth());
        assertEquals(16, javax.imageio.ImageIO.read(source("src/main/resources/assets/jdte/textures/item/ae_extraction_upgrade.png").toFile()).getHeight());

        for (String locale : List.of("en_us", "zh_cn")) {
            JsonObject lang = read("src/main/resources/assets/jdte/lang/" + locale + ".json");
            for (String key : List.of("item.jdte.ae_extraction_upgrade", "tooltip.jdte.ae_extraction.linked",
                    "tooltip.jdte.ae_extraction.unlinked", "tooltip.jdte.ae_extraction.enabled")) {
                assertTrue(lang.has(key), locale + " missing " + key);
            }
            String guide = Files.readString(source("src/main/resources/assets/jdte/guides/jdte/guide/" +
                    (locale.equals("en_us") ? "_en_us/" : "") + "upgrades.md"));
            assertTrue(guide.contains("ae_extraction_upgrade"));
            assertTrue(guide.toLowerCase().contains("applied flux"));
            String normalized = guide.toLowerCase();
            assertTrue(normalized.contains("empty universal") || guide.contains("空的通用") || guide.contains("空通用"));
        }

        String creativeTab = Files.readString(source("src/main/java/com/jdte/setup/JDTECreativeTabs.java"));
        assertTrue(creativeTab.contains("JDTEItems.AE_EXTRACTION_UPGRADE.get()"));
        assertTrue(JDTEItems.AE_EXTRACTION_UPGRADE.get().getDefaultInstance().getCount() == 1);
    }

    private static void assertAe2Condition(JsonObject recipe) {
        JsonArray conditions = recipe.getAsJsonArray("neoforge:conditions");
        assertTrue(conditions != null);
        assertTrue(conditions.toString().contains("neoforge:mod_loaded"));
        assertTrue(conditions.toString().contains("ae2"));
    }

    private static JsonObject read(String path) throws IOException {
        try (Reader reader = Files.newBufferedReader(source(path), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static Path source(String path) {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(path);
            if (Files.isRegularFile(candidate)) return candidate;
            current = current.getParent();
        }
        return Path.of(path);
    }
}
