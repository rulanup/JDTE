package com.jdte.common.greenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class GreenhouseMatrixRenderRegistry {
    private static final Map<Level, Map<BlockPos, Boolean>> HIDDEN = new WeakHashMap<>();

    private GreenhouseMatrixRenderRegistry() {}

    public static void replace(Level level, List<BlockPos> oldMembers, List<BlockPos> newMembers, boolean renderEnabled) {
        Map<BlockPos, Boolean> values = HIDDEN.computeIfAbsent(level, ignored -> new HashMap<>());
        for (BlockPos pos : oldMembers) values.remove(pos);
        if (!renderEnabled) for (BlockPos pos : newMembers) values.put(pos.immutable(), true);
        if (values.isEmpty()) HIDDEN.remove(level);
    }

    public static boolean shouldRender(Level level, BlockPos pos) {
        Map<BlockPos, Boolean> values = HIDDEN.get(level);
        return values == null || !values.getOrDefault(pos, false);
    }
}
