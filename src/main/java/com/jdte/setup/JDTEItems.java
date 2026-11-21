package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.items.AdvancedMachineSettingsCopierItem;
import com.jdte.common.items.BossEssenceItem;
import com.jdte.common.items.CraftingPatternItem;
import com.jdte.common.items.EssenceConversionUpgradeItem;
import com.jdte.common.items.ChargedLightningRodItem;
import com.jdte.common.items.BigFluidTankItem;
import com.jdte.common.items.EnergyBrewingUpgradeItem;
import com.jdte.common.items.EnergyOverloadUpgradeItem;
import com.jdte.common.items.EntityAccelerationUpgradeItem;
import com.jdte.common.items.MixingUpgradeItem;
import com.jdte.common.items.UltimateMixingParallelUpgradeItem;
import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.items.ExtendedUpgradeItem;
import com.jdte.common.items.FactoryPackageItem;
import com.jdte.common.items.LifeAppleItem;
import com.jdte.common.items.LargeFuelCanisterItem;
import com.jdte.common.items.LargePocketGeneratorItem;
import com.jdte.common.items.LargePotionCanisterItem;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.MineralSurveyItem;
import com.jdte.common.items.RepairTalismanItem;
import com.jdte.common.items.SharpnessUpgradeItem;
import com.jdte.common.items.TimeMultitoolItem;
import com.jdte.common.items.UltimateOverclockUpgradeItem;
import com.jdte.common.items.UltimateTimeWandItem;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.items.UpgradeStorageItem;
import com.jdte.common.items.AdvancedUpgradeStorageItem;
import com.jdte.common.items.AEOutputUpgradeItem;
import com.jdte.common.items.AdvancedAEOutputUpgradeItem;
import com.jdte.common.items.AEExtractionUpgradeItem;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class JDTEItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JDTE.MODID);

    public static final DeferredHolder<Item, UpgradeCardItem> CAPACITY_UPGRADE = ITEMS.registerItem("capacity_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.CAPACITY));
    public static final DeferredHolder<Item, UpgradeCardItem> OVERCLOCK_UPGRADE = ITEMS.registerItem("overclock_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.OVERCLOCK));
    public static final DeferredHolder<Item, UpgradeCardItem> THUNDERSTORM_UPGRADE = ITEMS.registerItem("thunderstorm_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.THUNDERSTORM));
    public static final DeferredHolder<Item, UpgradeCardItem> UNDERCLOCK_UPGRADE = ITEMS.registerItem("underclock_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.UNDERCLOCK));
    public static final DeferredHolder<Item, UpgradeCardItem> FLUID_UPGRADE = ITEMS.registerItem("fluid_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.FLUID));
    public static final DeferredHolder<Item, UpgradeCardItem> FLUID_STORAGE_UPGRADE = ITEMS.registerItem("fluid_storage_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.FLUID_STORAGE));
    public static final DeferredHolder<Item, UpgradeCardItem> GENERATOR_UPGRADE = ITEMS.registerItem("generator_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.GENERATOR));
    public static final DeferredHolder<Item, UpgradeCardItem> RANGE_UPGRADE = ITEMS.registerItem("range_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.RANGE));
    public static final DeferredHolder<Item, UpgradeCardItem> FILTER_UPGRADE = ITEMS.registerItem("filter_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.FILTER));
    public static final DeferredHolder<Item, UpgradeCardItem> CREATIVE_UPGRADE = ITEMS.registerItem("creative_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.CREATIVE));
    public static final DeferredHolder<Item, UpgradeCardItem> FORTUNE_UPGRADE = ITEMS.registerItem("fortune_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.FORTUNE));
    public static final DeferredHolder<Item, UpgradeCardItem> PRECISION_UPGRADE = ITEMS.registerItem("precision_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.PRECISION));
    public static final DeferredHolder<Item, UpgradeCardItem> AE_ACCELERATION_UPGRADE = ITEMS.registerItem("ae_acceleration_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.AE_ACCELERATION));
    public static final DeferredHolder<Item, UpgradeCardItem> AE_CRAFTING_READ_UPGRADE = ITEMS.registerItem("ae_crafting_read_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.AE_CRAFTING_READ));
    public static final DeferredHolder<Item, UpgradeCardItem> AE_OUTPUT_UPGRADE = ITEMS.registerItem("ae_output_upgrade", AEOutputUpgradeItem::new);
    public static final DeferredHolder<Item, UpgradeCardItem> ADVANCED_AE_OUTPUT_UPGRADE = ITEMS.registerItem("advanced_ae_output_upgrade", AdvancedAEOutputUpgradeItem::new);
    public static final DeferredHolder<Item, EssenceConversionUpgradeItem> ESSENCE_CONVERSION_UPGRADE = ITEMS.registerItem(
            "essence_conversion_upgrade", properties -> new EssenceConversionUpgradeItem(properties, 1));
    public static final DeferredHolder<Item, EssenceConversionUpgradeItem> ADVANCED_ESSENCE_CONVERSION_UPGRADE = ITEMS.registerItem(
            "advanced_essence_conversion_upgrade", properties -> new EssenceConversionUpgradeItem(properties, 3));
    public static final DeferredHolder<Item, EssenceConversionUpgradeItem> ULTIMATE_ESSENCE_CONVERSION_UPGRADE = ITEMS.registerItem(
            "ultimate_essence_conversion_upgrade", properties -> new EssenceConversionUpgradeItem(properties, Integer.MAX_VALUE));
    public static final DeferredHolder<Item, CraftingPatternItem> CRAFTING_PATTERN = ITEMS.registerItem(
            "crafting_pattern", CraftingPatternItem::new);
    public static final DeferredHolder<Item, UpgradeCardItem> ULTIMATE_CAPACITY_UPGRADE = ITEMS.registerItem("ultimate_capacity_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.ULTIMATE_CAPACITY));
    public static final DeferredHolder<Item, UltimateOverclockUpgradeItem> ULTIMATE_OVERCLOCK_UPGRADE = ITEMS.registerItem("ultimate_overclock_upgrade", UltimateOverclockUpgradeItem::new);
    public static final DeferredHolder<Item, AEExtractionUpgradeItem> AE_EXTRACTION_UPGRADE =
            ITEMS.registerItem("ae_extraction_upgrade", AEExtractionUpgradeItem::new);
    public static final DeferredHolder<Item, UpgradeCardItem> SEED_CONVERSION_UPGRADE = ITEMS.registerItem("seed_conversion_upgrade", properties -> new UpgradeCardItem(properties, UpgradeType.SEED_CONVERSION));
    public static final DeferredHolder<Item, ExtendedUpgradeItem> EXTENDED_UPGRADE = ITEMS.registerItem("extended_upgrade", ExtendedUpgradeItem::new);
    public static final DeferredHolder<Item, UpgradeStorageItem> UPGRADE_STORAGE = ITEMS.registerItem("upgrade_storage", UpgradeStorageItem::new);
    public static final DeferredHolder<Item, AdvancedUpgradeStorageItem> ADVANCED_UPGRADE_STORAGE = ITEMS.registerItem("advanced_upgrade_storage", AdvancedUpgradeStorageItem::new);
    public static final DeferredHolder<Item, EclipseAlloyWrenchItem> ECLIPSEALLOY_WRENCH = ITEMS.registerItem("eclipsealloy_wrench", EclipseAlloyWrenchItem::new);
    public static final DeferredHolder<Item, AdvancedMachineSettingsCopierItem> ADVANCED_MACHINE_SETTINGS_COPIER =
            ITEMS.registerItem("advanced_machine_settings_copier", AdvancedMachineSettingsCopierItem::new);
    public static final DeferredHolder<Item, Item> TIME_FLUID_CATALYST = ITEMS.registerItem("time_fluid_catalyst", Item::new);
    public static final DeferredHolder<Item, UltimatePortalGunItem> ULTIMATE_PORTAL_GUN = ITEMS.registerItem("ultimate_portal_gun", UltimatePortalGunItem::new);
    public static final DeferredHolder<Item, BigFluidTankItem> BIG_FLUID_TANK = ITEMS.registerItem("big_fluid_tank", BigFluidTankItem::new);
    public static final DeferredHolder<Item, TimeMultitoolItem> TIME_MULTITOOL = ITEMS.registerItem("time_multitool", TimeMultitoolItem::new);
    public static final DeferredHolder<Item, UltimateTimeWandItem> ULTIMATE_TIME_WAND =
            ITEMS.registerItem("ultimate_time_wand", UltimateTimeWandItem::new);
    public static final DeferredHolder<Item, LargePocketGeneratorItem> LARGE_POCKET_GENERATOR = ITEMS.registerItem("large_pocket_generator", LargePocketGeneratorItem::new, () -> new Item.Properties().stacksTo(1));
    public static final DeferredHolder<Item, LargePotionCanisterItem> LARGE_POTION_CANISTER = ITEMS.registerItem("large_potion_canister", LargePotionCanisterItem::new, () -> new Item.Properties().stacksTo(1));
    public static final DeferredHolder<Item, LargeFuelCanisterItem> LARGE_FUEL_CANISTER = ITEMS.registerItem("large_fuel_canister", LargeFuelCanisterItem::new, () -> new Item.Properties().stacksTo(1));
    public static final DeferredHolder<Item, FactoryPackageItem> FACTORY_PACKAGE = ITEMS.registerItem("factory_package", FactoryPackageItem::new);
    public static final DeferredHolder<Item, MineralSurveyItem> MINERAL_SURVEY = ITEMS.registerItem("mineral_survey", MineralSurveyItem::new);

    public static final DeferredHolder<Item, BlockItem> BASIC_TIME_ACCELERATOR = blockItem("basic_time_accelerator", JDTEBlocks.BASIC_TIME_ACCELERATOR);
    public static final DeferredHolder<Item, BlockItem> ADVANCED_TIME_ACCELERATOR = blockItem("advanced_time_accelerator", JDTEBlocks.ADVANCED_TIME_ACCELERATOR);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_TIME_ACCELERATOR = blockItem("extended_time_accelerator", JDTEBlocks.EXTENDED_TIME_ACCELERATOR);
    public static final DeferredHolder<Item, BlockItem> ADVANCED_ENTITY_ACCELERATOR = blockItem("advanced_entity_accelerator", JDTEBlocks.ADVANCED_ENTITY_ACCELERATOR);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_ENTITY_ACCELERATOR = blockItem("extended_entity_accelerator", JDTEBlocks.EXTENDED_ENTITY_ACCELERATOR);
    public static final DeferredHolder<Item, BlockItem> TIME_FREEZER = blockItem("time_freezer", JDTEBlocks.TIME_FREEZER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_TIME_FREEZER = blockItem("extended_time_freezer", JDTEBlocks.EXTENDED_TIME_FREEZER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_CLICKER = blockItem("extended_clicker", JDTEBlocks.EXTENDED_CLICKER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_GENERATOR = blockItem("extended_generator", JDTEBlocks.EXTENDED_GENERATOR);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_EXPERIENCE_HOLDER = blockItem("extended_experience_holder", JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_GENERATOR = blockItem("extended_fluid_generator", JDTEBlocks.EXTENDED_FLUID_GENERATOR);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_ENERGY_TRANSMITTER = blockItem("extended_energy_transmitter", JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_BLOCK_BREAKER = blockItem("extended_block_breaker", JDTEBlocks.EXTENDED_BLOCK_BREAKER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_BLOCK_PLACER = blockItem("extended_block_placer", JDTEBlocks.EXTENDED_BLOCK_PLACER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_BLOCK_SWAPPER = blockItem("extended_block_swapper", JDTEBlocks.EXTENDED_BLOCK_SWAPPER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_DROPPER = blockItem("extended_dropper", JDTEBlocks.EXTENDED_DROPPER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_SENSOR = blockItem("extended_sensor", JDTEBlocks.EXTENDED_SENSOR);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_COLLECTOR = blockItem("extended_fluid_collector", JDTEBlocks.EXTENDED_FLUID_COLLECTOR);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_PLACER = blockItem("extended_fluid_placer", JDTEBlocks.EXTENDED_FLUID_PLACER);
    public static final DeferredHolder<Item, BlockItem> ADVANCED_ITEM_COLLECTOR = blockItem("advanced_item_collector", JDTEBlocks.ADVANCED_ITEM_COLLECTOR);
    public static final DeferredHolder<Item, BlockItem> ENTITY_SUPPRESSOR = blockItem("entity_suppressor", JDTEBlocks.ENTITY_SUPPRESSOR);
    public static final DeferredHolder<Item, BlockItem> RANGE_BLOCKER = blockItem("range_blocker", JDTEBlocks.RANGE_BLOCKER);
    public static final DeferredHolder<Item, BlockItem> CREATIVE_ENERGY_CELL = blockItem("creative_energy_cell", JDTEBlocks.CREATIVE_ENERGY_CELL);
    public static final DeferredHolder<Item, ChargedLightningRodItem> CHARGED_LIGHTNING_ROD = ITEMS.registerItem("charged_lightning_rod", properties -> new ChargedLightningRodItem(JDTEBlocks.CHARGED_LIGHTNING_ROD.get(), properties));
    public static final DeferredHolder<Item, BlockItem> CRYSTAL_INCUBATOR = blockItem("crystal_incubator", JDTEBlocks.CRYSTAL_INCUBATOR);
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE = blockItem("greenhouse", JDTEBlocks.GREENHOUSE);
    public static final DeferredHolder<Item, BlockItem> LARGE_GREENHOUSE = blockItem("large_greenhouse", JDTEBlocks.LARGE_GREENHOUSE);
    public static final DeferredHolder<Item, BlockItem> LIFE_SYNTHESIS_VAT = blockItem("life_synthesis_vat", JDTEBlocks.LIFE_SYNTHESIS_VAT);
    public static final DeferredHolder<Item, BlockItem> BIO_FACTORY = blockItem("bio_factory", JDTEBlocks.BIO_FACTORY);
    public static final DeferredHolder<Item, BlockItem> LIFE_BREEDER = blockItem("life_breeder", JDTEBlocks.LIFE_BREEDER);
    public static final DeferredHolder<Item, BlockItem> FACTORY_PACKER = blockItem("factory_packer", JDTEBlocks.FACTORY_PACKER);
    public static final DeferredHolder<Item, BlockItem> MINERAL_EXTRACTOR = blockItem("mineral_extractor", JDTEBlocks.MINERAL_EXTRACTOR);
    public static final DeferredHolder<Item, BlockItem> LARGE_MINERAL_EXTRACTOR = blockItem("large_mineral_extractor", JDTEBlocks.LARGE_MINERAL_EXTRACTOR);

    public static final DeferredHolder<Item, BlockItem> XINGYE_FUMO = blockItem("xingye_fumo", JDTEBlocks.XINGYE_FUMO);
    public static final DeferredHolder<Item, BlockItem> CAINIAOYO_FUMO = blockItem("cainiaoyo_fumo", JDTEBlocks.CAINIAOYO_FUMO);
    public static final DeferredHolder<Item, BlockItem> MAIQILIN_FUMO = blockItem("maiqilin_fumo", JDTEBlocks.MAIQILIN_FUMO);

    // Glue Activator
    public static final DeferredHolder<Item, BlockItem> BASIC_GLUE_ACTIVATOR = blockItem("basic_glue_activator", JDTEBlocks.BASIC_GLUE_ACTIVATOR);
    public static final DeferredHolder<Item, BlockItem> ADVANCED_GLUE_ACTIVATOR = blockItem("advanced_glue_activator", JDTEBlocks.ADVANCED_GLUE_ACTIVATOR);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_GLUE_ACTIVATOR = blockItem("extended_glue_activator", JDTEBlocks.EXTENDED_GLUE_ACTIVATOR);

    // Gel Generator
    public static final DeferredHolder<Item, BlockItem> ADVANCED_GEL_GENERATOR = blockItem("advanced_gel_generator", JDTEBlocks.ADVANCED_GEL_GENERATOR);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_GEL_GENERATOR = blockItem("extended_gel_generator", JDTEBlocks.EXTENDED_GEL_GENERATOR);

    // Fluid Stabilizer
    public static final DeferredHolder<Item, BlockItem> BASIC_FLUID_STABILIZER = blockItem("basic_fluid_stabilizer", JDTEBlocks.BASIC_FLUID_STABILIZER);
    public static final DeferredHolder<Item, BlockItem> ADVANCED_FLUID_STABILIZER = blockItem("advanced_fluid_stabilizer", JDTEBlocks.ADVANCED_FLUID_STABILIZER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_STABILIZER = blockItem("extended_fluid_stabilizer", JDTEBlocks.EXTENDED_FLUID_STABILIZER);

    // Item Sender
    public static final DeferredHolder<Item, BlockItem> BASIC_ITEM_SENDER = blockItem("basic_item_sender", JDTEBlocks.BASIC_ITEM_SENDER);
    public static final DeferredHolder<Item, BlockItem> ADVANCED_ITEM_SENDER = blockItem("advanced_item_sender", JDTEBlocks.ADVANCED_ITEM_SENDER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_ITEM_SENDER = blockItem("extended_item_sender", JDTEBlocks.EXTENDED_ITEM_SENDER);

    // Fluid Sender
    public static final DeferredHolder<Item, BlockItem> BASIC_FLUID_SENDER = blockItem("basic_fluid_sender", JDTEBlocks.BASIC_FLUID_SENDER);
    public static final DeferredHolder<Item, BlockItem> ADVANCED_FLUID_SENDER = blockItem("advanced_fluid_sender", JDTEBlocks.ADVANCED_FLUID_SENDER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_SENDER = blockItem("extended_fluid_sender", JDTEBlocks.EXTENDED_FLUID_SENDER);

    // Item Receiver
    public static final DeferredHolder<Item, BlockItem> BASIC_ITEM_RECEIVER = blockItem("basic_item_receiver", JDTEBlocks.BASIC_ITEM_RECEIVER);
    public static final DeferredHolder<Item, BlockItem> ADVANCED_ITEM_RECEIVER = blockItem("advanced_item_receiver", JDTEBlocks.ADVANCED_ITEM_RECEIVER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_ITEM_RECEIVER = blockItem("extended_item_receiver", JDTEBlocks.EXTENDED_ITEM_RECEIVER);

    // Fluid Receiver
    public static final DeferredHolder<Item, BlockItem> BASIC_FLUID_RECEIVER = blockItem("basic_fluid_receiver", JDTEBlocks.BASIC_FLUID_RECEIVER);
    public static final DeferredHolder<Item, BlockItem> ADVANCED_FLUID_RECEIVER = blockItem("advanced_fluid_receiver", JDTEBlocks.ADVANCED_FLUID_RECEIVER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_RECEIVER = blockItem("extended_fluid_receiver", JDTEBlocks.EXTENDED_FLUID_RECEIVER);

    // Life Extractor
    public static final DeferredHolder<Item, BlockItem> ADVANCED_LIFE_EXTRACTOR = blockItem("advanced_life_extractor", JDTEBlocks.ADVANCED_LIFE_EXTRACTOR);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_LIFE_EXTRACTOR = blockItem("extended_life_extractor", JDTEBlocks.EXTENDED_LIFE_EXTRACTOR);
    public static final DeferredHolder<Item, LifeAppleItem> LIFE_APPLE = ITEMS.registerItem("life_apple", LifeAppleItem::new);

    // Bio Extractor
    public static final DeferredHolder<Item, BlockItem> ADVANCED_BIO_EXTRACTOR = blockItem("advanced_bio_extractor", JDTEBlocks.ADVANCED_BIO_EXTRACTOR);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_BIO_EXTRACTOR = blockItem("extended_bio_extractor", JDTEBlocks.EXTENDED_BIO_EXTRACTOR);

    // Infusion Machine
    public static final DeferredHolder<Item, BlockItem> ADVANCED_INFUSION_MACHINE = blockItem("advanced_infusion_machine", JDTEBlocks.ADVANCED_INFUSION_MACHINE);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_INFUSION_MACHINE = blockItem("extended_infusion_machine", JDTEBlocks.EXTENDED_INFUSION_MACHINE);

    // Potion Brewer
    public static final DeferredHolder<Item, BlockItem> ADVANCED_POTION_BREWER = blockItem("advanced_potion_brewer", JDTEBlocks.ADVANCED_POTION_BREWER);

    // Bio Crusher
    public static final DeferredHolder<Item, BlockItem> ADVANCED_BIO_CRUSHER = blockItem("advanced_bio_crusher", JDTEBlocks.ADVANCED_BIO_CRUSHER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_BIO_CRUSHER = blockItem("extended_bio_crusher", JDTEBlocks.EXTENDED_BIO_CRUSHER);
    public static final DeferredHolder<Item, BlockItem> LOOT_FABRICATOR = blockItem("loot_fabricator", JDTEBlocks.LOOT_FABRICATOR);

    // Advanced Energy Transmitter
    public static final DeferredHolder<Item, BlockItem> ADVANCED_ENERGY_TRANSMITTER = blockItem("advanced_energy_transmitter", JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER);

    // Fluid Mixer
    public static final DeferredHolder<Item, BlockItem> ADVANCED_FLUID_MIXER = blockItem("advanced_fluid_mixer", JDTEBlocks.ADVANCED_FLUID_MIXER);
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_MIXER = blockItem("extended_fluid_mixer", JDTEBlocks.EXTENDED_FLUID_MIXER);

    // Boss Essences
    public static final DeferredHolder<Item, BossEssenceItem> WITHER_ESSENCE = ITEMS.registerItem("wither_essence", BossEssenceItem::new);
    public static final DeferredHolder<Item, BossEssenceItem> ENDER_DRAGON_ESSENCE = ITEMS.registerItem("ender_dragon_essence", BossEssenceItem::new);
    public static final DeferredHolder<Item, BossEssenceItem> ELDER_GUARDIAN_ESSENCE = ITEMS.registerItem("elder_guardian_essence", BossEssenceItem::new);
    public static final DeferredHolder<Item, SpawnEggItem> WITHER_SPAWN_EGG = ITEMS.registerItem(
            "wither_spawn_egg", SpawnEggItem::new, () -> new Item.Properties().spawnEgg(EntityType.WITHER));
    public static final DeferredHolder<Item, SpawnEggItem> ENDER_DRAGON_SPAWN_EGG = ITEMS.registerItem(
            "ender_dragon_spawn_egg", SpawnEggItem::new, () -> new Item.Properties().spawnEgg(EntityType.ENDER_DRAGON));

    // Bio Crusher Upgrades
    public static final DeferredHolder<Item, LootingUpgradeItem> LOOTING_UPGRADE = ITEMS.registerItem("looting_upgrade", LootingUpgradeItem::new);
    public static final DeferredHolder<Item, SharpnessUpgradeItem> SHARPNESS_UPGRADE = ITEMS.registerItem("sharpness_upgrade", SharpnessUpgradeItem::new);
    public static final DeferredHolder<Item, EnergyBrewingUpgradeItem> ENERGY_BREWING_UPGRADE = ITEMS.registerItem("energy_brewing_upgrade", EnergyBrewingUpgradeItem::new);
    public static final DeferredHolder<Item, EnergyOverloadUpgradeItem> ENERGY_OVERLOAD_UPGRADE = ITEMS.registerItem("energy_overload_upgrade", EnergyOverloadUpgradeItem::new);
    public static final DeferredHolder<Item, MixingUpgradeItem> MIXING_UPGRADE = ITEMS.registerItem("mixing_upgrade", MixingUpgradeItem::new);
    public static final DeferredHolder<Item, UltimateMixingParallelUpgradeItem> ULTIMATE_MIXING_PARALLEL_UPGRADE =
            ITEMS.registerItem("ultimate_mixing_parallel_upgrade", UltimateMixingParallelUpgradeItem::new);
    public static final DeferredHolder<Item, EntityAccelerationUpgradeItem> ENTITY_ACCELERATION_UPGRADE = ITEMS.registerItem("entity_acceleration_upgrade", EntityAccelerationUpgradeItem::new);
    public static final DeferredHolder<Item, RepairTalismanItem> REPAIR_TALISMAN = ITEMS.registerItem("repair_talisman", RepairTalismanItem::new);

    public static List<DeferredHolder<Item, ? extends UpgradeCardItem>> upgrades() {
        return List.of(CAPACITY_UPGRADE, OVERCLOCK_UPGRADE, UNDERCLOCK_UPGRADE, FLUID_UPGRADE, FLUID_STORAGE_UPGRADE,
                GENERATOR_UPGRADE, RANGE_UPGRADE, FILTER_UPGRADE, CREATIVE_UPGRADE, FORTUNE_UPGRADE, PRECISION_UPGRADE,
                AE_ACCELERATION_UPGRADE, AE_CRAFTING_READ_UPGRADE, AE_OUTPUT_UPGRADE, ADVANCED_AE_OUTPUT_UPGRADE,
                ESSENCE_CONVERSION_UPGRADE, ADVANCED_ESSENCE_CONVERSION_UPGRADE, ULTIMATE_ESSENCE_CONVERSION_UPGRADE, SEED_CONVERSION_UPGRADE,
                ULTIMATE_CAPACITY_UPGRADE, ULTIMATE_OVERCLOCK_UPGRADE, THUNDERSTORM_UPGRADE);
    }

    private static DeferredHolder<Item, BlockItem> blockItem(String name, DeferredHolder<Block, ? extends Block> block) {
        return ITEMS.registerSimpleBlockItem(name, block);
    }
}
