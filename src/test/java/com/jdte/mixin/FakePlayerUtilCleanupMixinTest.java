package com.jdte.common.mixin;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
