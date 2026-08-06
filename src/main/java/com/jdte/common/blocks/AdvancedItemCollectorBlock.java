package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.ItemCollector;
import com.direwolf20.justdirethings.common.items.FerricoreWrench;
import com.direwolf20.justdirethings.common.items.MachineSettingsCopier;
import com.jdte.common.blockentities.AdvancedItemCollectorBE;
import com.jdte.common.containers.AdvancedItemCollectorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class AdvancedItemCollectorBlock extends ItemCollector {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedItemCollectorBE(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof FerricoreWrench || heldItem.getItem() instanceof MachineSettingsCopier) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof AdvancedItemCollectorBE) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }

        NetworkHooks.openScreen(serverPlayer,
                new SimpleMenuProvider((windowId, inventory, ignored) ->
                        new AdvancedItemCollectorContainer(windowId, inventory, pos),
                        Component.translatable("block.jdte.advanced_item_collector")),
                buffer -> buffer.writeBlockPos(pos));
        return InteractionResult.SUCCESS;
    }
}
