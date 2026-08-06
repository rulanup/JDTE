package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TimeFreezerConfig {
    public final ModConfigSpec.IntValue timeFreezerFluidCapacity;
    public final ModConfigSpec.IntValue timeFreezerFluidPerTick;
    public final ModConfigSpec.IntValue timeFreezerEnergyCapacity;
    public final ModConfigSpec.IntValue timeFreezerEnergyPerTick;

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
        timeFreezerEnergyCapacity = builder
                .translation("config.jdte.jdte.timeFreezer.energyCapacity")
                .defineInRange("energyCapacity", 200000, 1000, Integer.MAX_VALUE);
        timeFreezerEnergyPerTick = builder
                .comment("FE consumed per tick while the Time Freezer freezes time and weather")
                .translation("config.jdte.jdte.timeFreezer.energyPerTick")
                .defineInRange("energyPerTick", 100, 1, 100000);
        builder.pop();
    }
}
