package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.ExtendedFluidCollectorBE;
import com.jdte.common.containers.ExtendedFluidCollectorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ExtendedFluidCollectorBlock extends BaseMachineBlock {
    public ExtendedFluidCollectorBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(2.5f).isRedstoneConductor(BaseMachineBlock::never));
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ExtendedFluidCollectorBE(pos, state); }
    @Override public void openMenu(Player player, BlockPos blockPos) { player.openMenu(new SimpleMenuProvider((w, p, e) -> new ExtendedFluidCollectorContainer(w, p, blockPos), Component.translatable("block.jdte.extended_fluid_collector")), buf -> buf.writeBlockPos(blockPos)); }
    @Override public boolean isValidBE(BlockEntity blockEntity) { return blockEntity instanceof ExtendedFluidCollectorBE; }
}
