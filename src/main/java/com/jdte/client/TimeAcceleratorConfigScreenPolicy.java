package com.jdte.client;

import net.neoforged.fml.config.ModConfig;

public final class TimeAcceleratorConfigScreenPolicy {
    public enum ScreenRoute {
        STANDARD_CONFIGURATION_SCREEN(false),
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

    public static ScreenRoute selectScreen(boolean multiplayerConnected, boolean serverConfigAvailable) {
        return multiplayerConnected && serverConfigAvailable
                ? ScreenRoute.DIRECT_SERVER_SECTION
                : ScreenRoute.STANDARD_CONFIGURATION_SCREEN;
    }

    public static boolean lockServerFields(boolean activeWorld) {
        return shouldLockFields(ModConfig.Type.SERVER, activeWorld);
    }

    public static boolean shouldLockFields(ModConfig.Type configType, boolean activeWorld) {
        return configType == ModConfig.Type.SERVER && activeWorld;
    }
}
