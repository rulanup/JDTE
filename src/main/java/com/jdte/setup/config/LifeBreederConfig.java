package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class LifeBreederConfig {
    public final ForgeConfigSpec.IntValue lifeBreederEnergyCapacity;
    public final ForgeConfigSpec.IntValue lifeBreederFluidCapacity;
    public final ForgeConfigSpec.IntValue lifeBreederBreedEnergyCost;
    public final ForgeConfigSpec.IntValue lifeBreederBreedFluidCost;
    public final ForgeConfigSpec.IntValue lifeBreederEnergyPerGrowthTick;
    public final ForgeConfigSpec.IntValue lifeBreederGrowthTicksPerMb;
    public final ForgeConfigSpec.IntValue lifeBreederFluidCostMultiplierV3;
    public final ForgeConfigSpec.IntValue lifeBreederBreedingCooldownTicks;
    public final ForgeConfigSpec.IntValue lifeBreederProcessingInterval;
    public final ForgeConfigSpec.IntValue lifeBreederMaxEntitiesInspected;
    public final ForgeConfigSpec.IntValue lifeBreederMaxPairsPerCycle;
    public final ForgeConfigSpec.IntValue lifeBreederMaxAnimalsGrownPerCycle;
    public final ForgeConfigSpec.IntValue lifeBreederMaxAnimalsPerType;
    public final ForgeConfigSpec.IntValue lifeBreederMaxDropsCollectedPerCycle;
    public final ForgeConfigSpec.IntValue lifeBreederDefaultSpeedMultiplier;
    public final ForgeConfigSpec.IntValue lifeBreederMaxSpeedMultiplier;

    public LifeBreederConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Life Breeder Settings")
                .translation("config.jdte.jdte.lifeBreeder")
                .push("lifeBreeder");
        lifeBreederEnergyCapacity = builder.translation("config.jdte.jdte.lifeBreeder.energyCapacity")
                .defineInRange("energyCapacity", 10_000_000, 1000, Integer.MAX_VALUE);
        lifeBreederFluidCapacity = builder.translation("config.jdte.jdte.lifeBreeder.fluidCapacity")
                .defineInRange("fluidCapacity", 64_000, 1000, Integer.MAX_VALUE);
        lifeBreederBreedEnergyCost = builder.translation("config.jdte.jdte.lifeBreeder.breedEnergyCost")
                .defineInRange("breedEnergyCost", 1000, 0, Integer.MAX_VALUE);
        lifeBreederBreedFluidCost = builder.translation("config.jdte.jdte.lifeBreeder.breedFluidCost")
                .defineInRange("breedFluidCost", 100, 0, Integer.MAX_VALUE);
        lifeBreederEnergyPerGrowthTick = builder
                .comment("FE consumed per biological age or breeding-cooldown tick skipped")
                .translation("config.jdte.jdte.lifeBreeder.energyPerGrowthTick")
                .defineInRange("energyPerGrowthTick", 1, 0, 1000000);
        lifeBreederGrowthTicksPerMb = builder
                .comment("Biological age or cooldown ticks skipped per mB of Life Fluid")
                .translation("config.jdte.jdte.lifeBreeder.growthTicksPerMb")
                .defineInRange("growthTicksPerMb", 20, 1, 1000000);
        lifeBreederFluidCostMultiplierV3 = builder
                .comment("Multiplier applied after converting biological time into Life Fluid")
                .translation("config.jdte.jdte.lifeBreeder.fluidCostMultiplier")
                .defineInRange("fluidCostMultiplierV3", 10, 1, 1000000);
        lifeBreederBreedingCooldownTicks = builder
                .comment("Biological time used to price one completed breeding operation; vanilla animals and villagers use 6000 ticks")
                .translation("config.jdte.jdte.lifeBreeder.breedingCooldownTicks")
                .defineInRange("breedingCooldownTicks", 6000, 1, 72000);
        lifeBreederProcessingInterval = builder
                .comment("Ticks between bounded area processing cycles")
                .translation("config.jdte.jdte.lifeBreeder.processingInterval")
                .defineInRange("processingInterval", 20, 1, 1200);
        lifeBreederMaxEntitiesInspected = builder.translation("config.jdte.jdte.lifeBreeder.maxEntitiesInspected")
                .defineInRange("maxEntitiesInspected", 256, 1, 4096);
        lifeBreederMaxPairsPerCycle = builder.translation("config.jdte.jdte.lifeBreeder.maxPairsPerCycle")
                .defineInRange("maxPairsPerCycle", 16, 1, 1024);
        lifeBreederMaxAnimalsGrownPerCycle = builder.translation("config.jdte.jdte.lifeBreeder.maxAnimalsGrownPerCycle")
                .defineInRange("maxAnimalsGrownPerCycle", 128, 1, 4096);
        lifeBreederMaxAnimalsPerType = builder
                .comment("Pause breeding a type at this population; set to 0 to disable the density guard")
                .translation("config.jdte.jdte.lifeBreeder.maxAnimalsPerType")
                .defineInRange("maxAnimalsPerType", 64, 0, 4096);
        lifeBreederMaxDropsCollectedPerCycle = builder.translation("config.jdte.jdte.lifeBreeder.maxDropsCollectedPerCycle")
                .defineInRange("maxDropsCollectedPerCycle", 128, 0, 4096);
        lifeBreederDefaultSpeedMultiplier = builder.translation("config.jdte.jdte.lifeBreeder.defaultSpeedMultiplier")
                .defineInRange("defaultSpeedMultiplier", 1, 1, 32);
        lifeBreederMaxSpeedMultiplier = builder.translation("config.jdte.jdte.lifeBreeder.maxSpeedMultiplier")
                .defineInRange("maxSpeedMultiplier", 32, 1, 256);
        builder.pop();
    }
}
