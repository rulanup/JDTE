package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.containers.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class JDTEMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, JDTE.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<BasicTimeAcceleratorContainer>> BASIC_TIME_ACCELERATOR = MENUS.register(
            "basic_time_accelerator", () -> IMenuTypeExtension.create(BasicTimeAcceleratorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedTimeAcceleratorContainer>> ADVANCED_TIME_ACCELERATOR = MENUS.register(
            "advanced_time_accelerator", () -> IMenuTypeExtension.create(AdvancedTimeAcceleratorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedTimeAcceleratorContainer>> EXTENDED_TIME_ACCELERATOR = MENUS.register(
            "extended_time_accelerator", () -> IMenuTypeExtension.create(ExtendedTimeAcceleratorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedClickerContainer>> EXTENDED_CLICKER = MENUS.register(
            "extended_clicker", () -> IMenuTypeExtension.create(ExtendedClickerContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedBlockBreakerContainer>> EXTENDED_BLOCK_BREAKER = MENUS.register(
            "extended_block_breaker", () -> IMenuTypeExtension.create(ExtendedBlockBreakerContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedBlockPlacerContainer>> EXTENDED_BLOCK_PLACER = MENUS.register(
            "extended_block_placer", () -> IMenuTypeExtension.create(ExtendedBlockPlacerContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedBlockSwapperContainer>> EXTENDED_BLOCK_SWAPPER = MENUS.register(
            "extended_block_swapper", () -> IMenuTypeExtension.create(ExtendedBlockSwapperContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedDropperContainer>> EXTENDED_DROPPER = MENUS.register(
            "extended_dropper", () -> IMenuTypeExtension.create(ExtendedDropperContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedSensorContainer>> EXTENDED_SENSOR = MENUS.register(
            "extended_sensor", () -> IMenuTypeExtension.create(ExtendedSensorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedFluidCollectorContainer>> EXTENDED_FLUID_COLLECTOR = MENUS.register(
            "extended_fluid_collector", () -> IMenuTypeExtension.create(ExtendedFluidCollectorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedFluidPlacerContainer>> EXTENDED_FLUID_PLACER = MENUS.register(
            "extended_fluid_placer", () -> IMenuTypeExtension.create(ExtendedFluidPlacerContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedItemCollectorContainer>> ADVANCED_ITEM_COLLECTOR = MENUS.register(
            "advanced_item_collector", () -> IMenuTypeExtension.create(AdvancedItemCollectorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<EntitySuppressorContainer>> ENTITY_SUPPRESSOR = MENUS.register(
            "entity_suppressor", () -> IMenuTypeExtension.create(EntitySuppressorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<RangeBlockerContainer>> RANGE_BLOCKER = MENUS.register(
            "range_blocker", () -> IMenuTypeExtension.create(RangeBlockerContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<FactoryPackerContainer>> FACTORY_PACKER = MENUS.register(
            "factory_packer", () -> IMenuTypeExtension.create(FactoryPackerContainer::new));

    // Glue Activator
    public static final DeferredHolder<MenuType<?>, MenuType<BasicGlueActivatorContainer>> BASIC_GLUE_ACTIVATOR = MENUS.register(
            "basic_glue_activator", () -> IMenuTypeExtension.create(BasicGlueActivatorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedGlueActivatorContainer>> ADVANCED_GLUE_ACTIVATOR = MENUS.register(
            "advanced_glue_activator", () -> IMenuTypeExtension.create(AdvancedGlueActivatorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedGlueActivatorContainer>> EXTENDED_GLUE_ACTIVATOR = MENUS.register(
            "extended_glue_activator", () -> IMenuTypeExtension.create(ExtendedGlueActivatorContainer::new));

    // Gel Generator
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedGelGeneratorContainer>> ADVANCED_GEL_GENERATOR = MENUS.register(
            "advanced_gel_generator", () -> IMenuTypeExtension.create(AdvancedGelGeneratorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedGelGeneratorContainer>> EXTENDED_GEL_GENERATOR = MENUS.register(
            "extended_gel_generator", () -> IMenuTypeExtension.create(ExtendedGelGeneratorContainer::new));

    // Fluid Stabilizer
    public static final DeferredHolder<MenuType<?>, MenuType<BasicFluidStabilizerContainer>> BASIC_FLUID_STABILIZER = MENUS.register(
            "basic_fluid_stabilizer", () -> IMenuTypeExtension.create(BasicFluidStabilizerContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedFluidStabilizerContainer>> ADVANCED_FLUID_STABILIZER = MENUS.register(
            "advanced_fluid_stabilizer", () -> IMenuTypeExtension.create(AdvancedFluidStabilizerContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedFluidStabilizerContainer>> EXTENDED_FLUID_STABILIZER = MENUS.register(
            "extended_fluid_stabilizer", () -> IMenuTypeExtension.create(ExtendedFluidStabilizerContainer::new));

    // Item Sender
    public static final DeferredHolder<MenuType<?>, MenuType<BasicItemSenderContainer>> BASIC_ITEM_SENDER = MENUS.register(
            "basic_item_sender", () -> IMenuTypeExtension.create(BasicItemSenderContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedItemSenderContainer>> ADVANCED_ITEM_SENDER = MENUS.register(
            "advanced_item_sender", () -> IMenuTypeExtension.create(AdvancedItemSenderContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedItemSenderContainer>> EXTENDED_ITEM_SENDER = MENUS.register(
            "extended_item_sender", () -> IMenuTypeExtension.create(ExtendedItemSenderContainer::new));

    // Fluid Sender
    public static final DeferredHolder<MenuType<?>, MenuType<BasicFluidSenderContainer>> BASIC_FLUID_SENDER = MENUS.register(
            "basic_fluid_sender", () -> IMenuTypeExtension.create(BasicFluidSenderContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedFluidSenderContainer>> ADVANCED_FLUID_SENDER = MENUS.register(
            "advanced_fluid_sender", () -> IMenuTypeExtension.create(AdvancedFluidSenderContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedFluidSenderContainer>> EXTENDED_FLUID_SENDER = MENUS.register(
            "extended_fluid_sender", () -> IMenuTypeExtension.create(ExtendedFluidSenderContainer::new));

    // Item Receiver
    public static final DeferredHolder<MenuType<?>, MenuType<BasicItemReceiverContainer>> BASIC_ITEM_RECEIVER = MENUS.register(
            "basic_item_receiver", () -> IMenuTypeExtension.create(BasicItemReceiverContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedItemReceiverContainer>> ADVANCED_ITEM_RECEIVER = MENUS.register(
            "advanced_item_receiver", () -> IMenuTypeExtension.create(AdvancedItemReceiverContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedItemReceiverContainer>> EXTENDED_ITEM_RECEIVER = MENUS.register(
            "extended_item_receiver", () -> IMenuTypeExtension.create(ExtendedItemReceiverContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<CrystalIncubatorContainer>> CRYSTAL_INCUBATOR = MENUS.register(
            "crystal_incubator", () -> IMenuTypeExtension.create(CrystalIncubatorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<GreenhouseContainer>> GREENHOUSE = MENUS.register(
            "greenhouse", () -> IMenuTypeExtension.create(GreenhouseContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<BioFactoryContainer>> BIO_FACTORY = MENUS.register(
            "bio_factory", () -> IMenuTypeExtension.create(BioFactoryContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<LifeBreederContainer>> LIFE_BREEDER = MENUS.register(
            "life_breeder", () -> IMenuTypeExtension.create(LifeBreederContainer::new));

    // Fluid Receiver
    public static final DeferredHolder<MenuType<?>, MenuType<BasicFluidReceiverContainer>> BASIC_FLUID_RECEIVER = MENUS.register(
            "basic_fluid_receiver", () -> IMenuTypeExtension.create(BasicFluidReceiverContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedFluidReceiverContainer>> ADVANCED_FLUID_RECEIVER = MENUS.register(
            "advanced_fluid_receiver", () -> IMenuTypeExtension.create(AdvancedFluidReceiverContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedFluidReceiverContainer>> EXTENDED_FLUID_RECEIVER = MENUS.register(
            "extended_fluid_receiver", () -> IMenuTypeExtension.create(ExtendedFluidReceiverContainer::new));

    // Life Extractor
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedLifeExtractorContainer>> ADVANCED_LIFE_EXTRACTOR = MENUS.register(
            "advanced_life_extractor", () -> IMenuTypeExtension.create(AdvancedLifeExtractorContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedLifeExtractorContainer>> EXTENDED_LIFE_EXTRACTOR = MENUS.register(
            "extended_life_extractor", () -> IMenuTypeExtension.create(ExtendedLifeExtractorContainer::new));

    // Infusion Machine
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedInfusionMachineContainer>> ADVANCED_INFUSION_MACHINE = MENUS.register(
            "advanced_infusion_machine", () -> IMenuTypeExtension.create(AdvancedInfusionMachineContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedInfusionMachineContainer>> EXTENDED_INFUSION_MACHINE = MENUS.register(
            "extended_infusion_machine", () -> IMenuTypeExtension.create(ExtendedInfusionMachineContainer::new));

    // Potion Brewer
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedPotionBrewerContainer>> ADVANCED_POTION_BREWER = MENUS.register(
            "advanced_potion_brewer", () -> IMenuTypeExtension.create(AdvancedPotionBrewerContainer::new));

    // Bio Crusher
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedBioCrusherContainer>> ADVANCED_BIO_CRUSHER = MENUS.register(
            "advanced_bio_crusher", () -> IMenuTypeExtension.create(AdvancedBioCrusherContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedBioCrusherContainer>> EXTENDED_BIO_CRUSHER = MENUS.register(
            "extended_bio_crusher", () -> IMenuTypeExtension.create(ExtendedBioCrusherContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<LootFabricatorContainer>> LOOT_FABRICATOR = MENUS.register(
            "loot_fabricator", () -> IMenuTypeExtension.create(LootFabricatorContainer::new));
}
