package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.items.BossEssenceItem;
import com.jdte.common.items.BigFluidTankItem;
import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.items.ExtendedUpgradeItem;
import com.jdte.common.items.FactoryPackageItem;
import com.jdte.common.items.LifeAppleItem;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.SharpnessUpgradeItem;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class JDTEItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JDTE.MODID);

    public static final RegistryObject<com.jdte.common.items.LargeFuelCanisterItem> LARGE_FUEL_CANISTER = ITEMS.register("large_fuel_canister", com.jdte.common.items.LargeFuelCanisterItem::new);

    public static final RegistryObject<UpgradeCardItem> CAPACITY_UPGRADE = ITEMS.register("capacity_upgrade", () -> new UpgradeCardItem(UpgradeType.CAPACITY));
    public static final RegistryObject<UpgradeCardItem> OVERCLOCK_UPGRADE = ITEMS.register("overclock_upgrade", () -> new UpgradeCardItem(UpgradeType.OVERCLOCK));
    public static final RegistryObject<UpgradeCardItem> UNDERCLOCK_UPGRADE = ITEMS.register("underclock_upgrade", () -> new UpgradeCardItem(UpgradeType.UNDERCLOCK));
    public static final RegistryObject<UpgradeCardItem> FLUID_UPGRADE = ITEMS.register("fluid_upgrade", () -> new UpgradeCardItem(UpgradeType.FLUID));
    public static final RegistryObject<UpgradeCardItem> FLUID_STORAGE_UPGRADE = ITEMS.register("fluid_storage_upgrade", () -> new UpgradeCardItem(UpgradeType.FLUID_STORAGE));
    public static final RegistryObject<UpgradeCardItem> GENERATOR_UPGRADE = ITEMS.register("generator_upgrade", () -> new UpgradeCardItem(UpgradeType.GENERATOR));
    public static final RegistryObject<UpgradeCardItem> RANGE_UPGRADE = ITEMS.register("range_upgrade", () -> new UpgradeCardItem(UpgradeType.RANGE));
    public static final RegistryObject<UpgradeCardItem> FILTER_UPGRADE = ITEMS.register("filter_upgrade", () -> new UpgradeCardItem(UpgradeType.FILTER));
    public static final RegistryObject<UpgradeCardItem> CREATIVE_UPGRADE = ITEMS.register("creative_upgrade", () -> new UpgradeCardItem(UpgradeType.CREATIVE));
    public static final RegistryObject<UpgradeCardItem> FORTUNE_UPGRADE = ITEMS.register("fortune_upgrade", () -> new UpgradeCardItem(UpgradeType.FORTUNE));
    public static final RegistryObject<UpgradeCardItem> PRECISION_UPGRADE = ITEMS.register("precision_upgrade", () -> new UpgradeCardItem(UpgradeType.PRECISION));
    public static final RegistryObject<ExtendedUpgradeItem> EXTENDED_UPGRADE = ITEMS.register("extended_upgrade", ExtendedUpgradeItem::new);
    public static final RegistryObject<EclipseAlloyWrenchItem> ECLIPSEALLOY_WRENCH = ITEMS.register("eclipsealloy_wrench", EclipseAlloyWrenchItem::new);
    public static final RegistryObject<Item> TIME_FLUID_CATALYST = ITEMS.register("time_fluid_catalyst", () -> new Item(new Item.Properties()));
    public static final RegistryObject<FactoryPackageItem> FACTORY_PACKAGE = ITEMS.register("factory_package", FactoryPackageItem::new);
    public static final RegistryObject<BlockItem> TIME_FREEZER = ITEMS.register("time_freezer", () -> new BlockItem(JDTEBlocks.TIME_FREEZER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_TIME_FREEZER = ITEMS.register("extended_time_freezer", () -> new BlockItem(JDTEBlocks.EXTENDED_TIME_FREEZER.get(), new Item.Properties()));
    public static final RegistryObject<UltimatePortalGunItem> ULTIMATE_PORTAL_GUN = ITEMS.register(
            "ultimate_portal_gun", UltimatePortalGunItem::new);
    public static final RegistryObject<BigFluidTankItem> BIG_FLUID_TANK = ITEMS.register(
            "big_fluid_tank", BigFluidTankItem::new);

    public static final RegistryObject<BlockItem> BASIC_TIME_ACCELERATOR = ITEMS.register("basic_time_accelerator", () -> new BlockItem(JDTEBlocks.BASIC_TIME_ACCELERATOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ADVANCED_TIME_ACCELERATOR = ITEMS.register("advanced_time_accelerator", () -> new BlockItem(JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_TIME_ACCELERATOR = ITEMS.register("extended_time_accelerator", () -> new BlockItem(JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_CLICKER = ITEMS.register("extended_clicker", () -> new BlockItem(JDTEBlocks.EXTENDED_CLICKER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_BLOCK_BREAKER = ITEMS.register("extended_block_breaker", () -> new BlockItem(JDTEBlocks.EXTENDED_BLOCK_BREAKER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_BLOCK_PLACER = ITEMS.register("extended_block_placer", () -> new BlockItem(JDTEBlocks.EXTENDED_BLOCK_PLACER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_BLOCK_SWAPPER = ITEMS.register("extended_block_swapper", () -> new BlockItem(JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_DROPPER = ITEMS.register("extended_dropper", () -> new BlockItem(JDTEBlocks.EXTENDED_DROPPER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_SENSOR = ITEMS.register("extended_sensor", () -> new BlockItem(JDTEBlocks.EXTENDED_SENSOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_FLUID_COLLECTOR = ITEMS.register("extended_fluid_collector", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_FLUID_PLACER = ITEMS.register("extended_fluid_placer", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_PLACER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ADVANCED_ITEM_COLLECTOR = ITEMS.register("advanced_item_collector", () -> new BlockItem(JDTEBlocks.ADVANCED_ITEM_COLLECTOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ENTITY_SUPPRESSOR = ITEMS.register("entity_suppressor", () -> new BlockItem(JDTEBlocks.ENTITY_SUPPRESSOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> RANGE_BLOCKER = ITEMS.register("range_blocker", () -> new BlockItem(JDTEBlocks.RANGE_BLOCKER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> CRYSTAL_INCUBATOR = ITEMS.register("crystal_incubator", () -> new BlockItem(JDTEBlocks.CRYSTAL_INCUBATOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> GREENHOUSE = ITEMS.register("greenhouse", () -> new BlockItem(JDTEBlocks.GREENHOUSE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> LARGE_GREENHOUSE = ITEMS.register("large_greenhouse",
            () -> new BlockItem(JDTEBlocks.LARGE_GREENHOUSE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> LIFE_SYNTHESIS_VAT = ITEMS.register("life_synthesis_vat",
            () -> new BlockItem(JDTEBlocks.LIFE_SYNTHESIS_VAT.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> BIO_FACTORY = ITEMS.register("bio_factory", () -> new BlockItem(JDTEBlocks.BIO_FACTORY.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> LIFE_BREEDER = ITEMS.register("life_breeder", () -> new BlockItem(JDTEBlocks.LIFE_BREEDER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> FACTORY_PACKER = ITEMS.register("factory_packer", () -> new BlockItem(JDTEBlocks.FACTORY_PACKER.get(), new Item.Properties()));

    public static final RegistryObject<UpgradeCardItem> AE_ACCELERATION_UPGRADE = ITEMS.register("ae_acceleration_upgrade", () -> new UpgradeCardItem(UpgradeType.AE_ACCELERATION));
    public static final RegistryObject<UpgradeCardItem> AE_CRAFTING_READ_UPGRADE = ITEMS.register("ae_crafting_read_upgrade", () -> new UpgradeCardItem(UpgradeType.AE_CRAFTING_READ));
    public static final RegistryObject<com.jdte.common.items.AEExtractionUpgradeItem> AE_EXTRACTION_UPGRADE = ITEMS.register("ae_extraction_upgrade", com.jdte.common.items.AEExtractionUpgradeItem::new);
    public static final RegistryObject<com.jdte.common.items.AEOutputUpgradeItem> AE_OUTPUT_UPGRADE = ITEMS.register("ae_output_upgrade", com.jdte.common.items.AEOutputUpgradeItem::new);
    public static final RegistryObject<com.jdte.common.items.AdvancedAEOutputUpgradeItem> ADVANCED_AE_OUTPUT_UPGRADE = ITEMS.register("advanced_ae_output_upgrade", com.jdte.common.items.AdvancedAEOutputUpgradeItem::new);

    public static final RegistryObject<BlockItem> ADVANCED_ENERGY_TRANSMITTER = ITEMS.register("advanced_energy_transmitter", () -> new BlockItem(JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_ENERGY_TRANSMITTER = ITEMS.register("extended_energy_transmitter", () -> new BlockItem(JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ADVANCED_FLUID_MIXER = ITEMS.register("advanced_fluid_mixer", () -> new BlockItem(JDTEBlocks.ADVANCED_FLUID_MIXER.get(),new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_FLUID_MIXER = ITEMS.register("extended_fluid_mixer", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_MIXER.get(),new Item.Properties()));
    public static final RegistryObject<com.jdte.common.items.MixingUpgradeItem> MIXING_UPGRADE = ITEMS.register("mixing_upgrade",com.jdte.common.items.MixingUpgradeItem::new);
    public static final RegistryObject<com.jdte.common.items.UltimateMixingParallelUpgradeItem> ULTIMATE_MIXING_PARALLEL_UPGRADE = ITEMS.register("ultimate_mixing_parallel_upgrade",com.jdte.common.items.UltimateMixingParallelUpgradeItem::new);
    public static final RegistryObject<com.jdte.common.items.EnergyBrewingUpgradeItem> ENERGY_BREWING_UPGRADE = ITEMS.register("energy_brewing_upgrade",com.jdte.common.items.EnergyBrewingUpgradeItem::new);
    public static final RegistryObject<com.jdte.common.items.EnergyOverloadUpgradeItem> ENERGY_OVERLOAD_UPGRADE = ITEMS.register("energy_overload_upgrade",com.jdte.common.items.EnergyOverloadUpgradeItem::new);
    // Glue Activator
    public static final RegistryObject<BlockItem> BASIC_GLUE_ACTIVATOR = ITEMS.register("basic_glue_activator", () -> new BlockItem(JDTEBlocks.BASIC_GLUE_ACTIVATOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ADVANCED_GLUE_ACTIVATOR = ITEMS.register("advanced_glue_activator", () -> new BlockItem(JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_GLUE_ACTIVATOR = ITEMS.register("extended_glue_activator", () -> new BlockItem(JDTEBlocks.EXTENDED_GLUE_ACTIVATOR.get(), new Item.Properties()));

    // Gel Generator
    public static final RegistryObject<BlockItem> ADVANCED_GEL_GENERATOR = ITEMS.register("advanced_gel_generator", () -> new BlockItem(JDTEBlocks.ADVANCED_GEL_GENERATOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_GEL_GENERATOR = ITEMS.register("extended_gel_generator", () -> new BlockItem(JDTEBlocks.EXTENDED_GEL_GENERATOR.get(), new Item.Properties()));

    // Fluid Stabilizer
    public static final RegistryObject<BlockItem> BASIC_FLUID_STABILIZER = ITEMS.register("basic_fluid_stabilizer", () -> new BlockItem(JDTEBlocks.BASIC_FLUID_STABILIZER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ADVANCED_FLUID_STABILIZER = ITEMS.register("advanced_fluid_stabilizer", () -> new BlockItem(JDTEBlocks.ADVANCED_FLUID_STABILIZER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_FLUID_STABILIZER = ITEMS.register("extended_fluid_stabilizer", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_STABILIZER.get(), new Item.Properties()));

    // Item Sender
    public static final RegistryObject<BlockItem> BASIC_ITEM_SENDER = ITEMS.register("basic_item_sender", () -> new BlockItem(JDTEBlocks.BASIC_ITEM_SENDER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ADVANCED_ITEM_SENDER = ITEMS.register("advanced_item_sender", () -> new BlockItem(JDTEBlocks.ADVANCED_ITEM_SENDER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_ITEM_SENDER = ITEMS.register("extended_item_sender", () -> new BlockItem(JDTEBlocks.EXTENDED_ITEM_SENDER.get(), new Item.Properties()));

    // Fluid Sender
    public static final RegistryObject<BlockItem> BASIC_FLUID_SENDER = ITEMS.register("basic_fluid_sender", () -> new BlockItem(JDTEBlocks.BASIC_FLUID_SENDER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ADVANCED_FLUID_SENDER = ITEMS.register("advanced_fluid_sender", () -> new BlockItem(JDTEBlocks.ADVANCED_FLUID_SENDER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_FLUID_SENDER = ITEMS.register("extended_fluid_sender", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_SENDER.get(), new Item.Properties()));

    // Item Receiver
    public static final RegistryObject<BlockItem> BASIC_ITEM_RECEIVER = ITEMS.register("basic_item_receiver", () -> new BlockItem(JDTEBlocks.BASIC_ITEM_RECEIVER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ADVANCED_ITEM_RECEIVER = ITEMS.register("advanced_item_receiver", () -> new BlockItem(JDTEBlocks.ADVANCED_ITEM_RECEIVER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_ITEM_RECEIVER = ITEMS.register("extended_item_receiver", () -> new BlockItem(JDTEBlocks.EXTENDED_ITEM_RECEIVER.get(), new Item.Properties()));

    // Fluid Receiver
    public static final RegistryObject<BlockItem> BASIC_FLUID_RECEIVER = ITEMS.register("basic_fluid_receiver", () -> new BlockItem(JDTEBlocks.BASIC_FLUID_RECEIVER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ADVANCED_FLUID_RECEIVER = ITEMS.register("advanced_fluid_receiver", () -> new BlockItem(JDTEBlocks.ADVANCED_FLUID_RECEIVER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_FLUID_RECEIVER = ITEMS.register("extended_fluid_receiver", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_RECEIVER.get(), new Item.Properties()));

    // Life Extractor
    public static final RegistryObject<BlockItem> ADVANCED_LIFE_EXTRACTOR = ITEMS.register("advanced_life_extractor", () -> new BlockItem(JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_LIFE_EXTRACTOR = ITEMS.register("extended_life_extractor", () -> new BlockItem(JDTEBlocks.EXTENDED_LIFE_EXTRACTOR.get(), new Item.Properties()));
    public static final RegistryObject<LifeAppleItem> LIFE_APPLE = ITEMS.register("life_apple", LifeAppleItem::new);

    // Infusion Machine
    public static final RegistryObject<BlockItem> ADVANCED_INFUSION_MACHINE = ITEMS.register("advanced_infusion_machine", () -> new BlockItem(JDTEBlocks.ADVANCED_INFUSION_MACHINE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_INFUSION_MACHINE = ITEMS.register("extended_infusion_machine", () -> new BlockItem(JDTEBlocks.EXTENDED_INFUSION_MACHINE.get(), new Item.Properties()));

    // Potion Brewer
    public static final RegistryObject<BlockItem> ADVANCED_POTION_BREWER = ITEMS.register("advanced_potion_brewer", () -> new BlockItem(JDTEBlocks.ADVANCED_POTION_BREWER.get(), new Item.Properties()));

    // Bio Crusher
    public static final RegistryObject<BlockItem> ADVANCED_BIO_CRUSHER = ITEMS.register("advanced_bio_crusher", () -> new BlockItem(JDTEBlocks.ADVANCED_BIO_CRUSHER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_BIO_CRUSHER = ITEMS.register("extended_bio_crusher", () -> new BlockItem(JDTEBlocks.EXTENDED_BIO_CRUSHER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> LOOT_FABRICATOR = ITEMS.register("loot_fabricator", () -> new BlockItem(JDTEBlocks.LOOT_FABRICATOR.get(), new Item.Properties()));

    public static final RegistryObject<com.jdte.common.items.RepairTalismanItem> REPAIR_TALISMAN = ITEMS.register("repair_talisman",
            () -> new com.jdte.common.items.RepairTalismanItem(new Item.Properties().stacksTo(1)));

    // Boss Essences
    public static final RegistryObject<BossEssenceItem> WITHER_ESSENCE = ITEMS.register("wither_essence", BossEssenceItem::new);
    public static final RegistryObject<BossEssenceItem> ENDER_DRAGON_ESSENCE = ITEMS.register("ender_dragon_essence", BossEssenceItem::new);
    public static final RegistryObject<BossEssenceItem> ELDER_GUARDIAN_ESSENCE = ITEMS.register("elder_guardian_essence", BossEssenceItem::new);
    public static final RegistryObject<ForgeSpawnEggItem> WITHER_SPAWN_EGG = ITEMS.register(
            "wither_spawn_egg",
            () -> new ForgeSpawnEggItem(() -> EntityType.WITHER, 0x2B2B2B, 0x737373, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> ENDER_DRAGON_SPAWN_EGG = ITEMS.register(
            "ender_dragon_spawn_egg",
            () -> new ForgeSpawnEggItem(() -> EntityType.ENDER_DRAGON, 0x161616, 0xE079FA, new Item.Properties()));

    // Bio Crusher Upgrades
    public static final RegistryObject<LootingUpgradeItem> LOOTING_UPGRADE = ITEMS.register("looting_upgrade", LootingUpgradeItem::new);
    public static final RegistryObject<SharpnessUpgradeItem> SHARPNESS_UPGRADE = ITEMS.register("sharpness_upgrade", SharpnessUpgradeItem::new);

    public static List<RegistryObject<UpgradeCardItem>> upgrades() {
        return List.of(CAPACITY_UPGRADE, OVERCLOCK_UPGRADE, UNDERCLOCK_UPGRADE, FLUID_UPGRADE, FLUID_STORAGE_UPGRADE,
                GENERATOR_UPGRADE, RANGE_UPGRADE, FILTER_UPGRADE, CREATIVE_UPGRADE, FORTUNE_UPGRADE, PRECISION_UPGRADE);
    }
    public static final RegistryObject<UpgradeCardItem> SEED_CONVERSION_UPGRADE = ITEMS.register("seed_conversion_upgrade",()->new UpgradeCardItem(UpgradeType.SEED_CONVERSION));
    public static final RegistryObject<com.jdte.common.items.CraftingPatternItem> CRAFTING_PATTERN = ITEMS.register("crafting_pattern",com.jdte.common.items.CraftingPatternItem::new);
    public static final RegistryObject<com.jdte.common.items.EssenceConversionUpgradeItem> ESSENCE_CONVERSION_UPGRADE = ITEMS.register("essence_conversion_upgrade",()->new com.jdte.common.items.EssenceConversionUpgradeItem(1));
    public static final RegistryObject<com.jdte.common.items.EssenceConversionUpgradeItem> ADVANCED_ESSENCE_CONVERSION_UPGRADE = ITEMS.register("advanced_essence_conversion_upgrade",()->new com.jdte.common.items.EssenceConversionUpgradeItem(3));
    public static final RegistryObject<com.jdte.common.items.AdvancedMachineSettingsCopierItem> ADVANCED_MACHINE_SETTINGS_COPIER = ITEMS.register("advanced_machine_settings_copier", com.jdte.common.items.AdvancedMachineSettingsCopierItem::new);
    public static final RegistryObject<com.jdte.common.items.AdvancedUpgradeStorageItem> ADVANCED_UPGRADE_STORAGE = ITEMS.register("advanced_upgrade_storage", com.jdte.common.items.AdvancedUpgradeStorageItem::new);
    public static final RegistryObject<com.jdte.common.items.UpgradeStorageItem> UPGRADE_STORAGE = ITEMS.register("upgrade_storage", com.jdte.common.items.UpgradeStorageItem::new);
    public static final RegistryObject<BlockItem> MINERAL_EXTRACTOR = ITEMS.register("mineral_extractor", () -> new BlockItem(JDTEBlocks.MINERAL_EXTRACTOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> LARGE_MINERAL_EXTRACTOR = ITEMS.register("large_mineral_extractor", () -> new BlockItem(JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get(), new Item.Properties()));
    public static final RegistryObject<com.jdte.common.items.MineralSurveyItem> MINERAL_SURVEY = ITEMS.register("mineral_survey", com.jdte.common.items.MineralSurveyItem::new);
    public static final RegistryObject<UpgradeCardItem> ULTIMATE_CAPACITY_UPGRADE = ITEMS.register("ultimate_capacity_upgrade", () -> new UpgradeCardItem(UpgradeType.ULTIMATE_CAPACITY));
    public static final RegistryObject<UpgradeCardItem> ULTIMATE_OVERCLOCK_UPGRADE = ITEMS.register("ultimate_overclock_upgrade", () -> new UpgradeCardItem(UpgradeType.ULTIMATE_OVERCLOCK));
    public static final RegistryObject<BlockItem> EXTENDED_GENERATOR = ITEMS.register("extended_generator", () -> new BlockItem(JDTEBlocks.EXTENDED_GENERATOR.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> EXTENDED_FLUID_GENERATOR = ITEMS.register("extended_fluid_generator", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_GENERATOR.get(), new Item.Properties()));

    public static final RegistryObject<BlockItem> ADVANCED_BIO_EXTRACTOR = ITEMS.register("advanced_bio_extractor", () -> new BlockItem(JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> EXTENDED_BIO_EXTRACTOR = ITEMS.register("extended_bio_extractor", () -> new BlockItem(JDTEBlocks.EXTENDED_BIO_EXTRACTOR.get(), new Item.Properties()));
}
