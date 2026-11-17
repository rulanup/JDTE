package com.jdte.common.items;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Static resource contract for the Forge 1.20.1 port of the Ultimate Essence Conversion Upgrade.
 * These checks stay free of Minecraft classes so they run in the plain JUnit task.
 */
class UltimateEssenceConversionResourcesTest {

    private static Path resolveProjectFile(String relativePath) {
        Path current = Path.of(System.getProperty("user.dir", "")).toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("gradle.properties"))) {
            current = current.getParent();
        }
        assertNotNull(current, "Could not locate project root from test runtime path");
        return current.resolve(relativePath);
    }

    @Test
    void upgradesTagContainsUltimateEssenceConversionUpgrade() throws IOException {
        String content = Files.readString(resolveProjectFile("src/main/resources/data/jdte/tags/items/upgrades.json"), StandardCharsets.UTF_8);
        assertTrue(content.contains("\"jdte:ultimate_essence_conversion_upgrade\""));
    }

    @Test
    void smithingRecipeTransformsFromAdvancedUpgrade() throws IOException {
        Path path = resolveProjectFile("src/main/resources/data/jdte/recipes/ultimate_essence_conversion_upgrade.json");
        assertTrue(Files.exists(path));
        String content = Files.readString(path, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"minecraft:smithing_transform\""));
        assertTrue(content.contains("\"jdte:advanced_essence_conversion_upgrade\""));
        assertTrue(content.contains("\"justdirethings:time_crystal\""));
        assertTrue(content.contains("\"jdte:ultimate_essence_conversion_upgrade\""));
    }

    @Test
    void itemModelAndTextureExist() {
        assertTrue(Files.exists(resolveProjectFile(
                "src/main/resources/assets/jdte/models/item/ultimate_essence_conversion_upgrade.json")));
        assertTrue(Files.exists(resolveProjectFile(
                "src/main/resources/assets/jdte/textures/item/ultimate_essence_conversion_upgrade.png")));
    }

    @Test
    void langFilesContainRequiredTranslations() throws IOException {
        String zh = Files.readString(resolveProjectFile("src/main/resources/assets/jdte/lang/zh_cn.json"), StandardCharsets.UTF_8);
        assertTrue(zh.contains("\"item.jdte.ultimate_essence_conversion_upgrade\": \"顶级精华转化升级\""));
        assertTrue(zh.contains("\"jdte.tooltip.essence_conversion.more\""));

        String en = Files.readString(resolveProjectFile("src/main/resources/assets/jdte/lang/en_us.json"), StandardCharsets.UTF_8);
        assertTrue(en.contains("\"item.jdte.ultimate_essence_conversion_upgrade\": \"Ultimate Essence Conversion Upgrade\""));
        assertTrue(en.contains("\"jdte.tooltip.essence_conversion.more\""));
    }
}
