package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TimeAcceleratorServerConfig {
    public final ModConfigSpec.BooleanValue timeAcceleratorAccelerateAllMachines;
    public final ModConfigSpec.IntValue timeAcceleratorAccelerationDurationSeconds;

    public TimeAcceleratorServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Time Accelerator Server Settings")
                .translation("config.jdte.jdte.serverTimeAccelerator")
                .push("timeAccelerator");
        timeAcceleratorAccelerateAllMachines = builder
                .comment("Accelerate all discovered machines in one scheduler pass; may cause server lag")
                .translation("config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerateAllMachines")
                .define("timeAcceleratorAccelerateAllMachines", false);
        timeAcceleratorAccelerationDurationSeconds = builder
                .comment("Acceleration duration per submission in seconds; valid range 1-60")
                .translation("config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerationDurationSeconds")
                .defineInRange("timeAcceleratorAccelerationDurationSeconds", 1, 1, 60);
        builder.pop();
    }
}
