package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TimeAcceleratorLocalConfig {
    public final ModConfigSpec.BooleanValue timeAcceleratorAccelerateAllMachines;
    public final ModConfigSpec.IntValue timeAcceleratorAccelerationDurationSeconds;

    public TimeAcceleratorLocalConfig(ModConfigSpec.Builder builder) {
        builder.comment(
                "Local singleplayer Time Accelerator defaults.",
                "Edit these values from the main menu. They are copied into the",
                "authoritative SERVER config only when an integrated server starts.",
                "They never affect dedicated or remote multiplayer servers and are",
                "read-only after entering a world.")
                .translation("config.jdte.jdte.localTimeAccelerator")
                .push("timeAccelerator");
        timeAcceleratorAccelerateAllMachines = builder
                .comment(
                        "Local singleplayer default for processing every discovered machine",
                        "in one scheduler pass. Large machine counts may cause server lag.")
                .translation("config.jdte.jdte.localTimeAccelerator.timeAcceleratorAccelerateAllMachines")
                .define("timeAcceleratorAccelerateAllMachines", false);
        timeAcceleratorAccelerationDurationSeconds = builder
                .comment(
                        "Local singleplayer default acceleration duration per submission.",
                        "Valid range: 1-60 seconds. Applied at the next integrated server start.")
                .translation("config.jdte.jdte.localTimeAccelerator.timeAcceleratorAccelerationDurationSeconds")
                .defineInRange("timeAcceleratorAccelerationDurationSeconds", 1, 1, 60);
        builder.pop();
    }
}
