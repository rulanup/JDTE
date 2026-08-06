package com.jdte.common.integrations;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.apothic_spawners.stats.SpawnerStats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ApothicSpawnerIntegration {
    private static final String MOVABLE_TAG = "apotheosis:movable";

    private ApothicSpawnerIntegration() {
    }

    public static int getSpawnCount(BlockEntity blockEntity, int fallback) {
        if (blockEntity instanceof ApothSpawnerTile spawner) {
            return Math.max(0, SpawnerStats.SPAWN_COUNT.getValue(spawner));
        }
        return fallback;
    }

    public static void applySpawnModifiers(BlockEntity blockEntity, Entity entity) {
        if (!(blockEntity instanceof ApothSpawnerTile spawner)) return;
        entity.getSelfAndPassengers().forEach(target -> applySpawnModifiers(spawner, target));
    }

    private static void applySpawnModifiers(ApothSpawnerTile spawner, Entity entity) {
        if (SpawnerStats.NO_AI.getValue(spawner) && entity instanceof Mob mob) {
            mob.setNoAi(true);
            mob.getPersistentData().putBoolean(MOVABLE_TAG, true);
        }
        if (SpawnerStats.YOUTHFUL.getValue(spawner) && entity instanceof Mob mob) {
            mob.setBaby(true);
        }
        if (SpawnerStats.SILENT.getValue(spawner)) {
            entity.setSilent(true);
        }

        float initialHealth = SpawnerStats.INITIAL_HEALTH.getValue(spawner);
        if (initialHealth != 1.0F && entity instanceof LivingEntity livingEntity) {
            livingEntity.setHealth(livingEntity.getHealth() * initialHealth);
        }
        if (SpawnerStats.BURNING.getValue(spawner) && !entity.fireImmune()) {
            entity.setRemainingFireTicks(Integer.MAX_VALUE);
        }

        int echoing = SpawnerStats.ECHOING.getValue(spawner);
        if (echoing > 0) {
            entity.getPersistentData().putInt(SpawnerStats.ECHOING.getId().toString(), echoing);
        }
    }
}
