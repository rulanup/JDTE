package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BioFactoryConfig {
    public final ModConfigSpec.IntValue bioFactoryFluidCapacity;
    public final ModConfigSpec.IntValue bioFactoryEnergyCapacity;
    public final ModConfigSpec.IntValue bioFactoryEnergyPerCycle;
    public final ModConfigSpec.IntValue bioFactoryBaseProcessTicks;
    public final ModConfigSpec.IntValue bioFactorySettlementInterval;
    public final ModConfigSpec.IntValue bioFactoryTimeFluidPerCycle;
    public final ModConfigSpec.IntValue bioFactoryMaxSpeedMultiplier;
    public final ModConfigSpec.IntValue bioFactoryDefaultSpeedMultiplier;
    public final ModConfigSpec.IntValue bioFactoryOverclockMaxSpeedMultiplier;
    public final ModConfigSpec.IntValue bioFactoryLifeFluidPerCycle;
    public final ModConfigSpec.DoubleValue bioFactoryLifeYieldMultiplier;
    public final ModConfigSpec.IntValue bioFactoryProcessFluidPerCycle;
    public final ModConfigSpec.IntValue bioFactoryExternalTimeFluidCostMultiplier;
    public final ModConfigSpec.IntValue bioFactoryExternalLifeFluidCostMultiplier;

    public BioFactoryConfig(ModConfigSpec.Builder builder) {
        builder.comment("Bio Factory Settings")
                .translation("config.jdte.jdte.bioFactory")
                .push("bioFactory");
        bioFactoryFluidCapacity = builder.translation("config.jdte.jdte.bioFactory.fluidCapacity")
                .defineInRange("fluidCapacity", 64000, 1000, Integer.MAX_VALUE);
        bioFactoryEnergyCapacity = builder.translation("config.jdte.jdte.bioFactory.energyCapacity")
                .defineInRange("energyCapacity", 10000000, 1000, Integer.MAX_VALUE);
        bioFactoryEnergyPerCycle = builder.translation("config.jdte.jdte.bioFactory.energyPerCycle")
                .defineInRange("energyPerCycle", 1000, 0, Integer.MAX_VALUE);
        bioFactoryBaseProcessTicks = builder.translation("config.jdte.jdte.bioFactory.baseProcessTicks")
                .defineInRange("baseProcessTicks", 600, 1, 72000);
        bioFactorySettlementInterval = builder.comment("Ticks between lightweight production settlements")
                .translation("config.jdte.jdte.bioFactory.settlementInterval")
                .defineInRange("settlementInterval", 20, 1, 1200);
        bioFactoryTimeFluidPerCycle = builder.translation("config.jdte.jdte.bioFactory.timeFluidPerCycle")
                .defineInRange("timeFluidPerCycle", 10, 0, Integer.MAX_VALUE);
        bioFactoryMaxSpeedMultiplier = builder.translation("config.jdte.jdte.bioFactory.maxSpeedMultiplier")
                .defineInRange("maxSpeedMultiplier", 32, 1, 64);
        bioFactoryDefaultSpeedMultiplier = builder.translation("config.jdte.jdte.bioFactory.defaultSpeedMultiplier")
                .defineInRange("defaultSpeedMultiplier", 1, 1, 64);
        bioFactoryOverclockMaxSpeedMultiplier = builder.translation("config.jdte.jdte.bioFactory.overclockMaxSpeedMultiplier")
                .defineInRange("overclockMaxSpeedMultiplier", 64, 1, 128);
        bioFactoryLifeFluidPerCycle = builder.translation("config.jdte.jdte.bioFactory.lifeFluidPerCycle")
                .defineInRange("lifeFluidPerCycle", 100, 0, Integer.MAX_VALUE);
        bioFactoryLifeYieldMultiplier = builder.translation("config.jdte.jdte.bioFactory.lifeYieldMultiplier")
                .defineInRange("lifeYieldMultiplier", 2.0D, 1.0D, 64.0D);
        bioFactoryProcessFluidPerCycle = builder.translation("config.jdte.jdte.bioFactory.processFluidPerCycle")
                .defineInRange("processFluidPerCycle", 100, 0, Integer.MAX_VALUE);
        bioFactoryExternalTimeFluidCostMultiplier = builder
                .translation("config.jdte.jdte.bioFactory.externalTimeFluidCostMultiplier")
                .defineInRange("externalTimeFluidCostMultiplier", 10, 1, 1000);
        bioFactoryExternalLifeFluidCostMultiplier = builder
                .translation("config.jdte.jdte.bioFactory.externalLifeFluidCostMultiplier")
                .defineInRange("externalLifeFluidCostMultiplier", 5, 1, 1000);
        builder.pop();
    }
}
