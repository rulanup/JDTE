package com.jdte.mixin;

import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerAccessor {
    @Accessor("spawnDelay")
    int jdte$getSpawnDelay();

    @Accessor("spawnDelay")
    void jdte$setSpawnDelay(int delay);

    @Accessor("minSpawnDelay")
    int jdte$getMinSpawnDelay();

    @Accessor("maxSpawnDelay")
    int jdte$getMaxSpawnDelay();

    @Accessor("spawnCount")
    int jdte$getSpawnCount();

    @Accessor("maxNearbyEntities")
    int jdte$getMaxNearbyEntities();

    @Accessor("spawnRange")
    int jdte$getSpawnRange();

    @Accessor("nextSpawnData")
    SpawnData jdte$getNextSpawnData();
}
