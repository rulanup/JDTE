package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.GeneratorT1;
import com.jdte.common.blockentities.ExtendedGeneratorBE;
import com.jdte.common.containers.ExtendedGeneratorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ExtendedGeneratorBlock extends GeneratorT1 {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExtendedGeneratorBE(pos, state);
    }

    @Override
    public void openMenu(Player player, BlockPos blockPos) {
        player.openMenu(new SimpleMenuProvider(
                (windowId, playerInventory, ignored) -> new ExtendedGeneratorContainer(windowId, playerInventory, blockPos),
                Component.translatable("block.jdte.extended_generator")),
                buf -> buf.writeBlockPos(blockPos));
    }

    @Override
    public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof ExtendedGeneratorBE;
    }
}
