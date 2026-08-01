package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class GreenhouseConfig {
    public final ModConfigSpec.IntValue greenhouseFluidCapacity;
    public final ModConfigSpec.IntValue greenhouseEnergyCapacity;
    public final ModConfigSpec.IntValue greenhouseBaseMultiplier;
    public final ModConfigSpec.IntValue greenhouseDefaultSpeedMultiplier;
    public final ModConfigSpec.IntValue greenhouseMaxSpeedMultiplier;
    public final ModConfigSpec.IntValue greenhouseOverclockMaxSpeedMultiplier;
    public final ModConfigSpec.IntValue greenhouseFluidCostDivisor;
    public final ModConfigSpec.IntValue greenhouseSettlementInterval;
    public final ModConfigSpec.IntValue greenhouseDefaultGrowthWork;
    public final ModConfigSpec.IntValue greenhouseEnergyPerHarvestV2;
    public final ModConfigSpec.IntValue greenhouseMysticalBaseFluidCost;
    public final ModConfigSpec.IntValue greenhouseGenericFluidCost;
    public final ModConfigSpec.IntValue greenhouseMaxHarvestsPerSettlementV2;
    public final ModConfigSpec.LongValue greenhouseMaxPendingWork;
    public final ModConfigSpec.IntValue greenhouseEventOutputItemBudget;
    public final ModConfigSpec.IntValue greenhouseEventOutputTypeBudget;
    public final ModConfigSpec.IntValue greenhouseDynamicHarvestCallsPerTick;

    public GreenhouseConfig(ModConfigSpec.Builder builder) {
        builder.comment("Greenhouse Settings")
                .translation("config.jdte.jdte.greenhouse")
                .push("greenhouse");
        greenhouseFluidCapacity = builder
                .translation("config.jdte.jdte.greenhouse.fluidCapacity")
                .defineInRange("fluidCapacity", 64000, 1000, Integer.MAX_VALUE);
        greenhouseEnergyCapacity = builder
                .translation("config.jdte.jdte.greenhouse.energyCapacity")
                .defineInRange("energyCapacity", 10000000, 1000, Integer.MAX_VALUE);
        greenhouseBaseMultiplier = builder
                .translation("config.jdte.jdte.greenhouse.baseMultiplier")
                .defineInRange("baseMultiplier", 512, 1, 65536);
        greenhouseDefaultSpeedMultiplier = builder
                .translation("config.jdte.jdte.greenhouse.defaultSpeedMultiplier")
                .defineInRange("defaultSpeedMultiplier", 1, 1, 64);
        greenhouseMaxSpeedMultiplier = builder
                .translation("config.jdte.jdte.greenhouse.maxSpeedMultiplier")
                .defineInRange("maxSpeedMultiplier", 32, 1, 64);
        greenhouseOverclockMaxSpeedMultiplier = builder
                .translation("config.jdte.jdte.greenhouse.overclockMaxSpeedMultiplier")
                .defineInRange("overclockMaxSpeedMultiplier", 64, 1, 256);
        greenhouseFluidCostDivisor = builder
                .translation("config.jdte.jdte.greenhouse.fluidCostDivisor")
                .defineInRange("fluidCostDivisor", 100, 1, 1000000);
        greenhouseSettlementInterval = builder
                .comment("Ticks between batched production settlements")
                .translation("config.jdte.jdte.greenhouse.settlementInterval")
                .defineInRange("settlementInterval", 20, 1, 1200);
        greenhouseDefaultGrowthWork = builder
                .translation("config.jdte.jdte.greenhouse.defaultGrowthWork")
                .defineInRange("defaultGrowthWork", 4096, 1, Integer.MAX_VALUE);
        greenhouseEnergyPerHarvestV2 = builder
                .translation("config.jdte.jdte.greenhouse.energyPerHarvest")
                .defineInRange("energyPerHarvestV2", 10, 0, Integer.MAX_VALUE);
        greenhouseMysticalBaseFluidCost = builder
                .comment("Mystical Agriculture Time Fluid cost is this value multiplied by crop tier squared")
                .translation("config.jdte.jdte.greenhouse.mysticalBaseFluidCost")
                .defineInRange("mysticalBaseFluidCost", 25, 1, 1000000);
        greenhouseGenericFluidCost = builder
                .translation("config.jdte.jdte.greenhouse.genericFluidCost")
                .defineInRange("genericFluidCost", 10, 1, 1000000);
        greenhouseMaxHarvestsPerSettlementV2 = builder
                .translation("config.jdte.jdte.greenhouse.maxHarvestsPerSettlement")
                .defineInRange("maxHarvestsPerSettlementV2", 4096, 1, 65536);
        greenhouseMaxPendingWork = builder
                .comment("Maximum stored work debt per greenhouse planting slot")
                .translation("config.jdte.jdte.greenhouse.maxPendingWork")
                .defineInRange("maxPendingWork", 67_108_864L, 1L, Long.MAX_VALUE);
        greenhouseEventOutputItemBudget = builder
                .comment("Maximum items one Greenhouse may push into one adjacent item handler per real server tick")
                .translation("config.jdte.jdte.greenhouse.eventOutputItemBudget")
                .defineInRange("eventOutputItemBudget", 65_536, 1, 1_048_576);
        greenhouseEventOutputTypeBudget = builder
                .comment("Maximum distinct item types one Greenhouse may offer to one adjacent item handler per real server tick")
                .translation("config.jdte.jdte.greenhouse.eventOutputTypeBudget")
                .defineInRange("eventOutputTypeBudget", 16, 1, 64);
        greenhouseDynamicHarvestCallsPerTick = builder
                .comment("Maximum exact dynamic harvest-provider calls shared by one Greenhouse per real server tick")
                .translation("config.jdte.jdte.greenhouse.dynamicHarvestCallsPerTick")
                .defineInRange("dynamicHarvestCallsPerTick", 128, 1, 4096);
        builder.pop();
    }
}
