package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TimeAcceleratorConfig {
    public final ModConfigSpec.IntValue timeAcceleratorBaseFluidCapacity;
    public final ModConfigSpec.DoubleValue timeAcceleratorFluidCostMultiplier;
    public final ModConfigSpec.IntValue basicTimeAcceleratorDefaultMultiplier;
    public final ModConfigSpec.IntValue basicTimeAcceleratorOverclockMultiplier;
    public final ModConfigSpec.IntValue advancedTimeAcceleratorEnergyCapacity;
    public final ModConfigSpec.IntValue advancedTimeAcceleratorMaxMultiplier;
    public final ModConfigSpec.IntValue advancedTimeAcceleratorOverclockMultiplier;
    public final ModConfigSpec.IntValue advancedTimeAcceleratorDefaultMultiplier;
    public final ModConfigSpec.IntValue extendedTimeAcceleratorMaxMultiplier;
    public final ModConfigSpec.IntValue extendedTimeAcceleratorOverclockMultiplier;
    public final ModConfigSpec.IntValue timeAcceleratorMaxExecutionsPerTick;
    public final ModConfigSpec.IntValue timeAcceleratorMaxScannedBlocksPerTick;
    public final ModConfigSpec.LongValue timeAcceleratorMaxPendingTicks;
    public final ModConfigSpec.IntValue timeAcceleratorExecutionBatchSize;
    public final ModConfigSpec.IntValue timeAcceleratorRandomRefreshInterval;
    public final ModConfigSpec.BooleanValue timeAcceleratorAE2Enabled;
    public final ModConfigSpec.IntValue ultimateTimeWandFluidCapacity;
    public final ModConfigSpec.IntValue ultimateTimeWandEnergyCapacity;
    public final ModConfigSpec.IntValue ultimateTimeWandDuration;
    public final ModConfigSpec.IntValue ultimateTimeWandMaxExponent;
    public final ModConfigSpec.IntValue ultimateTimeWandNormalStep;
    public final ModConfigSpec.IntValue ultimateTimeWandX2Step;
    public final ModConfigSpec.IntValue ultimateTimeWandX4Step;
    public final ModConfigSpec.IntValue ultimateTimeWandMaxStep;
    public final ModConfigSpec.DoubleValue ultimateTimeWandBaseCostMultiplier;
    public final ModConfigSpec.DoubleValue ultimateTimeWandEnergyCostMultiplier;
    public final ModConfigSpec.BooleanValue ultimateTimeWandFractionalFluidSettlement;

    public TimeAcceleratorConfig(ModConfigSpec.Builder builder) {
        builder.comment("Time Accelerator Settings").translation("config.jdte.jdte.timeAccelerator").push("timeAccelerator");
        timeAcceleratorBaseFluidCapacity = builder
                .comment("Base fluid capacity for time accelerators (mB)")
                .translation("config.jdte.jdte.timeAccelerator.timeAcceleratorBaseFluidCapacity")
                .defineInRange("timeAcceleratorBaseFluidCapacity", 1000, 100, 100000);
        timeAcceleratorFluidCostMultiplier = builder
                .comment("Time accelerator fluid cost multiplier. 1.0 matches the JDT Time Wand cost spread over 30 seconds.")
                .translation("config.jdte.jdte.timeAccelerator.timeAcceleratorFluidCostMultiplier")
                .defineInRange("timeAcceleratorFluidCostMultiplier", 1.0D, 0.0D, 1000.0D);
        basicTimeAcceleratorDefaultMultiplier = builder
                .comment("Basic time accelerator default multiplier")
                .translation("config.jdte.jdte.timeAccelerator.basicTimeAcceleratorDefaultMultiplier")
                .defineInRange("basicTimeAcceleratorDefaultMultiplier", 16, 1, 100);
        basicTimeAcceleratorOverclockMultiplier = builder
                .comment("Basic time accelerator overclock multiplier")
                .translation("config.jdte.jdte.timeAccelerator.basicTimeAcceleratorOverclockMultiplier")
                .defineInRange("basicTimeAcceleratorOverclockMultiplier", 32, 1, 1000);
        advancedTimeAcceleratorEnergyCapacity = builder
                .comment("Advanced time accelerator energy capacity")
                .translation("config.jdte.jdte.timeAccelerator.advancedTimeAcceleratorEnergyCapacity")
                .defineInRange("advancedTimeAcceleratorEnergyCapacity", 200000, 10000, 10000000);
        advancedTimeAcceleratorMaxMultiplier = builder
                .comment("Advanced time accelerator max adjustable multiplier")
                .translation("config.jdte.jdte.timeAccelerator.advancedTimeAcceleratorMaxMultiplier")
                .defineInRange("advancedTimeAcceleratorMaxMultiplier", 64, 1, 1000);
        advancedTimeAcceleratorOverclockMultiplier = builder
                .comment("Advanced time accelerator overclock multiplier")
                .translation("config.jdte.jdte.timeAccelerator.advancedTimeAcceleratorOverclockMultiplier")
                .defineInRange("advancedTimeAcceleratorOverclockMultiplier", 128, 1, 10000);
        advancedTimeAcceleratorDefaultMultiplier = builder
                .comment("Advanced time accelerator default multiplier")
                .translation("config.jdte.jdte.timeAccelerator.advancedTimeAcceleratorDefaultMultiplier")
                .defineInRange("advancedTimeAcceleratorDefaultMultiplier", 4, 1, 100);
        extendedTimeAcceleratorMaxMultiplier = builder
                .comment("Extended time accelerator maximum adjustable multiplier")
                .translation("config.jdte.jdte.timeAccelerator.extendedTimeAcceleratorMaxMultiplier")
                .defineInRange("extendedTimeAcceleratorMaxMultiplier", 512, 1, 10000);
        extendedTimeAcceleratorOverclockMultiplier = builder
                .comment("Extended time accelerator multiplier with Overclock or Creative Upgrade")
                .translation("config.jdte.jdte.timeAccelerator.extendedTimeAcceleratorOverclockMultiplier")
                .defineInRange("extendedTimeAcceleratorOverclockMultiplier", 1024, 1, 100000);
        timeAcceleratorMaxExecutionsPerTick = builder
                .comment("Maximum managed virtual ticks executed per server tick. Work above this limit remains queued.")
                .translation("config.jdte.jdte.timeAccelerator.timeAcceleratorMaxExecutionsPerTick")
                .defineInRange("timeAcceleratorMaxExecutionsPerTick", 4096, 1, 1000000);
        timeAcceleratorMaxScannedBlocksPerTick = builder
                .comment("Maximum random-ticking block positions scanned per server tick")
                .translation("config.jdte.jdte.timeAccelerator.timeAcceleratorMaxScannedBlocksPerTick")
                .defineInRange("timeAcceleratorMaxScannedBlocksPerTick", 16384, 256, 1000000);
        timeAcceleratorMaxPendingTicks = builder
                .comment("Maximum paid virtual ticks retained per Time Accelerator target")
                .translation("config.jdte.jdte.timeAccelerator.timeAcceleratorMaxPendingTicks")
                .defineInRange("timeAcceleratorMaxPendingTicks", 1000000L, 1024L, 100000000L);
        timeAcceleratorExecutionBatchSize = builder
                .comment("Maximum virtual ticks processed for one target before rotating to the next target")
                .translation("config.jdte.jdte.timeAccelerator.timeAcceleratorExecutionBatchSize")
                .defineInRange("timeAcceleratorExecutionBatchSize", 64, 1, 4096);
        timeAcceleratorRandomRefreshInterval = builder
                .comment("Ticks between random-ticking block target cache refreshes")
                .translation("config.jdte.jdte.timeAccelerator.timeAcceleratorRandomRefreshInterval")
                .defineInRange("timeAcceleratorRandomRefreshInterval", 20, 1, 1200);
        timeAcceleratorAE2Enabled = builder
                .comment("Allow Time Accelerators with an AE Acceleration Upgrade to invoke AE2 IGridTickable services")
                .translation("config.jdte.jdte.timeAccelerator.timeAcceleratorAE2Enabled")
                .define("timeAcceleratorAE2Enabled", true);
        ultimateTimeWandFluidCapacity = builder
                .comment("Ultimate Time Wand Time Fluid capacity (mB)")
                .defineInRange("ultimateTimeWandFluidCapacity", 800000, 1000, 100000000);
        ultimateTimeWandEnergyCapacity = builder
                .comment("Ultimate Time Wand FE capacity")
                .defineInRange("ultimateTimeWandEnergyCapacity", 10000000, 10000, 1000000000);
        ultimateTimeWandDuration = builder
                .comment("Ultimate Time Wand effect duration (ticks)")
                .defineInRange("ultimateTimeWandDuration", 600, 1, 1000000);
        ultimateTimeWandMaxExponent = builder
                .comment("Ultimate Time Wand maximum time exponent")
                .defineInRange("ultimateTimeWandMaxExponent", 10, 0, 30);
        ultimateTimeWandNormalStep = builder.defineInRange("ultimateTimeWandNormalStep", 1, 0, 30);
        ultimateTimeWandX2Step = builder.defineInRange("ultimateTimeWandX2Step", 2, 0, 30);
        ultimateTimeWandX4Step = builder.defineInRange("ultimateTimeWandX4Step", 4, 0, 30);
        ultimateTimeWandMaxStep = builder.defineInRange("ultimateTimeWandMaxStep", 10, 0, 30);
        ultimateTimeWandBaseCostMultiplier = builder
                .comment("Ultimate Time Wand base multiplier cost multiplier")
                .defineInRange("ultimateTimeWandBaseCostMultiplier", 1.0D, 0.0D, 1000000.0D);
        ultimateTimeWandEnergyCostMultiplier = builder
                .comment("Ultimate Time Wand FE base cost multiplier")
                .defineInRange("ultimateTimeWandEnergyCostMultiplier", 1.0D, 0.0D, 1000000.0D);
        ultimateTimeWandFractionalFluidSettlement = builder
                .comment("Keep fractional Ultimate Time Wand fluid cost between settlements")
                .define("ultimateTimeWandFractionalFluidSettlement", true);
        builder.pop();
    }
}
