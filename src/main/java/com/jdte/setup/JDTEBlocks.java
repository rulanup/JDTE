package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.blocks.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class JDTEBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, JDTE.MODID);

    public static final RegistryObject<BasicTimeAcceleratorBlock> BASIC_TIME_ACCELERATOR = BLOCKS.register("basic_time_accelerator", BasicTimeAcceleratorBlock::new);
    public static final RegistryObject<AdvancedTimeAcceleratorBlock> ADVANCED_TIME_ACCELERATOR = BLOCKS.register("advanced_time_accelerator", AdvancedTimeAcceleratorBlock::new);
    public static final RegistryObject<ExtendedTimeAcceleratorBlock> EXTENDED_TIME_ACCELERATOR = BLOCKS.register("extended_time_accelerator", ExtendedTimeAcceleratorBlock::new);
    public static final RegistryObject<TimeFreezerBlock> TIME_FREEZER = BLOCKS.register("time_freezer", TimeFreezerBlock::new);
    public static final RegistryObject<ExtendedTimeFreezerBlock> EXTENDED_TIME_FREEZER = BLOCKS.register("extended_time_freezer", ExtendedTimeFreezerBlock::new);
    public static final RegistryObject<ExtendedClickerBlock> EXTENDED_CLICKER = BLOCKS.register("extended_clicker", ExtendedClickerBlock::new);
    public static final RegistryObject<ExtendedBlockBreakerBlock> EXTENDED_BLOCK_BREAKER = BLOCKS.register("extended_block_breaker", ExtendedBlockBreakerBlock::new);
    public static final RegistryObject<ExtendedBlockPlacerBlock> EXTENDED_BLOCK_PLACER = BLOCKS.register("extended_block_placer", ExtendedBlockPlacerBlock::new);
    public static final RegistryObject<ExtendedBlockSwapperBlock> EXTENDED_BLOCK_SWAPPER = BLOCKS.register("extended_block_swapper", ExtendedBlockSwapperBlock::new);
    public static final RegistryObject<ExtendedDropperBlock> EXTENDED_DROPPER = BLOCKS.register("extended_dropper", ExtendedDropperBlock::new);
    public static final RegistryObject<ExtendedSensorBlock> EXTENDED_SENSOR = BLOCKS.register("extended_sensor", ExtendedSensorBlock::new);
    public static final RegistryObject<ExtendedFluidCollectorBlock> EXTENDED_FLUID_COLLECTOR = BLOCKS.register("extended_fluid_collector", ExtendedFluidCollectorBlock::new);
    public static final RegistryObject<ExtendedFluidPlacerBlock> EXTENDED_FLUID_PLACER = BLOCKS.register("extended_fluid_placer", ExtendedFluidPlacerBlock::new);
    public static final RegistryObject<AdvancedItemCollectorBlock> ADVANCED_ITEM_COLLECTOR = BLOCKS.register("advanced_item_collector", AdvancedItemCollectorBlock::new);
    public static final RegistryObject<EntitySuppressorBlock> ENTITY_SUPPRESSOR = BLOCKS.register("entity_suppressor", EntitySuppressorBlock::new);
    public static final RegistryObject<RangeBlockerBlock> RANGE_BLOCKER = BLOCKS.register("range_blocker", RangeBlockerBlock::new);
    public static final RegistryObject<CrystalIncubatorBlock> CRYSTAL_INCUBATOR = BLOCKS.register("crystal_incubator", CrystalIncubatorBlock::new);
    public static final RegistryObject<GreenhouseBlock> GREENHOUSE = BLOCKS.register("greenhouse", GreenhouseBlock::new);
    public static final RegistryObject<LargeGreenhouseBlock> LARGE_GREENHOUSE = BLOCKS.register("large_greenhouse", LargeGreenhouseBlock::new);
    public static final RegistryObject<LargeGreenhousePartBlock> LARGE_GREENHOUSE_PART = BLOCKS.register("large_greenhouse_part", LargeGreenhousePartBlock::new);
    public static final RegistryObject<LifeSynthesisVatBlock> LIFE_SYNTHESIS_VAT = BLOCKS.register("life_synthesis_vat", LifeSynthesisVatBlock::new);
    public static final RegistryObject<LifeSynthesisPartBlock> LIFE_SYNTHESIS_PART = BLOCKS.register("life_synthesis_part", LifeSynthesisPartBlock::new);
    public static final RegistryObject<BioFactoryBlock> BIO_FACTORY = BLOCKS.register("bio_factory", BioFactoryBlock::new);
    public static final RegistryObject<LifeBreederBlock> LIFE_BREEDER = BLOCKS.register("life_breeder", LifeBreederBlock::new);
    public static final RegistryObject<FactoryPackerBlock> FACTORY_PACKER = BLOCKS.register("factory_packer", FactoryPackerBlock::new);

    // Glue Activator
    public static final RegistryObject<BasicGlueActivatorBlock> BASIC_GLUE_ACTIVATOR = BLOCKS.register("basic_glue_activator", BasicGlueActivatorBlock::new);
    public static final RegistryObject<AdvancedGlueActivatorBlock> ADVANCED_GLUE_ACTIVATOR = BLOCKS.register("advanced_glue_activator", AdvancedGlueActivatorBlock::new);
    public static final RegistryObject<ExtendedGlueActivatorBlock> EXTENDED_GLUE_ACTIVATOR = BLOCKS.register("extended_glue_activator", ExtendedGlueActivatorBlock::new);

    // Gel Generator
    public static final RegistryObject<AdvancedGelGeneratorBlock> ADVANCED_GEL_GENERATOR = BLOCKS.register("advanced_gel_generator", AdvancedGelGeneratorBlock::new);
    public static final RegistryObject<ExtendedGelGeneratorBlock> EXTENDED_GEL_GENERATOR = BLOCKS.register("extended_gel_generator", ExtendedGelGeneratorBlock::new);

    // Fluid Stabilizer
    public static final RegistryObject<BasicFluidStabilizerBlock> BASIC_FLUID_STABILIZER = BLOCKS.register("basic_fluid_stabilizer", BasicFluidStabilizerBlock::new);
    public static final RegistryObject<AdvancedFluidStabilizerBlock> ADVANCED_FLUID_STABILIZER = BLOCKS.register("advanced_fluid_stabilizer", AdvancedFluidStabilizerBlock::new);
    public static final RegistryObject<ExtendedFluidStabilizerBlock> EXTENDED_FLUID_STABILIZER = BLOCKS.register("extended_fluid_stabilizer", ExtendedFluidStabilizerBlock::new);

    // Item Sender
    public static final RegistryObject<BasicItemSenderBlock> BASIC_ITEM_SENDER = BLOCKS.register("basic_item_sender", BasicItemSenderBlock::new);
    public static final RegistryObject<AdvancedItemSenderBlock> ADVANCED_ITEM_SENDER = BLOCKS.register("advanced_item_sender", AdvancedItemSenderBlock::new);
    public static final RegistryObject<ExtendedItemSenderBlock> EXTENDED_ITEM_SENDER = BLOCKS.register("extended_item_sender", ExtendedItemSenderBlock::new);

    // Fluid Sender
    public static final RegistryObject<BasicFluidSenderBlock> BASIC_FLUID_SENDER = BLOCKS.register("basic_fluid_sender", BasicFluidSenderBlock::new);
    public static final RegistryObject<AdvancedFluidSenderBlock> ADVANCED_FLUID_SENDER = BLOCKS.register("advanced_fluid_sender", AdvancedFluidSenderBlock::new);
    public static final RegistryObject<ExtendedFluidSenderBlock> EXTENDED_FLUID_SENDER = BLOCKS.register("extended_fluid_sender", ExtendedFluidSenderBlock::new);

    // Item Receiver
    public static final RegistryObject<BasicItemReceiverBlock> BASIC_ITEM_RECEIVER = BLOCKS.register("basic_item_receiver", BasicItemReceiverBlock::new);
    public static final RegistryObject<AdvancedItemReceiverBlock> ADVANCED_ITEM_RECEIVER = BLOCKS.register("advanced_item_receiver", AdvancedItemReceiverBlock::new);
    public static final RegistryObject<ExtendedItemReceiverBlock> EXTENDED_ITEM_RECEIVER = BLOCKS.register("extended_item_receiver", ExtendedItemReceiverBlock::new);

    // Fluid Receiver
    public static final RegistryObject<BasicFluidReceiverBlock> BASIC_FLUID_RECEIVER = BLOCKS.register("basic_fluid_receiver", BasicFluidReceiverBlock::new);
    public static final RegistryObject<AdvancedFluidReceiverBlock> ADVANCED_FLUID_RECEIVER = BLOCKS.register("advanced_fluid_receiver", AdvancedFluidReceiverBlock::new);
    public static final RegistryObject<ExtendedFluidReceiverBlock> EXTENDED_FLUID_RECEIVER = BLOCKS.register("extended_fluid_receiver", ExtendedFluidReceiverBlock::new);

    // Life Extractor
    public static final RegistryObject<AdvancedLifeExtractorBlock> ADVANCED_LIFE_EXTRACTOR = BLOCKS.register("advanced_life_extractor", AdvancedLifeExtractorBlock::new);
    public static final RegistryObject<ExtendedLifeExtractorBlock> EXTENDED_LIFE_EXTRACTOR = BLOCKS.register("extended_life_extractor", ExtendedLifeExtractorBlock::new);

    // Infusion Machine
    public static final RegistryObject<AdvancedInfusionMachineBlock> ADVANCED_INFUSION_MACHINE = BLOCKS.register("advanced_infusion_machine", AdvancedInfusionMachineBlock::new);
    public static final RegistryObject<ExtendedInfusionMachineBlock> EXTENDED_INFUSION_MACHINE = BLOCKS.register("extended_infusion_machine", ExtendedInfusionMachineBlock::new);

    // Potion Brewer
    public static final RegistryObject<AdvancedPotionBrewerBlock> ADVANCED_POTION_BREWER = BLOCKS.register("advanced_potion_brewer", AdvancedPotionBrewerBlock::new);

    // Bio Crusher
    public static final RegistryObject<AdvancedBioCrusherBlock> ADVANCED_BIO_CRUSHER = BLOCKS.register("advanced_bio_crusher", AdvancedBioCrusherBlock::new);
    public static final RegistryObject<ExtendedBioCrusherBlock> EXTENDED_BIO_CRUSHER = BLOCKS.register("extended_bio_crusher", ExtendedBioCrusherBlock::new);
    public static final RegistryObject<LootFabricatorBlock> LOOT_FABRICATOR = BLOCKS.register("loot_fabricator", LootFabricatorBlock::new);
}
