package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.items.BossEssenceItem;
import com.jdte.common.items.BigFluidTankItem;
import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.items.ExtendedUpgradeItem;
import com.jdte.common.items.FactoryPackageItem;
import com.jdte.common.items.LifeAppleItem;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.MineralSurveyItem;
import com.jdte.common.items.SharpnessUpgradeItem;
import com.jdte.common.items.TimeMultitoolItem;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.items.GreenhouseMatrixQuickInstallUpgradeItem;
import com.jdte.common.items.AEOutputUpgradeItem;
import com.jdte.common.items.UltimatePortalGunItem;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class JDTEItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JDTE.MODID);

    public static final DeferredHolder<Item, UpgradeCardItem> CAPACITY_UPGRADE = ITEMS.register("capacity_upgrade", () -> new UpgradeCardItem(UpgradeType.CAPACITY));
    public static final DeferredHolder<Item, UpgradeCardItem> OVERCLOCK_UPGRADE = ITEMS.register("overclock_upgrade", () -> new UpgradeCardItem(UpgradeType.OVERCLOCK));
    public static final DeferredHolder<Item, UpgradeCardItem> UNDERCLOCK_UPGRADE = ITEMS.register("underclock_upgrade", () -> new UpgradeCardItem(UpgradeType.UNDERCLOCK));
    public static final DeferredHolder<Item, UpgradeCardItem> FLUID_UPGRADE = ITEMS.register("fluid_upgrade", () -> new UpgradeCardItem(UpgradeType.FLUID));
    public static final DeferredHolder<Item, UpgradeCardItem> FLUID_STORAGE_UPGRADE = ITEMS.register("fluid_storage_upgrade", () -> new UpgradeCardItem(UpgradeType.FLUID_STORAGE));
    public static final DeferredHolder<Item, UpgradeCardItem> GENERATOR_UPGRADE = ITEMS.register("generator_upgrade", () -> new UpgradeCardItem(UpgradeType.GENERATOR));
    public static final DeferredHolder<Item, UpgradeCardItem> RANGE_UPGRADE = ITEMS.register("range_upgrade", () -> new UpgradeCardItem(UpgradeType.RANGE));
    public static final DeferredHolder<Item, UpgradeCardItem> FILTER_UPGRADE = ITEMS.register("filter_upgrade", () -> new UpgradeCardItem(UpgradeType.FILTER));
    public static final DeferredHolder<Item, UpgradeCardItem> CREATIVE_UPGRADE = ITEMS.register("creative_upgrade", () -> new UpgradeCardItem(UpgradeType.CREATIVE));
    public static final DeferredHolder<Item, UpgradeCardItem> FORTUNE_UPGRADE = ITEMS.register("fortune_upgrade", () -> new UpgradeCardItem(UpgradeType.FORTUNE));
    public static final DeferredHolder<Item, UpgradeCardItem> PRECISION_UPGRADE = ITEMS.register("precision_upgrade", () -> new UpgradeCardItem(UpgradeType.PRECISION));
    public static final DeferredHolder<Item, UpgradeCardItem> AE_ACCELERATION_UPGRADE = ITEMS.register("ae_acceleration_upgrade", () -> new UpgradeCardItem(UpgradeType.AE_ACCELERATION));
    public static final DeferredHolder<Item, UpgradeCardItem> AE_OUTPUT_UPGRADE = ITEMS.register("ae_output_upgrade", AEOutputUpgradeItem::new);
    public static final DeferredHolder<Item, UpgradeCardItem> ESSENCE_CONVERSION_UPGRADE = ITEMS.register("essence_conversion_upgrade", () -> new UpgradeCardItem(UpgradeType.ESSENCE_CONVERSION));
    public static final DeferredHolder<Item, UpgradeCardItem> SEED_CONVERSION_UPGRADE = ITEMS.register("seed_conversion_upgrade", () -> new UpgradeCardItem(UpgradeType.SEED_CONVERSION));
    public static final DeferredHolder<Item, GreenhouseMatrixQuickInstallUpgradeItem> GREENHOUSE_MATRIX_QUICK_INSTALL_UPGRADE =
            ITEMS.register("greenhouse_matrix_quick_install_upgrade", GreenhouseMatrixQuickInstallUpgradeItem::new);
    public static final DeferredHolder<Item, ExtendedUpgradeItem> EXTENDED_UPGRADE = ITEMS.register("extended_upgrade", ExtendedUpgradeItem::new);
    public static final DeferredHolder<Item, EclipseAlloyWrenchItem> ECLIPSEALLOY_WRENCH = ITEMS.register("eclipsealloy_wrench", EclipseAlloyWrenchItem::new);
    public static final DeferredHolder<Item, Item> TIME_FLUID_CATALYST = ITEMS.register("time_fluid_catalyst", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, UltimatePortalGunItem> ULTIMATE_PORTAL_GUN = ITEMS.register("ultimate_portal_gun", UltimatePortalGunItem::new);
    public static final DeferredHolder<Item, BigFluidTankItem> BIG_FLUID_TANK = ITEMS.register("big_fluid_tank", BigFluidTankItem::new);
    public static final DeferredHolder<Item, TimeMultitoolItem> TIME_MULTITOOL = ITEMS.register("time_multitool", TimeMultitoolItem::new);
    public static final DeferredHolder<Item, FactoryPackageItem> FACTORY_PACKAGE = ITEMS.register("factory_package", FactoryPackageItem::new);
    public static final DeferredHolder<Item, MineralSurveyItem> MINERAL_SURVEY = ITEMS.register("mineral_survey", MineralSurveyItem::new);

    public static final DeferredHolder<Item, BlockItem> BASIC_TIME_ACCELERATOR = ITEMS.register("basic_time_accelerator", () -> new BlockItem(JDTEBlocks.BASIC_TIME_ACCELERATOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ADVANCED_TIME_ACCELERATOR = ITEMS.register("advanced_time_accelerator", () -> new BlockItem(JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_TIME_ACCELERATOR = ITEMS.register("extended_time_accelerator", () -> new BlockItem(JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> TIME_FREEZER = ITEMS.register("time_freezer", () -> new BlockItem(JDTEBlocks.TIME_FREEZER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_TIME_FREEZER = ITEMS.register("extended_time_freezer", () -> new BlockItem(JDTEBlocks.EXTENDED_TIME_FREEZER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_CLICKER = ITEMS.register("extended_clicker", () -> new BlockItem(JDTEBlocks.EXTENDED_CLICKER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_BLOCK_BREAKER = ITEMS.register("extended_block_breaker", () -> new BlockItem(JDTEBlocks.EXTENDED_BLOCK_BREAKER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_BLOCK_PLACER = ITEMS.register("extended_block_placer", () -> new BlockItem(JDTEBlocks.EXTENDED_BLOCK_PLACER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_BLOCK_SWAPPER = ITEMS.register("extended_block_swapper", () -> new BlockItem(JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_DROPPER = ITEMS.register("extended_dropper", () -> new BlockItem(JDTEBlocks.EXTENDED_DROPPER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_SENSOR = ITEMS.register("extended_sensor", () -> new BlockItem(JDTEBlocks.EXTENDED_SENSOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_COLLECTOR = ITEMS.register("extended_fluid_collector", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_PLACER = ITEMS.register("extended_fluid_placer", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_PLACER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ADVANCED_ITEM_COLLECTOR = ITEMS.register("advanced_item_collector", () -> new BlockItem(JDTEBlocks.ADVANCED_ITEM_COLLECTOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ENTITY_SUPPRESSOR = ITEMS.register("entity_suppressor", () -> new BlockItem(JDTEBlocks.ENTITY_SUPPRESSOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> RANGE_BLOCKER = ITEMS.register("range_blocker", () -> new BlockItem(JDTEBlocks.RANGE_BLOCKER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> CRYSTAL_INCUBATOR = ITEMS.register("crystal_incubator", () -> new BlockItem(JDTEBlocks.CRYSTAL_INCUBATOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE = ITEMS.register("greenhouse", () -> new BlockItem(JDTEBlocks.GREENHOUSE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> CREATIVE_GREENHOUSE = ITEMS.register("creative_greenhouse",
            () -> new BlockItem(JDTEBlocks.CREATIVE_GREENHOUSE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> LARGE_GREENHOUSE = ITEMS.register("large_greenhouse",
            () -> new BlockItem(JDTEBlocks.LARGE_GREENHOUSE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE_MATRIX_CONTROLLER = blockItem("greenhouse_matrix_controller", JDTEBlocks.GREENHOUSE_MATRIX_CONTROLLER);
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE_MATRIX_CASING = blockItem("greenhouse_matrix_casing", JDTEBlocks.GREENHOUSE_MATRIX_CASING);
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE_MATRIX_ITEM_INPUT = blockItem("greenhouse_matrix_item_input", JDTEBlocks.GREENHOUSE_MATRIX_ITEM_INPUT);
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE_MATRIX_ITEM_OUTPUT = blockItem("greenhouse_matrix_item_output", JDTEBlocks.GREENHOUSE_MATRIX_ITEM_OUTPUT);
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE_MATRIX_FLUID_INPUT = blockItem("greenhouse_matrix_fluid_input", JDTEBlocks.GREENHOUSE_MATRIX_FLUID_INPUT);
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE_MATRIX_ENERGY_INPUT = blockItem("greenhouse_matrix_energy_input", JDTEBlocks.GREENHOUSE_MATRIX_ENERGY_INPUT);
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE_MATRIX_SPEED = blockItem("greenhouse_matrix_speed", JDTEBlocks.GREENHOUSE_MATRIX_SPEED);
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE_MATRIX_EFFICIENCY = blockItem("greenhouse_matrix_efficiency", JDTEBlocks.GREENHOUSE_MATRIX_EFFICIENCY);
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE_MATRIX_SEED = blockItem("greenhouse_matrix_seed", JDTEBlocks.GREENHOUSE_MATRIX_SEED);
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE_MATRIX_ESSENCE = blockItem("greenhouse_matrix_essence", JDTEBlocks.GREENHOUSE_MATRIX_ESSENCE);
    public static final DeferredHolder<Item, BlockItem> GREENHOUSE_MATRIX_AUTO_CRAFTING = blockItem(
            "greenhouse_matrix_auto_crafting", JDTEBlocks.GREENHOUSE_MATRIX_AUTO_CRAFTING);
    public static final DeferredHolder<Item, BlockItem> LIFE_SYNTHESIS_VAT = ITEMS.register("life_synthesis_vat",
            () -> new BlockItem(JDTEBlocks.LIFE_SYNTHESIS_VAT.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> BIO_FACTORY = ITEMS.register("bio_factory", () -> new BlockItem(JDTEBlocks.BIO_FACTORY.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> LIFE_BREEDER = ITEMS.register("life_breeder", () -> new BlockItem(JDTEBlocks.LIFE_BREEDER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> FACTORY_PACKER = ITEMS.register("factory_packer", () -> new BlockItem(JDTEBlocks.FACTORY_PACKER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> MINERAL_EXTRACTOR = ITEMS.register("mineral_extractor", () -> new BlockItem(JDTEBlocks.MINERAL_EXTRACTOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> LARGE_MINERAL_EXTRACTOR = ITEMS.register("large_mineral_extractor",
            () -> new BlockItem(JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get(), new Item.Properties()));

    // Glue Activator
    public static final DeferredHolder<Item, BlockItem> BASIC_GLUE_ACTIVATOR = ITEMS.register("basic_glue_activator", () -> new BlockItem(JDTEBlocks.BASIC_GLUE_ACTIVATOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ADVANCED_GLUE_ACTIVATOR = ITEMS.register("advanced_glue_activator", () -> new BlockItem(JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_GLUE_ACTIVATOR = ITEMS.register("extended_glue_activator", () -> new BlockItem(JDTEBlocks.EXTENDED_GLUE_ACTIVATOR.get(), new Item.Properties()));

    // Gel Generator
    public static final DeferredHolder<Item, BlockItem> ADVANCED_GEL_GENERATOR = ITEMS.register("advanced_gel_generator", () -> new BlockItem(JDTEBlocks.ADVANCED_GEL_GENERATOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_GEL_GENERATOR = ITEMS.register("extended_gel_generator", () -> new BlockItem(JDTEBlocks.EXTENDED_GEL_GENERATOR.get(), new Item.Properties()));

    // Fluid Stabilizer
    public static final DeferredHolder<Item, BlockItem> BASIC_FLUID_STABILIZER = ITEMS.register("basic_fluid_stabilizer", () -> new BlockItem(JDTEBlocks.BASIC_FLUID_STABILIZER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ADVANCED_FLUID_STABILIZER = ITEMS.register("advanced_fluid_stabilizer", () -> new BlockItem(JDTEBlocks.ADVANCED_FLUID_STABILIZER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_STABILIZER = ITEMS.register("extended_fluid_stabilizer", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_STABILIZER.get(), new Item.Properties()));

    // Item Sender
    public static final DeferredHolder<Item, BlockItem> BASIC_ITEM_SENDER = ITEMS.register("basic_item_sender", () -> new BlockItem(JDTEBlocks.BASIC_ITEM_SENDER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ADVANCED_ITEM_SENDER = ITEMS.register("advanced_item_sender", () -> new BlockItem(JDTEBlocks.ADVANCED_ITEM_SENDER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_ITEM_SENDER = ITEMS.register("extended_item_sender", () -> new BlockItem(JDTEBlocks.EXTENDED_ITEM_SENDER.get(), new Item.Properties()));

    // Fluid Sender
    public static final DeferredHolder<Item, BlockItem> BASIC_FLUID_SENDER = ITEMS.register("basic_fluid_sender", () -> new BlockItem(JDTEBlocks.BASIC_FLUID_SENDER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ADVANCED_FLUID_SENDER = ITEMS.register("advanced_fluid_sender", () -> new BlockItem(JDTEBlocks.ADVANCED_FLUID_SENDER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_SENDER = ITEMS.register("extended_fluid_sender", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_SENDER.get(), new Item.Properties()));

    // Item Receiver
    public static final DeferredHolder<Item, BlockItem> BASIC_ITEM_RECEIVER = ITEMS.register("basic_item_receiver", () -> new BlockItem(JDTEBlocks.BASIC_ITEM_RECEIVER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ADVANCED_ITEM_RECEIVER = ITEMS.register("advanced_item_receiver", () -> new BlockItem(JDTEBlocks.ADVANCED_ITEM_RECEIVER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_ITEM_RECEIVER = ITEMS.register("extended_item_receiver", () -> new BlockItem(JDTEBlocks.EXTENDED_ITEM_RECEIVER.get(), new Item.Properties()));

    // Fluid Receiver
    public static final DeferredHolder<Item, BlockItem> BASIC_FLUID_RECEIVER = ITEMS.register("basic_fluid_receiver", () -> new BlockItem(JDTEBlocks.BASIC_FLUID_RECEIVER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ADVANCED_FLUID_RECEIVER = ITEMS.register("advanced_fluid_receiver", () -> new BlockItem(JDTEBlocks.ADVANCED_FLUID_RECEIVER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_FLUID_RECEIVER = ITEMS.register("extended_fluid_receiver", () -> new BlockItem(JDTEBlocks.EXTENDED_FLUID_RECEIVER.get(), new Item.Properties()));

    // Life Extractor
    public static final DeferredHolder<Item, BlockItem> ADVANCED_LIFE_EXTRACTOR = ITEMS.register("advanced_life_extractor", () -> new BlockItem(JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_LIFE_EXTRACTOR = ITEMS.register("extended_life_extractor", () -> new BlockItem(JDTEBlocks.EXTENDED_LIFE_EXTRACTOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, LifeAppleItem> LIFE_APPLE = ITEMS.register("life_apple", LifeAppleItem::new);

    // Infusion Machine
    public static final DeferredHolder<Item, BlockItem> ADVANCED_INFUSION_MACHINE = ITEMS.register("advanced_infusion_machine", () -> new BlockItem(JDTEBlocks.ADVANCED_INFUSION_MACHINE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_INFUSION_MACHINE = ITEMS.register("extended_infusion_machine", () -> new BlockItem(JDTEBlocks.EXTENDED_INFUSION_MACHINE.get(), new Item.Properties()));

    // Potion Brewer
    public static final DeferredHolder<Item, BlockItem> ADVANCED_POTION_BREWER = ITEMS.register("advanced_potion_brewer", () -> new BlockItem(JDTEBlocks.ADVANCED_POTION_BREWER.get(), new Item.Properties()));

    // Bio Crusher
    public static final DeferredHolder<Item, BlockItem> ADVANCED_BIO_CRUSHER = ITEMS.register("advanced_bio_crusher", () -> new BlockItem(JDTEBlocks.ADVANCED_BIO_CRUSHER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_BIO_CRUSHER = ITEMS.register("extended_bio_crusher", () -> new BlockItem(JDTEBlocks.EXTENDED_BIO_CRUSHER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> LOOT_FABRICATOR = ITEMS.register("loot_fabricator", () -> new BlockItem(JDTEBlocks.LOOT_FABRICATOR.get(), new Item.Properties()));

    // Advanced Energy Transmitter
    public static final DeferredHolder<Item, BlockItem> ADVANCED_ENERGY_TRANSMITTER = ITEMS.register("advanced_energy_transmitter", () -> new BlockItem(JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get(), new Item.Properties()));

    // Tiered Solar Panels
    public static final DeferredHolder<Item, BlockItem> CONCENTRATED_SOLAR_PANEL = blockItem(
            "concentrated_solar_panel", JDTEBlocks.CONCENTRATED_SOLAR_PANEL);
    public static final DeferredHolder<Item, BlockItem> SINGULARITY_SOLAR_PANEL = blockItem(
            "singularity_solar_panel", JDTEBlocks.SINGULARITY_SOLAR_PANEL);
    public static final DeferredHolder<Item, BlockItem> STELLAR_FUSION_SOLAR_PANEL = blockItem(
            "stellar_fusion_solar_panel", JDTEBlocks.STELLAR_FUSION_SOLAR_PANEL);
    public static final DeferredHolder<Item, BlockItem> DIMENSIONAL_COLLAPSE_SOLAR_PANEL = blockItem(
            "dimensional_collapse_solar_panel", JDTEBlocks.DIMENSIONAL_COLLAPSE_SOLAR_PANEL);
    public static final DeferredHolder<Item, BlockItem> CREATIVE_SOLAR_PANEL = blockItem(
            "creative_solar_panel", JDTEBlocks.CREATIVE_SOLAR_PANEL);

    // Boss Essences
    public static final DeferredHolder<Item, BossEssenceItem> WITHER_ESSENCE = ITEMS.register("wither_essence", BossEssenceItem::new);
    public static final DeferredHolder<Item, BossEssenceItem> ENDER_DRAGON_ESSENCE = ITEMS.register("ender_dragon_essence", BossEssenceItem::new);
    public static final DeferredHolder<Item, BossEssenceItem> ELDER_GUARDIAN_ESSENCE = ITEMS.register("elder_guardian_essence", BossEssenceItem::new);
    public static final DeferredHolder<Item, DeferredSpawnEggItem> WITHER_SPAWN_EGG = ITEMS.register(
            "wither_spawn_egg",
            () -> new DeferredSpawnEggItem(() -> EntityType.WITHER, 0x2B2B2B, 0x737373, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> ENDER_DRAGON_SPAWN_EGG = ITEMS.register(
            "ender_dragon_spawn_egg",
            () -> new DeferredSpawnEggItem(() -> EntityType.ENDER_DRAGON, 0x161616, 0xE079FA, new Item.Properties()));

    // Bio Crusher Upgrades
    public static final DeferredHolder<Item, LootingUpgradeItem> LOOTING_UPGRADE = ITEMS.register("looting_upgrade", LootingUpgradeItem::new);
    public static final DeferredHolder<Item, SharpnessUpgradeItem> SHARPNESS_UPGRADE = ITEMS.register("sharpness_upgrade", SharpnessUpgradeItem::new);

    public static List<DeferredHolder<Item, UpgradeCardItem>> upgrades() {
        return List.of(CAPACITY_UPGRADE, OVERCLOCK_UPGRADE, UNDERCLOCK_UPGRADE, FLUID_UPGRADE, FLUID_STORAGE_UPGRADE,
                GENERATOR_UPGRADE, RANGE_UPGRADE, FILTER_UPGRADE, CREATIVE_UPGRADE, FORTUNE_UPGRADE, PRECISION_UPGRADE,
                AE_ACCELERATION_UPGRADE, AE_OUTPUT_UPGRADE, ESSENCE_CONVERSION_UPGRADE, SEED_CONVERSION_UPGRADE);
    }

    private static DeferredHolder<Item, BlockItem> blockItem(String name, DeferredHolder<Block, ? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
