package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class LifeBreederConfig {
    public final ModConfigSpec.IntValue lifeBreederEnergyCapacity;
    public final ModConfigSpec.IntValue lifeBreederFluidCapacity;
    public final ModConfigSpec.IntValue lifeBreederBreedEnergyCost;
    public final ModConfigSpec.IntValue lifeBreederBreedFluidCost;
    public final ModConfigSpec.IntValue lifeBreederEnergyPerGrowthTick;
    public final ModConfigSpec.IntValue lifeBreederGrowthTicksPerMb;
    public final ModConfigSpec.IntValue lifeBreederFluidCostMultiplierV3;
    public final ModConfigSpec.IntValue lifeBreederBreedingCooldownTicks;
    public final ModConfigSpec.IntValue lifeBreederProcessingInterval;
    public final ModConfigSpec.IntValue lifeBreederMaxEntitiesInspected;
    public final ModConfigSpec.IntValue lifeBreederMaxPairsPerCycle;
    public final ModConfigSpec.IntValue lifeBreederMaxAnimalsGrownPerCycle;
    public final ModConfigSpec.IntValue lifeBreederMaxAnimalsPerType;
    public final ModConfigSpec.IntValue lifeBreederMaxDropsCollectedPerCycle;
    public final ModConfigSpec.IntValue lifeBreederDefaultSpeedMultiplier;
    public final ModConfigSpec.IntValue lifeBreederMaxSpeedMultiplier;

    public LifeBreederConfig(ModConfigSpec.Builder builder) {
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
