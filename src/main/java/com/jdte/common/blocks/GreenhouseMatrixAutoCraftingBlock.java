package com.jdte.common.blocks;

import com.jdte.common.blockentities.GreenhouseMatrixAutoCraftingBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

public final class GreenhouseMatrixAutoCraftingBlock extends Block implements EntityBlock {
    public GreenhouseMatrixAutoCraftingBlock() {
        super(Properties.of().strength(4.0F).sound(SoundType.METAL).lightLevel(state -> 5)
                .pushReaction(PushReaction.BLOCK));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GreenhouseMatrixAutoCraftingBE(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof GreenhouseMatrixAutoCraftingBE autoCrafting) {
            autoCrafting.dropPatterns();
            autoCrafting.unlink(autoCrafting.controllerPos());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
