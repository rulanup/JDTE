package com.jdte.common.items;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorFuelRuntimeHookTest {
    @Test
    void generatorBaseRuntimeStillRedirectsDoBurnFuelCanisterMultiplierLookup() throws Exception {
        Path projectRoot = findProjectRoot(Path.of(System.getProperty("user.dir", "")).toAbsolutePath());
        if (projectRoot == null) {
            projectRoot = findProjectRoot(Path.of(GeneratorFuelRuntimeHookTest.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()).toAbsolutePath());
        }
        assertTrue(projectRoot != null, "Could not locate project root from test runtime path");
        String source = Files.readString(projectRoot.resolve("src/main/java/com/jdte/mixin/GeneratorT1UpgradeMixin.java"));

        assertTrue(source.contains("method = \"doBurn\""));
        assertTrue(source.contains("FuelCanister;getBurnSpeedMultiplier"));
        assertTrue(source.contains("PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier"));
    }

    private static Path findProjectRoot(Path start) {
        Path current = start;
        while (current != null) {
            if (Files.exists(current.resolve("gradle.properties"))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }
}
