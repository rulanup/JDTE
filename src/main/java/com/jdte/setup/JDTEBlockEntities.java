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
            "basic_time_accelerator", () -> BlockEntityType.Builder.of(BasicTimeAcceleratorBE::new, JDTEBlocks.BASIC_TIME_ACCELERATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedTimeAcceleratorBE>> ADVANCED_TIME_ACCELERATOR = BLOCK_ENTITIES.register(
            "advanced_time_accelerator", () -> BlockEntityType.Builder.of(AdvancedTimeAcceleratorBE::new, JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedTimeAcceleratorBE>> EXTENDED_TIME_ACCELERATOR = BLOCK_ENTITIES.register(
            "extended_time_accelerator", () -> BlockEntityType.Builder.of(ExtendedTimeAcceleratorBE::new, JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedClickerBE>> EXTENDED_CLICKER = BLOCK_ENTITIES.register(
            "extended_clicker", () -> BlockEntityType.Builder.of(ExtendedClickerBE::new, JDTEBlocks.EXTENDED_CLICKER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedBlockBreakerBE>> EXTENDED_BLOCK_BREAKER = BLOCK_ENTITIES.register(
            "extended_block_breaker", () -> BlockEntityType.Builder.of(ExtendedBlockBreakerBE::new, JDTEBlocks.EXTENDED_BLOCK_BREAKER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedBlockPlacerBE>> EXTENDED_BLOCK_PLACER = BLOCK_ENTITIES.register(
            "extended_block_placer", () -> BlockEntityType.Builder.of(ExtendedBlockPlacerBE::new, JDTEBlocks.EXTENDED_BLOCK_PLACER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedBlockSwapperBE>> EXTENDED_BLOCK_SWAPPER = BLOCK_ENTITIES.register(
            "extended_block_swapper", () -> BlockEntityType.Builder.of(ExtendedBlockSwapperBE::new, JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedDropperBE>> EXTENDED_DROPPER = BLOCK_ENTITIES.register(
            "extended_dropper", () -> BlockEntityType.Builder.of(ExtendedDropperBE::new, JDTEBlocks.EXTENDED_DROPPER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedSensorBE>> EXTENDED_SENSOR = BLOCK_ENTITIES.register(
            "extended_sensor", () -> BlockEntityType.Builder.of(ExtendedSensorBE::new, JDTEBlocks.EXTENDED_SENSOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidCollectorBE>> EXTENDED_FLUID_COLLECTOR = BLOCK_ENTITIES.register(
            "extended_fluid_collector", () -> BlockEntityType.Builder.of(ExtendedFluidCollectorBE::new, JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidPlacerBE>> EXTENDED_FLUID_PLACER = BLOCK_ENTITIES.register(
            "extended_fluid_placer", () -> BlockEntityType.Builder.of(ExtendedFluidPlacerBE::new, JDTEBlocks.EXTENDED_FLUID_PLACER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedItemCollectorBE>> ADVANCED_ITEM_COLLECTOR = BLOCK_ENTITIES.register(
            "advanced_item_collector", () -> BlockEntityType.Builder.of(AdvancedItemCollectorBE::new, JDTEBlocks.ADVANCED_ITEM_COLLECTOR.get()).build(null));

    // Glue Activator
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicGlueActivatorBE>> BASIC_GLUE_ACTIVATOR = BLOCK_ENTITIES.register(
            "basic_glue_activator", () -> BlockEntityType.Builder.of(BasicGlueActivatorBE::new, JDTEBlocks.BASIC_GLUE_ACTIVATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedGlueActivatorBE>> ADVANCED_GLUE_ACTIVATOR = BLOCK_ENTITIES.register(
            "advanced_glue_activator", () -> BlockEntityType.Builder.of(AdvancedGlueActivatorBE::new, JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedGlueActivatorBE>> EXTENDED_GLUE_ACTIVATOR = BLOCK_ENTITIES.register(
            "extended_glue_activator", () -> BlockEntityType.Builder.of(ExtendedGlueActivatorBE::new, JDTEBlocks.EXTENDED_GLUE_ACTIVATOR.get()).build(null));

    // Gel Generator
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedGelGeneratorBE>> ADVANCED_GEL_GENERATOR = BLOCK_ENTITIES.register(
            "advanced_gel_generator", () -> BlockEntityType.Builder.of(AdvancedGelGeneratorBE::new, JDTEBlocks.ADVANCED_GEL_GENERATOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedGelGeneratorBE>> EXTENDED_GEL_GENERATOR = BLOCK_ENTITIES.register(
            "extended_gel_generator", () -> BlockEntityType.Builder.of(ExtendedGelGeneratorBE::new, JDTEBlocks.EXTENDED_GEL_GENERATOR.get()).build(null));

    // Fluid Stabilizer
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicFluidStabilizerBE>> BASIC_FLUID_STABILIZER = BLOCK_ENTITIES.register(
            "basic_fluid_stabilizer", () -> BlockEntityType.Builder.of(BasicFluidStabilizerBE::new, JDTEBlocks.BASIC_FLUID_STABILIZER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedFluidStabilizerBE>> ADVANCED_FLUID_STABILIZER = BLOCK_ENTITIES.register(
            "advanced_fluid_stabilizer", () -> BlockEntityType.Builder.of(AdvancedFluidStabilizerBE::new, JDTEBlocks.ADVANCED_FLUID_STABILIZER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidStabilizerBE>> EXTENDED_FLUID_STABILIZER = BLOCK_ENTITIES.register(
            "extended_fluid_stabilizer", () -> BlockEntityType.Builder.of(ExtendedFluidStabilizerBE::new, JDTEBlocks.EXTENDED_FLUID_STABILIZER.get()).build(null));

    // Item Sender
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicItemSenderBE>> BASIC_ITEM_SENDER = BLOCK_ENTITIES.register(
            "basic_item_sender", () -> BlockEntityType.Builder.of(BasicItemSenderBE::new, JDTEBlocks.BASIC_ITEM_SENDER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedItemSenderBE>> ADVANCED_ITEM_SENDER = BLOCK_ENTITIES.register(
            "advanced_item_sender", () -> BlockEntityType.Builder.of(AdvancedItemSenderBE::new, JDTEBlocks.ADVANCED_ITEM_SENDER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedItemSenderBE>> EXTENDED_ITEM_SENDER = BLOCK_ENTITIES.register(
            "extended_item_sender", () -> BlockEntityType.Builder.of(ExtendedItemSenderBE::new, JDTEBlocks.EXTENDED_ITEM_SENDER.get()).build(null));

    // Fluid Sender
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicFluidSenderBE>> BASIC_FLUID_SENDER = BLOCK_ENTITIES.register(
            "basic_fluid_sender", () -> BlockEntityType.Builder.of(BasicFluidSenderBE::new, JDTEBlocks.BASIC_FLUID_SENDER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedFluidSenderBE>> ADVANCED_FLUID_SENDER = BLOCK_ENTITIES.register(
            "advanced_fluid_sender", () -> BlockEntityType.Builder.of(AdvancedFluidSenderBE::new, JDTEBlocks.ADVANCED_FLUID_SENDER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidSenderBE>> EXTENDED_FLUID_SENDER = BLOCK_ENTITIES.register(
            "extended_fluid_sender", () -> BlockEntityType.Builder.of(ExtendedFluidSenderBE::new, JDTEBlocks.EXTENDED_FLUID_SENDER.get()).build(null));

    // Item Receiver
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicItemReceiverBE>> BASIC_ITEM_RECEIVER = BLOCK_ENTITIES.register(
            "basic_item_receiver", () -> BlockEntityType.Builder.of(BasicItemReceiverBE::new, JDTEBlocks.BASIC_ITEM_RECEIVER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedItemReceiverBE>> ADVANCED_ITEM_RECEIVER = BLOCK_ENTITIES.register(
            "advanced_item_receiver", () -> BlockEntityType.Builder.of(AdvancedItemReceiverBE::new, JDTEBlocks.ADVANCED_ITEM_RECEIVER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedItemReceiverBE>> EXTENDED_ITEM_RECEIVER = BLOCK_ENTITIES.register(
            "extended_item_receiver", () -> BlockEntityType.Builder.of(ExtendedItemReceiverBE::new, JDTEBlocks.EXTENDED_ITEM_RECEIVER.get()).build(null));

    // Fluid Receiver
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicFluidReceiverBE>> BASIC_FLUID_RECEIVER = BLOCK_ENTITIES.register(
            "basic_fluid_receiver", () -> BlockEntityType.Builder.of(BasicFluidReceiverBE::new, JDTEBlocks.BASIC_FLUID_RECEIVER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedFluidReceiverBE>> ADVANCED_FLUID_RECEIVER = BLOCK_ENTITIES.register(
            "advanced_fluid_receiver", () -> BlockEntityType.Builder.of(AdvancedFluidReceiverBE::new, JDTEBlocks.ADVANCED_FLUID_RECEIVER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedFluidReceiverBE>> EXTENDED_FLUID_RECEIVER = BLOCK_ENTITIES.register(
            "extended_fluid_receiver", () -> BlockEntityType.Builder.of(ExtendedFluidReceiverBE::new, JDTEBlocks.EXTENDED_FLUID_RECEIVER.get()).build(null));

    // Life Extractor
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedLifeExtractorBE>> ADVANCED_LIFE_EXTRACTOR = BLOCK_ENTITIES.register(
            "advanced_life_extractor", () -> BlockEntityType.Builder.of(AdvancedLifeExtractorBE::new, JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedLifeExtractorBE>> EXTENDED_LIFE_EXTRACTOR = BLOCK_ENTITIES.register(
            "extended_life_extractor", () -> BlockEntityType.Builder.of(ExtendedLifeExtractorBE::new, JDTEBlocks.EXTENDED_LIFE_EXTRACTOR.get()).build(null));

    // Infusion Machine
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedInfusionMachineBE>> ADVANCED_INFUSION_MACHINE = BLOCK_ENTITIES.register(
            "advanced_infusion_machine", () -> BlockEntityType.Builder.of(AdvancedInfusionMachineBE::new, JDTEBlocks.ADVANCED_INFUSION_MACHINE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedInfusionMachineBE>> EXTENDED_INFUSION_MACHINE = BLOCK_ENTITIES.register(
            "extended_infusion_machine", () -> BlockEntityType.Builder.of(ExtendedInfusionMachineBE::new, JDTEBlocks.EXTENDED_INFUSION_MACHINE.get()).build(null));

    // Potion Brewer
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedPotionBrewerBE>> ADVANCED_POTION_BREWER = BLOCK_ENTITIES.register(
            "advanced_potion_brewer", () -> BlockEntityType.Builder.of(AdvancedPotionBrewerBE::new, JDTEBlocks.ADVANCED_POTION_BREWER.get()).build(null));

    // Bio Crusher
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedBioCrusherBE>> ADVANCED_BIO_CRUSHER = BLOCK_ENTITIES.register(
            "advanced_bio_crusher", () -> BlockEntityType.Builder.of(AdvancedBioCrusherBE::new, JDTEBlocks.ADVANCED_BIO_CRUSHER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedBioCrusherBE>> EXTENDED_BIO_CRUSHER = BLOCK_ENTITIES.register(
            "extended_bio_crusher", () -> BlockEntityType.Builder.of(ExtendedBioCrusherBE::new, JDTEBlocks.EXTENDED_BIO_CRUSHER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LootFabricatorBE>> LOOT_FABRICATOR = BLOCK_ENTITIES.register(
            "loot_fabricator", () -> BlockEntityType.Builder.of(LootFabricatorBE::new, JDTEBlocks.LOOT_FABRICATOR.get()).build(null));
}
