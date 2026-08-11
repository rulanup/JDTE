package com.jdte.common.greenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

/** Server-only transient effects supplied by a formed greenhouse matrix. */
public final class GreenhouseMatrixRuntime {
    public record Effects(BlockPos controller, boolean enabled, int speed, int efficiency,
                          int seedConversion, int essenceConversion, boolean aeOutput) {
        public int speedPercent() { return Math.min(300, speed * 25); }
        public int efficiencyPercent() { return Math.min(80, efficiency * 10); }
    }

    private static final Map<ServerLevel, Map<BlockPos, Effects>> EFFECTS = new HashMap<>();

    private GreenhouseMatrixRuntime() {}

    public static void put(ServerLevel level, BlockPos greenhouse, Effects effects) {
        EFFECTS.computeIfAbsent(level, ignored -> new HashMap<>()).put(greenhouse.immutable(), effects);
    }

    public static void remove(ServerLevel level, BlockPos greenhouse, BlockPos controller) {
        Map<BlockPos, Effects> values = EFFECTS.get(level);
        if (values == null) return;
        Effects current = values.get(greenhouse);
        if (current != null && current.controller().equals(controller)) values.remove(greenhouse);
        if (values.isEmpty()) EFFECTS.remove(level);
    }

    public static Effects get(Object blockEntity) {
        if (blockEntity instanceof net.minecraft.world.level.block.entity.BlockEntity be
                && be.getLevel() instanceof ServerLevel serverLevel) {
            Map<BlockPos, Effects> values = EFFECTS.get(serverLevel);
            return values == null ? null : values.get(be.getBlockPos());
        }
        return null;
    }

    public static boolean isDisabled(Object blockEntity) {
        Effects effects = get(blockEntity);
        return effects != null && !effects.enabled();
    }

    public static boolean hasSeedConversion(Object blockEntity) {
        Effects effects = get(blockEntity);
        return effects != null && effects.enabled() && effects.seedConversion() > 0;
    }

    public static boolean hasEssenceConversion(Object blockEntity) {
        Effects effects = get(blockEntity);
        return effects != null && effects.enabled() && effects.essenceConversion() > 0;
    }

    public static boolean hasAEOutput(Object blockEntity) {
        Effects effects = get(blockEntity);
        return effects != null && effects.aeOutput();
    }

    public static long applySpeed(Object blockEntity, long work) {
        Effects effects = get(blockEntity);
        return effects == null || !effects.enabled() ? work : work * (100L + effects.speedPercent()) / 100L;
    }

    public static int applyEfficiency(Object blockEntity, int cost) {
        Effects effects = get(blockEntity);
        if (effects == null || !effects.enabled()) return cost;
        return Math.max(cost > 0 ? 1 : 0, cost * (100 - effects.efficiencyPercent()) / 100);
    }
}
