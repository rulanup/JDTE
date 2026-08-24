package com.jdte.setup;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UltimateTimeWandConfigLanguageContractTest {
    private static final String LANGUAGE_ROOT = "src/main/resources/assets/jdte/lang";
    private static final Path ENGLISH = sourceLanguage("en_us.json");
    private static final Path CHINESE = sourceLanguage("zh_cn.json");

    private static final List<String> REQUIRED_KEYS = List.of(
            "entity.jdte.ultimate_time_wand",
            "item.jdte.ultimate_time_wand",
            "tooltip.jdte.ultimate_time_wand.mode.normal",
            "tooltip.jdte.ultimate_time_wand.mode.x2",
            "tooltip.jdte.ultimate_time_wand.mode.x4",
            "tooltip.jdte.ultimate_time_wand.mode.max",
            "message.jdte.ultimate_time_wand.mode",
            "message.jdte.ultimate_time_wand.switch",
            "message.jdte.ultimate_time_wand.insufficient_resources",
            "message.jdte.ultimate_time_wand.invalid_target",
            "message.jdte.ultimate_time_wand.max_multiplier",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandFluidCapacity",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandEnergyCapacity",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandDuration",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandBaseCostMultiplier",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandEnergyCostMultiplier",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandFractionalFluidSettlement");

    @Test
    void englishAndChineseContainEveryUltimateTimeWandLanguageKey() throws IOException {
        assertTrue(Files.isRegularFile(ENGLISH), "Missing English language file: " + ENGLISH);
        assertTrue(Files.isRegularFile(CHINESE), "Missing Chinese language file: " + CHINESE);

        JsonObject english = readLanguage(ENGLISH);
        JsonObject chinese = readLanguage(CHINESE);
        assertEquals(17, REQUIRED_KEYS.size(), "The contract must cover exactly 17 keys");

        for (String key : REQUIRED_KEYS) {
            assertTrue(english.has(key), () -> "Missing en_us key: " + key);
            assertTrue(chinese.has(key), () -> "Missing zh_cn key: " + key);
            assertFalse(english.get(key).getAsString().isBlank(), () -> "Blank en_us key: " + key);
            assertFalse(chinese.get(key).getAsString().isBlank(), () -> "Blank zh_cn key: " + key);
        }
    }

    private static JsonObject readLanguage(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static Path sourceLanguage(String fileName) {
        Path relativePath = Path.of(LANGUAGE_ROOT, fileName);
        Path workingDirectory = Path.of("").toAbsolutePath();
        Path discovered = findFrom(workingDirectory, relativePath);
        if (discovered != null) {
            return discovered;
        }

        try {
            URI location = UltimateTimeWandConfigLanguageContractTest.class
                    .getProtectionDomain().getCodeSource().getLocation().toURI();
            discovered = findFrom(Path.of(location), relativePath);
        } catch (Exception ignored) {
            // The assertion below reports the useful source path if discovery fails.
        }
        return discovered == null ? relativePath : discovered;
    }

    private static Path findFrom(Path start, Path relativePath) {
        Path current = Files.isDirectory(start) ? start : start.getParent();
        while (current != null) {
            Path candidate = current.resolve(relativePath);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return null;
    }
}
