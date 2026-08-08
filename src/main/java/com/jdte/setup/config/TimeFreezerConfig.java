package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class TimeFreezerConfig {
    public final ForgeConfigSpec.IntValue timeFreezerFluidCapacity;
    public final ForgeConfigSpec.IntValue timeFreezerFluidPerTick;
    public final ForgeConfigSpec.IntValue timeFreezerEnergyCapacity;
    public final ForgeConfigSpec.IntValue timeFreezerEnergyPerTick;

    public TimeFreezerConfig(ForgeConfigSpec.Builder builder) {
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
