package com.jdte.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

final class FluidContainerTransfer {
    private FluidContainerTransfer() {
    }

    static InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.PASS;
        }

        IFluidHandlerItem itemHandler = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (itemHandler == null || itemHandler.getTanks() <= 0) {
            return InteractionResult.PASS;
        }

        IFluidHandler blockHandler = com.jdte.common.capabilities.ForgeCapabilityHelper.get(level, blockPos, ForgeCapabilities.FLUID_HANDLER, hit.getDirection());
        if (blockHandler == null) {
            return InteractionResult.PASS;
        }

        FluidStack containerFluid = itemHandler.getFluidInTank(0);
        if (containerFluid.isEmpty()) {
            return fillContainerFromBlock(itemStack, level, blockPos, player, hand, itemHandler, blockHandler);
        }
        return fillBlockFromContainer(itemStack, level, blockPos, player, hand, itemHandler, blockHandler);
    }

    private static InteractionResult fillContainerFromBlock(ItemStack itemStack, Level level, BlockPos blockPos, Player player, InteractionHand hand, IFluidHandlerItem itemHandler, IFluidHandler blockHandler) {
        FluidStack simulatedDrain = blockHandler.drain(itemHandler.getTankCapacity(0), IFluidHandler.FluidAction.SIMULATE);
        if (simulatedDrain.getAmount() <= 0) {
            return InteractionResult.PASS;
        }

        int fillAmount = itemHandler.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
        if (fillAmount <= 0) {
            return InteractionResult.PASS;
        }

        FluidStack drained = blockHandler.drain(fillAmount, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            return InteractionResult.PASS;
        }

        itemHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        updateHeldContainer(itemStack, player, hand, itemHandler);
        markChanged(level, blockPos);
        level.playSound(null, blockPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult fillBlockFromContainer(ItemStack itemStack, Level level, BlockPos blockPos, Player player, InteractionHand hand, IFluidHandlerItem itemHandler, IFluidHandler blockHandler) {
        FluidStack containerFluid = itemHandler.getFluidInTank(0);
        int fillAmount = blockHandler.fill(containerFluid, IFluidHandler.FluidAction.SIMULATE);
        if (fillAmount <= 0) {
            return InteractionResult.PASS;
        }

        FluidStack drained = itemHandler.drain(fillAmount, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            return InteractionResult.PASS;
        }

        int filled = blockHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            itemHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            return InteractionResult.PASS;
        }

        if (filled < drained.getAmount()) {
            FluidStack remainder = drained.copy();
            remainder.setAmount(drained.getAmount() - filled);
            itemHandler.fill(remainder, IFluidHandler.FluidAction.EXECUTE);
        }
        updateHeldContainer(itemStack, player, hand, itemHandler);
        markChanged(level, blockPos);
        level.playSound(null, blockPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private static void updateHeldContainer(ItemStack itemStack, Player player, InteractionHand hand, IFluidHandlerItem itemHandler) {
        if (itemStack.getCount() == 1 || itemStack.getItem() instanceof BucketItem) {
            player.setItemSlot(hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, itemHandler.getContainer());
        }
    }

    private static void markChanged(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity != null) {
            blockEntity.setChanged();
        }
    }
}
