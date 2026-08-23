package com.jdte.setup.config;

import com.jdte.setup.JDTEConfig;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

public final class TimeAcceleratorIntegratedServerDefaults {
    private TimeAcceleratorIntegratedServerDefaults() {
    }

    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        applyLoadedDefaults(event.getServer().isDedicatedServer());
    }

    public static boolean applyLoadedDefaults(boolean dedicatedServer) {
        if (dedicatedServer || !JDTEConfig.LOCAL_SPEC.isLoaded() || !JDTEConfig.SERVER_SPEC.isLoaded()) {
            return false;
        }

        JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerateAllMachines.set(
                JDTEConfig.LOCAL.timeAccelerator.timeAcceleratorAccelerateAllMachines.get());
        JDTEConfig.SERVER.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.set(
                JDTEConfig.LOCAL.timeAccelerator.timeAcceleratorAccelerationDurationSeconds.get());
        return true;
    }
}
