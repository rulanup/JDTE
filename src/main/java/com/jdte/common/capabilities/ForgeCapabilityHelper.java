package com.jdte.common.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;

/** Forge 1.20.1 exposes world capabilities through the block entity at a position. */
public final class ForgeCapabilityHelper {
    private ForgeCapabilityHelper() {
    }

    public static <T> T get(Level level, BlockPos pos, Capability<T> capability, Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity == null ? null : blockEntity.getCapability(capability, side).orElse(null);
    }
}
