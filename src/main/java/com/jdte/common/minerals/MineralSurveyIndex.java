package com.jdte.common.minerals;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class MineralSurveyIndex {
    private static final Map<MinecraftServer, Snapshot> SERVERS = new IdentityHashMap<>();
    private static final int DEFAULT_MAX_ENTRIES = 128;

    private MineralSurveyIndex() {
    }

    public static synchronized void rebuild(MinecraftServer server) {
        Registry<Biome> biomes = server.registryAccess().registryOrThrow(Registries.BIOME);
        Map<ResourceLocation, List<MineralEntry>> profiles = new java.util.LinkedHashMap<>();
        for (Map.Entry<ResourceKey<Biome>, Biome> entry : biomes.entrySet()) {
            Holder<Biome> holder = biomes.wrapAsHolder(entry.getValue());
            ResourceLocation biomeId = entry.getKey().location();
            List<MineralEntry> minerals = new java.util.ArrayList<>(
                    MineralFeatureAnalyzer.analyze(holder, DEFAULT_MAX_ENTRIES));
            for (MineralSourceOverride override : MineralSourceReloadListener.overrides()) {
                if (!override.matches(biomeId, holder)) continue;
                if (override.replace()) minerals.clear();
                merge(minerals, override.entries());
            }
            minerals = minerals.stream()
                    .sorted(java.util.Comparator.comparingLong(MineralEntry::weight).reversed()
                            .thenComparing(value -> value.oreId().toString()))
                    .limit(DEFAULT_MAX_ENTRIES)
                    .toList();
            if (!minerals.isEmpty()) profiles.put(biomeId, minerals);
        }
        Map<ResourceLocation, List<MineralEntry>> immutableProfiles = Map.copyOf(profiles);
        SERVERS.put(server, new Snapshot(immutableProfiles, fingerprint(immutableProfiles)));
    }

    public static synchronized Profile profile(ServerLevel level, Holder<Biome> biome) {
        Snapshot snapshot = snapshot(level.getServer());
        ResourceLocation biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        List<MineralEntry> entries = biomeId == null
                ? List.of()
                : snapshot.profiles().getOrDefault(biomeId, List.of());
        return new Profile(snapshot.version(), entries);
    }

    public static synchronized List<MineralEntry> get(ServerLevel level, Holder<Biome> biome) {
        return profile(level, biome).entries();
    }

    public static synchronized long version(MinecraftServer server) {
        return snapshot(server).version();
    }

    private static Snapshot snapshot(MinecraftServer server) {
        Snapshot snapshot = SERVERS.get(server);
        if (snapshot == null) {
            rebuild(server);
            snapshot = SERVERS.get(server);
        }
        return snapshot;
    }

    public static synchronized void clear(MinecraftServer server) {
        SERVERS.remove(server);
    }

    private static void merge(List<MineralEntry> target, List<MineralEntry> additions) {
        Map<ResourceLocation, MineralEntry> merged = new java.util.LinkedHashMap<>();
        for (MineralEntry entry : target) merged.put(entry.oreId(), entry);
        for (MineralEntry entry : additions) {
            merged.merge(entry.oreId(), entry, (left, right) -> new MineralEntry(
                    left.oreId(), saturatingAdd(left.weight(), right.weight()),
                    Math.min(left.minY(), right.minY()), Math.max(left.maxY(), right.maxY()),
                    Math.max(left.veinSize(), right.veinSize()), right.confidence()));
        }
        target.clear();
        target.addAll(merged.values());
    }

    private static long fingerprint(Map<ResourceLocation, List<MineralEntry>> profiles) {
        long hash = 0xcbf29ce484222325L;
        List<Map.Entry<ResourceLocation, List<MineralEntry>>> sortedProfiles = profiles.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();
        for (Map.Entry<ResourceLocation, List<MineralEntry>> profile : sortedProfiles) {
            hash = mix(hash, profile.getKey().toString());
            for (MineralEntry entry : profile.getValue()) {
                hash = mix(hash, entry.oreId().toString());
                hash = mix(hash, entry.weight());
                hash = mix(hash, entry.minY());
                hash = mix(hash, entry.maxY());
                hash = mix(hash, entry.veinSize());
                hash = mix(hash, entry.confidence().ordinal());
            }
        }
        return hash;
    }

    private static long mix(long hash, String value) {
        for (int index = 0; index < value.length(); index++) {
            hash ^= value.charAt(index);
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private static long mix(long hash, long value) {
        hash ^= value;
        return hash * 0x100000001b3L;
    }

    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    public record Profile(long version, List<MineralEntry> entries) {
        public Profile {
            entries = List.copyOf(entries);
        }
    }

    private record Snapshot(Map<ResourceLocation, List<MineralEntry>> profiles, long version) {
    }
}