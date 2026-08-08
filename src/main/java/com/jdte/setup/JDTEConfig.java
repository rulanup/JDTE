package com.jdte.setup;

import com.jdte.setup.config.AdvancedItemCollectorConfig;
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
import com.jdte.setup.config.RangeBlockerConfig;
import com.jdte.setup.config.SenderReceiverConfig;
import com.jdte.setup.config.TimeAcceleratorConfig;
import com.jdte.setup.config.TimeFreezerConfig;
import com.jdte.setup.config.UltimatePortalGunConfig;
import com.jdte.setup.config.UpgradeItemsConfig;
import com.jdte.setup.config.UpgradesConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class JDTEConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    public static class Common {
        public final UpgradesConfig upgrades;
        public final TimeAcceleratorConfig timeAccelerator;
        public final TimeFreezerConfig timeFreezer;
        public final UltimatePortalGunConfig ultimatePortalGun;
        public final BioCrusherConfig bioCrusher;
        public final LifeExtractorConfig lifeExtractor;
        public final LootFabricatorConfig lootFabricator;
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

        // Upgrade System
        public final ForgeConfigSpec.IntValue filterSlotsPerUpgrade;
        public final ForgeConfigSpec.DoubleValue underclockEnergyMultiplier;
        public final ForgeConfigSpec.IntValue overclockEnergyMultiplier;
        public final ForgeConfigSpec.IntValue underclockTickSpeed;
        public final ForgeConfigSpec.IntValue overclockTickSpeed;
        public final ForgeConfigSpec.DoubleValue baseAreaRadius;
        public final ForgeConfigSpec.IntValue baseAreaOffset;

        // Time Accelerator
        public final ForgeConfigSpec.IntValue basicTimeAcceleratorDefaultMultiplier;
        public final ForgeConfigSpec.IntValue basicTimeAcceleratorOverclockMultiplier;
        public final ForgeConfigSpec.IntValue advancedTimeAcceleratorEnergyCapacity;
        public final ForgeConfigSpec.IntValue advancedTimeAcceleratorMaxMultiplier;
        public final ForgeConfigSpec.IntValue advancedTimeAcceleratorOverclockMultiplier;
        public final ForgeConfigSpec.IntValue advancedTimeAcceleratorDefaultMultiplier;
        public final ForgeConfigSpec.IntValue extendedTimeAcceleratorMaxMultiplier;
        public final ForgeConfigSpec.IntValue extendedTimeAcceleratorOverclockMultiplier;
        public final ForgeConfigSpec.IntValue timeAcceleratorMaxExecutionsPerTick;
        public final ForgeConfigSpec.IntValue timeAcceleratorMaxScannedBlocksPerTick;
        public final ForgeConfigSpec.LongValue timeAcceleratorMaxPendingTicks;
        public final ForgeConfigSpec.IntValue timeAcceleratorExecutionBatchSize;
        public final ForgeConfigSpec.IntValue timeAcceleratorRandomRefreshInterval;
        public final ForgeConfigSpec.BooleanValue timeAcceleratorAE2Enabled;
        public final ForgeConfigSpec.IntValue timeAcceleratorBaseFluidCapacity;
        public final ForgeConfigSpec.DoubleValue timeAcceleratorFluidCostMultiplier;

        // Bio Crusher
        public final ForgeConfigSpec.IntValue bioCrusherFluidCapacity;
        public final ForgeConfigSpec.IntValue bioCrusherEnergyCost;
        public final ForgeConfigSpec.DoubleValue bioCrusherBaseRadius;
        public final ForgeConfigSpec.DoubleValue bioCrusherExperienceFluidMultiplier;
        public final ForgeConfigSpec.IntValue bioCrusherOutputSlotsPerCapacityUpgradeMultiplier;
        public final ForgeConfigSpec.IntValue bioCrusherBaseDamage;
        public final ForgeConfigSpec.IntValue bioCrusherProcessTime;
        public final ForgeConfigSpec.BooleanValue bioCrusherRespectDamageRestrictions;
        public final ForgeConfigSpec.BooleanValue bioCrusherAllowDestroyChaosGuardianCrystals;
        public final ForgeConfigSpec.BooleanValue bioCrusherAllowInstantKillChaosGuardian;
        public final ForgeConfigSpec.DoubleValue lootingExtraDropChance;
        public final ForgeConfigSpec.IntValue advancedBioCrusherEnergyCapacity;
        public final ForgeConfigSpec.IntValue extendedBioCrusherEnergyCapacity;
        public final ForgeConfigSpec.IntValue advancedBioCrusherMaxEntities;
        public final ForgeConfigSpec.IntValue extendedBioCrusherMaxEntities;

        // Life Extractor
        public final ForgeConfigSpec.DoubleValue lifeExtractorFluidPerHealth;
        public final ForgeConfigSpec.DoubleValue lifeExtractorHighHealthLossPercent;

        // Loot Fabricator
        public final ForgeConfigSpec.IntValue lootFabricatorLifeFluidCost;
        public final ForgeConfigSpec.IntValue lootFabricatorBaseTimeFluidCost;
        public final ForgeConfigSpec.IntValue lootFabricatorLootingFluidCostIncreasePercent;

        // Item/Fluid Sender/Receiver
        public final ForgeConfigSpec.IntValue senderStorageSlots;
        public final ForgeConfigSpec.IntValue advancedItemSenderEnergyCapacity;
        public final ForgeConfigSpec.IntValue advancedItemSenderEnergyCost;
        public final ForgeConfigSpec.IntValue extendedItemSenderEnergyCapacity;
        public final ForgeConfigSpec.IntValue advancedFluidSenderEnergyCapacity;
        public final ForgeConfigSpec.IntValue advancedFluidSenderEnergyCost;
        public final ForgeConfigSpec.IntValue extendedFluidSenderEnergyCapacity;
        public final ForgeConfigSpec.IntValue fluidSenderFluidCapacity;
        public final ForgeConfigSpec.BooleanValue fluidSenderUnlimitedTransfer;
        public final ForgeConfigSpec.IntValue autoIoItemTransferRate;
        public final ForgeConfigSpec.IntValue autoIoFluidTransferRate;
        public final ForgeConfigSpec.IntValue senderReceiverItemTransferRate;
        public final ForgeConfigSpec.IntValue senderReceiverOverclockItemTransferRate;
        public final ForgeConfigSpec.IntValue senderReceiverFluidTransferRate;
        public final ForgeConfigSpec.IntValue senderReceiverOverclockFluidTransferRate;
        public final ForgeConfigSpec.IntValue transferFailureBackoffStart;
        public final ForgeConfigSpec.IntValue transferFailureBackoffMax;

        // Advanced Item Collector
        public final ForgeConfigSpec.BooleanValue advancedItemCollectorPreDrainEnabled;
        public final ForgeConfigSpec.IntValue advancedItemCollectorPreDrainThreshold;
        public final ForgeConfigSpec.BooleanValue advancedItemCollectorMeDirectTransferEnabled;
        public final ForgeConfigSpec.BooleanValue advancedItemCollectorExistingItemScanEnabled;
        public final ForgeConfigSpec.IntValue advancedItemCollectorExistingItemScanInterval;
        public final ForgeConfigSpec.IntValue advancedItemCollectorExistingItemScanLimit;

        // Entity Suppressor
        public final ForgeConfigSpec.IntValue entitySuppressorEnergyCapacity;
        public final ForgeConfigSpec.IntValue entitySuppressorEnergyPerTick;
        public final ForgeConfigSpec.BooleanValue entitySuppressorProtectNamed;
        public final ForgeConfigSpec.BooleanValue entitySuppressorProtectTamed;
        public final ForgeConfigSpec.BooleanValue entitySuppressorProtectBosses;
        public final ForgeConfigSpec.BooleanValue entitySuppressorRemoveExisting;

        // Range Blocker
        public final ForgeConfigSpec.IntValue rangeBlockerEnergyCapacity;
        public final ForgeConfigSpec.IntValue rangeBlockerContainmentEnergyPerTick;
        public final ForgeConfigSpec.IntValue rangeBlockerDemagnetizationEnergyPerTick;
        public final ForgeConfigSpec.IntValue rangeBlockerSilenceEnergyPerTick;
        public final ForgeConfigSpec.BooleanValue rangeBlockerProtectNamed;
        public final ForgeConfigSpec.BooleanValue rangeBlockerProtectTamed;
        public final ForgeConfigSpec.BooleanValue rangeBlockerProtectBosses;
        public final ForgeConfigSpec.BooleanValue rangeBlockerMekanismIntegration;
        public final ForgeConfigSpec.BooleanValue rangeBlockerContainProjectiles;
        public final ForgeConfigSpec.BooleanValue rangeBlockerContainOwnerlessProjectiles;
        public final ForgeConfigSpec.BooleanValue rangeBlockerContainProjectileExplosions;

        // Factory Packer
        public final ForgeConfigSpec.IntValue factoryPackerEnergyCapacity;
        public final ForgeConfigSpec.DoubleValue factoryPackerBaseRadius;
        public final ForgeConfigSpec.IntValue factoryPackerEnergyPerBlock;
        public final ForgeConfigSpec.IntValue factoryPackerBlocksPerTick;
        public final ForgeConfigSpec.IntValue factoryPackerSourceChangeRetries;
        public final ForgeConfigSpec.IntValue factoryPackerMaxAxis;
        public final ForgeConfigSpec.IntValue factoryPackerMaxVolume;
        public final ForgeConfigSpec.IntValue factoryPackerMaxCompressedBytes;
        public final ForgeConfigSpec.IntValue factoryPackerMaxUncompressedBytes;
        public final ForgeConfigSpec.IntValue factoryPackerPreviewMaxBlocks;
        public final ForgeConfigSpec.IntValue factoryPackerMaxEntities;
        public final ForgeConfigSpec.IntValue factoryPackerEnergyPerEntity;
        public final ForgeConfigSpec.BooleanValue factoryPackerMoveEntities;
        public final ForgeConfigSpec.BooleanValue factoryPackerMoveScheduledTicks;
        public final ForgeConfigSpec.BooleanValue factoryPackerRemapInternalLinks;
        public final ForgeConfigSpec.BooleanValue factoryPackerUseModMoveStrategies;
        public final ForgeConfigSpec.BooleanValue factoryPackerChatNotifications;

        // Advanced Potion Brewer
        public final ForgeConfigSpec.BooleanValue potionBrewerRejectPatternProviderFuelInput;

        // Crystal Incubator
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

        // Greenhouse
        public final ForgeConfigSpec.IntValue greenhouseFluidCapacity;
        public final ForgeConfigSpec.IntValue greenhouseEnergyCapacity;
        public final ForgeConfigSpec.IntValue greenhouseBaseMultiplier;
        public final ForgeConfigSpec.IntValue greenhouseDefaultSpeedMultiplier;
        public final ForgeConfigSpec.IntValue greenhouseMaxSpeedMultiplier;
        public final ForgeConfigSpec.IntValue greenhouseOverclockMaxSpeedMultiplier;
        public final ForgeConfigSpec.IntValue greenhouseFluidCostDivisor;
        public final ForgeConfigSpec.IntValue greenhouseSettlementInterval;
        public final ForgeConfigSpec.IntValue greenhouseDefaultGrowthWork;
        public final ForgeConfigSpec.IntValue greenhouseEnergyPerHarvestV2;
        public final ForgeConfigSpec.IntValue greenhouseMysticalBaseFluidCost;
        public final ForgeConfigSpec.IntValue greenhouseGenericFluidCost;
        public final ForgeConfigSpec.IntValue greenhouseMaxHarvestsPerSettlementV2;
        public final ForgeConfigSpec.LongValue greenhouseMaxPendingWork;
        public final ForgeConfigSpec.IntValue greenhouseEventOutputItemBudget;
        public final ForgeConfigSpec.IntValue greenhouseEventOutputTypeBudget;
        public final ForgeConfigSpec.IntValue greenhouseDynamicHarvestCallsPerTick;

        // Bio Factory
        public final ForgeConfigSpec.IntValue bioFactoryFluidCapacity;
        public final ForgeConfigSpec.IntValue bioFactoryEnergyCapacity;
        public final ForgeConfigSpec.IntValue bioFactoryEnergyPerCycle;
        public final ForgeConfigSpec.IntValue bioFactoryBaseProcessTicks;
        public final ForgeConfigSpec.IntValue bioFactorySettlementInterval;
        public final ForgeConfigSpec.IntValue bioFactoryTimeFluidPerCycle;
        public final ForgeConfigSpec.IntValue bioFactoryMaxSpeedMultiplier;
        public final ForgeConfigSpec.IntValue bioFactoryDefaultSpeedMultiplier;
        public final ForgeConfigSpec.IntValue bioFactoryOverclockMaxSpeedMultiplier;
        public final ForgeConfigSpec.IntValue bioFactoryLifeFluidPerCycle;
        public final ForgeConfigSpec.DoubleValue bioFactoryLifeYieldMultiplier;
        public final ForgeConfigSpec.IntValue bioFactoryProcessFluidPerCycle;
        public final ForgeConfigSpec.IntValue bioFactoryExternalTimeFluidCostMultiplier;
        public final ForgeConfigSpec.IntValue bioFactoryExternalLifeFluidCostMultiplier;

        // Life Breeder
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

        // Gel Generator
        public final ForgeConfigSpec.IntValue gelGeneratorInputSlots;
        public final ForgeConfigSpec.IntValue gelGeneratorOutputSlots;
        public final ForgeConfigSpec.IntValue gelGeneratorFluidCapacity;
        public final ForgeConfigSpec.IntValue gelGeneratorEnergyCapacity;
        public final ForgeConfigSpec.IntValue gelGeneratorFluidConversionAmount;
        public final ForgeConfigSpec.IntValue gelGeneratorFuelUsesPerItem;
        public final ForgeConfigSpec.IntValue gelGeneratorEnergyCost;

        // Generator Upgrade
        public final ForgeConfigSpec.IntValue generatorUpgradeEnergyMultiplier;
        public final ForgeConfigSpec.IntValue generatorUpgradeFluidCost;

        // Upgrade Items
        public final ForgeConfigSpec.IntValue maxSharpnessUpgrades;
        public final ForgeConfigSpec.IntValue sharpnessDamagePerUpgrade;
        public final ForgeConfigSpec.IntValue maxLootingUpgrades;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("JDT Extras Settings").translation("config.jdte.jdte").push("jdte");

            upgrades = new UpgradesConfig(builder);
            timeAccelerator = new TimeAcceleratorConfig(builder);
            timeFreezer = new TimeFreezerConfig(builder);
            ultimatePortalGun = new UltimatePortalGunConfig(builder);
            bioCrusher = new BioCrusherConfig(builder);
            lifeExtractor = new LifeExtractorConfig(builder);
            lootFabricator = new LootFabricatorConfig(builder);
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
        }
    }
}
