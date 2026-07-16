package com.jdte.setup;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class JDTEConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    public static class Common {
        // Upgrade System
        public final ModConfigSpec.IntValue filterSlotsPerUpgrade;
        public final ModConfigSpec.DoubleValue underclockEnergyMultiplier;
        public final ModConfigSpec.IntValue overclockEnergyMultiplier;
        public final ModConfigSpec.IntValue underclockTickSpeed;
        public final ModConfigSpec.IntValue overclockTickSpeed;
        public final ModConfigSpec.DoubleValue baseAreaRadius;
        public final ModConfigSpec.IntValue baseAreaOffset;

        // Time Accelerator
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
        public final ModConfigSpec.IntValue timeAcceleratorBaseFluidCapacity;
        public final ModConfigSpec.DoubleValue timeAcceleratorFluidCostMultiplier;

        // Bio Crusher
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

        // Life Extractor
        public final ModConfigSpec.DoubleValue lifeExtractorFluidPerHealth;

        // Loot Fabricator
        public final ModConfigSpec.IntValue lootFabricatorLifeFluidCost;
        public final ModConfigSpec.IntValue lootFabricatorBaseTimeFluidCost;
        public final ModConfigSpec.IntValue lootFabricatorLootingFluidCostIncreasePercent;

        // Item/Fluid Sender/Receiver
        public final ModConfigSpec.IntValue senderStorageSlots;
        public final ModConfigSpec.IntValue advancedItemSenderEnergyCapacity;
        public final ModConfigSpec.IntValue advancedItemSenderEnergyCost;
        public final ModConfigSpec.IntValue extendedItemSenderEnergyCapacity;
        public final ModConfigSpec.IntValue advancedFluidSenderEnergyCapacity;
        public final ModConfigSpec.IntValue advancedFluidSenderEnergyCost;
        public final ModConfigSpec.IntValue extendedFluidSenderEnergyCapacity;
        public final ModConfigSpec.IntValue fluidSenderFluidCapacity;
        public final ModConfigSpec.BooleanValue fluidSenderUnlimitedTransfer;
        public final ModConfigSpec.IntValue autoIoItemTransferRate;
        public final ModConfigSpec.IntValue autoIoFluidTransferRate;
        public final ModConfigSpec.IntValue senderReceiverItemTransferRate;
        public final ModConfigSpec.IntValue senderReceiverOverclockItemTransferRate;
        public final ModConfigSpec.IntValue senderReceiverFluidTransferRate;
        public final ModConfigSpec.IntValue senderReceiverOverclockFluidTransferRate;
        public final ModConfigSpec.IntValue transferFailureBackoffStart;
        public final ModConfigSpec.IntValue transferFailureBackoffMax;

        // Advanced Item Collector
        public final ModConfigSpec.BooleanValue advancedItemCollectorPreDrainEnabled;
        public final ModConfigSpec.IntValue advancedItemCollectorPreDrainThreshold;
        public final ModConfigSpec.BooleanValue advancedItemCollectorMeDirectTransferEnabled;
        public final ModConfigSpec.BooleanValue advancedItemCollectorExistingItemScanEnabled;
        public final ModConfigSpec.IntValue advancedItemCollectorExistingItemScanInterval;
        public final ModConfigSpec.IntValue advancedItemCollectorExistingItemScanLimit;

        // Entity Suppressor
        public final ModConfigSpec.IntValue entitySuppressorEnergyCapacity;
        public final ModConfigSpec.IntValue entitySuppressorEnergyPerTick;
        public final ModConfigSpec.BooleanValue entitySuppressorProtectNamed;
        public final ModConfigSpec.BooleanValue entitySuppressorProtectTamed;
        public final ModConfigSpec.BooleanValue entitySuppressorProtectBosses;
        public final ModConfigSpec.BooleanValue entitySuppressorRemoveExisting;

        // Range Blocker
        public final ModConfigSpec.IntValue rangeBlockerEnergyCapacity;
        public final ModConfigSpec.IntValue rangeBlockerContainmentEnergyPerTick;
        public final ModConfigSpec.DoubleValue rangeBlockerDemagnetizationEnergyPerTick;
        public final ModConfigSpec.BooleanValue rangeBlockerProtectNamed;
        public final ModConfigSpec.BooleanValue rangeBlockerProtectTamed;
        public final ModConfigSpec.BooleanValue rangeBlockerProtectBosses;
        public final ModConfigSpec.BooleanValue rangeBlockerMekanismIntegration;
        public final ModConfigSpec.BooleanValue rangeBlockerContainProjectiles;
        public final ModConfigSpec.BooleanValue rangeBlockerContainOwnerlessProjectiles;
        public final ModConfigSpec.BooleanValue rangeBlockerContainProjectileExplosions;

        // Advanced Potion Brewer
        public final ModConfigSpec.BooleanValue potionBrewerRejectPatternProviderFuelInput;

        // Crystal Incubator
        public final ModConfigSpec.IntValue crystalIncubatorFluidCapacity;
        public final ModConfigSpec.IntValue crystalIncubatorEnergyCapacity;
        public final ModConfigSpec.DoubleValue crystalIncubatorEnergyCostMultiplier;
        public final ModConfigSpec.IntValue crystalIncubatorMaxMultiplier;
        public final ModConfigSpec.IntValue crystalIncubatorOverclockMultiplier;
        public final ModConfigSpec.DoubleValue crystalIncubatorFluidCostMultiplier;
        public final ModConfigSpec.DoubleValue crystalIncubatorRegularGrowthAcceleratorsAt8x;
        public final ModConfigSpec.IntValue crystalIncubatorScanBatchSize;
        public final ModConfigSpec.IntValue crystalIncubatorCacheRefreshInterval;
        public final ModConfigSpec.IntValue crystalIncubatorMotherBatchSize;
        public final ModConfigSpec.IntValue crystalIncubatorGrowthOperationsPerTick;
        public final ModConfigSpec.IntValue crystalIncubatorHarvestOperationsPerTick;
        public final ModConfigSpec.IntValue crystalIncubatorDynaGrowthAttempts;

        // Greenhouse
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

        // Bio Factory
        public final ModConfigSpec.IntValue bioFactoryFluidCapacity;
        public final ModConfigSpec.IntValue bioFactoryEnergyCapacity;
        public final ModConfigSpec.IntValue bioFactoryEnergyPerCycle;
        public final ModConfigSpec.IntValue bioFactoryBaseProcessTicks;
        public final ModConfigSpec.IntValue bioFactorySettlementInterval;
        public final ModConfigSpec.IntValue bioFactoryTimeFluidPerCycle;
        public final ModConfigSpec.IntValue bioFactoryMaxSpeedMultiplier;
        public final ModConfigSpec.IntValue bioFactoryDefaultSpeedMultiplier;
        public final ModConfigSpec.IntValue bioFactoryOverclockMaxSpeedMultiplier;
        public final ModConfigSpec.IntValue bioFactoryLifeFluidPerCycle;
        public final ModConfigSpec.DoubleValue bioFactoryLifeYieldMultiplier;
        public final ModConfigSpec.IntValue bioFactoryProcessFluidPerCycle;
        public final ModConfigSpec.IntValue bioFactoryExternalTimeFluidCostMultiplier;
        public final ModConfigSpec.IntValue bioFactoryExternalLifeFluidCostMultiplier;

        // Gel Generator
        public final ModConfigSpec.IntValue gelGeneratorInputSlots;
        public final ModConfigSpec.IntValue gelGeneratorOutputSlots;
        public final ModConfigSpec.IntValue gelGeneratorFluidCapacity;
        public final ModConfigSpec.IntValue gelGeneratorEnergyCapacity;
        public final ModConfigSpec.IntValue gelGeneratorFluidConversionAmount;
        public final ModConfigSpec.IntValue gelGeneratorFuelUsesPerItem;
        public final ModConfigSpec.IntValue gelGeneratorEnergyCost;

        // Generator Upgrade
        public final ModConfigSpec.IntValue generatorUpgradeEnergyMultiplier;
        public final ModConfigSpec.IntValue generatorUpgradeFluidCost;

        // Upgrade Items
        public final ModConfigSpec.IntValue maxSharpnessUpgrades;
        public final ModConfigSpec.IntValue sharpnessDamagePerUpgrade;
        public final ModConfigSpec.IntValue maxLootingUpgrades;

        public Common(ModConfigSpec.Builder builder) {
            builder.comment("JDT Extras Settings").translation("config.jdte.jdte").push("jdte");

            // Upgrade System
            builder.comment("Upgrade System Settings").translation("config.jdte.jdte.upgrades").push("upgrades");
            filterSlotsPerUpgrade = builder
                    .comment("Number of extra filter slots per filter upgrade card")
                    .translation("config.jdte.jdte.upgrades.filterSlotsPerUpgrade")
                    .defineInRange("filterSlotsPerUpgrade", 9, 1, 27);
            underclockEnergyMultiplier = builder
                    .comment("Underclock energy cost multiplier (0.2 = 20% of original)")
                    .translation("config.jdte.jdte.upgrades.underclockEnergyMultiplier")
                    .defineInRange("underclockEnergyMultiplier", 0.2D, 0.01D, 1.0D);
            overclockEnergyMultiplier = builder
                    .comment("Overclock energy cost multiplier (3 = 3x original)")
                    .translation("config.jdte.jdte.upgrades.overclockEnergyMultiplier")
                    .defineInRange("overclockEnergyMultiplier", 3, 1, 10);
            underclockTickSpeed = builder
                    .comment("Underclock tick speed (locks machine to this tick interval)")
                    .translation("config.jdte.jdte.upgrades.underclockTickSpeed")
                    .defineInRange("underclockTickSpeed", 40, 1, 100);
            overclockTickSpeed = builder
                    .comment("Overclock tick speed (locks machine to this tick interval)")
                    .translation("config.jdte.jdte.upgrades.overclockTickSpeed")
                    .defineInRange("overclockTickSpeed", 1, 1, 10);
            baseAreaRadius = builder
                    .comment("Base area radius for range upgrade")
                    .translation("config.jdte.jdte.upgrades.baseAreaRadius")
                    .defineInRange("baseAreaRadius", 5.0D, 1.0D, 50.0D);
            baseAreaOffset = builder
                    .comment("Base area offset for range upgrade")
                    .translation("config.jdte.jdte.upgrades.baseAreaOffset")
                    .defineInRange("baseAreaOffset", 9, 0, 50);
            builder.pop();

            // Time Accelerator
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

            // Bio Crusher
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

            // Life Extractor
            builder.comment("Life Extractor Settings").translation("config.jdte.jdte.lifeExtractor").push("lifeExtractor");
            lifeExtractorFluidPerHealth = builder
                    .comment("Life Fluid produced per point of the entity's current health (mB)")
                    .translation("config.jdte.jdte.lifeExtractor.fluidPerHealth")
                    .defineInRange("fluidPerHealth", 0.1D, 0.001D, 100000.0D);
            builder.pop();

            // Loot Fabricator
            builder.comment("Loot Fabricator Settings").translation("config.jdte.jdte.lootFabricator").push("lootFabricator");
            lootFabricatorLifeFluidCost = builder
                    .comment("Life Fluid consumed per successful loot fabrication operation (mB)")
                    .translation("config.jdte.jdte.lootFabricator.lifeFluidCost")
                    .defineInRange("lifeFluidCost", 100, 1, 1_000_000);
            lootFabricatorBaseTimeFluidCost = builder
                    .comment("Base Time Fluid consumed per successful loot fabrication operation (mB). Faster machine speeds multiply this integer cost.")
                    .translation("config.jdte.jdte.lootFabricator.baseTimeFluidCost")
                    .defineInRange("baseTimeFluidCost", 1, 1, 1_000_000);
            lootFabricatorLootingFluidCostIncreasePercent = builder
                    .comment("Additional Life Fluid and Time Fluid cost per Looting Upgrade installed in a Loot Fabricator, in percent.")
                    .translation("config.jdte.jdte.lootFabricator.lootingFluidCostIncreasePercent")
                    .defineInRange("lootingFluidCostIncreasePercent", 50, 0, 10_000);
            builder.pop();

            // Sender/Receiver
            builder.comment("Sender/Receiver Settings").translation("config.jdte.jdte.senderReceiver").push("senderReceiver");
            senderStorageSlots = builder
                    .comment("Internal storage slots for item sender/receiver")
                    .translation("config.jdte.jdte.senderReceiver.senderStorageSlots")
                    .defineInRange("senderStorageSlots", 9, 1, 54);
            advancedItemSenderEnergyCapacity = builder
                    .comment("Advanced item sender energy capacity")
                    .translation("config.jdte.jdte.senderReceiver.advancedItemSenderEnergyCapacity")
                    .defineInRange("advancedItemSenderEnergyCapacity", 50000, 1000, 10000000);
            advancedItemSenderEnergyCost = builder
                    .comment("Advanced item sender energy cost per cycle")
                    .translation("config.jdte.jdte.senderReceiver.advancedItemSenderEnergyCost")
                    .defineInRange("advancedItemSenderEnergyCost", 500, 10, 100000);
            extendedItemSenderEnergyCapacity = builder
                    .comment("Extended item sender energy capacity")
                    .translation("config.jdte.jdte.senderReceiver.extendedItemSenderEnergyCapacity")
                    .defineInRange("extendedItemSenderEnergyCapacity", 100000, 1000, 10000000);
            advancedFluidSenderEnergyCapacity = builder
                    .comment("Advanced fluid sender energy capacity")
                    .translation("config.jdte.jdte.senderReceiver.advancedFluidSenderEnergyCapacity")
                    .defineInRange("advancedFluidSenderEnergyCapacity", 50000, 1000, 10000000);
            advancedFluidSenderEnergyCost = builder
                    .comment("Advanced fluid sender energy cost per cycle")
                    .translation("config.jdte.jdte.senderReceiver.advancedFluidSenderEnergyCost")
                    .defineInRange("advancedFluidSenderEnergyCost", 500, 10, 100000);
            extendedFluidSenderEnergyCapacity = builder
                    .comment("Extended fluid sender energy capacity")
                    .translation("config.jdte.jdte.senderReceiver.extendedFluidSenderEnergyCapacity")
                    .defineInRange("extendedFluidSenderEnergyCapacity", 100000, 1000, 10000000);
            fluidSenderFluidCapacity = builder
                    .comment("Fluid sender/receiver base fluid capacity (mB)")
                    .translation("config.jdte.jdte.senderReceiver.fluidSenderFluidCapacity")
                    .defineInRange("fluidSenderFluidCapacity", 8000, 100, 1000000);
            fluidSenderUnlimitedTransfer = builder
                    .comment("When enabled, Fluid Senders move all available internal fluid per operation instead of using the configured fluid batch limits")
                    .translation("config.jdte.jdte.senderReceiver.fluidSenderUnlimitedTransfer")
                    .define("fluidSenderUnlimitedTransfer", true);
            autoIoItemTransferRate = builder
                    .comment("Maximum items transferred per side and auto I/O operation. Default matches Logistics Network's Netherite tier.")
                    .translation("config.jdte.jdte.senderReceiver.autoIoItemTransferRate")
                    .defineInRange("autoIoItemTransferRate", 10000, 1, 1000000);
            autoIoFluidTransferRate = builder
                    .comment("Maximum fluid transferred per side and auto I/O operation in mB. Default matches Logistics Network's Netherite tier.")
                    .translation("config.jdte.jdte.senderReceiver.autoIoFluidTransferRate")
                    .defineInRange("autoIoFluidTransferRate", 1000000, 1, Integer.MAX_VALUE);
            senderReceiverItemTransferRate = builder
                    .comment("Maximum items moved per sender/receiver operation without Overclock. Default matches Logistics Network's Diamond tier.")
                    .translation("config.jdte.jdte.senderReceiver.senderReceiverItemTransferRate")
                    .defineInRange("senderReceiverItemTransferRate", 64, 1, 1000000);
            senderReceiverOverclockItemTransferRate = builder
                    .comment("Maximum items moved per sender/receiver operation with Overclock or Creative. Default matches Logistics Network's Netherite tier.")
                    .translation("config.jdte.jdte.senderReceiver.senderReceiverOverclockItemTransferRate")
                    .defineInRange("senderReceiverOverclockItemTransferRate", 10000, 1, 1000000);
            senderReceiverFluidTransferRate = builder
                    .comment("Maximum fluid moved per sender/receiver operation without Overclock in mB. Default matches Logistics Network's Diamond tier.")
                    .translation("config.jdte.jdte.senderReceiver.senderReceiverFluidTransferRate")
                    .defineInRange("senderReceiverFluidTransferRate", 20000, 1, Integer.MAX_VALUE);
            senderReceiverOverclockFluidTransferRate = builder
                    .comment("Maximum fluid moved per sender/receiver operation with Overclock or Creative in mB. Default matches Logistics Network's Netherite tier.")
                    .translation("config.jdte.jdte.senderReceiver.senderReceiverOverclockFluidTransferRate")
                    .defineInRange("senderReceiverOverclockFluidTransferRate", 1000000, 1, Integer.MAX_VALUE);
            transferFailureBackoffStart = builder
                    .comment("Initial idle retry delay for auto I/O and sender/receiver transfers in ticks")
                    .translation("config.jdte.jdte.senderReceiver.transferFailureBackoffStart")
                    .defineInRange("transferFailureBackoffStart", 10, 1, 200);
            transferFailureBackoffMax = builder
                    .comment("Maximum idle retry delay for auto I/O and sender/receiver transfers in ticks")
                    .translation("config.jdte.jdte.senderReceiver.transferFailureBackoffMax")
                    .defineInRange("transferFailureBackoffMax", 40, 1, 1200);
            builder.pop();

            // Advanced Item Collector
            builder.comment("Advanced Item Collector Settings")
                    .translation("config.jdte.jdte.advancedItemCollector")
                    .push("advancedItemCollector");
            advancedItemCollectorPreDrainEnabled = builder
                    .comment("Directly transfer oversized container slots before player block breaking creates item entities")
                    .translation("config.jdte.jdte.advancedItemCollector.preDrainEnabled")
                    .define("preDrainEnabled", true);
            advancedItemCollectorPreDrainThreshold = builder
                    .comment("Minimum item count in one source slot that enables pre-break direct transfer")
                    .translation("config.jdte.jdte.advancedItemCollector.preDrainThreshold")
                    .defineInRange("preDrainThreshold", 10_000_000, 65, Integer.MAX_VALUE);
            advancedItemCollectorMeDirectTransferEnabled = builder
                    .comment("Use direct AE2 ME storage insertion when the attached interface item handler cannot accept a complete collected stack")
                    .translation("config.jdte.jdte.advancedItemCollector.meDirectTransferEnabled")
                    .define("meDirectTransferEnabled", true);
            advancedItemCollectorExistingItemScanEnabled = builder
                    .comment("Periodically collect item entities that already exist in configured collector areas")
                    .translation("config.jdte.jdte.advancedItemCollector.existingItemScanEnabled")
                    .define("existingItemScanEnabled", true);
            advancedItemCollectorExistingItemScanInterval = builder
                    .comment("Minimum ticks between existing-item scans for each collector")
                    .translation("config.jdte.jdte.advancedItemCollector.existingItemScanInterval")
                    .defineInRange("existingItemScanInterval", 10, 1, 1200);
            advancedItemCollectorExistingItemScanLimit = builder
                    .comment("Maximum existing item entities processed by one collector scan")
                    .translation("config.jdte.jdte.advancedItemCollector.existingItemScanLimit")
                    .defineInRange("existingItemScanLimit", 256, 1, 4096);
            builder.pop();

            builder.comment("Entity Suppressor Settings")
                    .translation("config.jdte.jdte.entitySuppressor")
                    .push("entitySuppressor");
            entitySuppressorEnergyCapacity = builder
                    .translation("config.jdte.jdte.entitySuppressor.energyCapacity")
                    .defineInRange("energyCapacity", 200000, 1000, 100000000);
            entitySuppressorEnergyPerTick = builder
                    .translation("config.jdte.jdte.entitySuppressor.energyPerTick")
                    .defineInRange("energyPerTick", 250, 0, 1000000);
            entitySuppressorProtectNamed = builder
                    .translation("config.jdte.jdte.entitySuppressor.protectNamed")
                    .define("protectNamed", true);
            entitySuppressorProtectTamed = builder
                    .translation("config.jdte.jdte.entitySuppressor.protectTamed")
                    .define("protectTamed", true);
            entitySuppressorProtectBosses = builder
                    .translation("config.jdte.jdte.entitySuppressor.protectBosses")
                    .define("protectBosses", true);
            entitySuppressorRemoveExisting = builder
                    .comment("Periodically remove matching existing entities while Block Entities mode is active")
                    .translation("config.jdte.jdte.entitySuppressor.removeExisting")
                    .define("removeExistingEntities", false);
            builder.pop();

            builder.comment("Range Blocker Settings")
                    .translation("config.jdte.jdte.rangeBlocker")
                    .push("rangeBlocker");
            rangeBlockerEnergyCapacity = builder
                    .translation("config.jdte.jdte.rangeBlocker.energyCapacity")
                    .defineInRange("energyCapacity", 200000, 1000, 100000000);
            rangeBlockerContainmentEnergyPerTick = builder
                    .translation("config.jdte.jdte.rangeBlocker.containmentEnergyPerTick")
                    .defineInRange("containmentEnergyPerTick", 250, 0, 1000000);
            rangeBlockerDemagnetizationEnergyPerTick = builder
                    .comment("Average FE consumed per active Demagnetization tick; fractional values are accumulated")
                    .translation("config.jdte.jdte.rangeBlocker.demagnetizationEnergyPerTick")
                    .defineInRange("demagnetizationEnergyPerTick", 0.25D, 0.0D, 1000000.0D);
            rangeBlockerProtectNamed = builder
                    .translation("config.jdte.jdte.rangeBlocker.protectNamed")
                    .define("protectNamed", true);
            rangeBlockerProtectTamed = builder
                    .translation("config.jdte.jdte.rangeBlocker.protectTamed")
                    .define("protectTamed", true);
            rangeBlockerProtectBosses = builder
                    .translation("config.jdte.jdte.rangeBlocker.protectBosses")
                    .define("protectBosses", true);
            rangeBlockerMekanismIntegration = builder
                    .comment("Make Mekanism's MekaSuit magnetic attraction respect demagnetization fields")
                    .translation("config.jdte.jdte.rangeBlocker.mekanismIntegration")
                    .define("mekanismIntegration", true);
            rangeBlockerContainProjectiles = builder
                    .comment("Destroy non-player projectiles before they cross a Containment field boundary")
                    .translation("config.jdte.jdte.rangeBlocker.containProjectiles")
                    .define("containProjectiles", true);
            rangeBlockerContainOwnerlessProjectiles = builder
                    .comment("Contain projectiles with no owner, including modded projectiles that do not expose one")
                    .translation("config.jdte.jdte.rangeBlocker.containOwnerlessProjectiles")
                    .define("containOwnerlessProjectiles", true);
            rangeBlockerContainProjectileExplosions = builder
                    .comment("Prevent explosions from contained projectiles from affecting blocks and entities outside the field")
                    .translation("config.jdte.jdte.rangeBlocker.containProjectileExplosions")
                    .define("containProjectileExplosions", true);
            builder.pop();

            builder.comment("Advanced Potion Brewer Settings")
                    .translation("config.jdte.jdte.advancedPotionBrewer")
                    .push("advancedPotionBrewer");
            potionBrewerRejectPatternProviderFuelInput = builder
                    .comment("Reject Blaze Powder insertion into the fuel slot from adjacent AE2 crafting providers")
                    .translation("config.jdte.jdte.advancedPotionBrewer.rejectPatternProviderFuelInput")
                    .define("rejectPatternProviderFuelInput", true);
            builder.pop();

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
            builder.pop();

            builder.comment("Bio Factory Settings")
                    .translation("config.jdte.jdte.bioFactory")
                    .push("bioFactory");
            bioFactoryFluidCapacity = builder.translation("config.jdte.jdte.bioFactory.fluidCapacity")
                    .defineInRange("fluidCapacity", 64000, 1000, Integer.MAX_VALUE);
            bioFactoryEnergyCapacity = builder.translation("config.jdte.jdte.bioFactory.energyCapacity")
                    .defineInRange("energyCapacity", 10000000, 1000, Integer.MAX_VALUE);
            bioFactoryEnergyPerCycle = builder.translation("config.jdte.jdte.bioFactory.energyPerCycle")
                    .defineInRange("energyPerCycle", 1000, 0, Integer.MAX_VALUE);
            bioFactoryBaseProcessTicks = builder.translation("config.jdte.jdte.bioFactory.baseProcessTicks")
                    .defineInRange("baseProcessTicks", 600, 1, 72000);
            bioFactorySettlementInterval = builder.comment("Ticks between lightweight production settlements")
                    .translation("config.jdte.jdte.bioFactory.settlementInterval")
                    .defineInRange("settlementInterval", 20, 1, 1200);
            bioFactoryTimeFluidPerCycle = builder.translation("config.jdte.jdte.bioFactory.timeFluidPerCycle")
                    .defineInRange("timeFluidPerCycle", 10, 0, Integer.MAX_VALUE);
            bioFactoryMaxSpeedMultiplier = builder.translation("config.jdte.jdte.bioFactory.maxSpeedMultiplier")
                    .defineInRange("maxSpeedMultiplier", 32, 1, 64);
            bioFactoryDefaultSpeedMultiplier = builder.translation("config.jdte.jdte.bioFactory.defaultSpeedMultiplier")
                    .defineInRange("defaultSpeedMultiplier", 1, 1, 64);
            bioFactoryOverclockMaxSpeedMultiplier = builder.translation("config.jdte.jdte.bioFactory.overclockMaxSpeedMultiplier")
                    .defineInRange("overclockMaxSpeedMultiplier", 64, 1, 128);
            bioFactoryLifeFluidPerCycle = builder.translation("config.jdte.jdte.bioFactory.lifeFluidPerCycle")
                    .defineInRange("lifeFluidPerCycle", 100, 0, Integer.MAX_VALUE);
            bioFactoryLifeYieldMultiplier = builder.translation("config.jdte.jdte.bioFactory.lifeYieldMultiplier")
                    .defineInRange("lifeYieldMultiplier", 2.0D, 1.0D, 64.0D);
            bioFactoryProcessFluidPerCycle = builder.translation("config.jdte.jdte.bioFactory.processFluidPerCycle")
                    .defineInRange("processFluidPerCycle", 100, 0, Integer.MAX_VALUE);
            bioFactoryExternalTimeFluidCostMultiplier = builder
                    .translation("config.jdte.jdte.bioFactory.externalTimeFluidCostMultiplier")
                    .defineInRange("externalTimeFluidCostMultiplier", 10, 1, 1000);
            bioFactoryExternalLifeFluidCostMultiplier = builder
                    .translation("config.jdte.jdte.bioFactory.externalLifeFluidCostMultiplier")
                    .defineInRange("externalLifeFluidCostMultiplier", 5, 1, 1000);
            builder.pop();

            // Gel Generator
            builder.comment("Gel Generator Settings").translation("config.jdte.jdte.gelGenerator").push("gelGenerator");
            gelGeneratorInputSlots = builder
                    .comment("Number of input slots")
                    .translation("config.jdte.jdte.gelGenerator.gelGeneratorInputSlots")
                    .defineInRange("gelGeneratorInputSlots", 4, 1, 27);
            gelGeneratorOutputSlots = builder
                    .comment("Number of output slots")
                    .translation("config.jdte.jdte.gelGenerator.gelGeneratorOutputSlots")
                    .defineInRange("gelGeneratorOutputSlots", 4, 1, 27);
            gelGeneratorFluidCapacity = builder
                    .comment("Base fluid capacity (mB)")
                    .translation("config.jdte.jdte.gelGenerator.gelGeneratorFluidCapacity")
                    .defineInRange("gelGeneratorFluidCapacity", 4000, 100, 1000000);
            gelGeneratorEnergyCapacity = builder
                    .comment("Base energy capacity")
                    .translation("config.jdte.jdte.gelGenerator.gelGeneratorEnergyCapacity")
                    .defineInRange("gelGeneratorEnergyCapacity", 100000, 1000, 10000000);
            gelGeneratorFluidConversionAmount = builder
                    .comment("Fluid conversion amount per operation (mB)")
                    .translation("config.jdte.jdte.gelGenerator.gelGeneratorFluidConversionAmount")
                    .defineInRange("gelGeneratorFluidConversionAmount", 1000, 1, 100000);
            gelGeneratorFuelUsesPerItem = builder
                    .comment("Number of uses per food item")
                    .translation("config.jdte.jdte.gelGenerator.gelGeneratorFuelUsesPerItem")
                    .defineInRange("gelGeneratorFuelUsesPerItem", 2, 1, 100);
            gelGeneratorEnergyCost = builder
                    .comment("Base energy cost per conversion")
                    .translation("config.jdte.jdte.gelGenerator.gelGeneratorEnergyCost")
                    .defineInRange("gelGeneratorEnergyCost", 1000, 10, 100000);
            builder.pop();

            // Generator Upgrade
            builder.comment("Generator Upgrade Settings").translation("config.jdte.jdte.generatorUpgrade").push("generatorUpgrade");
            generatorUpgradeEnergyMultiplier = builder
                    .comment("Generator upgrade energy output multiplier")
                    .translation("config.jdte.jdte.generatorUpgrade.generatorUpgradeEnergyMultiplier")
                    .defineInRange("generatorUpgradeEnergyMultiplier", 3, 1, 10);
            generatorUpgradeFluidCost = builder
                    .comment("Generator upgrade fluid cost per tick (mB)")
                    .translation("config.jdte.jdte.generatorUpgrade.generatorUpgradeFluidCost")
                    .defineInRange("generatorUpgradeFluidCost", 2, 1, 100);
            builder.pop();

            // Upgrade Items
            builder.comment("Upgrade Item Settings").translation("config.jdte.jdte.upgradeItems").push("upgradeItems");
            maxSharpnessUpgrades = builder
                    .comment("Max sharpness upgrades stackable")
                    .translation("config.jdte.jdte.upgradeItems.maxSharpnessUpgrades")
                    .defineInRange("maxSharpnessUpgrades", 6, 1, 64);
            sharpnessDamagePerUpgrade = builder
                    .comment("Damage added per sharpness upgrade")
                    .translation("config.jdte.jdte.upgradeItems.sharpnessDamagePerUpgrade")
                    .defineInRange("sharpnessDamagePerUpgrade", 5, 1, 100);
            maxLootingUpgrades = builder
                    .comment("Max looting upgrades stackable")
                    .translation("config.jdte.jdte.upgradeItems.maxLootingUpgrades")
                    .defineInRange("maxLootingUpgrades", 6, 1, 64);
            builder.pop();

            builder.pop();
        }
    }
}
