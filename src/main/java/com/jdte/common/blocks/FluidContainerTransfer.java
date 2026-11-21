package com.jdte.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;

final class FluidContainerTransfer {
    private FluidContainerTransfer() {
    }

    static InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos,
                                           Player player, InteractionHand hand, BlockHitResult hit) {
        return useItemOn(itemStack, level, blockPos, blockPos, player, hand, hit);
    }

    static InteractionResult useItemOn(ItemStack itemStack, Level level, BlockPos capabilityPos,
                                           BlockPos ownerPos, Player player, InteractionHand hand,
                                           BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        var item = ItemAccess.forPlayerInteraction(player, hand).getCapability(Capabilities.Fluid.ITEM);
        var block = level.getCapability(Capabilities.Fluid.BLOCK, capabilityPos, hit.getDirection());
        if (item == null || item.size() == 0 || block == null) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        boolean fillingContainer = item.getResource(0).isEmpty();
        if (!transfer(item, block)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        markChanged(level, ownerPos);
        level.playSound(null, capabilityPos, fillingContainer ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    static boolean transfer(ResourceHandler<FluidResource> item, ResourceHandler<FluidResource> block) {
        var heldFluid = item.getResource(0);
        boolean fillingContainer = heldFluid.isEmpty();
        int limit = fillingContainer ? item.getCapacityAsInt(0, heldFluid) : item.getAmountAsInt(0);
        if (limit <= 0) return false;
        try (var tx = Transaction.openRoot()) {
            var moved = fillingContainer
                    ? ResourceHandlerUtil.moveFirst(block, item, resource -> true, limit, tx)
                    : ResourceHandlerUtil.moveFirst(item, block, heldFluid::equals, limit, tx);
            if (moved == null) return false;
            tx.commit();
        }
        return true;
    }

    private static void markChanged(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity != null) {
            blockEntity.setChanged();
        }
    }
}
