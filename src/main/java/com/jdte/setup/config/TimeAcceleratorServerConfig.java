package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TimeAcceleratorServerConfig {
    public final ModConfigSpec.BooleanValue timeAcceleratorAccelerateAllMachines;
    public final ModConfigSpec.IntValue timeAcceleratorAccelerationDurationSeconds;

    public TimeAcceleratorServerConfig(ModConfigSpec.Builder builder) {
        builder.comment(
                "Authoritative Time Accelerator settings for the running server.",
                "The in-game config screen is read-only after entering a world.",
                "Dedicated server administrators should stop the server, edit",
                "world/serverconfig/jdte/time-accelerator-server.toml, then restart",
                "the server so NeoForge loads the updated values.")
                .translation("config.jdte.jdte.serverTimeAccelerator")
                .push("timeAccelerator");
        timeAcceleratorAccelerateAllMachines = builder
                .comment(
                        "Process all discovered machines in one scheduler pass.",
                        "This can cause server lag when many machines are discovered.",
                        "This authoritative server value is read-only while a world is active.")
                .translation("config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerateAllMachines")
                .define("timeAcceleratorAccelerateAllMachines", false);
        timeAcceleratorAccelerationDurationSeconds = builder
                .comment(
                        "Authoritative acceleration duration per submission in seconds.",
                        "Valid range: 1-60. Edit the serverconfig file while the server is",
                        "stopped, then restart the world/server to load the change.")
                .translation("config.jdte.jdte.serverTimeAccelerator.timeAcceleratorAccelerationDurationSeconds")
                .defineInRange("timeAcceleratorAccelerationDurationSeconds", 1, 1, 60);
        builder.pop();
    }
}
