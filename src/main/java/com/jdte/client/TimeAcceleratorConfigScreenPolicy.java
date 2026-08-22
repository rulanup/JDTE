package com.jdte.client;

import net.neoforged.fml.config.ModConfig;

public final class TimeAcceleratorConfigScreenPolicy {
    private TimeAcceleratorConfigScreenPolicy() {
    }

    public static boolean lockServerFields(boolean activeWorld) {
        return shouldLockFields(ModConfig.Type.SERVER, activeWorld);
    }

    public static boolean shouldLockFields(ModConfig.Type configType, boolean activeWorld) {
        return configType == ModConfig.Type.SERVER && activeWorld;
    }
}
