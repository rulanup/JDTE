package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class LifeSynthesisVatConfig {
    public final ForgeConfigSpec.IntValue energyCapacity;
    public final ForgeConfigSpec.IntValue fluidCapacity;
    public final ForgeConfigSpec.IntValue baseWorkRate;
    public final ForgeConfigSpec.IntValue defaultSpeedMultiplier;
    public final ForgeConfigSpec.IntValue maxSpeedMultiplier;
    public final ForgeConfigSpec.IntValue overclockMaxSpeedMultiplier;
    public final ForgeConfigSpec.IntValue settlementInterval;
    public final ForgeConfigSpec.IntValue timeFluidPerBatch;
    public final ForgeConfigSpec.IntValue pendingLifeFluidCap;
    public final ForgeConfigSpec.IntValue maxBatchesPerSettlement;

    public LifeSynthesisVatConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Life Synthesis Vat Settings")
                .translation("config.jdte.jdte.lifeSynthesisVat")
                .push("lifeSynthesisVat");
        energyCapacity = builder
                .translation("config.jdte.jdte.lifeSynthesisVat.energyCapacity")
                .defineInRange("energyCapacity", 20_000_000, 1000, Integer.MAX_VALUE);
        fluidCapacity = builder
                .translation("config.jdte.jdte.lifeSynthesisVat.fluidCapacity")
                .defineInRange("fluidCapacity", 64_000, 1000, Integer.MAX_VALUE);
        baseWorkRate = builder
                .comment("Culture work accumulated per elapsed tick per speed multiplier")
                .translation("config.jdte.jdte.lifeSynthesisVat.baseWorkRate")
                .defineInRange("baseWorkRate", 1, 1, 4096);
        defaultSpeedMultiplier = builder
                .translation("config.jdte.jdte.lifeSynthesisVat.defaultSpeedMultiplier")
                .defineInRange("defaultSpeedMultiplier", 1, 1, 64);
        maxSpeedMultiplier = builder
                .translation("config.jdte.jdte.lifeSynthesisVat.maxSpeedMultiplier")
                .defineInRange("maxSpeedMultiplier", 32, 1, 64);
        overclockMaxSpeedMultiplier = builder
                .translation("config.jdte.jdte.lifeSynthesisVat.overclockMaxSpeedMultiplier")
                .defineInRange("overclockMaxSpeedMultiplier", 64, 1, 256);
        settlementInterval = builder
                .comment("Ticks between batched production settlements")
                .translation("config.jdte.jdte.lifeSynthesisVat.settlementInterval")
                .defineInRange("settlementInterval", 20, 1, 1200);
        timeFluidPerBatch = builder
                .comment("Time Fluid drained per completed batch while boosted (0 disables the boost)")
                .translation("config.jdte.jdte.lifeSynthesisVat.timeFluidPerBatch")
                .defineInRange("timeFluidPerBatch", 100, 0, 1_000_000);
        pendingLifeFluidCap = builder
                .comment("Maximum pending (undistilled) Life Fluid backlog in millibuckets")
                .translation("config.jdte.jdte.lifeSynthesisVat.pendingLifeFluidCap")
                .defineInRange("pendingLifeFluidCap", 262_144, 0, Integer.MAX_VALUE);
        maxBatchesPerSettlement = builder
                .comment("Maximum completed batches processed per settlement")
                .translation("config.jdte.jdte.lifeSynthesisVat.maxBatchesPerSettlement")
                .defineInRange("maxBatchesPerSettlement", 1024, 1, 65536);
        builder.pop();
    }
}