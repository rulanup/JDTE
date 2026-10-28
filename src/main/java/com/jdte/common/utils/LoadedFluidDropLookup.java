package com.jdte.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** A global dropped-item scan must not refresh UNKNOWN tickets and pin otherwise idle chunks. */
public final class LoadedFluidDropLookup {
    private LoadedFluidDropLookup() {}
    public static BlockState state(Level level,BlockPos pos) {
        if(level instanceof ServerLevel server) {
            var chunk=server.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4);
            return chunk==null?Blocks.AIR.defaultBlockState():chunk.getBlockState(pos);
        }
        return level.getBlockState(pos);
    }
}
