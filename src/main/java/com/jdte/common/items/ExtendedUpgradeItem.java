package com.jdte.common.items;

import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedUpgradeItem extends Item {
    private static final Map<Block, Block> UPGRADE_MAP = new HashMap<>();

    static {
        UPGRADE_MAP.put(Registration.ClickerT2.get(), JDTEBlocks.EXTENDED_CLICKER.get());
        UPGRADE_MAP.put(Registration.BlockBreakerT2.get(), JDTEBlocks.EXTENDED_BLOCK_BREAKER.get());
        UPGRADE_MAP.put(Registration.BlockPlacerT2.get(), JDTEBlocks.EXTENDED_BLOCK_PLACER.get());
        UPGRADE_MAP.put(Registration.BlockSwapperT2.get(), JDTEBlocks.EXTENDED_BLOCK_SWAPPER.get());
        UPGRADE_MAP.put(Registration.DropperT2.get(), JDTEBlocks.EXTENDED_DROPPER.get());
        UPGRADE_MAP.put(Registration.SensorT2.get(), JDTEBlocks.EXTENDED_SENSOR.get());
        UPGRADE_MAP.put(Registration.FluidCollectorT2.get(), JDTEBlocks.EXTENDED_FLUID_COLLECTOR.get());
        UPGRADE_MAP.put(Registration.FluidPlacerT2.get(), JDTEBlocks.EXTENDED_FLUID_PLACER.get());
        UPGRADE_MAP.put(Registration.GeneratorT1.get(), JDTEBlocks.EXTENDED_GENERATOR.get());
        UPGRADE_MAP.put(Registration.GeneratorFluidT1.get(), JDTEBlocks.EXTENDED_FLUID_GENERATOR.get());
        UPGRADE_MAP.put(Registration.ExperienceHolder.get(), JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get());
        UPGRADE_MAP.put(Registration.EnergyTransmitter.get(), JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get(), JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get());
        UPGRADE_MAP.put(JDTEBlocks.TIME_FREEZER.get(), JDTEBlocks.EXTENDED_TIME_FREEZER.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_FLUID_STABILIZER.get(), JDTEBlocks.EXTENDED_FLUID_STABILIZER.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get(), JDTEBlocks.EXTENDED_LIFE_EXTRACTOR.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_INFUSION_MACHINE.get(), JDTEBlocks.EXTENDED_INFUSION_MACHINE.get());
        UPGRADE_MAP.put(JDTEBlocks.ADVANCED_GEL_GENERATOR.get(), JDTEBlocks.EXTENDED_GEL_GENERATOR.get());
    }

    public ExtendedUpgradeItem() {
        super(new Properties().stacksTo(1));
    }

    static Block targetFor(Block source) {
        return UPGRADE_MAP.get(source);
    }

    static boolean isValidReplacement(BlockState replacedState, Block extendedBlock,
                                      BlockEntity newBE, BlockEntityType<?> expectedType) {
        return replacedState.getBlock() == extendedBlock
                && newBE != null
                && !newBE.isRemoved()
                && newBE.getBlockState().getBlock() == extendedBlock
                && newBE.getType() == expectedType
                && newBE.getType().isValid(replacedState);
    }

    static BlockState targetStateWithCompatibleFacing(BlockState sourceState, BlockState targetDefaultState) {
        boolean sourceHasFacing = sourceState.hasProperty(BlockStateProperties.FACING);
        boolean targetHasFacing = targetDefaultState.hasProperty(BlockStateProperties.FACING);
        if (sourceHasFacing != targetHasFacing) {
            return null;
        }
        if (!sourceHasFacing) {
            return targetDefaultState;
        }
        return targetDefaultState.setValue(
                BlockStateProperties.FACING,
                sourceState.getValue(BlockStateProperties.FACING));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        Block extendedBlock = targetFor(block);
        if (extendedBlock == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity oldBE = level.getBlockEntity(pos);
        if (oldBE == null
                || oldBE.isRemoved()
                || oldBE.getBlockState().getBlock() != block
                || !oldBE.getType().isValid(state)) {
            return InteractionResult.FAIL;
        }

        BlockState extendedState = targetStateWithCompatibleFacing(
                state, extendedBlock.defaultBlockState());
        if (extendedState == null) {
            return InteractionResult.FAIL;
        }
        if (!(extendedBlock instanceof EntityBlock entityBlock)) {
            return InteractionResult.FAIL;
        }
        BlockEntity expectedBE = entityBlock.newBlockEntity(pos, extendedState);
        if (expectedBE == null
                || expectedBE.getBlockState().getBlock() != extendedBlock
                || !expectedBE.getType().isValid(extendedState)) {
            return InteractionResult.FAIL;
        }
        BlockEntityType<?> expectedType = expectedBE.getType();

        CompoundTag data;
        try {
            data = oldBE.saveWithFullMetadata(level.registryAccess());
        } catch (RuntimeException exception) {
            return InteractionResult.FAIL;
        }

        try {
            level.removeBlockEntity(pos);
            if (!level.setBlock(pos, extendedState, Block.UPDATE_ALL)) {
                restoreOriginal(level, pos, state, data, oldBE);
                return InteractionResult.FAIL;
            }

            BlockState replacedState = level.getBlockState(pos);
            BlockEntity newBE = level.getBlockEntity(pos);
            if (!isValidReplacement(replacedState, extendedBlock, newBE, expectedType)) {
                restoreOriginal(level, pos, state, data, oldBE);
                return InteractionResult.FAIL;
            }

            newBE.loadCustomOnly(data, level.registryAccess());
            newBE.setChanged();
        } catch (RuntimeException exception) {
            restoreOriginal(level, pos, state, data, oldBE);
            return InteractionResult.FAIL;
        }

        context.getItemInHand().shrink(1);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);

        return InteractionResult.SUCCESS;
    }

    private static void restoreOriginal(Level level, BlockPos pos, BlockState state, CompoundTag data,
                                        BlockEntity oldBE) {
        try {
            level.removeBlockEntity(pos);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            if (level.getBlockState(pos) != state) {
                return;
            }

            BlockEntity restoredBE = level.getBlockEntity(pos);
            if (restoredBE == null || !restoredBE.getType().isValid(state)) {
                oldBE.setBlockState(state);
                oldBE.clearRemoved();
                level.setBlockEntity(oldBE);
                restoredBE = oldBE;
            }

            restoredBE.loadCustomOnly(data, level.registryAccess());
            restoredBE.setChanged();
        } catch (RuntimeException ignored) {
            // Best-effort rollback: conversion failure must never consume the upgrade item.
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.jdte.extended_upgrade").withStyle(ChatFormatting.GRAY));
    }
}
