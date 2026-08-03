package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MineralExtractorConfig {
    public final ModConfigSpec.IntValue energyCapacity;
    public final ModConfigSpec.IntValue fluidCapacity;
    public final ModConfigSpec.IntValue energyPerCycle;
    public final ModConfigSpec.IntValue experienceFluidPerCycle;
    public final ModConfigSpec.IntValue timeFluidPerAcceleratedCycle;
    public final ModConfigSpec.IntValue fortuneBonusPercent;
    public final ModConfigSpec.IntValue processTicks;
    public final ModConfigSpec.IntValue settlementInterval;
    public final ModConfigSpec.IntValue defaultMultiplier;
    public final ModConfigSpec.IntValue maxMultiplier;
    public final ModConfigSpec.IntValue overclockMaxMultiplier;
    public final ModConfigSpec.IntValue maxCyclesPerSettlement;
    public final ModConfigSpec.LongValue maxPendingWork;

    public MineralExtractorConfig(ModConfigSpec.Builder builder) {
        builder.comment("Mineral Extractor Settings")
                .translation("config.jdte.jdte.mineralExtractor")
                .push("mineralExtractor");
        energyCapacity = builder.translation("config.jdte.jdte.mineralExtractor.energyCapacity")
                .defineInRange("energyCapacity", 2_000_000, 1, Integer.MAX_VALUE);
        fluidCapacity = builder.translation("config.jdte.jdte.mineralExtractor.fluidCapacity")
                .defineInRange("fluidCapacity", 64_000, 1, Integer.MAX_VALUE);
        energyPerCycle = builder.translation("config.jdte.jdte.mineralExtractor.energyPerCycle")
                .defineInRange("energyPerCycle", 5_000, 0, Integer.MAX_VALUE);
        experienceFluidPerCycle = builder.translation("config.jdte.jdte.mineralExtractor.experienceFluidPerCycle")
                .defineInRange("experienceFluidPerCycle", 25, 0, Integer.MAX_VALUE);
        timeFluidPerAcceleratedCycle = builder.translation("config.jdte.jdte.mineralExtractor.timeFluidPerAcceleratedCycle")
                .defineInRange("timeFluidPerAcceleratedCycle", 5, 0, Integer.MAX_VALUE);
        fortuneBonusPercent = builder.translation("config.jdte.jdte.mineralExtractor.fortuneBonusPercent")
                .defineInRange("fortuneBonusPercent", 100, 0, 10_000);
        processTicks = builder.translation("config.jdte.jdte.mineralExtractor.processTicks")
                .defineInRange("processTicks", 20, 1, 72_000);
        settlementInterval = builder.translation("config.jdte.jdte.mineralExtractor.settlementInterval")
                .defineInRange("settlementInterval", 20, 1, 1200);
        defaultMultiplier = builder.translation("config.jdte.jdte.mineralExtractor.defaultMultiplier")
                .defineInRange("defaultMultiplier", 1, 1, 1024);
        maxMultiplier = builder.translation("config.jdte.jdte.mineralExtractor.maxMultiplier")
                .defineInRange("maxMultiplier", 32, 1, 1024);
        overclockMaxMultiplier = builder.translation("config.jdte.jdte.mineralExtractor.overclockMaxMultiplier")
                .defineInRange("overclockMaxMultiplier", 1024, 1, 1024);
        maxCyclesPerSettlement = builder.translation("config.jdte.jdte.mineralExtractor.maxCyclesPerSettlement")
                .defineInRange("maxCyclesPerSettlement", 65_536, 1, 10_000_000);
        maxPendingWork = builder.translation("config.jdte.jdte.mineralExtractor.maxPendingWork")
                .defineInRange("maxPendingWork", 20_000_000L, 1L, Long.MAX_VALUE);
        builder.pop();
    }
}