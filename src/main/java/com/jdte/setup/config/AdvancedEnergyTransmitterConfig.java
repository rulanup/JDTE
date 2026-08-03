package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class AdvancedEnergyTransmitterConfig {
    public final ModConfigSpec.IntValue energyCapacity;
    public final ModConfigSpec.IntValue baseTickDelay;
    public final ModConfigSpec.IntValue targetRefreshInterval;
    public final ModConfigSpec.IntValue scanBlocksPerTick;
    public final ModConfigSpec.IntValue maxTargetsPerTick;
    public final ModConfigSpec.IntValue maxTransferPerTarget;
    public final ModConfigSpec.IntValue transferBudgetPerTick;
    public final ModConfigSpec.IntValue overclockTransferMultiplier;
    public final ModConfigSpec.IntValue meExtractionLimitPerTick;
    public final ModConfigSpec.BooleanValue excludeTransmitters;
    public final ModConfigSpec.BooleanValue showParticlesByDefault;
    public final ModConfigSpec.IntValue maxParticleTargetsPerTick;
    public final ModConfigSpec.IntValue playerChargeMaxItemsPerTick;
    public final ModConfigSpec.IntValue playerChargeMaxCallsPerItem;

    public AdvancedEnergyTransmitterConfig(ModConfigSpec.Builder builder) {
        builder.comment("Advanced Energy Transmitter Settings")
                .translation("config.jdte.jdte.advancedEnergyTransmitter")
                .push("advancedEnergyTransmitter");

        energyCapacity = builder
                .comment("Internal energy buffer size in FE")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.energyCapacity")
                .defineInRange("energyCapacity", 2_000_000_000, 1000, Integer.MAX_VALUE);

        baseTickDelay = builder
                .comment("Base tick interval between transmit operations")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.baseTickDelay")
                .defineInRange("baseTickDelay", 1, 1, 100);

        targetRefreshInterval = builder
                .comment("Ticks between refreshing the target block cache")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.targetRefreshInterval")
                .defineInRange("targetRefreshInterval", 20, 1, 1200);

        scanBlocksPerTick = builder
                .comment("Maximum block positions inspected per server tick while rebuilding the target cache")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.scanBlocksPerTick")
                .defineInRange("scanBlocksPerTick", 512, 1, 100000);

        maxTargetsPerTick = builder
                .comment("Maximum cached targets attempted per transmit operation, including full or invalid targets")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.maxTargetsPerTick")
                .defineInRange("maxTargetsPerTick", 512, 1, 100000);

        maxTransferPerTarget = builder
                .comment("Maximum FE sent to one target per operation; 0 removes the per-target cap")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.maxTransferPerTarget")
                .defineInRange("maxTransferPerTarget", 0, 0, Integer.MAX_VALUE);

        transferBudgetPerTick = builder
                .comment("Maximum total FE delivered per operation before overclock scaling")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.transferBudgetPerTick")
                .defineInRange("transferBudgetPerTick", 268_435_456, 1, Integer.MAX_VALUE);

        overclockTransferMultiplier = builder
                .comment("Transfer and ME extraction budget multiplier while overclocked")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.overclockTransferMultiplier")
                .defineInRange("overclockTransferMultiplier", 8, 1, 64);

        meExtractionLimitPerTick = builder
                .comment("Maximum FE pulled from Applied Flux ME storage per operation before overclock scaling; 0 disables ME extraction")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.meExtractionLimitPerTick")
                .defineInRange("meExtractionLimitPerTick", 268_435_456, 0, Integer.MAX_VALUE);

        excludeTransmitters = builder
                .comment("Exclude other Advanced Energy Transmitters from targets to prevent energy loops")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.excludeTransmitters")
                .define("excludeTransmitters", true);

        showParticlesByDefault = builder
                .comment("Whether newly placed transmitters show particles by default")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.showParticlesByDefault")
                .define("showParticlesByDefault", false);

        maxParticleTargetsPerTick = builder
                .comment("Maximum successful targets that emit one transfer particle per operation")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.maxParticleTargetsPerTick")
                .defineInRange("maxParticleTargetsPerTick", 8, 0, 128);

        playerChargeMaxItemsPerTick = builder
                .comment("Maximum equipped player items inspected per operation; hard-capped at 64 and separate from block target budgets")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.playerChargeMaxItemsPerTick")
                .defineInRange("playerChargeMaxItemsPerTick", 64, 1, 64);

        playerChargeMaxCallsPerItem = builder
                .comment("Maximum receiveEnergy calls per equipped item per operation for items with per-call limits; hard-capped at 16")
                .translation("config.jdte.jdte.advancedEnergyTransmitter.playerChargeMaxCallsPerItem")
                .defineInRange("playerChargeMaxCallsPerItem", 16, 1, 16);

        builder.pop();
    }
}
