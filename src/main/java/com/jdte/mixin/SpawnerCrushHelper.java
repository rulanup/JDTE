package com.jdte.mixin;

import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blocks.AdvancedBioCrusherBlock;
import com.jdte.common.blocks.ExtendedBioCrusherBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

final class SpawnerCrushHelper {
    private SpawnerCrushHelper() {
    }

    /**
     * If a bio crusher sits directly above the spawner, consume the spawn cycle and
     * generate drops instead of spawning entities. Returns true when the cycle was consumed;
     * the caller must then reset the spawner delay and cancel vanilla spawning.
     */
    static boolean tryCrush(ServerLevel level, BlockPos pos, BaseSpawner spawner, SpawnData spawnData, int spawnCount) {
        BlockPos crusherPos = pos.above();
        BlockState crusherState = level.getBlockState(crusherPos);
        if (!(crusherState.getBlock() instanceof AdvancedBioCrusherBlock)
                && !(crusherState.getBlock() instanceof ExtendedBioCrusherBlock)) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(crusherPos);
        return be instanceof BioCrusherBE crusher
                && crusher.processSpawnerCrush(level, pos, spawner, spawnData, spawnCount);
    }
}
