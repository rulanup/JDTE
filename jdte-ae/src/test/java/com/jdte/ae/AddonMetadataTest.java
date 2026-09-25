package com.jdte.ae;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddonMetadataTest {
    @Test
    void declaresAddonAndRequiredDependencies() throws IOException {
        Config modsToml = loadModsToml();

        assertEquals("jdte_ae", onlyMod(modsToml).get("modId"));
        assertDependency(modsToml, "jdte", "[0.6.0-fix1]");
        assertDependency(modsToml, "ae2", "[19.2.17,20)");
        assertDependency(modsToml, "minecraft", "[1.21.1,1.22)");
    }

    @Test
    void registersRequiredTickHandlerMixin() throws IOException {
        Config modsToml = loadModsToml();
        List<? extends Config> mixinRegistrations = modsToml.get("mixins");
        assertNotNull(mixinRegistrations, "mixins must be a TOML table array");
        assertEquals(1, mixinRegistrations.size(), "exactly one [[mixins]] table is required");
        assertEquals("mixins.jdte_ae.json", mixinRegistrations.getFirst().get("config"));

        JsonObject mixinConfig = loadMixinConfig();
        assertTrue(mixinConfig.getAsJsonPrimitive("required").isBoolean());
        assertTrue(mixinConfig.get("required").getAsBoolean());
        assertEquals("com.jdte.ae.mixin", mixinConfig.get("package").getAsString());
        JsonArray mixins = mixinConfig.getAsJsonArray("mixins");
        assertEquals(1, mixins.size());
        assertEquals("TickHandlerMixin", mixins.get(0).getAsString());
    }

    private static Config loadModsToml() throws IOException {
        Enumeration<URL> resources = AddonMetadataTest.class.getClassLoader()
                .getResources("META-INF/neoforge.mods.toml");
        while (resources.hasMoreElements()) {
            try (InputStream input = resources.nextElement().openStream()) {
                Config candidate = new TomlParser().parse(input);
                List<? extends Config> mods = candidate.get("mods");
                if (mods != null && mods.stream()
                        .anyMatch(mod -> "jdte_ae".equals(mod.get("modId")))) {
                    return candidate;
                }
            }
        }
        throw new AssertionError(
                "jdte_ae META-INF/neoforge.mods.toml must be present on the test classpath");
    }

    private static Config onlyMod(Config modsToml) {
        List<? extends Config> mods = modsToml.get("mods");
        assertNotNull(mods, "mods must be a TOML table array");
        assertEquals(1, mods.size(), "exactly one [[mods]] table is required");
        return mods.getFirst();
    }

    private static JsonObject loadMixinConfig() throws IOException {
        try (InputStream input = AddonMetadataTest.class.getClassLoader()
                .getResourceAsStream("mixins.jdte_ae.json")) {
            assertNotNull(input, "mixins.jdte_ae.json must be present on the test classpath");
            return JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }

    private static void assertDependency(Config modsToml, String modId, String versionRange) {
        List<? extends Config> dependencies = modsToml.get("dependencies.jdte_ae");
        assertNotNull(dependencies, "dependencies.jdte_ae must be a TOML table array");

        List<? extends Config> matches = dependencies.stream()
                .filter(dependency -> modId.equals(dependency.get("modId")))
                .toList();
        assertEquals(1, matches.size(), () -> "exactly one dependency is required for " + modId);

        Config dependency = matches.getFirst();
        assertEquals("required", dependency.get("type"), () -> modId + " must be required");
        assertEquals(versionRange, dependency.get("versionRange"), () -> modId + " version range");
    }
}
