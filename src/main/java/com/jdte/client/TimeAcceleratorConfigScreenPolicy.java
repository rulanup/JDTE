package com.jdte.client;

import net.neoforged.fml.config.ModConfig;

public final class TimeAcceleratorConfigScreenPolicy {
    public enum ScreenRoute {
        STANDARD_CONFIGURATION_SCREEN(false),
        DIRECT_LOCAL_DEFAULTS_SECTION(true),
        DIRECT_SERVER_SECTION(true);

        private final boolean standardConfigurationParent;

        ScreenRoute(boolean standardConfigurationParent) {
            this.standardConfigurationParent = standardConfigurationParent;
        }

        public boolean usesStandardConfigurationParent() {
            return standardConfigurationParent;
        }
    }

    private TimeAcceleratorConfigScreenPolicy() {
    }

    public static ScreenRoute selectScreen(boolean activeWorld, boolean serverConfigAvailable,
                                           boolean localDefaultsAvailable) {
        if (activeWorld && serverConfigAvailable) {
            return ScreenRoute.DIRECT_SERVER_SECTION;
        }
        if (!activeWorld && localDefaultsAvailable) {
            return ScreenRoute.DIRECT_LOCAL_DEFAULTS_SECTION;
        }
        return ScreenRoute.STANDARD_CONFIGURATION_SCREEN;
    }

    public static boolean shouldLockFields(ModConfig.Type configType, boolean localDefaultsConfig,
                                           boolean activeWorld) {
        return activeWorld && (configType == ModConfig.Type.SERVER || localDefaultsConfig);
    }
}
