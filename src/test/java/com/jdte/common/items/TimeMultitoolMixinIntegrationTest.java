package com.jdte.common.items;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TimeMultitoolMixinIntegrationTest {
    @Test
    void jdtHelpersLoadsWithTheRemovalResultMixinApplied() throws ClassNotFoundException {
        assertNotNull(Class.forName(
                "com.direwolf20.justdirethings.common.items.interfaces.Helpers",
                true,
                TimeMultitoolMixinIntegrationTest.class.getClassLoader()));
    }
}
