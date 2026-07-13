package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.ItemCollector;
import com.jdte.common.blockentities.AdvancedItemCollectorBE;
import com.jdte.common.containers.AdvancedItemCollectorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class AdvancedItemCollectorBlock extends ItemCollector {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedItemCollectorBE(pos, state);
    }

    @Override
    public void openMenu(Player player, BlockPos blockPos) {
        player.openMenu(new SimpleMenuProvider(
                        (windowId, inventory, ignored) ->
                                new AdvancedItemCollectorContainer(windowId, inventory, blockPos),
                        Component.translatable("block.jdte.advanced_item_collector")),
                buffer -> buffer.writeBlockPos(blockPos));
    }

    @Override
    public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof AdvancedItemCollectorBE;
    }
}
