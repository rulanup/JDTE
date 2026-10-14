package com.jdte.common.blocks;

import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseMachineBlock;
import com.jdte.common.blockentities.ChargedLightningRodBE;
import com.jdte.common.containers.ChargedLightningRodContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ChargedLightningRodBlock extends BaseMachineBlock {
    public ChargedLightningRodBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(3.0f).noOcclusion()
                .isRedstoneConductor(BaseMachineBlock::never));
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChargedLightningRodBE(pos, state);
    }

    @Override public boolean isValidBE(BlockEntity blockEntity) {
        return blockEntity instanceof ChargedLightningRodBE;
    }

    @Override public void openMenu(Player player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                new ChargedLightningRodContainer(id, inventory, pos), Component.translatable("block.jdte.charged_lightning_rod")),
                buffer -> buffer.writeBlockPos(pos));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof ChargedLightningRodBE blockEntity) {
            blockEntity.removeOwnedLightningRod();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
