package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.blockentities.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class JDTEBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, JDTE.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicTimeAcceleratorBE>> BASIC_TIME_ACCELERATOR = BLOCK_ENTITIES.register(
            "basic_time_accelerator", () -> new BlockEntityType<>(BasicTimeAcceleratorBE::new, JDTEBlocks.BASIC_TIME_ACCELERATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedTimeAcceleratorBE>> ADVANCED_TIME_ACCELERATOR = BLOCK_ENTITIES.register(
            "advanced_time_accelerator", () -> new BlockEntityType<>(AdvancedTimeAcceleratorBE::new, JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedTimeAcceleratorBE>> EXTENDED_TIME_ACCELERATOR = BLOCK_ENTITIES.register(
            "extended_time_accelerator", () -> new BlockEntityType<>(ExtendedTimeAcceleratorBE::new, JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedEntityAcceleratorBE>> ADVANCED_ENTITY_ACCELERATOR = BLOCK_ENTITIES.register(
            "advanced_entity_accelerator", () -> new BlockEntityType<>(AdvancedEntityAcceleratorBE::new, JDTEBlocks.ADVANCED_ENTITY_ACCELERATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedEntityAcceleratorBE>> EXTENDED_ENTITY_ACCELERATOR = BLOCK_ENTITIES.register(
            "extended_entity_accelerator", () -> new BlockEntityType<>(ExtendedEntityAcceleratorBE::new, JDTEBlocks.EXTENDED_ENTITY_ACCELERATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TimeFreezerBE>> TIME_FREEZER = BLOCK_ENTITIES.register(
            "time_freezer", () -> new BlockEntityType<>(TimeFreezerBE::new, JDTEBlocks.TIME_FREEZER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeEnergyCellBE>> CREATIVE_ENERGY_CELL = BLOCK_ENTITIES.register(
            "creative_energy_cell", () -> new BlockEntityType<>(CreativeEnergyCellBE::new, JDTEBlocks.CREATIVE_ENERGY_CELL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChargedLightningRodBE>> CHARGED_LIGHTNING_ROD = BLOCK_ENTITIES.register(
            "charged_lightning_rod", () -> new BlockEntityType<>(ChargedLightningRodBE::new, JDTEBlocks.CHARGED_LIGHTNING_ROD.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedTimeFreezerBE>> EXTENDED_TIME_FREEZER = BLOCK_ENTITIES.register(
            "extended_time_freezer", () -> new BlockEntityType<>(ExtendedTimeFreezerBE::new, JDTEBlocks.EXTENDED_TIME_FREEZER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedClickerBE>> EXTENDED_CLICKER = BLOCK_ENTITIES.register(
            "extended_clicker", () -> new BlockEntityType<>(ExtendedClickerBE::new, JDTEBlocks.EXTENDED_CLICKER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedGeneratorBE>> EXTENDED_GENERATOR = BLOCK_ENTITIES.register(
            "extended_generator", () -> new BlockEntityType<>(ExtendedGeneratorBE::new, JDTEBlocks.EXTENDED_GENERATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedExperienceHolderBE>> EXTENDED_EXPERIENCE_HOLDER = BLOCK_ENTITIES.register(
            "extended_experience_holder", () -> new BlockEntityType<>(ExtendedExperienceHolderBE::new, JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidGeneratorBE>> EXTENDED_FLUID_GENERATOR = BLOCK_ENTITIES.register(
            "extended_fluid_generator", () -> new BlockEntityType<>(ExtendedFluidGeneratorBE::new, JDTEBlocks.EXTENDED_FLUID_GENERATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedEnergyTransmitterBE>> EXTENDED_ENERGY_TRANSMITTER = BLOCK_ENTITIES.register(
            "extended_energy_transmitter", () -> new BlockEntityType<>(ExtendedEnergyTransmitterBE::new, JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedBlockBreakerBE>> EXTENDED_BLOCK_BREAKER = BLOCK_ENTITIES.register(
            "extended_block_breaker", () -> new BlockEntityType<>(ExtendedBlockBreakerBE::new, JDTEBlocks.EXTENDED_BLOCK_BREAKER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedBlockPlacerBE>> EXTENDED_BLOCK_PLACER = BLOCK_ENTITIES.register(
            "extended_block_placer", () -> new BlockEntityType<>(ExtendedBlockPlacerBE::new, JDTEBlocks.EXTENDED_BLOCK_PLACER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedBlockSwapperBE>> EXTENDED_BLOCK_SWAPPER = BLOCK_ENTITIES.register(
            "extended_block_swapper", () -> new BlockEntityType<>(ExtendedBlockSwapperBE::new, JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedDropperBE>> EXTENDED_DROPPER = BLOCK_ENTITIES.register(
            "extended_dropper", () -> new BlockEntityType<>(ExtendedDropperBE::new, JDTEBlocks.EXTENDED_DROPPER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedSensorBE>> EXTENDED_SENSOR = BLOCK_ENTITIES.register(
            "extended_sensor", () -> new BlockEntityType<>(ExtendedSensorBE::new, JDTEBlocks.EXTENDED_SENSOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidCollectorBE>> EXTENDED_FLUID_COLLECTOR = BLOCK_ENTITIES.register(
            "extended_fluid_collector", () -> new BlockEntityType<>(ExtendedFluidCollectorBE::new, JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidPlacerBE>> EXTENDED_FLUID_PLACER = BLOCK_ENTITIES.register(
            "extended_fluid_placer", () -> new BlockEntityType<>(ExtendedFluidPlacerBE::new, JDTEBlocks.EXTENDED_FLUID_PLACER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedItemCollectorBE>> ADVANCED_ITEM_COLLECTOR = BLOCK_ENTITIES.register(
            "advanced_item_collector", () -> new BlockEntityType<>(AdvancedItemCollectorBE::new, JDTEBlocks.ADVANCED_ITEM_COLLECTOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EntitySuppressorBE>> ENTITY_SUPPRESSOR = BLOCK_ENTITIES.register(
            "entity_suppressor", () -> new BlockEntityType<>(EntitySuppressorBE::new, JDTEBlocks.ENTITY_SUPPRESSOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RangeBlockerBE>> RANGE_BLOCKER = BLOCK_ENTITIES.register(
            "range_blocker", () -> new BlockEntityType<>(RangeBlockerBE::new, JDTEBlocks.RANGE_BLOCKER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FactoryPackerBE>> FACTORY_PACKER = BLOCK_ENTITIES.register(
            "factory_packer", () -> new BlockEntityType<>(FactoryPackerBE::new, JDTEBlocks.FACTORY_PACKER.get()));

    // Glue Activator
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicGlueActivatorBE>> BASIC_GLUE_ACTIVATOR = BLOCK_ENTITIES.register(
            "basic_glue_activator", () -> new BlockEntityType<>(BasicGlueActivatorBE::new, JDTEBlocks.BASIC_GLUE_ACTIVATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedGlueActivatorBE>> ADVANCED_GLUE_ACTIVATOR = BLOCK_ENTITIES.register(
            "advanced_glue_activator", () -> new BlockEntityType<>(AdvancedGlueActivatorBE::new, JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedGlueActivatorBE>> EXTENDED_GLUE_ACTIVATOR = BLOCK_ENTITIES.register(
            "extended_glue_activator", () -> new BlockEntityType<>(ExtendedGlueActivatorBE::new, JDTEBlocks.EXTENDED_GLUE_ACTIVATOR.get()));

    // Gel Generator
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedGelGeneratorBE>> ADVANCED_GEL_GENERATOR = BLOCK_ENTITIES.register(
            "advanced_gel_generator", () -> new BlockEntityType<>(AdvancedGelGeneratorBE::new, JDTEBlocks.ADVANCED_GEL_GENERATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedGelGeneratorBE>> EXTENDED_GEL_GENERATOR = BLOCK_ENTITIES.register(
            "extended_gel_generator", () -> new BlockEntityType<>(ExtendedGelGeneratorBE::new, JDTEBlocks.EXTENDED_GEL_GENERATOR.get()));

    // Fluid Stabilizer
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicFluidStabilizerBE>> BASIC_FLUID_STABILIZER = BLOCK_ENTITIES.register(
            "basic_fluid_stabilizer", () -> new BlockEntityType<>(BasicFluidStabilizerBE::new, JDTEBlocks.BASIC_FLUID_STABILIZER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedFluidStabilizerBE>> ADVANCED_FLUID_STABILIZER = BLOCK_ENTITIES.register(
            "advanced_fluid_stabilizer", () -> new BlockEntityType<>(AdvancedFluidStabilizerBE::new, JDTEBlocks.ADVANCED_FLUID_STABILIZER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidStabilizerBE>> EXTENDED_FLUID_STABILIZER = BLOCK_ENTITIES.register(
            "extended_fluid_stabilizer", () -> new BlockEntityType<>(ExtendedFluidStabilizerBE::new, JDTEBlocks.EXTENDED_FLUID_STABILIZER.get()));

    // Item Sender
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicItemSenderBE>> BASIC_ITEM_SENDER = BLOCK_ENTITIES.register(
            "basic_item_sender", () -> new BlockEntityType<>(BasicItemSenderBE::new, JDTEBlocks.BASIC_ITEM_SENDER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedItemSenderBE>> ADVANCED_ITEM_SENDER = BLOCK_ENTITIES.register(
            "advanced_item_sender", () -> new BlockEntityType<>(AdvancedItemSenderBE::new, JDTEBlocks.ADVANCED_ITEM_SENDER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedItemSenderBE>> EXTENDED_ITEM_SENDER = BLOCK_ENTITIES.register(
            "extended_item_sender", () -> new BlockEntityType<>(ExtendedItemSenderBE::new, JDTEBlocks.EXTENDED_ITEM_SENDER.get()));

    // Fluid Sender
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicFluidSenderBE>> BASIC_FLUID_SENDER = BLOCK_ENTITIES.register(
            "basic_fluid_sender", () -> new BlockEntityType<>(BasicFluidSenderBE::new, JDTEBlocks.BASIC_FLUID_SENDER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedFluidSenderBE>> ADVANCED_FLUID_SENDER = BLOCK_ENTITIES.register(
            "advanced_fluid_sender", () -> new BlockEntityType<>(AdvancedFluidSenderBE::new, JDTEBlocks.ADVANCED_FLUID_SENDER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidSenderBE>> EXTENDED_FLUID_SENDER = BLOCK_ENTITIES.register(
            "extended_fluid_sender", () -> new BlockEntityType<>(ExtendedFluidSenderBE::new, JDTEBlocks.EXTENDED_FLUID_SENDER.get()));

    // Item Receiver
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicItemReceiverBE>> BASIC_ITEM_RECEIVER = BLOCK_ENTITIES.register(
            "basic_item_receiver", () -> new BlockEntityType<>(BasicItemReceiverBE::new, JDTEBlocks.BASIC_ITEM_RECEIVER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedItemReceiverBE>> ADVANCED_ITEM_RECEIVER = BLOCK_ENTITIES.register(
            "advanced_item_receiver", () -> new BlockEntityType<>(AdvancedItemReceiverBE::new, JDTEBlocks.ADVANCED_ITEM_RECEIVER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedItemReceiverBE>> EXTENDED_ITEM_RECEIVER = BLOCK_ENTITIES.register(
            "extended_item_receiver", () -> new BlockEntityType<>(ExtendedItemReceiverBE::new, JDTEBlocks.EXTENDED_ITEM_RECEIVER.get()));

    // Fluid Receiver
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicFluidReceiverBE>> BASIC_FLUID_RECEIVER = BLOCK_ENTITIES.register(
            "basic_fluid_receiver", () -> new BlockEntityType<>(BasicFluidReceiverBE::new, JDTEBlocks.BASIC_FLUID_RECEIVER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedFluidReceiverBE>> ADVANCED_FLUID_RECEIVER = BLOCK_ENTITIES.register(
            "advanced_fluid_receiver", () -> new BlockEntityType<>(AdvancedFluidReceiverBE::new, JDTEBlocks.ADVANCED_FLUID_RECEIVER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidReceiverBE>> EXTENDED_FLUID_RECEIVER = BLOCK_ENTITIES.register(
            "extended_fluid_receiver", () -> new BlockEntityType<>(ExtendedFluidReceiverBE::new, JDTEBlocks.EXTENDED_FLUID_RECEIVER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalIncubatorBE>> CRYSTAL_INCUBATOR = BLOCK_ENTITIES.register(
            "crystal_incubator", () -> new BlockEntityType<>(CrystalIncubatorBE::new, JDTEBlocks.CRYSTAL_INCUBATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreenhouseBE>> GREENHOUSE = BLOCK_ENTITIES.register(
            "greenhouse", () -> new BlockEntityType<>(GreenhouseBE::new, JDTEBlocks.GREENHOUSE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LargeGreenhouseBE>> LARGE_GREENHOUSE = BLOCK_ENTITIES.register(
            "large_greenhouse", () -> new BlockEntityType<>(LargeGreenhouseBE::new, JDTEBlocks.LARGE_GREENHOUSE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LifeSynthesisVatBE>> LIFE_SYNTHESIS_VAT = BLOCK_ENTITIES.register(
            "life_synthesis_vat", () -> new BlockEntityType<>(LifeSynthesisVatBE::new, JDTEBlocks.LIFE_SYNTHESIS_VAT.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BioFactoryBE>> BIO_FACTORY = BLOCK_ENTITIES.register(
            "bio_factory", () -> new BlockEntityType<>(BioFactoryBE::new, JDTEBlocks.BIO_FACTORY.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LifeBreederBE>> LIFE_BREEDER = BLOCK_ENTITIES.register(
            "life_breeder", () -> new BlockEntityType<>(LifeBreederBE::new, JDTEBlocks.LIFE_BREEDER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MineralExtractorBE>> MINERAL_EXTRACTOR = BLOCK_ENTITIES.register(
            "mineral_extractor", () -> new BlockEntityType<>(MineralExtractorBE::new, JDTEBlocks.MINERAL_EXTRACTOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LargeMineralExtractorBE>> LARGE_MINERAL_EXTRACTOR = BLOCK_ENTITIES.register(
            "large_mineral_extractor", () -> new BlockEntityType<>(LargeMineralExtractorBE::new, JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get()));

    // Life Extractor
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedLifeExtractorBE>> ADVANCED_LIFE_EXTRACTOR = BLOCK_ENTITIES.register(
            "advanced_life_extractor", () -> new BlockEntityType<>(AdvancedLifeExtractorBE::new, JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedLifeExtractorBE>> EXTENDED_LIFE_EXTRACTOR = BLOCK_ENTITIES.register(
            "extended_life_extractor", () -> new BlockEntityType<>(ExtendedLifeExtractorBE::new, JDTEBlocks.EXTENDED_LIFE_EXTRACTOR.get()));

    // Bio Extractor
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedBioExtractorBE>> ADVANCED_BIO_EXTRACTOR = BLOCK_ENTITIES.register(
            "advanced_bio_extractor", () -> new BlockEntityType<>(AdvancedBioExtractorBE::new, JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedBioExtractorBE>> EXTENDED_BIO_EXTRACTOR = BLOCK_ENTITIES.register(
            "extended_bio_extractor", () -> new BlockEntityType<>(ExtendedBioExtractorBE::new, JDTEBlocks.EXTENDED_BIO_EXTRACTOR.get()));

    // Infusion Machine
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedInfusionMachineBE>> ADVANCED_INFUSION_MACHINE = BLOCK_ENTITIES.register(
            "advanced_infusion_machine", () -> new BlockEntityType<>(AdvancedInfusionMachineBE::new, JDTEBlocks.ADVANCED_INFUSION_MACHINE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedInfusionMachineBE>> EXTENDED_INFUSION_MACHINE = BLOCK_ENTITIES.register(
            "extended_infusion_machine", () -> new BlockEntityType<>(ExtendedInfusionMachineBE::new, JDTEBlocks.EXTENDED_INFUSION_MACHINE.get()));

    // Potion Brewer
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedPotionBrewerBE>> ADVANCED_POTION_BREWER = BLOCK_ENTITIES.register(
            "advanced_potion_brewer", () -> new BlockEntityType<>(AdvancedPotionBrewerBE::new, JDTEBlocks.ADVANCED_POTION_BREWER.get()));

    // Bio Crusher
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedBioCrusherBE>> ADVANCED_BIO_CRUSHER = BLOCK_ENTITIES.register(
            "advanced_bio_crusher", () -> new BlockEntityType<>(AdvancedBioCrusherBE::new, JDTEBlocks.ADVANCED_BIO_CRUSHER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedBioCrusherBE>> EXTENDED_BIO_CRUSHER = BLOCK_ENTITIES.register(
            "extended_bio_crusher", () -> new BlockEntityType<>(ExtendedBioCrusherBE::new, JDTEBlocks.EXTENDED_BIO_CRUSHER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LootFabricatorBE>> LOOT_FABRICATOR = BLOCK_ENTITIES.register(
            "loot_fabricator", () -> new BlockEntityType<>(LootFabricatorBE::new, JDTEBlocks.LOOT_FABRICATOR.get()));

    // Advanced Energy Transmitter
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedEnergyTransmitterBE>> ADVANCED_ENERGY_TRANSMITTER = BLOCK_ENTITIES.register(
            "advanced_energy_transmitter", () -> new BlockEntityType<>(AdvancedEnergyTransmitterBE::new, JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get()));

    // Fluid Mixer
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedFluidMixerBE>> ADVANCED_FLUID_MIXER = BLOCK_ENTITIES.register(
            "advanced_fluid_mixer", () -> new BlockEntityType<>(AdvancedFluidMixerBE::new, JDTEBlocks.ADVANCED_FLUID_MIXER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidMixerBE>> EXTENDED_FLUID_MIXER = BLOCK_ENTITIES.register(
            "extended_fluid_mixer", () -> new BlockEntityType<>(ExtendedFluidMixerBE::new, JDTEBlocks.EXTENDED_FLUID_MIXER.get()));
}
