package com.jdte.setup;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.jdte.setup.config.TimeAcceleratorIntegratedServerDefaults;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeAcceleratorIntegratedServerDefaultsTest {

    @AfterEach
    void unloadConfigs() {
        JDTEConfig.LOCAL_SPEC.acceptConfig(null);
        JDTEConfig.SERVER_SPEC.acceptConfig(null);
    }

    @Test
    void integratedServerAppliesLoadedLocalDefaultsToAuthoritativeServerValues() {
        JDTEConfig.LOCAL_SPEC.acceptConfig(loadedConfig(JDTEConfig.LOCAL_SPEC, false, 7));
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedConfig(JDTEConfig.SERVER_SPEC, true, 55));

        assertTrue(TimeAcceleratorIntegratedServerDefaults.applyLoadedDefaults(false));
        assertFalse(JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerateAllMachines.get());
        assertEquals(7, JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.get());
    }

    @Test
    void dedicatedServerNeverReadsOrAppliesClientLocalDefaults() {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedConfig(JDTEConfig.SERVER_SPEC, true, 55));

        assertFalse(TimeAcceleratorIntegratedServerDefaults.applyLoadedDefaults(true));
        assertTrue(JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerateAllMachines.get());
        assertEquals(55, JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.get());
    }

    @Test
    void integratedServerLeavesServerConfigUntouchedWhenLocalDefaultsAreUnavailable() {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedConfig(JDTEConfig.SERVER_SPEC, true, 55));

        assertFalse(TimeAcceleratorIntegratedServerDefaults.applyLoadedDefaults(false));
        assertTrue(JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerateAllMachines.get());
        assertEquals(55, JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.get());
    }

    private static IConfigSpec.ILoadedConfig loadedConfig(ModConfigSpec spec, boolean accelerateAll, int durationSeconds) {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set(List.of("jdte", "timeAccelerator", "timeAcceleratorAccelerateAllMachines"), accelerateAll);
        config.set(List.of("jdte", "timeAccelerator", "timeAcceleratorAccelerationDurationSeconds"), durationSeconds);
        spec.correct(config);
        try {
            Class<?> loadedConfigClass = Class.forName("net.neoforged.fml.config.LoadedConfig");
            Constructor<?> constructor = loadedConfigClass.getDeclaredConstructor(
                    CommentedConfig.class,
                    java.nio.file.Path.class,
                    Class.forName("net.neoforged.fml.config.ModConfig"));
            constructor.setAccessible(true);
            return (IConfigSpec.ILoadedConfig) constructor.newInstance(config, null, null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to create LoadedConfig test fixture", e);
        }
    }
}
