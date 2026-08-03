package com.jdte.setup;

import com.jdte.setup.config.AdvancedItemCollectorConfig;
import com.jdte.setup.config.AdvancedEnergyTransmitterConfig;
import com.jdte.setup.config.AdvancedPotionBrewerConfig;
import com.jdte.setup.config.BioCrusherConfig;
import com.jdte.setup.config.BioFactoryConfig;
import com.jdte.setup.config.CrystalIncubatorConfig;
import com.jdte.setup.config.EntitySuppressorConfig;
import com.jdte.setup.config.FactoryPackerConfig;
import com.jdte.setup.config.GelGeneratorConfig;
import com.jdte.setup.config.GeneratorUpgradeConfig;
import com.jdte.setup.config.GreenhouseConfig;
import com.jdte.setup.config.LifeBreederConfig;
import com.jdte.setup.config.LifeExtractorConfig;
import com.jdte.setup.config.LifeSynthesisVatConfig;
import com.jdte.setup.config.LootFabricatorConfig;
import com.jdte.setup.config.MineralExtractorConfig;
import com.jdte.setup.config.RangeBlockerConfig;
import com.jdte.setup.config.SenderReceiverConfig;
import com.jdte.setup.config.TimeAcceleratorConfig;
import com.jdte.setup.config.UpgradeItemsConfig;
import com.jdte.setup.config.UpgradesConfig;
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
        public final UpgradesConfig upgrades;
        public final TimeAcceleratorConfig timeAccelerator;
        public final BioCrusherConfig bioCrusher;
        public final LifeExtractorConfig lifeExtractor;
        public final LootFabricatorConfig lootFabricator;
        public final MineralExtractorConfig mineralExtractor;
        public final SenderReceiverConfig senderReceiver;
        public final AdvancedItemCollectorConfig advancedItemCollector;
        public final EntitySuppressorConfig entitySuppressor;
        public final RangeBlockerConfig rangeBlocker;
        public final FactoryPackerConfig factoryPacker;
        public final AdvancedPotionBrewerConfig advancedPotionBrewer;
        public final CrystalIncubatorConfig crystalIncubator;
        public final GreenhouseConfig greenhouse;
        public final LifeSynthesisVatConfig lifeSynthesisVat;
        public final BioFactoryConfig bioFactory;
        public final LifeBreederConfig lifeBreeder;
        public final GelGeneratorConfig gelGenerator;
        public final GeneratorUpgradeConfig generatorUpgrade;
        public final UpgradeItemsConfig upgradeItems;
        public final AdvancedEnergyTransmitterConfig advancedEnergyTransmitter;

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
        public final ModConfigSpec.IntValue timeAcceleratorMaxExecutionsPerTick;
        public final ModConfigSpec.IntValue timeAcceleratorMaxScannedBlocksPerTick;
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
        public final ModConfigSpec.DoubleValue lifeExtractorHighHealthLossPercent;

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
        public final ModConfigSpec.IntValue rangeBlockerDemagnetizationEnergyPerTick;
        public final ModConfigSpec.IntValue rangeBlockerSilenceEnergyPerTick;
        public final ModConfigSpec.BooleanValue rangeBlockerProtectNamed;
        public final ModConfigSpec.BooleanValue rangeBlockerProtectTamed;
        public final ModConfigSpec.BooleanValue rangeBlockerProtectBosses;
        public final ModConfigSpec.BooleanValue rangeBlockerMekanismIntegration;
        public final ModConfigSpec.BooleanValue rangeBlockerContainProjectiles;
        public final ModConfigSpec.BooleanValue rangeBlockerContainOwnerlessProjectiles;
        public final ModConfigSpec.BooleanValue rangeBlockerContainProjectileExplosions;

        // Factory Packer
        public final ModConfigSpec.IntValue factoryPackerEnergyCapacity;
        public final ModConfigSpec.DoubleValue factoryPackerBaseRadius;
        public final ModConfigSpec.IntValue factoryPackerEnergyPerBlock;
        public final ModConfigSpec.IntValue factoryPackerBlocksPerTick;
        public final ModConfigSpec.IntValue factoryPackerSourceChangeRetries;
        public final ModConfigSpec.IntValue factoryPackerMaxAxis;
        public final ModConfigSpec.IntValue factoryPackerMaxVolume;
        public final ModConfigSpec.IntValue factoryPackerMaxCompressedBytes;
        public final ModConfigSpec.IntValue factoryPackerMaxUncompressedBytes;
        public final ModConfigSpec.IntValue factoryPackerPreviewMaxBlocks;
        public final ModConfigSpec.IntValue factoryPackerMaxEntities;
        public final ModConfigSpec.IntValue factoryPackerEnergyPerEntity;
        public final ModConfigSpec.BooleanValue factoryPackerMoveEntities;
        public final ModConfigSpec.BooleanValue factoryPackerMoveScheduledTicks;
        public final ModConfigSpec.BooleanValue factoryPackerRemapInternalLinks;
        public final ModConfigSpec.BooleanValue factoryPackerUseModMoveStrategies;
        public final ModConfigSpec.BooleanValue factoryPackerChatNotifications;

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
        public final ModConfigSpec.LongValue greenhouseMaxPendingWork;
        public final ModConfigSpec.IntValue greenhouseEventOutputItemBudget;
        public final ModConfigSpec.IntValue greenhouseEventOutputTypeBudget;
        public final ModConfigSpec.IntValue greenhouseDynamicHarvestCallsPerTick;

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

        // Life Breeder
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

        // Advanced Energy Transmitter
        public final ModConfigSpec.IntValue advancedEnergyTransmitterEnergyCapacity;
        public final ModConfigSpec.IntValue advancedEnergyTransmitterBaseTickDelay;
        public final ModConfigSpec.IntValue advancedEnergyTransmitterTargetRefreshInterval;
        public final ModConfigSpec.IntValue advancedEnergyTransmitterScanBlocksPerTick;
        public final ModConfigSpec.IntValue advancedEnergyTransmitterMaxTargetsPerTick;
        public final ModConfigSpec.IntValue advancedEnergyTransmitterMaxTransferPerTarget;
        public final ModConfigSpec.IntValue advancedEnergyTransmitterTransferBudgetPerTick;
        public final ModConfigSpec.IntValue advancedEnergyTransmitterOverclockTransferMultiplier;
        public final ModConfigSpec.IntValue advancedEnergyTransmitterMeExtractionLimitPerTick;
        public final ModConfigSpec.BooleanValue advancedEnergyTransmitterExcludeTransmitters;
        public final ModConfigSpec.BooleanValue advancedEnergyTransmitterShowParticlesByDefault;
        public final ModConfigSpec.IntValue advancedEnergyTransmitterMaxParticleTargetsPerTick;
        public final ModConfigSpec.IntValue advancedEnergyTransmitterPlayerChargeMaxItemsPerTick;
        public final ModConfigSpec.IntValue advancedEnergyTransmitterPlayerChargeMaxCallsPerItem;

        public Common(ModConfigSpec.Builder builder) {
            builder.comment("JDT Extras Settings").translation("config.jdte.jdte").push("jdte");

            upgrades = new UpgradesConfig(builder);
            timeAccelerator = new TimeAcceleratorConfig(builder);
            bioCrusher = new BioCrusherConfig(builder);
            lifeExtractor = new LifeExtractorConfig(builder);
            lootFabricator = new LootFabricatorConfig(builder);
            mineralExtractor = new MineralExtractorConfig(builder);
            senderReceiver = new SenderReceiverConfig(builder);
            advancedItemCollector = new AdvancedItemCollectorConfig(builder);
            entitySuppressor = new EntitySuppressorConfig(builder);
            rangeBlocker = new RangeBlockerConfig(builder);
            factoryPacker = new FactoryPackerConfig(builder);
            advancedPotionBrewer = new AdvancedPotionBrewerConfig(builder);
            crystalIncubator = new CrystalIncubatorConfig(builder);
            greenhouse = new GreenhouseConfig(builder);
            lifeSynthesisVat = new LifeSynthesisVatConfig(builder);
            bioFactory = new BioFactoryConfig(builder);
            lifeBreeder = new LifeBreederConfig(builder);
            gelGenerator = new GelGeneratorConfig(builder);
            generatorUpgrade = new GeneratorUpgradeConfig(builder);
            upgradeItems = new UpgradeItemsConfig(builder);
            advancedEnergyTransmitter = new AdvancedEnergyTransmitterConfig(builder);

            builder.pop();

            this.filterSlotsPerUpgrade = upgrades.filterSlotsPerUpgrade;
            this.underclockEnergyMultiplier = upgrades.underclockEnergyMultiplier;
            this.overclockEnergyMultiplier = upgrades.overclockEnergyMultiplier;
            this.underclockTickSpeed = upgrades.underclockTickSpeed;
            this.overclockTickSpeed = upgrades.overclockTickSpeed;
            this.baseAreaRadius = upgrades.baseAreaRadius;
            this.baseAreaOffset = upgrades.baseAreaOffset;

            this.basicTimeAcceleratorDefaultMultiplier = timeAccelerator.basicTimeAcceleratorDefaultMultiplier;
            this.basicTimeAcceleratorOverclockMultiplier = timeAccelerator.basicTimeAcceleratorOverclockMultiplier;
            this.advancedTimeAcceleratorEnergyCapacity = timeAccelerator.advancedTimeAcceleratorEnergyCapacity;
            this.advancedTimeAcceleratorMaxMultiplier = timeAccelerator.advancedTimeAcceleratorMaxMultiplier;
            this.advancedTimeAcceleratorOverclockMultiplier = timeAccelerator.advancedTimeAcceleratorOverclockMultiplier;
            this.advancedTimeAcceleratorDefaultMultiplier = timeAccelerator.advancedTimeAcceleratorDefaultMultiplier;
            this.extendedTimeAcceleratorMaxMultiplier = timeAccelerator.extendedTimeAcceleratorMaxMultiplier;
            this.extendedTimeAcceleratorOverclockMultiplier = timeAccelerator.extendedTimeAcceleratorOverclockMultiplier;
            this.timeAcceleratorMaxExecutionsPerTick = timeAccelerator.timeAcceleratorMaxExecutionsPerTick;
            this.timeAcceleratorMaxScannedBlocksPerTick = timeAccelerator.timeAcceleratorMaxScannedBlocksPerTick;
            this.timeAcceleratorMaxPendingTicks = timeAccelerator.timeAcceleratorMaxPendingTicks;
            this.timeAcceleratorExecutionBatchSize = timeAccelerator.timeAcceleratorExecutionBatchSize;
            this.timeAcceleratorRandomRefreshInterval = timeAccelerator.timeAcceleratorRandomRefreshInterval;
            this.timeAcceleratorAE2Enabled = timeAccelerator.timeAcceleratorAE2Enabled;
            this.timeAcceleratorBaseFluidCapacity = timeAccelerator.timeAcceleratorBaseFluidCapacity;
            this.timeAcceleratorFluidCostMultiplier = timeAccelerator.timeAcceleratorFluidCostMultiplier;

            this.bioCrusherFluidCapacity = bioCrusher.bioCrusherFluidCapacity;
            this.bioCrusherEnergyCost = bioCrusher.bioCrusherEnergyCost;
            this.bioCrusherBaseRadius = bioCrusher.bioCrusherBaseRadius;
            this.bioCrusherExperienceFluidMultiplier = bioCrusher.bioCrusherExperienceFluidMultiplier;
            this.bioCrusherOutputSlotsPerCapacityUpgradeMultiplier = bioCrusher.bioCrusherOutputSlotsPerCapacityUpgradeMultiplier;
            this.bioCrusherBaseDamage = bioCrusher.bioCrusherBaseDamage;
            this.bioCrusherProcessTime = bioCrusher.bioCrusherProcessTime;
            this.bioCrusherRespectDamageRestrictions = bioCrusher.bioCrusherRespectDamageRestrictions;
            this.bioCrusherAllowDestroyChaosGuardianCrystals = bioCrusher.bioCrusherAllowDestroyChaosGuardianCrystals;
            this.bioCrusherAllowInstantKillChaosGuardian = bioCrusher.bioCrusherAllowInstantKillChaosGuardian;
            this.lootingExtraDropChance = bioCrusher.lootingExtraDropChance;
            this.advancedBioCrusherEnergyCapacity = bioCrusher.advancedBioCrusherEnergyCapacity;
            this.extendedBioCrusherEnergyCapacity = bioCrusher.extendedBioCrusherEnergyCapacity;
            this.advancedBioCrusherMaxEntities = bioCrusher.advancedBioCrusherMaxEntities;
            this.extendedBioCrusherMaxEntities = bioCrusher.extendedBioCrusherMaxEntities;

            this.lifeExtractorFluidPerHealth = lifeExtractor.lifeExtractorFluidPerHealth;
            this.lifeExtractorHighHealthLossPercent = lifeExtractor.lifeExtractorHighHealthLossPercent;

            this.lootFabricatorLifeFluidCost = lootFabricator.lootFabricatorLifeFluidCost;
            this.lootFabricatorBaseTimeFluidCost = lootFabricator.lootFabricatorBaseTimeFluidCost;
            this.lootFabricatorLootingFluidCostIncreasePercent = lootFabricator.lootFabricatorLootingFluidCostIncreasePercent;

            this.senderStorageSlots = senderReceiver.senderStorageSlots;
            this.advancedItemSenderEnergyCapacity = senderReceiver.advancedItemSenderEnergyCapacity;
            this.advancedItemSenderEnergyCost = senderReceiver.advancedItemSenderEnergyCost;
            this.extendedItemSenderEnergyCapacity = senderReceiver.extendedItemSenderEnergyCapacity;
            this.advancedFluidSenderEnergyCapacity = senderReceiver.advancedFluidSenderEnergyCapacity;
            this.advancedFluidSenderEnergyCost = senderReceiver.advancedFluidSenderEnergyCost;
            this.extendedFluidSenderEnergyCapacity = senderReceiver.extendedFluidSenderEnergyCapacity;
            this.fluidSenderFluidCapacity = senderReceiver.fluidSenderFluidCapacity;
            this.fluidSenderUnlimitedTransfer = senderReceiver.fluidSenderUnlimitedTransfer;
            this.autoIoItemTransferRate = senderReceiver.autoIoItemTransferRate;
            this.autoIoFluidTransferRate = senderReceiver.autoIoFluidTransferRate;
            this.senderReceiverItemTransferRate = senderReceiver.senderReceiverItemTransferRate;
            this.senderReceiverOverclockItemTransferRate = senderReceiver.senderReceiverOverclockItemTransferRate;
            this.senderReceiverFluidTransferRate = senderReceiver.senderReceiverFluidTransferRate;
            this.senderReceiverOverclockFluidTransferRate = senderReceiver.senderReceiverOverclockFluidTransferRate;
            this.transferFailureBackoffStart = senderReceiver.transferFailureBackoffStart;
            this.transferFailureBackoffMax = senderReceiver.transferFailureBackoffMax;

            this.advancedItemCollectorPreDrainEnabled = advancedItemCollector.advancedItemCollectorPreDrainEnabled;
            this.advancedItemCollectorPreDrainThreshold = advancedItemCollector.advancedItemCollectorPreDrainThreshold;
            this.advancedItemCollectorMeDirectTransferEnabled = advancedItemCollector.advancedItemCollectorMeDirectTransferEnabled;
            this.advancedItemCollectorExistingItemScanEnabled = advancedItemCollector.advancedItemCollectorExistingItemScanEnabled;
            this.advancedItemCollectorExistingItemScanInterval = advancedItemCollector.advancedItemCollectorExistingItemScanInterval;
            this.advancedItemCollectorExistingItemScanLimit = advancedItemCollector.advancedItemCollectorExistingItemScanLimit;

            this.entitySuppressorEnergyCapacity = entitySuppressor.entitySuppressorEnergyCapacity;
            this.entitySuppressorEnergyPerTick = entitySuppressor.entitySuppressorEnergyPerTick;
            this.entitySuppressorProtectNamed = entitySuppressor.entitySuppressorProtectNamed;
            this.entitySuppressorProtectTamed = entitySuppressor.entitySuppressorProtectTamed;
            this.entitySuppressorProtectBosses = entitySuppressor.entitySuppressorProtectBosses;
            this.entitySuppressorRemoveExisting = entitySuppressor.entitySuppressorRemoveExisting;

            this.rangeBlockerEnergyCapacity = rangeBlocker.rangeBlockerEnergyCapacity;
            this.rangeBlockerContainmentEnergyPerTick = rangeBlocker.rangeBlockerContainmentEnergyPerTick;
            this.rangeBlockerDemagnetizationEnergyPerTick = rangeBlocker.rangeBlockerDemagnetizationEnergyPerTick;
            this.rangeBlockerSilenceEnergyPerTick = rangeBlocker.rangeBlockerSilenceEnergyPerTick;
            this.rangeBlockerProtectNamed = rangeBlocker.rangeBlockerProtectNamed;
            this.rangeBlockerProtectTamed = rangeBlocker.rangeBlockerProtectTamed;
            this.rangeBlockerProtectBosses = rangeBlocker.rangeBlockerProtectBosses;
            this.rangeBlockerMekanismIntegration = rangeBlocker.rangeBlockerMekanismIntegration;
            this.rangeBlockerContainProjectiles = rangeBlocker.rangeBlockerContainProjectiles;
            this.rangeBlockerContainOwnerlessProjectiles = rangeBlocker.rangeBlockerContainOwnerlessProjectiles;
            this.rangeBlockerContainProjectileExplosions = rangeBlocker.rangeBlockerContainProjectileExplosions;

            this.factoryPackerEnergyCapacity = factoryPacker.factoryPackerEnergyCapacity;
            this.factoryPackerBaseRadius = factoryPacker.factoryPackerBaseRadius;
            this.factoryPackerEnergyPerBlock = factoryPacker.factoryPackerEnergyPerBlock;
            this.factoryPackerBlocksPerTick = factoryPacker.factoryPackerBlocksPerTick;
            this.factoryPackerSourceChangeRetries = factoryPacker.factoryPackerSourceChangeRetries;
            this.factoryPackerMaxAxis = factoryPacker.factoryPackerMaxAxis;
            this.factoryPackerMaxVolume = factoryPacker.factoryPackerMaxVolume;
            this.factoryPackerMaxCompressedBytes = factoryPacker.factoryPackerMaxCompressedBytes;
            this.factoryPackerMaxUncompressedBytes = factoryPacker.factoryPackerMaxUncompressedBytes;
            this.factoryPackerPreviewMaxBlocks = factoryPacker.factoryPackerPreviewMaxBlocks;
            this.factoryPackerMaxEntities = factoryPacker.factoryPackerMaxEntities;
            this.factoryPackerEnergyPerEntity = factoryPacker.factoryPackerEnergyPerEntity;
            this.factoryPackerMoveEntities = factoryPacker.factoryPackerMoveEntities;
            this.factoryPackerMoveScheduledTicks = factoryPacker.factoryPackerMoveScheduledTicks;
            this.factoryPackerRemapInternalLinks = factoryPacker.factoryPackerRemapInternalLinks;
            this.factoryPackerUseModMoveStrategies = factoryPacker.factoryPackerUseModMoveStrategies;
            this.factoryPackerChatNotifications = factoryPacker.factoryPackerChatNotifications;

            this.potionBrewerRejectPatternProviderFuelInput = advancedPotionBrewer.potionBrewerRejectPatternProviderFuelInput;

            this.crystalIncubatorFluidCapacity = crystalIncubator.crystalIncubatorFluidCapacity;
            this.crystalIncubatorEnergyCapacity = crystalIncubator.crystalIncubatorEnergyCapacity;
            this.crystalIncubatorEnergyCostMultiplier = crystalIncubator.crystalIncubatorEnergyCostMultiplier;
            this.crystalIncubatorMaxMultiplier = crystalIncubator.crystalIncubatorMaxMultiplier;
            this.crystalIncubatorOverclockMultiplier = crystalIncubator.crystalIncubatorOverclockMultiplier;
            this.crystalIncubatorFluidCostMultiplier = crystalIncubator.crystalIncubatorFluidCostMultiplier;
            this.crystalIncubatorRegularGrowthAcceleratorsAt8x = crystalIncubator.crystalIncubatorRegularGrowthAcceleratorsAt8x;
            this.crystalIncubatorScanBatchSize = crystalIncubator.crystalIncubatorScanBatchSize;
            this.crystalIncubatorCacheRefreshInterval = crystalIncubator.crystalIncubatorCacheRefreshInterval;
            this.crystalIncubatorMotherBatchSize = crystalIncubator.crystalIncubatorMotherBatchSize;
            this.crystalIncubatorGrowthOperationsPerTick = crystalIncubator.crystalIncubatorGrowthOperationsPerTick;
            this.crystalIncubatorHarvestOperationsPerTick = crystalIncubator.crystalIncubatorHarvestOperationsPerTick;
            this.crystalIncubatorDynaGrowthAttempts = crystalIncubator.crystalIncubatorDynaGrowthAttempts;

            this.greenhouseFluidCapacity = greenhouse.greenhouseFluidCapacity;
            this.greenhouseEnergyCapacity = greenhouse.greenhouseEnergyCapacity;
            this.greenhouseBaseMultiplier = greenhouse.greenhouseBaseMultiplier;
            this.greenhouseDefaultSpeedMultiplier = greenhouse.greenhouseDefaultSpeedMultiplier;
            this.greenhouseMaxSpeedMultiplier = greenhouse.greenhouseMaxSpeedMultiplier;
            this.greenhouseOverclockMaxSpeedMultiplier = greenhouse.greenhouseOverclockMaxSpeedMultiplier;
            this.greenhouseFluidCostDivisor = greenhouse.greenhouseFluidCostDivisor;
            this.greenhouseSettlementInterval = greenhouse.greenhouseSettlementInterval;
            this.greenhouseDefaultGrowthWork = greenhouse.greenhouseDefaultGrowthWork;
            this.greenhouseEnergyPerHarvestV2 = greenhouse.greenhouseEnergyPerHarvestV2;
            this.greenhouseMysticalBaseFluidCost = greenhouse.greenhouseMysticalBaseFluidCost;
            this.greenhouseGenericFluidCost = greenhouse.greenhouseGenericFluidCost;
            this.greenhouseMaxHarvestsPerSettlementV2 = greenhouse.greenhouseMaxHarvestsPerSettlementV2;
            this.greenhouseMaxPendingWork = greenhouse.greenhouseMaxPendingWork;
            this.greenhouseEventOutputItemBudget = greenhouse.greenhouseEventOutputItemBudget;
            this.greenhouseEventOutputTypeBudget = greenhouse.greenhouseEventOutputTypeBudget;
            this.greenhouseDynamicHarvestCallsPerTick = greenhouse.greenhouseDynamicHarvestCallsPerTick;

            this.bioFactoryFluidCapacity = bioFactory.bioFactoryFluidCapacity;
            this.bioFactoryEnergyCapacity = bioFactory.bioFactoryEnergyCapacity;
            this.bioFactoryEnergyPerCycle = bioFactory.bioFactoryEnergyPerCycle;
            this.bioFactoryBaseProcessTicks = bioFactory.bioFactoryBaseProcessTicks;
            this.bioFactorySettlementInterval = bioFactory.bioFactorySettlementInterval;
            this.bioFactoryTimeFluidPerCycle = bioFactory.bioFactoryTimeFluidPerCycle;
            this.bioFactoryMaxSpeedMultiplier = bioFactory.bioFactoryMaxSpeedMultiplier;
            this.bioFactoryDefaultSpeedMultiplier = bioFactory.bioFactoryDefaultSpeedMultiplier;
            this.bioFactoryOverclockMaxSpeedMultiplier = bioFactory.bioFactoryOverclockMaxSpeedMultiplier;
            this.bioFactoryLifeFluidPerCycle = bioFactory.bioFactoryLifeFluidPerCycle;
            this.bioFactoryLifeYieldMultiplier = bioFactory.bioFactoryLifeYieldMultiplier;
            this.bioFactoryProcessFluidPerCycle = bioFactory.bioFactoryProcessFluidPerCycle;
            this.bioFactoryExternalTimeFluidCostMultiplier = bioFactory.bioFactoryExternalTimeFluidCostMultiplier;
            this.bioFactoryExternalLifeFluidCostMultiplier = bioFactory.bioFactoryExternalLifeFluidCostMultiplier;

            this.lifeBreederEnergyCapacity = lifeBreeder.lifeBreederEnergyCapacity;
            this.lifeBreederFluidCapacity = lifeBreeder.lifeBreederFluidCapacity;
            this.lifeBreederBreedEnergyCost = lifeBreeder.lifeBreederBreedEnergyCost;
            this.lifeBreederBreedFluidCost = lifeBreeder.lifeBreederBreedFluidCost;
            this.lifeBreederEnergyPerGrowthTick = lifeBreeder.lifeBreederEnergyPerGrowthTick;
            this.lifeBreederGrowthTicksPerMb = lifeBreeder.lifeBreederGrowthTicksPerMb;
            this.lifeBreederFluidCostMultiplierV3 = lifeBreeder.lifeBreederFluidCostMultiplierV3;
            this.lifeBreederBreedingCooldownTicks = lifeBreeder.lifeBreederBreedingCooldownTicks;
            this.lifeBreederProcessingInterval = lifeBreeder.lifeBreederProcessingInterval;
            this.lifeBreederMaxEntitiesInspected = lifeBreeder.lifeBreederMaxEntitiesInspected;
            this.lifeBreederMaxPairsPerCycle = lifeBreeder.lifeBreederMaxPairsPerCycle;
            this.lifeBreederMaxAnimalsGrownPerCycle = lifeBreeder.lifeBreederMaxAnimalsGrownPerCycle;
            this.lifeBreederMaxAnimalsPerType = lifeBreeder.lifeBreederMaxAnimalsPerType;
            this.lifeBreederMaxDropsCollectedPerCycle = lifeBreeder.lifeBreederMaxDropsCollectedPerCycle;
            this.lifeBreederDefaultSpeedMultiplier = lifeBreeder.lifeBreederDefaultSpeedMultiplier;
            this.lifeBreederMaxSpeedMultiplier = lifeBreeder.lifeBreederMaxSpeedMultiplier;

            this.gelGeneratorInputSlots = gelGenerator.gelGeneratorInputSlots;
            this.gelGeneratorOutputSlots = gelGenerator.gelGeneratorOutputSlots;
            this.gelGeneratorFluidCapacity = gelGenerator.gelGeneratorFluidCapacity;
            this.gelGeneratorEnergyCapacity = gelGenerator.gelGeneratorEnergyCapacity;
            this.gelGeneratorFluidConversionAmount = gelGenerator.gelGeneratorFluidConversionAmount;
            this.gelGeneratorFuelUsesPerItem = gelGenerator.gelGeneratorFuelUsesPerItem;
            this.gelGeneratorEnergyCost = gelGenerator.gelGeneratorEnergyCost;

            this.generatorUpgradeEnergyMultiplier = generatorUpgrade.generatorUpgradeEnergyMultiplier;
            this.generatorUpgradeFluidCost = generatorUpgrade.generatorUpgradeFluidCost;

            this.maxSharpnessUpgrades = upgradeItems.maxSharpnessUpgrades;
            this.sharpnessDamagePerUpgrade = upgradeItems.sharpnessDamagePerUpgrade;
            this.maxLootingUpgrades = upgradeItems.maxLootingUpgrades;

            this.advancedEnergyTransmitterEnergyCapacity = advancedEnergyTransmitter.energyCapacity;
            this.advancedEnergyTransmitterBaseTickDelay = advancedEnergyTransmitter.baseTickDelay;
            this.advancedEnergyTransmitterTargetRefreshInterval = advancedEnergyTransmitter.targetRefreshInterval;
            this.advancedEnergyTransmitterScanBlocksPerTick = advancedEnergyTransmitter.scanBlocksPerTick;
            this.advancedEnergyTransmitterMaxTargetsPerTick = advancedEnergyTransmitter.maxTargetsPerTick;
            this.advancedEnergyTransmitterMaxTransferPerTarget = advancedEnergyTransmitter.maxTransferPerTarget;
            this.advancedEnergyTransmitterTransferBudgetPerTick = advancedEnergyTransmitter.transferBudgetPerTick;
            this.advancedEnergyTransmitterOverclockTransferMultiplier = advancedEnergyTransmitter.overclockTransferMultiplier;
            this.advancedEnergyTransmitterMeExtractionLimitPerTick = advancedEnergyTransmitter.meExtractionLimitPerTick;
            this.advancedEnergyTransmitterExcludeTransmitters = advancedEnergyTransmitter.excludeTransmitters;
            this.advancedEnergyTransmitterShowParticlesByDefault = advancedEnergyTransmitter.showParticlesByDefault;
            this.advancedEnergyTransmitterMaxParticleTargetsPerTick = advancedEnergyTransmitter.maxParticleTargetsPerTick;
            this.advancedEnergyTransmitterPlayerChargeMaxItemsPerTick = advancedEnergyTransmitter.playerChargeMaxItemsPerTick;
            this.advancedEnergyTransmitterPlayerChargeMaxCallsPerItem = advancedEnergyTransmitter.playerChargeMaxCallsPerItem;
        }
    }
}
