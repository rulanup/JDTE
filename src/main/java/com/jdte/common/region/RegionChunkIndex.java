package com.jdte.common.region;

import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionChunkIndex<K, T> {
    private final Map<Level, LevelData<K, T>> levels = new ConcurrentHashMap<>();

    public void add(Level level, K owner, T entry, AABB area) {
        LevelData<K, T> data = levels.computeIfAbsent(level, ignored -> new LevelData<>());
        data.remove(owner);
        data.add(owner, entry, area);
    }

    public void remove(Level level, K owner) {
        LevelData<K, T> data = levels.get(level);
        if (data != null) data.remove(owner);
    }

    public Set<T> entriesAt(Level level, Vec3 position) {
        return entriesAt(level, position.x, position.z);
    }

    public Set<T> entriesAt(Level level, double x, double z) {
        LevelData<K, T> data = levels.get(level);
        if (data == null) return Collections.emptySet();
        return data.byChunk.getOrDefault(ChunkPos.asLong(
                SectionPos.blockToSectionCoord(Mth.floor(x)),
                SectionPos.blockToSectionCoord(Mth.floor(z))), Collections.emptySet());
    }

    public Collection<T> entries(Level level) {
        LevelData<K, T> data = levels.get(level);
        return data == null ? Collections.emptyList() : data.entryByOwner.values();
    }

    public void clear(LevelAccessor level) {
        levels.remove(level);
    }

    private static final class LevelData<K, T> {
        private final Map<Long, Set<T>> byChunk = new HashMap<>();
        private final Map<K, Set<Long>> chunksByOwner = new IdentityHashMap<>();
        private final Map<K, T> entryByOwner = new IdentityHashMap<>();

        void add(K owner, T entry, AABB area) {
            int minX = SectionPos.blockToSectionCoord(Mth.floor(area.minX));
            int maxX = SectionPos.blockToSectionCoord(Mth.ceil(area.maxX) - 1);
            int minZ = SectionPos.blockToSectionCoord(Mth.floor(area.minZ));
            int maxZ = SectionPos.blockToSectionCoord(Mth.ceil(area.maxZ) - 1);
            Set<Long> chunks = new LinkedHashSet<>();
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    long key = ChunkPos.asLong(x, z);
                    chunks.add(key);
                    byChunk.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(entry);
                }
            }
            chunksByOwner.put(owner, chunks);
            entryByOwner.put(owner, entry);
        }

        void remove(K owner) {
            T entry = entryByOwner.remove(owner);
            Set<Long> chunks = chunksByOwner.remove(owner);
            if (chunks == null) return;
            for (long key : chunks) {
                Set<T> values = byChunk.get(key);
                if (values == null) continue;
                values.remove(entry);
                if (values.isEmpty()) byChunk.remove(key);
            }
        }
    }
}
