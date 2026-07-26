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
    public final ModConfigSpec.DoubleValue timeAcceleratorTargetMspt;
    public final ModConfigSpec.LongValue timeAcceleratorMaxPendingTicks;
    public final ModConfigSpec.IntValue timeAcceleratorExecutionBatchSize;
    public final ModConfigSpec.IntValue timeAcceleratorRandomRefreshInterval;
    public final ModConfigSpec.BooleanValue timeAcceleratorAE2Enabled;

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
        timeAcceleratorTargetMspt = builder
                .comment("Target total server tick time used by managed Time Accelerator work")
                .translation("config.jdte.jdte.timeAccelerator.timeAcceleratorTargetMspt")
                .defineInRange("timeAcceleratorTargetMspt", 45.0D, 1.0D, 50.0D);
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
                .comment("Allow Time Accelerators to invoke AE2 IGridTickable services")
                .translation("config.jdte.jdte.timeAccelerator.timeAcceleratorAE2Enabled")
                .define("timeAcceleratorAE2Enabled", true);
        builder.pop();
    }
}
