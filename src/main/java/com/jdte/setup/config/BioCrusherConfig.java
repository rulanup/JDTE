package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BioCrusherConfig {
    public final ModConfigSpec.IntValue bioCrusherFluidCapacity;
    public final ModConfigSpec.IntValue bioCrusherEnergyCost;
    public final ModConfigSpec.DoubleValue bioCrusherBaseRadius;
    public final ModConfigSpec.DoubleValue bioCrusherExperienceFluidMultiplier;
    public final ModConfigSpec.IntValue bioCrusherOutputSlotsPerCapacityUpgradeMultiplier;
    public final ModConfigSpec.IntValue bioCrusherBaseDamage;
    public final ModConfigSpec.IntValue bioCrusherProcessTime;
    public final ModConfigSpec.BooleanValue bioCrusherRespectDamageRestrictions;
    public final ModConfigSpec.BooleanValue bioCrusherAllowDestroyChaosGuardianCrystals;
    public final ModConfigSpec.BooleanValue bioCrusherAllowInstantKillChaosGuardian;
    public final ModConfigSpec.DoubleValue lootingExtraDropChance;
    public final ModConfigSpec.IntValue advancedBioCrusherEnergyCapacity;
    public final ModConfigSpec.IntValue extendedBioCrusherEnergyCapacity;
    public final ModConfigSpec.IntValue advancedBioCrusherMaxEntities;
    public final ModConfigSpec.IntValue extendedBioCrusherMaxEntities;

    public BioCrusherConfig(ModConfigSpec.Builder builder) {
        builder.comment("Bio Crusher Settings").translation("config.jdte.jdte.bioCrusher").push("bioCrusher");
        bioCrusherFluidCapacity = builder
                .comment("Base fluid capacity for bio crusher (mB)")
                .translation("config.jdte.jdte.bioCrusher.bioCrusherFluidCapacity")
                .defineInRange("bioCrusherFluidCapacity", 16000, 1000, 1000000);
        bioCrusherEnergyCost = builder
                .comment("Base energy cost per operation")
                .translation("config.jdte.jdte.bioCrusher.bioCrusherEnergyCost")
                .defineInRange("bioCrusherEnergyCost", 300, 10, 100000);
        bioCrusherBaseRadius = builder
                .comment("Base search radius for bio crusher")
                .translation("config.jdte.jdte.bioCrusher.bioCrusherBaseRadius")
                .defineInRange("bioCrusherBaseRadius", 2.5D, 1.0D, 20.0D);
        bioCrusherExperienceFluidMultiplier = builder
                .comment("Experience fluid produced per actual experience point dropped by the entity (mB)")
                .translation("config.jdte.jdte.bioCrusher.bioCrusherExperienceFluidMultiplier")
                .defineInRange("experienceFluidPerPoint", 1.0D, 0.0D, 10000.0D);
        bioCrusherOutputSlotsPerCapacityUpgradeMultiplier = builder
                .comment("Multiplier applied to the base output slots opened by each Capacity Upgrade (base is 9 slots per upgrade)")
                .translation("config.jdte.jdte.bioCrusher.bioCrusherOutputSlotsPerCapacityUpgradeMultiplier")
                .defineInRange("bioCrusherOutputSlotsPerCapacityUpgradeMultiplier", 2, 1, 10);
        bioCrusherBaseDamage = builder
                .comment("Base damage dealt by bio crusher")
                .translation("config.jdte.jdte.bioCrusher.bioCrusherBaseDamage")
                .defineInRange("bioCrusherBaseDamage", 5, 1, 1000);
        bioCrusherProcessTime = builder
                .comment("Base interval in ticks between bio crusher operations")
                .translation("config.jdte.jdte.bioCrusher.bioCrusherProcessTime")
                .defineInRange("bioCrusherProcessTime", 5, 1, 200);
        bioCrusherRespectDamageRestrictions = builder
                .comment("When enabled, the bio crusher will not force-kill entities that survive its FakePlayer attack. Disabled by default so protected bosses can still be processed.")
                .translation("config.jdte.jdte.bioCrusher.bioCrusherRespectDamageRestrictions")
                .define("respectDamageRestrictions", false);
        bioCrusherAllowDestroyChaosGuardianCrystals = builder
                .comment("Allow Bio Crushers to automatically destroy Draconic Evolution Chaos Guardian Crystals. Disabled by default.")
                .translation("config.jdte.jdte.bioCrusher.bioCrusherAllowDestroyChaosGuardianCrystals")
                .define("allowDestroyChaosGuardianCrystals", false);
        bioCrusherAllowInstantKillChaosGuardian = builder
                .comment("Allow Bio Crushers to instantly kill the Draconic Evolution Chaos Guardian with FakePlayer attribution. Disabled by default.")
                .translation("config.jdte.jdte.bioCrusher.bioCrusherAllowInstantKillChaosGuardian")
                .define("allowInstantKillChaosGuardian", false);
        lootingExtraDropChance = builder
                .comment("Looting extra drop chance per level (0.5 = 50%)")
                .translation("config.jdte.jdte.bioCrusher.lootingExtraDropChance")
                .defineInRange("lootingExtraDropChance", 0.5D, 0.01D, 1.0D);
        advancedBioCrusherEnergyCapacity = builder
                .comment("Advanced bio crusher energy capacity")
                .translation("config.jdte.jdte.bioCrusher.advancedBioCrusherEnergyCapacity")
                .defineInRange("advancedBioCrusherEnergyCapacity", 100000, 10000, 10000000);
        extendedBioCrusherEnergyCapacity = builder
                .comment("Extended bio crusher energy capacity")
                .translation("config.jdte.jdte.bioCrusher.extendedBioCrusherEnergyCapacity")
                .defineInRange("extendedBioCrusherEnergyCapacity", 200000, 10000, 10000000);
        advancedBioCrusherMaxEntities = builder
                .comment("Max entities processed per tick (advanced)")
                .translation("config.jdte.jdte.bioCrusher.advancedBioCrusherMaxEntities")
                .defineInRange("advancedBioCrusherMaxEntities", 2, 1, 100);
        extendedBioCrusherMaxEntities = builder
                .comment("Max entities processed per tick (extended)")
                .translation("config.jdte.jdte.bioCrusher.extendedBioCrusherMaxEntities")
                .defineInRange("extendedBioCrusherMaxEntities", 4, 1, 100);
        builder.pop();
    }
}
