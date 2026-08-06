package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TimeFreezerConfig {
    public final ModConfigSpec.IntValue timeFreezerFluidCapacity;
    public final ModConfigSpec.IntValue timeFreezerFluidPerTick;

    public TimeFreezerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Time Freezer Settings")
                .translation("config.jdte.jdte.timeFreezer")
                .push("timeFreezer");
        timeFreezerFluidCapacity = builder
                .translation("config.jdte.jdte.timeFreezer.fluidCapacity")
                .defineInRange("fluidCapacity", 8000, 100, 1000000);
        timeFreezerFluidPerTick = builder
                .comment("Time Fluid consumed per tick while the Time Freezer freezes time and weather")
                .translation("config.jdte.jdte.timeFreezer.fluidPerTick")
                .defineInRange("fluidPerTick", 100, 1, 100000);
        builder.pop();
    }
}
