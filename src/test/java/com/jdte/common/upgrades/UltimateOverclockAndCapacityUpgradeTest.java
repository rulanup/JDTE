package com.jdte.common.upgrades;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jdte.common.items.UltimateTimeWandData;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UltimateOverclockAndCapacityUpgradeTest {

    @Test
    void upgradeTypesAreProperlyConfigured() {
        assertEquals("ultimate_overclock", UpgradeType.ULTIMATE_OVERCLOCK.getSerializedName());
        assertEquals(1, UpgradeType.ULTIMATE_OVERCLOCK.getMaxPerMachine());
        assertTrue(UpgradeType.ULTIMATE_OVERCLOCK.isSpeedUpgrade());

        assertEquals("ultimate_capacity", UpgradeType.ULTIMATE_CAPACITY.getSerializedName());
        assertEquals(1, UpgradeType.ULTIMATE_CAPACITY.getMaxPerMachine());
        assertFalse(UpgradeType.ULTIMATE_CAPACITY.isSpeedUpgrade());
    }

    @Test
    void wandAccelerationCeilingReaches32768X() {
        assertEquals(15, UltimateTimeWandData.ULTIMATE_MAX_EXPONENT);
        assertEquals(32768, UltimateTimeWandData.multiplierForExponent(15));
        assertEquals(32768, UltimateTimeWandData.multiplierForExponent(16));
        assertEquals(15, UltimateTimeWandData.addStep(0, UltimateTimeWandData.Mode.MAX, UltimateTimeWandData.ULTIMATE_MAX_EXPONENT));
    }

    @Test
    void resourcesAndContractsHonored() throws IOException {
        // 1. Crafting Recipes
        JsonObject ultOverclockRecipe = read("src/main/resources/data/jdte/recipe/ultimate_overclock_upgrade.json");
        assertEquals("minecraft:crafting_shaped", ultOverclockRecipe.get("type").getAsString());
        assertEquals("jdte:ultimate_overclock_upgrade", ultOverclockRecipe.getAsJsonObject("result").get("id").getAsString());

        JsonObject ultCapacityRecipe = read("src/main/resources/data/jdte/recipe/ultimate_capacity_upgrade.json");
        assertEquals("minecraft:crafting_shaped", ultCapacityRecipe.get("type").getAsString());
        assertEquals("jdte:ultimate_capacity_upgrade", ultCapacityRecipe.getAsJsonObject("result").get("id").getAsString());

        // 2. Models
        Path overclockModel = source("src/main/resources/assets/jdte/models/item/ultimate_overclock_upgrade.json");
        assertTrue(Files.isRegularFile(overclockModel), "Ultimate overclock model missing");

        Path capacityModel = source("src/main/resources/assets/jdte/models/item/ultimate_capacity_upgrade.json");
        assertTrue(Files.isRegularFile(capacityModel), "Ultimate capacity model missing");

        // 3. Textures
        Path overclockTexture = source("src/main/resources/assets/jdte/textures/item/ultimate_overclock_upgrade.png");
        assertTrue(Files.isRegularFile(overclockTexture), "Ultimate overclock texture missing");
        assertEquals(16, ImageIO.read(overclockTexture.toFile()).getWidth());
        assertEquals(16, ImageIO.read(overclockTexture.toFile()).getHeight());

        Path capacityTexture = source("src/main/resources/assets/jdte/textures/item/ultimate_capacity_upgrade.png");
        assertTrue(Files.isRegularFile(capacityTexture), "Ultimate capacity texture missing");
        assertEquals(16, ImageIO.read(capacityTexture.toFile()).getWidth());
        assertEquals(16, ImageIO.read(capacityTexture.toFile()).getHeight());

        // 4. Tags
        JsonObject tags = read("src/main/resources/data/jdte/tags/item/upgrades.json");
        assertTrue(tags.getAsJsonArray("values").toString().contains("jdte:ultimate_overclock_upgrade"));
        assertTrue(tags.getAsJsonArray("values").toString().contains("jdte:ultimate_capacity_upgrade"));

        // 5. Translations
        for (String locale : List.of("en_us", "zh_cn")) {
            JsonObject lang = read("src/main/resources/assets/jdte/lang/" + locale + ".json");
            assertTrue(lang.has("item.jdte.ultimate_overclock_upgrade"), locale + " missing item.jdte.ultimate_overclock_upgrade");
            assertTrue(lang.has("item.jdte.ultimate_capacity_upgrade"), locale + " missing item.jdte.ultimate_capacity_upgrade");
            assertTrue(lang.has("tooltip.jdte.ultimate_overclock"), locale + " missing tooltip.jdte.ultimate_overclock");
            assertTrue(lang.has("tooltip.jdte.ultimate_capacity"), locale + " missing tooltip.jdte.ultimate_capacity");
            assertTrue(lang.has("tooltip.jdte.ultimate_time_wand.ultimate_overclock_installed"),
                    locale + " missing tooltip.jdte.ultimate_time_wand.ultimate_overclock_installed");
        }

        // 6. GuideME Documentation
        String zhGuide = Files.readString(source("src/main/resources/assets/jdte/guides/jdte/guide/upgrades.md"), StandardCharsets.UTF_8);
        assertTrue(zhGuide.contains("jdte:ultimate_overclock_upgrade"), "GuideME zh_cn missing ultimate_overclock_upgrade");
        assertTrue(zhGuide.contains("jdte:ultimate_capacity_upgrade"), "GuideME zh_cn missing ultimate_capacity_upgrade");

        String enGuide = Files.readString(source("src/main/resources/assets/jdte/guides/jdte/guide/_en_us/upgrades.md"), StandardCharsets.UTF_8);
        assertTrue(enGuide.contains("jdte:ultimate_overclock_upgrade"), "GuideME en_us missing ultimate_overclock_upgrade");
        assertTrue(enGuide.contains("jdte:ultimate_capacity_upgrade"), "GuideME en_us missing ultimate_capacity_upgrade");
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

    private static JsonObject read(String relativePath) throws IOException {
        try (Reader reader = Files.newBufferedReader(source(relativePath), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
