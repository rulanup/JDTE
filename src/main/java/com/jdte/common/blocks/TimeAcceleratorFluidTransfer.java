package com.jdte.common.blocks;

import com.jdte.common.blockentities.TimeAcceleratorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

final class TimeAcceleratorFluidTransfer {
    private TimeAcceleratorFluidTransfer() {
    }

    static ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        IFluidHandlerItem itemHandler = itemStack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos, hit.getDirection());
        if (tank == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (itemHandler.getFluidInTank(0).isEmpty()) {
            FluidStack simulatedDrain = tank.drain(itemHandler.getTankCapacity(0), IFluidHandler.FluidAction.SIMULATE);
            int fillAmount = itemHandler.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
            if (fillAmount <= 0) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            FluidStack drained = tank.drain(fillAmount, IFluidHandler.FluidAction.EXECUTE);
            itemHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            setHeldContainer(player, hand, itemHandler);
            markChanged(level, blockPos);
            level.playSound(null, blockPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1F, 1F);
            return ItemInteractionResult.SUCCESS;
        }

        FluidStack containedFluid = itemHandler.getFluidInTank(0);
        int insertAmount = tank.fill(containedFluid, IFluidHandler.FluidAction.SIMULATE);
        if (insertAmount <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        FluidStack drained = itemHandler.drain(insertAmount, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        setHeldContainer(player, hand, itemHandler);
        markChanged(level, blockPos);
        level.playSound(null, blockPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1F, 1F);
        return ItemInteractionResult.SUCCESS;
    }

    private static void setHeldContainer(Player player, InteractionHand hand, IFluidHandlerItem itemHandler) {
        player.setItemSlot(hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, itemHandler.getContainer());
    }

    private static void markChanged(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof TimeAcceleratorBE accelerator) {
            accelerator.markDirtyClient();
        }
    }
}
