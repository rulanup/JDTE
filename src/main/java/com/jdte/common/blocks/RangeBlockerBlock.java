package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.containers.RangeBlockerContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RangeBlockerBlock extends BaseMachineBlock {
    public RangeBlockerBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(2.5F).isRedstoneConductor(BaseMachineBlock::never));
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new RangeBlockerBE(pos, state); }
    @Override public boolean isValidBE(BlockEntity blockEntity) { return blockEntity instanceof RangeBlockerBE; }
    @Override public void openMenu(Player player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                        new RangeBlockerContainer(id, inventory, pos), Component.translatable("block.jdte.range_blocker")),
                buffer -> buffer.writeBlockPos(pos));
    }
}
