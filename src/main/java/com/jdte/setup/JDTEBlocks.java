package com.jdte.setup;

import com.jdte.JDTE;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import com.jdte.common.blocks.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class JDTEBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(JDTE.MODID);
    public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES =
            DeferredRegister.create(Registries.BLOCK_TYPE, JDTE.MODID);
    public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<DecorativeFumoBlock>> DECORATIVE_FUMO_CODEC =
            BLOCK_TYPES.register("decorative_fumo", () -> DecorativeFumoBlock.CODEC);

    public static final DeferredHolder<Block, BasicTimeAcceleratorBlock> BASIC_TIME_ACCELERATOR = BLOCKS.registerBlock("basic_time_accelerator", BasicTimeAcceleratorBlock::new);
    public static final DeferredHolder<Block, AdvancedTimeAcceleratorBlock> ADVANCED_TIME_ACCELERATOR = BLOCKS.registerBlock("advanced_time_accelerator", AdvancedTimeAcceleratorBlock::new);
    public static final DeferredHolder<Block, ExtendedTimeAcceleratorBlock> EXTENDED_TIME_ACCELERATOR = BLOCKS.registerBlock("extended_time_accelerator", ExtendedTimeAcceleratorBlock::new);
    public static final DeferredHolder<Block, AdvancedEntityAcceleratorBlock> ADVANCED_ENTITY_ACCELERATOR = BLOCKS.registerBlock("advanced_entity_accelerator", AdvancedEntityAcceleratorBlock::new);
    public static final DeferredHolder<Block, ExtendedEntityAcceleratorBlock> EXTENDED_ENTITY_ACCELERATOR = BLOCKS.registerBlock("extended_entity_accelerator", ExtendedEntityAcceleratorBlock::new);
    public static final DeferredHolder<Block, TimeFreezerBlock> TIME_FREEZER = BLOCKS.registerBlock("time_freezer", TimeFreezerBlock::new);
    public static final DeferredHolder<Block, ExtendedTimeFreezerBlock> EXTENDED_TIME_FREEZER = BLOCKS.registerBlock("extended_time_freezer", ExtendedTimeFreezerBlock::new);
    public static final DeferredHolder<Block, ExtendedClickerBlock> EXTENDED_CLICKER = BLOCKS.registerBlock("extended_clicker", ExtendedClickerBlock::new);
    public static final DeferredHolder<Block, ExtendedGeneratorBlock> EXTENDED_GENERATOR = BLOCKS.registerBlock("extended_generator", ExtendedGeneratorBlock::new);
    public static final DeferredHolder<Block, ExtendedExperienceHolderBlock> EXTENDED_EXPERIENCE_HOLDER = BLOCKS.registerBlock("extended_experience_holder", ExtendedExperienceHolderBlock::new);
    public static final DeferredHolder<Block, ExtendedFluidGeneratorBlock> EXTENDED_FLUID_GENERATOR = BLOCKS.registerBlock("extended_fluid_generator", ExtendedFluidGeneratorBlock::new);
    public static final DeferredHolder<Block, ExtendedEnergyTransmitterBlock> EXTENDED_ENERGY_TRANSMITTER = BLOCKS.registerBlock("extended_energy_transmitter", ExtendedEnergyTransmitterBlock::new);
    public static final DeferredHolder<Block, ExtendedBlockBreakerBlock> EXTENDED_BLOCK_BREAKER = BLOCKS.registerBlock("extended_block_breaker", ExtendedBlockBreakerBlock::new);
    public static final DeferredHolder<Block, ExtendedBlockPlacerBlock> EXTENDED_BLOCK_PLACER = BLOCKS.registerBlock("extended_block_placer", ExtendedBlockPlacerBlock::new);
    public static final DeferredHolder<Block, ExtendedBlockSwapperBlock> EXTENDED_BLOCK_SWAPPER = BLOCKS.registerBlock("extended_block_swapper", ExtendedBlockSwapperBlock::new);
    public static final DeferredHolder<Block, ExtendedDropperBlock> EXTENDED_DROPPER = BLOCKS.registerBlock("extended_dropper", ExtendedDropperBlock::new);
    public static final DeferredHolder<Block, ExtendedSensorBlock> EXTENDED_SENSOR = BLOCKS.registerBlock("extended_sensor", ExtendedSensorBlock::new);
    public static final DeferredHolder<Block, ExtendedFluidCollectorBlock> EXTENDED_FLUID_COLLECTOR = BLOCKS.registerBlock("extended_fluid_collector", ExtendedFluidCollectorBlock::new);
    public static final DeferredHolder<Block, ExtendedFluidPlacerBlock> EXTENDED_FLUID_PLACER = BLOCKS.registerBlock("extended_fluid_placer", ExtendedFluidPlacerBlock::new);
    public static final DeferredHolder<Block, AdvancedItemCollectorBlock> ADVANCED_ITEM_COLLECTOR = BLOCKS.registerBlock("advanced_item_collector", AdvancedItemCollectorBlock::new);
    public static final DeferredHolder<Block, EntitySuppressorBlock> ENTITY_SUPPRESSOR = BLOCKS.registerBlock("entity_suppressor", EntitySuppressorBlock::new);
    public static final DeferredHolder<Block, RangeBlockerBlock> RANGE_BLOCKER = BLOCKS.registerBlock("range_blocker", RangeBlockerBlock::new);
    public static final DeferredHolder<Block, CreativeEnergyCellBlock> CREATIVE_ENERGY_CELL = BLOCKS.registerBlock("creative_energy_cell", CreativeEnergyCellBlock::new);
    public static final DeferredHolder<Block, ChargedLightningRodBlock> CHARGED_LIGHTNING_ROD = BLOCKS.registerBlock("charged_lightning_rod", ChargedLightningRodBlock::new);
    public static final DeferredHolder<Block, CrystalIncubatorBlock> CRYSTAL_INCUBATOR = BLOCKS.registerBlock("crystal_incubator", CrystalIncubatorBlock::new);
    public static final DeferredHolder<Block, GreenhouseBlock> GREENHOUSE = BLOCKS.registerBlock("greenhouse", GreenhouseBlock::new);
    public static final DeferredHolder<Block, LargeGreenhouseBlock> LARGE_GREENHOUSE = BLOCKS.registerBlock("large_greenhouse", LargeGreenhouseBlock::new);
    public static final DeferredHolder<Block, LargeGreenhousePartBlock> LARGE_GREENHOUSE_PART = BLOCKS.registerBlock("large_greenhouse_part", LargeGreenhousePartBlock::new);
    public static final DeferredHolder<Block, LifeSynthesisVatBlock> LIFE_SYNTHESIS_VAT = BLOCKS.registerBlock("life_synthesis_vat", LifeSynthesisVatBlock::new);
    public static final DeferredHolder<Block, LifeSynthesisPartBlock> LIFE_SYNTHESIS_PART = BLOCKS.registerBlock("life_synthesis_part", LifeSynthesisPartBlock::new);
    public static final DeferredHolder<Block, BioFactoryBlock> BIO_FACTORY = BLOCKS.registerBlock("bio_factory", BioFactoryBlock::new);
    public static final DeferredHolder<Block, LifeBreederBlock> LIFE_BREEDER = BLOCKS.registerBlock("life_breeder", LifeBreederBlock::new);
    public static final DeferredHolder<Block, FactoryPackerBlock> FACTORY_PACKER = BLOCKS.registerBlock("factory_packer", FactoryPackerBlock::new);
    public static final DeferredHolder<Block, MineralExtractorBlock> MINERAL_EXTRACTOR = BLOCKS.registerBlock("mineral_extractor", MineralExtractorBlock::new);
    public static final DeferredHolder<Block, LargeMineralExtractorBlock> LARGE_MINERAL_EXTRACTOR = BLOCKS.registerBlock("large_mineral_extractor", LargeMineralExtractorBlock::new);
    public static final DeferredHolder<Block, LargeMineralExtractorPartBlock> LARGE_MINERAL_EXTRACTOR_PART = BLOCKS.registerBlock("large_mineral_extractor_part", LargeMineralExtractorPartBlock::new);
    public static final DeferredHolder<Block, DecorativeFumoBlock> XINGYE_FUMO = BLOCKS.registerBlock("xingye_fumo", DecorativeFumoBlock::create);
    public static final DeferredHolder<Block, DecorativeFumoBlock> CAINIAOYO_FUMO = BLOCKS.registerBlock("cainiaoyo_fumo", DecorativeFumoBlock::create);
    public static final DeferredHolder<Block, DecorativeFumoBlock> MAIQILIN_FUMO = BLOCKS.registerBlock("maiqilin_fumo", DecorativeFumoBlock::create);

    // Tiered Solar Panels
    // Glue Activator
    public static final DeferredHolder<Block, BasicGlueActivatorBlock> BASIC_GLUE_ACTIVATOR = BLOCKS.registerBlock("basic_glue_activator", BasicGlueActivatorBlock::new);
    public static final DeferredHolder<Block, AdvancedGlueActivatorBlock> ADVANCED_GLUE_ACTIVATOR = BLOCKS.registerBlock("advanced_glue_activator", AdvancedGlueActivatorBlock::new);
    public static final DeferredHolder<Block, ExtendedGlueActivatorBlock> EXTENDED_GLUE_ACTIVATOR = BLOCKS.registerBlock("extended_glue_activator", ExtendedGlueActivatorBlock::new);

    // Gel Generator
    public static final DeferredHolder<Block, AdvancedGelGeneratorBlock> ADVANCED_GEL_GENERATOR = BLOCKS.registerBlock("advanced_gel_generator", AdvancedGelGeneratorBlock::new);
    public static final DeferredHolder<Block, ExtendedGelGeneratorBlock> EXTENDED_GEL_GENERATOR = BLOCKS.registerBlock("extended_gel_generator", ExtendedGelGeneratorBlock::new);

    // Fluid Stabilizer
    public static final DeferredHolder<Block, BasicFluidStabilizerBlock> BASIC_FLUID_STABILIZER = BLOCKS.registerBlock("basic_fluid_stabilizer", BasicFluidStabilizerBlock::new);
    public static final DeferredHolder<Block, AdvancedFluidStabilizerBlock> ADVANCED_FLUID_STABILIZER = BLOCKS.registerBlock("advanced_fluid_stabilizer", AdvancedFluidStabilizerBlock::new);
    public static final DeferredHolder<Block, ExtendedFluidStabilizerBlock> EXTENDED_FLUID_STABILIZER = BLOCKS.registerBlock("extended_fluid_stabilizer", ExtendedFluidStabilizerBlock::new);

    // Item Sender
    public static final DeferredHolder<Block, BasicItemSenderBlock> BASIC_ITEM_SENDER = BLOCKS.registerBlock("basic_item_sender", BasicItemSenderBlock::new);
    public static final DeferredHolder<Block, AdvancedItemSenderBlock> ADVANCED_ITEM_SENDER = BLOCKS.registerBlock("advanced_item_sender", AdvancedItemSenderBlock::new);
    public static final DeferredHolder<Block, ExtendedItemSenderBlock> EXTENDED_ITEM_SENDER = BLOCKS.registerBlock("extended_item_sender", ExtendedItemSenderBlock::new);

    // Fluid Sender
    public static final DeferredHolder<Block, BasicFluidSenderBlock> BASIC_FLUID_SENDER = BLOCKS.registerBlock("basic_fluid_sender", BasicFluidSenderBlock::new);
    public static final DeferredHolder<Block, AdvancedFluidSenderBlock> ADVANCED_FLUID_SENDER = BLOCKS.registerBlock("advanced_fluid_sender", AdvancedFluidSenderBlock::new);
    public static final DeferredHolder<Block, ExtendedFluidSenderBlock> EXTENDED_FLUID_SENDER = BLOCKS.registerBlock("extended_fluid_sender", ExtendedFluidSenderBlock::new);

    // Item Receiver
    public static final DeferredHolder<Block, BasicItemReceiverBlock> BASIC_ITEM_RECEIVER = BLOCKS.registerBlock("basic_item_receiver", BasicItemReceiverBlock::new);
    public static final DeferredHolder<Block, AdvancedItemReceiverBlock> ADVANCED_ITEM_RECEIVER = BLOCKS.registerBlock("advanced_item_receiver", AdvancedItemReceiverBlock::new);
    public static final DeferredHolder<Block, ExtendedItemReceiverBlock> EXTENDED_ITEM_RECEIVER = BLOCKS.registerBlock("extended_item_receiver", ExtendedItemReceiverBlock::new);

    // Fluid Receiver
    public static final DeferredHolder<Block, BasicFluidReceiverBlock> BASIC_FLUID_RECEIVER = BLOCKS.registerBlock("basic_fluid_receiver", BasicFluidReceiverBlock::new);
    public static final DeferredHolder<Block, AdvancedFluidReceiverBlock> ADVANCED_FLUID_RECEIVER = BLOCKS.registerBlock("advanced_fluid_receiver", AdvancedFluidReceiverBlock::new);
    public static final DeferredHolder<Block, ExtendedFluidReceiverBlock> EXTENDED_FLUID_RECEIVER = BLOCKS.registerBlock("extended_fluid_receiver", ExtendedFluidReceiverBlock::new);

    // Life Extractor
    public static final DeferredHolder<Block, AdvancedLifeExtractorBlock> ADVANCED_LIFE_EXTRACTOR = BLOCKS.registerBlock("advanced_life_extractor", AdvancedLifeExtractorBlock::new);
    public static final DeferredHolder<Block, ExtendedLifeExtractorBlock> EXTENDED_LIFE_EXTRACTOR = BLOCKS.registerBlock("extended_life_extractor", ExtendedLifeExtractorBlock::new);

    // Bio Extractor
    public static final DeferredHolder<Block, AdvancedBioExtractorBlock> ADVANCED_BIO_EXTRACTOR = BLOCKS.registerBlock("advanced_bio_extractor", AdvancedBioExtractorBlock::new);
    public static final DeferredHolder<Block, ExtendedBioExtractorBlock> EXTENDED_BIO_EXTRACTOR = BLOCKS.registerBlock("extended_bio_extractor", ExtendedBioExtractorBlock::new);

    // Infusion Machine
    public static final DeferredHolder<Block, AdvancedInfusionMachineBlock> ADVANCED_INFUSION_MACHINE = BLOCKS.registerBlock("advanced_infusion_machine", AdvancedInfusionMachineBlock::new);
    public static final DeferredHolder<Block, ExtendedInfusionMachineBlock> EXTENDED_INFUSION_MACHINE = BLOCKS.registerBlock("extended_infusion_machine", ExtendedInfusionMachineBlock::new);

    // Potion Brewer
    public static final DeferredHolder<Block, AdvancedPotionBrewerBlock> ADVANCED_POTION_BREWER = BLOCKS.registerBlock("advanced_potion_brewer", AdvancedPotionBrewerBlock::new);

    // Bio Crusher
    public static final DeferredHolder<Block, AdvancedBioCrusherBlock> ADVANCED_BIO_CRUSHER = BLOCKS.registerBlock("advanced_bio_crusher", AdvancedBioCrusherBlock::new);
    public static final DeferredHolder<Block, ExtendedBioCrusherBlock> EXTENDED_BIO_CRUSHER = BLOCKS.registerBlock("extended_bio_crusher", ExtendedBioCrusherBlock::new);
    public static final DeferredHolder<Block, LootFabricatorBlock> LOOT_FABRICATOR = BLOCKS.registerBlock("loot_fabricator", LootFabricatorBlock::new);

    // Advanced Energy Transmitter
    public static final DeferredHolder<Block, AdvancedEnergyTransmitterBlock> ADVANCED_ENERGY_TRANSMITTER = BLOCKS.registerBlock("advanced_energy_transmitter", AdvancedEnergyTransmitterBlock::new);

    // Fluid Mixer
    public static final DeferredHolder<Block, AdvancedFluidMixerBlock> ADVANCED_FLUID_MIXER = BLOCKS.registerBlock("advanced_fluid_mixer", AdvancedFluidMixerBlock::new);
    public static final DeferredHolder<Block, ExtendedFluidMixerBlock> EXTENDED_FLUID_MIXER = BLOCKS.registerBlock("extended_fluid_mixer", ExtendedFluidMixerBlock::new);
}
