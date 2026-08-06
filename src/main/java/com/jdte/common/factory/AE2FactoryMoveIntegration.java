package com.jdte.common.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forge 1.20.1 fallback until the matching AE2 move-strategy API is bound.
 * Ordinary block entity persistence remains available to Factory Packer.
 */
final class AE2FactoryMoveIntegration {
    private AE2FactoryMoveIntegration() {
    }

    static BlockState rotateState(BlockState state, Rotation rotation) {
        return state.rotate(rotation);
    }

    static CompoundTag rotateMoveData(CompoundTag data, Rotation rotation) {
        return data;
    }

    static CompoundTag beginMove(BlockEntity blockEntity) {
        return blockEntity.saveWithId();
    }

    static boolean completeMove(BlockState state, CompoundTag data, Level level, BlockPos pos) {
        BlockEntity restored = BlockEntity.loadStatic(pos, state, data);
        if (restored == null) {
            return false;
        }
        level.setBlockEntity(restored);
        restored.setChanged();
        return true;
    }
}
