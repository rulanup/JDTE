package com.jdte.client;

import net.neoforged.fml.config.ModConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeAcceleratorConfigScreenPolicyTest {

    @Test
    void serverFieldsAreEditableOnlyWithoutAnActiveWorld() {
        assertFalse(TimeAcceleratorConfigScreenPolicy.lockServerFields(false));
        assertTrue(TimeAcceleratorConfigScreenPolicy.lockServerFields(true));
    }

    @Test
    void nonServerConfigsRemainEditableEvenWhenAWorldIsActive() {
        assertFalse(TimeAcceleratorConfigScreenPolicy.shouldLockFields(ModConfig.Type.COMMON, true));
        assertFalse(TimeAcceleratorConfigScreenPolicy.shouldLockFields(ModConfig.Type.CLIENT, true));
        assertFalse(TimeAcceleratorConfigScreenPolicy.shouldLockFields(ModConfig.Type.STARTUP, true));
    }
}
