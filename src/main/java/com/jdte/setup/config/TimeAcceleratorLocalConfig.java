package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TimeAcceleratorLocalConfig {
    public final ModConfigSpec.BooleanValue timeAcceleratorAccelerateAllMachines;
    public final ModConfigSpec.IntValue timeAcceleratorAccelerationDurationSeconds;

    public TimeAcceleratorLocalConfig(ModConfigSpec.Builder builder) {
        builder.comment("Local singleplayer Time Accelerator defaults")
                .translation("config.jdte.jdte.localTimeAccelerator")
                .push("timeAccelerator");
        timeAcceleratorAccelerateAllMachines = builder
                .comment("Default for processing all discovered machines in one scheduler pass in local singleplayer worlds")
                .translation("config.jdte.jdte.localTimeAccelerator.timeAcceleratorAccelerateAllMachines")
                .define("timeAcceleratorAccelerateAllMachines", false);
        timeAcceleratorAccelerationDurationSeconds = builder
                .comment("Default acceleration duration per submission for local singleplayer worlds; valid range 1-60 seconds")
                .translation("config.jdte.jdte.localTimeAccelerator.timeAcceleratorAccelerationDurationSeconds")
                .defineInRange("timeAcceleratorAccelerationDurationSeconds", 1, 1, 60);
        builder.pop();
    }
}
