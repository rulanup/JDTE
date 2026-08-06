package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CrystalIncubatorConfig {
    public final ForgeConfigSpec.IntValue crystalIncubatorFluidCapacity;
    public final ForgeConfigSpec.IntValue crystalIncubatorEnergyCapacity;
    public final ForgeConfigSpec.DoubleValue crystalIncubatorEnergyCostMultiplier;
    public final ForgeConfigSpec.IntValue crystalIncubatorMaxMultiplier;
    public final ForgeConfigSpec.IntValue crystalIncubatorOverclockMultiplier;
    public final ForgeConfigSpec.DoubleValue crystalIncubatorFluidCostMultiplier;
    public final ForgeConfigSpec.DoubleValue crystalIncubatorRegularGrowthAcceleratorsAt8x;
    public final ForgeConfigSpec.IntValue crystalIncubatorScanBatchSize;
    public final ForgeConfigSpec.IntValue crystalIncubatorCacheRefreshInterval;
    public final ForgeConfigSpec.IntValue crystalIncubatorMotherBatchSize;
    public final ForgeConfigSpec.IntValue crystalIncubatorGrowthOperationsPerTick;
    public final ForgeConfigSpec.IntValue crystalIncubatorHarvestOperationsPerTick;
    public final ForgeConfigSpec.IntValue crystalIncubatorDynaGrowthAttempts;

    public CrystalIncubatorConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Crystal Incubator Settings")
                .translation("config.jdte.jdte.crystalIncubator")
                .push("crystalIncubator");
        crystalIncubatorFluidCapacity = builder
                .translation("config.jdte.jdte.crystalIncubator.fluidCapacity")
                .defineInRange("fluidCapacity", 8000, 100, 1000000);
        crystalIncubatorEnergyCapacity = builder
                .translation("config.jdte.jdte.crystalIncubator.energyCapacity")
                .defineInRange("energyCapacity", 10000000, 1000, Integer.MAX_VALUE);
        crystalIncubatorEnergyCostMultiplier = builder
                .comment("Multiplier applied to JDT Time Wand-equivalent FE usage")
                .translation("config.jdte.jdte.crystalIncubator.energyCostMultiplier")
                .defineInRange("energyCostMultiplier", 1.0D, 0.0D, 1000.0D);
        crystalIncubatorMaxMultiplier = builder
                .translation("config.jdte.jdte.crystalIncubator.maxMultiplier")
                .defineInRange("maxMultiplier", 512, 1, 65536);
        crystalIncubatorOverclockMultiplier = builder
                .translation("config.jdte.jdte.crystalIncubator.overclockMultiplier")
                .defineInRange("overclockMultiplier", 1024, 1, 65536);
        crystalIncubatorFluidCostMultiplier = builder
                .comment("Multiplier applied to JDT Time Wand-equivalent fluid usage")
                .translation("config.jdte.jdte.crystalIncubator.fluidCostMultiplier")
                .defineInRange("fluidCostMultiplier", 1.0D, 0.0D, 1000.0D);
        crystalIncubatorRegularGrowthAcceleratorsAt8x = builder
                .comment("Equivalent AE2 Growth Accelerators used for ordinary budding blocks at 8x; each calls randomTick once every 10 ticks")
                .translation("config.jdte.jdte.crystalIncubator.regularGrowthAcceleratorsAt8x")
                .defineInRange("regularGrowthAcceleratorsAt8x", 6.0D, 0.01D, 1024.0D);
        crystalIncubatorScanBatchSize = builder
                .translation("config.jdte.jdte.crystalIncubator.scanBatchSize")
                .defineInRange("scanBatchSize", 4096, 16, 1048576);
        crystalIncubatorCacheRefreshInterval = builder
                .translation("config.jdte.jdte.crystalIncubator.cacheRefreshInterval")
                .defineInRange("cacheRefreshInterval", 200, 20, 72000);
        crystalIncubatorMotherBatchSize = builder
                .translation("config.jdte.jdte.crystalIncubator.motherBatchSize")
                .defineInRange("motherBatchSize", 64, 1, 4096);
        crystalIncubatorGrowthOperationsPerTick = builder
                .translation("config.jdte.jdte.crystalIncubator.growthOperationsPerTick")
                .defineInRange("growthOperationsPerTick", 256, 1, 65536);
        crystalIncubatorHarvestOperationsPerTick = builder
                .translation("config.jdte.jdte.crystalIncubator.harvestOperationsPerTick")
                .defineInRange("harvestOperationsPerTick", 64, 1, 4096);
        crystalIncubatorDynaGrowthAttempts = builder
                .translation("config.jdte.jdte.crystalIncubator.dynaGrowthAttempts")
                .defineInRange("dynaGrowthAttempts", 128, 1, 4096);
        builder.pop();
    }
}
