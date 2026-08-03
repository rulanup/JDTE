package com.jdte.common.minerals;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MineralFeatureAnalyzer {
    private static final long ATTEMPT_SCALE = 1_000_000L;

    private MineralFeatureAnalyzer() {
    }

    public static List<MineralEntry> analyze(Holder<Biome> biome, int maxEntries) {
        Map<ResourceLocation, MutableEntry> minerals = new LinkedHashMap<>();
        List<HolderSet<PlacedFeature>> steps = biome.value().getGenerationSettings().features();
        int oreStep = GenerationStep.Decoration.UNDERGROUND_ORES.ordinal();
        if (oreStep < steps.size()) {
            steps.get(oreStep).forEach(feature -> analyzePlaced(feature.value(), minerals));
        }
        return minerals.values().stream()
                .map(MutableEntry::freeze)
                .sorted(Comparator.comparingLong(MineralEntry::weight).reversed()
                        .thenComparing(entry -> entry.oreId().toString()))
                .limit(Math.max(1, maxEntries))
                .toList();
    }

    private static void analyzePlaced(PlacedFeature placed, Map<ResourceLocation, MutableEntry> minerals) {
        ConfiguredFeature<?, ?> configured = placed.feature().value();
        if (!(configured.config() instanceof OreConfiguration ore)) return;

        PlacementEstimate placement = estimatePlacement(placed.placement());
        int targetCount = Math.max(1, ore.targetStates.size());
        long totalWeight = Math.max(1L, saturatingMultiply(placement.attempts(), Math.max(1, ore.size)));
        long targetWeight = Math.max(1L, totalWeight / targetCount);
        for (OreConfiguration.TargetBlockState target : ore.targetStates) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(target.state.getBlock());
            if (blockId == null) continue;
            minerals.computeIfAbsent(blockId, MutableEntry::new)
                    .merge(targetWeight, placement.minY(), placement.maxY(), ore.size,
                            MineralEntry.Confidence.ESTIMATED);
        }
    }

    private static PlacementEstimate estimatePlacement(List<PlacementModifier> modifiers) {
        long attempts = ATTEMPT_SCALE;
        int minY = Integer.MIN_VALUE;
        int maxY = Integer.MAX_VALUE;
        for (PlacementModifier modifier : modifiers) {
            JsonElement encoded = PlacementModifier.CODEC.encodeStart(JsonOps.INSTANCE, modifier)
                    .result().orElse(null);
            if (!(encoded instanceof JsonObject object)) continue;
            String type = object.has("type") ? object.get("type").getAsString() : "";
            if (type.endsWith("count")) {
                attempts = saturatingMultiply(ATTEMPT_SCALE,
                        Math.max(1L, estimateIntProvider(object.get("count"))));
            } else if (type.endsWith("rarity_filter") && object.has("chance")) {
                attempts = Math.max(1L, ATTEMPT_SCALE / Math.max(1, object.get("chance").getAsInt()));
            } else if (type.endsWith("height_range") && object.has("height")) {
                HeightBounds bounds = readHeightBounds(object.getAsJsonObject("height"));
                minY = bounds.minY();
                maxY = bounds.maxY();
            }
        }
        return new PlacementEstimate(attempts, minY, maxY);
    }

    private static long estimateIntProvider(JsonElement element) {
        if (element == null) return 1L;
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return Math.max(1L, element.getAsLong());
        }
        if (!(element instanceof JsonObject object)) return 1L;
        if (object.has("value")) return estimateIntProvider(object.get("value"));
        long min = object.has("min_inclusive") ? estimateIntProvider(object.get("min_inclusive")) : 1L;
        long max = object.has("max_inclusive") ? estimateIntProvider(object.get("max_inclusive")) : min;
        return Math.max(1L, (min + max + 1L) / 2L);
    }

    private static HeightBounds readHeightBounds(JsonObject height) {
        int min = readAbsoluteAnchor(height.get("min_inclusive"), Integer.MIN_VALUE);
        int max = readAbsoluteAnchor(height.get("max_inclusive"), Integer.MAX_VALUE);
        return new HeightBounds(min, max);
    }

    private static int readAbsoluteAnchor(JsonElement element, int fallback) {
        if (!(element instanceof JsonObject object) || !object.has("absolute")) return fallback;
        return object.get("absolute").getAsInt();
    }

    private static long saturatingMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) return 0L;
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }

    private record PlacementEstimate(long attempts, int minY, int maxY) {
    }

    private record HeightBounds(int minY, int maxY) {
    }

    private static final class MutableEntry {
        private final ResourceLocation oreId;
        private long weight;
        private int minY = Integer.MIN_VALUE;
        private int maxY = Integer.MAX_VALUE;
        private int veinSize = 1;
        private MineralEntry.Confidence confidence = MineralEntry.Confidence.ESTIMATED;

        private MutableEntry(ResourceLocation oreId) {
            this.oreId = oreId;
        }

        private void merge(long addedWeight, int addedMinY, int addedMaxY, int addedVeinSize,
                           MineralEntry.Confidence addedConfidence) {
            weight = addedWeight > Long.MAX_VALUE - weight ? Long.MAX_VALUE : weight + addedWeight;
            if (addedMinY != Integer.MIN_VALUE) minY = minY == Integer.MIN_VALUE ? addedMinY : Math.min(minY, addedMinY);
            if (addedMaxY != Integer.MAX_VALUE) maxY = maxY == Integer.MAX_VALUE ? addedMaxY : Math.max(maxY, addedMaxY);
            veinSize = Math.max(veinSize, addedVeinSize);
            if (addedConfidence.ordinal() > confidence.ordinal()) confidence = addedConfidence;
        }

        private MineralEntry freeze() {
            return new MineralEntry(oreId, Math.max(1L, weight), minY, maxY, veinSize, confidence);
        }
    }
}