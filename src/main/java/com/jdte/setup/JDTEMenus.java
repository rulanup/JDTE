package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.containers.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class JDTEMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, JDTE.MODID);

    public static final RegistryObject<MenuType<BasicTimeAcceleratorContainer>> BASIC_TIME_ACCELERATOR = MENUS.register(
            "basic_time_accelerator", () -> IForgeMenuType.create(BasicTimeAcceleratorContainer::new));
    public static final RegistryObject<MenuType<AdvancedTimeAcceleratorContainer>> ADVANCED_TIME_ACCELERATOR = MENUS.register(
            "advanced_time_accelerator", () -> IForgeMenuType.create(AdvancedTimeAcceleratorContainer::new));
    public static final RegistryObject<MenuType<ExtendedTimeAcceleratorContainer>> EXTENDED_TIME_ACCELERATOR = MENUS.register(
            "extended_time_accelerator", () -> IForgeMenuType.create(ExtendedTimeAcceleratorContainer::new));
    public static final RegistryObject<MenuType<ExtendedClickerContainer>> EXTENDED_CLICKER = MENUS.register(
            "extended_clicker", () -> IForgeMenuType.create(ExtendedClickerContainer::new));
    public static final RegistryObject<MenuType<ExtendedBlockBreakerContainer>> EXTENDED_BLOCK_BREAKER = MENUS.register(
            "extended_block_breaker", () -> IForgeMenuType.create(ExtendedBlockBreakerContainer::new));
    public static final RegistryObject<MenuType<ExtendedBlockPlacerContainer>> EXTENDED_BLOCK_PLACER = MENUS.register(
            "extended_block_placer", () -> IForgeMenuType.create(ExtendedBlockPlacerContainer::new));
    public static final RegistryObject<MenuType<ExtendedBlockSwapperContainer>> EXTENDED_BLOCK_SWAPPER = MENUS.register(
            "extended_block_swapper", () -> IForgeMenuType.create(ExtendedBlockSwapperContainer::new));
    public static final RegistryObject<MenuType<ExtendedDropperContainer>> EXTENDED_DROPPER = MENUS.register(
            "extended_dropper", () -> IForgeMenuType.create(ExtendedDropperContainer::new));
    public static final RegistryObject<MenuType<ExtendedSensorContainer>> EXTENDED_SENSOR = MENUS.register(
            "extended_sensor", () -> IForgeMenuType.create(ExtendedSensorContainer::new));
    public static final RegistryObject<MenuType<ExtendedFluidCollectorContainer>> EXTENDED_FLUID_COLLECTOR = MENUS.register(
            "extended_fluid_collector", () -> IForgeMenuType.create(ExtendedFluidCollectorContainer::new));
    public static final RegistryObject<MenuType<ExtendedFluidPlacerContainer>> EXTENDED_FLUID_PLACER = MENUS.register(
            "extended_fluid_placer", () -> IForgeMenuType.create(ExtendedFluidPlacerContainer::new));
    public static final RegistryObject<MenuType<AdvancedItemCollectorContainer>> ADVANCED_ITEM_COLLECTOR = MENUS.register(
            "advanced_item_collector", () -> IForgeMenuType.create(AdvancedItemCollectorContainer::new));
    public static final RegistryObject<MenuType<EntitySuppressorContainer>> ENTITY_SUPPRESSOR = MENUS.register(
            "entity_suppressor", () -> IForgeMenuType.create(EntitySuppressorContainer::new));
    public static final RegistryObject<MenuType<RangeBlockerContainer>> RANGE_BLOCKER = MENUS.register(
            "range_blocker", () -> IForgeMenuType.create(RangeBlockerContainer::new));
    public static final RegistryObject<MenuType<FactoryPackerContainer>> FACTORY_PACKER = MENUS.register(
            "factory_packer", () -> IForgeMenuType.create(FactoryPackerContainer::new));

    // Glue Activator
    public static final RegistryObject<MenuType<BasicGlueActivatorContainer>> BASIC_GLUE_ACTIVATOR = MENUS.register(
            "basic_glue_activator", () -> IForgeMenuType.create(BasicGlueActivatorContainer::new));
    public static final RegistryObject<MenuType<AdvancedGlueActivatorContainer>> ADVANCED_GLUE_ACTIVATOR = MENUS.register(
            "advanced_glue_activator", () -> IForgeMenuType.create(AdvancedGlueActivatorContainer::new));
    public static final RegistryObject<MenuType<ExtendedGlueActivatorContainer>> EXTENDED_GLUE_ACTIVATOR = MENUS.register(
            "extended_glue_activator", () -> IForgeMenuType.create(ExtendedGlueActivatorContainer::new));

    // Gel Generator
    public static final RegistryObject<MenuType<AdvancedGelGeneratorContainer>> ADVANCED_GEL_GENERATOR = MENUS.register(
            "advanced_gel_generator", () -> IForgeMenuType.create(AdvancedGelGeneratorContainer::new));
    public static final RegistryObject<MenuType<ExtendedGelGeneratorContainer>> EXTENDED_GEL_GENERATOR = MENUS.register(
            "extended_gel_generator", () -> IForgeMenuType.create(ExtendedGelGeneratorContainer::new));

    // Fluid Stabilizer
    public static final RegistryObject<MenuType<BasicFluidStabilizerContainer>> BASIC_FLUID_STABILIZER = MENUS.register(
            "basic_fluid_stabilizer", () -> IForgeMenuType.create(BasicFluidStabilizerContainer::new));
    public static final RegistryObject<MenuType<AdvancedFluidStabilizerContainer>> ADVANCED_FLUID_STABILIZER = MENUS.register(
            "advanced_fluid_stabilizer", () -> IForgeMenuType.create(AdvancedFluidStabilizerContainer::new));
    public static final RegistryObject<MenuType<ExtendedFluidStabilizerContainer>> EXTENDED_FLUID_STABILIZER = MENUS.register(
            "extended_fluid_stabilizer", () -> IForgeMenuType.create(ExtendedFluidStabilizerContainer::new));

    // Item Sender
    public static final RegistryObject<MenuType<BasicItemSenderContainer>> BASIC_ITEM_SENDER = MENUS.register(
            "basic_item_sender", () -> IForgeMenuType.create(BasicItemSenderContainer::new));
    public static final RegistryObject<MenuType<AdvancedItemSenderContainer>> ADVANCED_ITEM_SENDER = MENUS.register(
            "advanced_item_sender", () -> IForgeMenuType.create(AdvancedItemSenderContainer::new));
    public static final RegistryObject<MenuType<ExtendedItemSenderContainer>> EXTENDED_ITEM_SENDER = MENUS.register(
            "extended_item_sender", () -> IForgeMenuType.create(ExtendedItemSenderContainer::new));

    // Fluid Sender
    public static final RegistryObject<MenuType<BasicFluidSenderContainer>> BASIC_FLUID_SENDER = MENUS.register(
            "basic_fluid_sender", () -> IForgeMenuType.create(BasicFluidSenderContainer::new));
    public static final RegistryObject<MenuType<AdvancedFluidSenderContainer>> ADVANCED_FLUID_SENDER = MENUS.register(
            "advanced_fluid_sender", () -> IForgeMenuType.create(AdvancedFluidSenderContainer::new));
    public static final RegistryObject<MenuType<ExtendedFluidSenderContainer>> EXTENDED_FLUID_SENDER = MENUS.register(
            "extended_fluid_sender", () -> IForgeMenuType.create(ExtendedFluidSenderContainer::new));

    // Item Receiver
    public static final RegistryObject<MenuType<BasicItemReceiverContainer>> BASIC_ITEM_RECEIVER = MENUS.register(
            "basic_item_receiver", () -> IForgeMenuType.create(BasicItemReceiverContainer::new));
    public static final RegistryObject<MenuType<AdvancedItemReceiverContainer>> ADVANCED_ITEM_RECEIVER = MENUS.register(
            "advanced_item_receiver", () -> IForgeMenuType.create(AdvancedItemReceiverContainer::new));
    public static final RegistryObject<MenuType<ExtendedItemReceiverContainer>> EXTENDED_ITEM_RECEIVER = MENUS.register(
            "extended_item_receiver", () -> IForgeMenuType.create(ExtendedItemReceiverContainer::new));
    public static final RegistryObject<MenuType<CrystalIncubatorContainer>> CRYSTAL_INCUBATOR = MENUS.register(
            "crystal_incubator", () -> IForgeMenuType.create(CrystalIncubatorContainer::new));
    public static final RegistryObject<MenuType<GreenhouseContainer>> GREENHOUSE = MENUS.register(
            "greenhouse", () -> IForgeMenuType.create(GreenhouseContainer::new));
    public static final RegistryObject<MenuType<LargeGreenhouseContainer>> LARGE_GREENHOUSE = MENUS.register(
            "large_greenhouse", () -> IForgeMenuType.create(LargeGreenhouseContainer::new));
    public static final RegistryObject<MenuType<LifeSynthesisContainer>> LIFE_SYNTHESIS_VAT = MENUS.register(
            "life_synthesis_vat", () -> IForgeMenuType.create(LifeSynthesisContainer::new));
    public static final RegistryObject<MenuType<BioFactoryContainer>> BIO_FACTORY = MENUS.register(
            "bio_factory", () -> IForgeMenuType.create(BioFactoryContainer::new));
    public static final RegistryObject<MenuType<LifeBreederContainer>> LIFE_BREEDER = MENUS.register(
            "life_breeder", () -> IForgeMenuType.create(LifeBreederContainer::new));

    // Fluid Receiver
    public static final RegistryObject<MenuType<BasicFluidReceiverContainer>> BASIC_FLUID_RECEIVER = MENUS.register(
            "basic_fluid_receiver", () -> IForgeMenuType.create(BasicFluidReceiverContainer::new));
    public static final RegistryObject<MenuType<AdvancedFluidReceiverContainer>> ADVANCED_FLUID_RECEIVER = MENUS.register(
            "advanced_fluid_receiver", () -> IForgeMenuType.create(AdvancedFluidReceiverContainer::new));
    public static final RegistryObject<MenuType<ExtendedFluidReceiverContainer>> EXTENDED_FLUID_RECEIVER = MENUS.register(
            "extended_fluid_receiver", () -> IForgeMenuType.create(ExtendedFluidReceiverContainer::new));

    // Life Extractor
    public static final RegistryObject<MenuType<AdvancedLifeExtractorContainer>> ADVANCED_LIFE_EXTRACTOR = MENUS.register(
            "advanced_life_extractor", () -> IForgeMenuType.create(AdvancedLifeExtractorContainer::new));
    public static final RegistryObject<MenuType<ExtendedLifeExtractorContainer>> EXTENDED_LIFE_EXTRACTOR = MENUS.register(
            "extended_life_extractor", () -> IForgeMenuType.create(ExtendedLifeExtractorContainer::new));

    // Infusion Machine
    public static final RegistryObject<MenuType<AdvancedInfusionMachineContainer>> ADVANCED_INFUSION_MACHINE = MENUS.register(
            "advanced_infusion_machine", () -> IForgeMenuType.create(AdvancedInfusionMachineContainer::new));
    public static final RegistryObject<MenuType<ExtendedInfusionMachineContainer>> EXTENDED_INFUSION_MACHINE = MENUS.register(
            "extended_infusion_machine", () -> IForgeMenuType.create(ExtendedInfusionMachineContainer::new));

    // Potion Brewer
    public static final RegistryObject<MenuType<AdvancedPotionBrewerContainer>> ADVANCED_POTION_BREWER = MENUS.register(
            "advanced_potion_brewer", () -> IForgeMenuType.create(AdvancedPotionBrewerContainer::new));

    // Bio Crusher
    public static final RegistryObject<MenuType<AdvancedBioCrusherContainer>> ADVANCED_BIO_CRUSHER = MENUS.register(
            "advanced_bio_crusher", () -> IForgeMenuType.create(AdvancedBioCrusherContainer::new));
    public static final RegistryObject<MenuType<ExtendedBioCrusherContainer>> EXTENDED_BIO_CRUSHER = MENUS.register(
            "extended_bio_crusher", () -> IForgeMenuType.create(ExtendedBioCrusherContainer::new));
    public static final RegistryObject<MenuType<LootFabricatorContainer>> LOOT_FABRICATOR = MENUS.register(
            "loot_fabricator", () -> IForgeMenuType.create(LootFabricatorContainer::new));
}
