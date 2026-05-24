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
}
