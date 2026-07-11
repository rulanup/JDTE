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

        // Item/Fluid Sender/Receiver
        public final ModConfigSpec.IntValue senderStorageSlots;
        public final ModConfigSpec.IntValue basicItemSenderRate;
        public final ModConfigSpec.IntValue basicItemSenderDelay;
        public final ModConfigSpec.IntValue advancedItemSenderRate;
        public final ModConfigSpec.IntValue advancedItemSenderEnergyCapacity;
        public final ModConfigSpec.IntValue advancedItemSenderEnergyCost;
        public final ModConfigSpec.IntValue extendedItemSenderEnergyCapacity;
        public final ModConfigSpec.IntValue basicFluidSenderRate;
        public final ModConfigSpec.IntValue basicFluidSenderDelay;
        public final ModConfigSpec.IntValue advancedFluidSenderRate;
        public final ModConfigSpec.IntValue advancedFluidSenderEnergyCapacity;
        public final ModConfigSpec.IntValue advancedFluidSenderEnergyCost;
        public final ModConfigSpec.IntValue extendedFluidSenderEnergyCapacity;
        public final ModConfigSpec.IntValue fluidSenderFluidCapacity;

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
                    .defineInRange("basicTimeAcceleratorDefaultMultiplier", 4, 1, 100);
            basicTimeAcceleratorOverclockMultiplier = builder
                    .comment("Basic time accelerator overclock multiplier")
                    .translation("config.jdte.jdte.timeAccelerator.basicTimeAcceleratorOverclockMultiplier")
                    .defineInRange("basicTimeAcceleratorOverclockMultiplier", 16, 1, 1000);
            advancedTimeAcceleratorEnergyCapacity = builder
                    .comment("Advanced time accelerator energy capacity")
                    .translation("config.jdte.jdte.timeAccelerator.advancedTimeAcceleratorEnergyCapacity")
                    .defineInRange("advancedTimeAcceleratorEnergyCapacity", 200000, 10000, 10000000);
            advancedTimeAcceleratorMaxMultiplier = builder
                    .comment("Advanced time accelerator max adjustable multiplier")
                    .translation("config.jdte.jdte.timeAccelerator.advancedTimeAcceleratorMaxMultiplier")
                    .defineInRange("advancedTimeAcceleratorMaxMultiplier", 128, 1, 1000);
            advancedTimeAcceleratorOverclockMultiplier = builder
                    .comment("Advanced time accelerator overclock multiplier")
                    .translation("config.jdte.jdte.timeAccelerator.advancedTimeAcceleratorOverclockMultiplier")
                    .defineInRange("advancedTimeAcceleratorOverclockMultiplier", 256, 1, 10000);
            advancedTimeAcceleratorDefaultMultiplier = builder
                    .comment("Advanced time accelerator default multiplier")
                    .translation("config.jdte.jdte.timeAccelerator.advancedTimeAcceleratorDefaultMultiplier")
                    .defineInRange("advancedTimeAcceleratorDefaultMultiplier", 4, 1, 100);
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

            // Sender/Receiver
            builder.comment("Sender/Receiver Settings").translation("config.jdte.jdte.senderReceiver").push("senderReceiver");
            senderStorageSlots = builder
                    .comment("Internal storage slots for item sender/receiver")
                    .translation("config.jdte.jdte.senderReceiver.senderStorageSlots")
                    .defineInRange("senderStorageSlots", 9, 1, 54);
            basicItemSenderRate = builder
                    .comment("Basic item sender items per cycle")
                    .translation("config.jdte.jdte.senderReceiver.basicItemSenderRate")
                    .defineInRange("basicItemSenderRate", 4, 1, 64);
            basicItemSenderDelay = builder
                    .comment("Basic item sender tick delay")
                    .translation("config.jdte.jdte.senderReceiver.basicItemSenderDelay")
                    .defineInRange("basicItemSenderDelay", 10, 1, 100);
            advancedItemSenderRate = builder
                    .comment("Advanced item sender items per cycle")
                    .translation("config.jdte.jdte.senderReceiver.advancedItemSenderRate")
                    .defineInRange("advancedItemSenderRate", 32, 1, 256);
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
            basicFluidSenderRate = builder
                    .comment("Basic fluid sender amount per cycle (mB)")
                    .translation("config.jdte.jdte.senderReceiver.basicFluidSenderRate")
                    .defineInRange("basicFluidSenderRate", 1000, 1, 100000);
            basicFluidSenderDelay = builder
                    .comment("Basic fluid sender tick delay")
                    .translation("config.jdte.jdte.senderReceiver.basicFluidSenderDelay")
                    .defineInRange("basicFluidSenderDelay", 10, 1, 100);
            advancedFluidSenderRate = builder
                    .comment("Advanced fluid sender amount per cycle (mB)")
                    .translation("config.jdte.jdte.senderReceiver.advancedFluidSenderRate")
                    .defineInRange("advancedFluidSenderRate", 4000, 1, 100000);
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
