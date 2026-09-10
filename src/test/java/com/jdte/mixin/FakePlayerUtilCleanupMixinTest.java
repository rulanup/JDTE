package com.jdte.common.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakePlayerUtilCleanupMixinTest {
    @Test
    void registersTheFakePlayerCleanupDropSuppressionMixin() throws IOException {
        try (var stream = FakePlayerUtilCleanupMixinTest.class.getClassLoader()
                .getResourceAsStream("mixins.jdte.json")) {
            assertNotNull(stream);
            String mixinConfig = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(mixinConfig.contains("FakePlayerUtilCleanupMixin"));
        }
    }

    @Test
    void containerScreenMixinIsRegisteredOnlyForClients() throws IOException {
        try (var stream = FakePlayerUtilCleanupMixinTest.class.getClassLoader()
                .getResourceAsStream("mixins.jdte.json")) {
            assertNotNull(stream);
            JsonObject config = JsonParser.parseString(
                    new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray commonMixins = config.getAsJsonArray("mixins");
            JsonArray clientMixins = config.getAsJsonArray("client");

            assertFalse(commonMixins.asList().stream()
                    .anyMatch(element -> "AbstractContainerScreenMixin".equals(element.getAsString())));
            assertTrue(clientMixins.asList().stream()
                    .anyMatch(element -> "AbstractContainerScreenMixin".equals(element.getAsString())));
        }
    }
}
