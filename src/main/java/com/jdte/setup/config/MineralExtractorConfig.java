package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class MineralExtractorConfig {
    public final ForgeConfigSpec.IntValue energyCapacity;
    public final ForgeConfigSpec.IntValue fluidCapacity;
    public final ForgeConfigSpec.IntValue energyPerCycle;
    public final ForgeConfigSpec.IntValue experienceFluidPerCycle;
    public final ForgeConfigSpec.IntValue timeFluidPerAcceleratedCycle;
    public final ForgeConfigSpec.IntValue fortuneBonusPercent;
    public final ForgeConfigSpec.IntValue processTicks;
    public final ForgeConfigSpec.IntValue settlementInterval;
    public final ForgeConfigSpec.IntValue defaultMultiplier;
    public final ForgeConfigSpec.IntValue maxMultiplier;
    public final ForgeConfigSpec.IntValue overclockMaxMultiplier;
    public final ForgeConfigSpec.IntValue maxCyclesPerSettlement;
    public final ForgeConfigSpec.LongValue maxPendingWork;

    public MineralExtractorConfig(ForgeConfigSpec.Builder builder) {
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
                .comment("Optional fortune fluid consumed per bonus-yield batch. Base production does not require it; 0 makes the bonus free.")
                .defineInRange("experienceFluidPerCycle", 25, 0, Integer.MAX_VALUE);
        timeFluidPerAcceleratedCycle = builder.translation("config.jdte.jdte.mineralExtractor.timeFluidPerAcceleratedCycle")
                .comment("Optional acceleration fluid consumed per extra batch above base 1x. Base batches do not consume it; 0 makes acceleration free.")
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
                .defineInRange("maxMultiplier", 32, 1, 32);
        overclockMaxMultiplier = builder.translation("config.jdte.jdte.mineralExtractor.overclockMaxMultiplier")
                .defineInRange("overclockMaxMultiplier", 64, 1, 64);
        maxCyclesPerSettlement = builder.translation("config.jdte.jdte.mineralExtractor.maxCyclesPerSettlement")
                .defineInRange("maxCyclesPerSettlement", 65_536, 1, 10_000_000);
        maxPendingWork = builder.translation("config.jdte.jdte.mineralExtractor.maxPendingWork")
                .defineInRange("maxPendingWork", 20_000_000L, 1L, Long.MAX_VALUE);
        builder.pop();
    }
}
