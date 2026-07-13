package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.items.BossEssenceItem;
import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.items.ExtendedUpgradeItem;
import com.jdte.common.items.LifeAppleItem;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.SharpnessUpgradeItem;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
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
    public static final DeferredHolder<Item, ExtendedUpgradeItem> EXTENDED_UPGRADE = ITEMS.register("extended_upgrade", ExtendedUpgradeItem::new);
    public static final DeferredHolder<Item, EclipseAlloyWrenchItem> ECLIPSEALLOY_WRENCH = ITEMS.register("eclipsealloy_wrench", EclipseAlloyWrenchItem::new);
    public static final DeferredHolder<Item, Item> TIME_FLUID_CATALYST = ITEMS.register("time_fluid_catalyst", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> BASIC_TIME_ACCELERATOR = ITEMS.register("basic_time_accelerator", () -> new BlockItem(JDTEBlocks.BASIC_TIME_ACCELERATOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ADVANCED_TIME_ACCELERATOR = ITEMS.register("advanced_time_accelerator", () -> new BlockItem(JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> EXTENDED_TIME_ACCELERATOR = ITEMS.register("extended_time_accelerator", () -> new BlockItem(JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get(), new Item.Properties()));
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
                GENERATOR_UPGRADE, RANGE_UPGRADE, FILTER_UPGRADE, CREATIVE_UPGRADE, FORTUNE_UPGRADE);
    }
}
