package com.jdte.client.screens;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LargeCanisterScreenLiveRefreshTest {
    @Test
    void potionScreenUsesTheLiveMenuStackForItsDisplayState() throws Exception {
        String source = Files.readString(findProjectRoot().resolve(
                "src/main/java/com/jdte/client/screens/LargePotionCanisterScreen.java"));

        assertTrue(source.contains("menu.getCurrentStack()"));
        assertFalse(source.contains("menu.getBoundStack()"));
    }

    @Test
    void fuelScreenUsesTheLiveMenuStackForItsDisplayState() throws Exception {
        String source = Files.readString(findProjectRoot().resolve(
                "src/main/java/com/jdte/client/screens/LargeFuelCanisterScreen.java"));

        assertTrue(source.contains("menu.getCurrentStack()"));
        assertFalse(source.contains("menu.getBoundStack()"));
    }

    private static Path findProjectRoot() {
        Path current = Path.of(System.getProperty("user.dir", "")).toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("gradle.properties"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new AssertionError("Could not locate project root from test runtime path");
        }
        return current;
    }
}
