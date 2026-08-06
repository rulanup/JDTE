package com.jdte.common.integrations;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Deliberately disabled on Forge 1.20.1. Apothic Spawners has no Forge
 * 1.20.1 release, so keeping this adapter inert avoids linking a NeoForge-only
 * API while leaving vanilla spawner processing available to the crusher.
 */
public final class ApothicSpawnerIntegration {
    private ApothicSpawnerIntegration() {
    }

    public static int getSpawnCount(BlockEntity blockEntity, int fallback) {
        return fallback;
    }

    public static void applySpawnModifiers(BlockEntity blockEntity, Entity entity) {
    }
}
