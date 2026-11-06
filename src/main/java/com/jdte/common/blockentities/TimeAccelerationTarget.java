package com.jdte.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

public record TimeAccelerationTarget(ServerLevel level, BlockPos pos) {
    public TimeAccelerationTarget {
        Objects.requireNonNull(level, "level");
        pos = Objects.requireNonNull(pos, "pos").immutable();
    }
}
