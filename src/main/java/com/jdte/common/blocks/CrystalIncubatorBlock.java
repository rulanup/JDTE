package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.CrystalIncubatorBE;
import com.jdte.common.containers.CrystalIncubatorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CrystalIncubatorBlock extends BaseMachineBlock {
    public CrystalIncubatorBlock() {
        super(Properties.of()
                .sound(SoundType.METAL)
                .strength(3.0F)
                .isRedstoneConductor(BaseMachineBlock::never));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalIncubatorBE(pos, state);
    }

    @Override
    public void openMenu(Player player, BlockPos blockPos) {
        player.openMenu(new SimpleMenuProvider(
                (windowId, inventory, ignored) -> new CrystalIncubatorContainer(windowId, inventory, blockPos),
                Component.translatable("block.jdte.crystal_incubator")), buffer -> buffer.writeBlockPos(blockPos));
    }

    @Override
    public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof CrystalIncubatorBE;
    }
}
