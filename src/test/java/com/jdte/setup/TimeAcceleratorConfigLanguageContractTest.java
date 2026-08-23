package com.jdte.setup;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeAcceleratorConfigLanguageContractTest {
    private static final String SERVER_PARALLEL_TOOLTIP =
            "config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerateAllMachines.tooltip";
    private static final String SERVER_DURATION_TOOLTIP =
            "config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerationDurationSeconds.tooltip";
    private static final String LOCAL_PARALLEL_TOOLTIP =
            "config.jdte.jdte.localTimeAccelerator.timeAcceleratorAccelerateAllMachines.tooltip";
    private static final String LOCAL_DURATION_TOOLTIP =
            "config.jdte.jdte.localTimeAccelerator.timeAcceleratorAccelerationDurationSeconds.tooltip";

    private static final List<String> REQUIRED_KEYS = List.of(
            "config.jdte.jdte.server",
            "config.jdte.jdte.local",
            "config.jdte.jdte.serverTimeAccelerator",
            "config.jdte.jdte.localTimeAccelerator",
            "jdte.configuration.section.jdte.time.accelerator.server.toml.title",
            "jdte.configuration.section.jdte.time.accelerator.local.toml.title",
            SERVER_PARALLEL_TOOLTIP,
            SERVER_DURATION_TOOLTIP,
            LOCAL_PARALLEL_TOOLTIP,
            LOCAL_DURATION_TOOLTIP);

    @Test
    void englishAndChineseExposeEveryRealNeoForgeTooltipAndSectionTitle() {
        JsonObject english = language("en_us");
        JsonObject chinese = language("zh_cn");

        for (String key : REQUIRED_KEYS) {
            assertTrue(english.has(key), () -> "Missing en_us key: " + key);
            assertTrue(chinese.has(key), () -> "Missing zh_cn key: " + key);
            assertFalse(english.get(key).getAsString().isBlank(), () -> "Blank en_us key: " + key);
            assertFalse(chinese.get(key).getAsString().isBlank(), () -> "Blank zh_cn key: " + key);
        }
    }

    @Test
    void tooltipsRetainTheRequiredOperationalWarningsAndEditingRules() {
        JsonObject english = language("en_us");
        JsonObject chinese = language("zh_cn");
        String englishText = tooltipText(english).toLowerCase();
        String chineseText = tooltipText(chinese);

        assertTrue(englishText.contains("lag"));
        assertTrue(englishText.contains("read-only"));
        assertTrue(englishText.contains("main menu"));
        assertTrue(englishText.contains("1 to 60"));
        assertTrue(englishText.contains("serverconfig"));
        assertTrue(englishText.contains("singleplayer"));

        assertTrue(chineseText.contains("卡顿"));
        assertTrue(chineseText.contains("只读"));
        assertTrue(chineseText.contains("主菜单"));
        assertTrue(chineseText.contains("1-60"));
        assertTrue(chineseText.contains("serverconfig"));
        assertTrue(chineseText.contains("单人"));
    }

    private static String tooltipText(JsonObject language) {
        return language.get(SERVER_PARALLEL_TOOLTIP).getAsString()
                + language.get(SERVER_DURATION_TOOLTIP).getAsString()
                + language.get(LOCAL_PARALLEL_TOOLTIP).getAsString()
                + language.get(LOCAL_DURATION_TOOLTIP).getAsString();
    }

    private static JsonObject language(String locale) {
        String path = "/assets/jdte/lang/" + locale + ".json";
        InputStream resource = TimeAcceleratorConfigLanguageContractTest.class.getResourceAsStream(path);
        assertNotNull(resource, () -> "Missing language resource: " + path);
        try (InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            throw new AssertionError("Unable to parse language resource: " + path, e);
        }
    }
}
