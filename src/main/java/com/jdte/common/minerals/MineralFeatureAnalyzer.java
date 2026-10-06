package com.jdte.common.minerals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

public final class MineralFeatureAnalyzer {
    private static final long ATTEMPT_SCALE = 1_000_000L;
    private static final ResourceLocation AIR_ID = ResourceLocation.withDefaultNamespace("air");

    private MineralFeatureAnalyzer() {
    }

    public static List<MineralEntry> analyze(Holder<Biome> biome, int maxEntries) {
        Map<ResourceLocation, MutableEntry> minerals = new LinkedHashMap<>();
        List<HolderSet<PlacedFeature>> steps = biome.value().getGenerationSettings().features();
        Set<PlacedFeature> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (HolderSet<PlacedFeature> step : steps) {
            for (Holder<PlacedFeature> featureHolder : step) {
                PlacedFeature feature = featureHolder.value();
                if (feature != null && visited.add(feature)) {
                    analyzePlaced(feature, minerals);
                }
            }
        }
        return minerals.values().stream()
                .map(MutableEntry::freeze)
                .sorted(Comparator.comparingLong(MineralEntry::weight).reversed()
                        .thenComparing(entry -> entry.oreId().toString()))
                .limit(Math.max(1, maxEntries))
                .toList();
    }

    private static void analyzePlaced(PlacedFeature placed, Map<ResourceLocation, MutableEntry> minerals) {
        if (placed == null) return;
        PlacementEstimate placement = estimatePlacement(placed.placement());
        if (placement.disabled() || placement.attempts() <= 0L) return;

        ConfiguredFeature<?, ?> configured = placed.feature().value();
        ExtractedOreConfig oreConfig = extractOreConfig(configured);
        if (oreConfig == null || oreConfig.targets().isEmpty()) return;

        int targetCount = Math.max(1, oreConfig.targets().size());
        long totalWeight = Math.max(1L, saturatingMultiply(placement.attempts(), Math.max(1, oreConfig.veinSize())));
        long targetWeight = Math.max(1L, totalWeight / targetCount);
        for (ResourceLocation blockId : oreConfig.targets()) {
            if (AIR_ID.equals(blockId)) continue;
            minerals.computeIfAbsent(blockId, MutableEntry::new)
                    .merge(targetWeight, placement.minY(), placement.maxY(), oreConfig.veinSize(),
                            MineralEntry.Confidence.ESTIMATED);
        }
    }

    private static ExtractedOreConfig extractOreConfig(ConfiguredFeature<?, ?> configured) {
        if (configured == null) return null;
        Object config = configured.config();
        if (config == null) return null;

        if (config instanceof Holder<?> holder && holder.value() instanceof ConfiguredFeature<?, ?> child) {
            return extractOreConfig(child);
        }

        // 1. Vanilla OreConfiguration
        if (config instanceof OreConfiguration ore) {
            List<ResourceLocation> targets = new ArrayList<>(ore.targetStates.size());
            for (OreConfiguration.TargetBlockState target : ore.targetStates) {
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(target.state.getBlock());
                if (id != null && !AIR_ID.equals(id)) targets.add(id);
            }
            return new ExtractedOreConfig(targets, Math.max(1, ore.size));
        }

        // 2. Reflection / Duck typing (handles Mekanism ResizableOreFeatureConfig and other mod configs)
        ExtractedOreConfig reflected = extractFromReflection(config);
        if (reflected != null && !reflected.targets().isEmpty()) {
            return reflected;
        }

        // 3. Codec / JSON fallback
        return extractFromJson(configured);
    }

    private static ExtractedOreConfig extractFromReflection(Object config) {
        Class<?> clazz = config.getClass();
        List<ResourceLocation> targetBlocks = null;

        for (String methodName : List.of("targetStates", "targets", "getTargetStates", "getTargets")) {
            try {
                Method method = clazz.getMethod(methodName);
                Object result = method.invoke(config);
                if (result instanceof List<?> list && !list.isEmpty()) {
                    List<ResourceLocation> parsed = parseTargetList(list);
                    if (!parsed.isEmpty()) {
                        targetBlocks = parsed;
                        break;
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        if (targetBlocks == null) {
            for (String fieldName : List.of("targetStates", "targets", "target_states")) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object result = field.get(config);
                    if (result instanceof List<?> list && !list.isEmpty()) {
                        List<ResourceLocation> parsed = parseTargetList(list);
                        if (!parsed.isEmpty()) {
                            targetBlocks = parsed;
                            break;
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }

        if (targetBlocks == null || targetBlocks.isEmpty()) {
            return null;
        }

        int size = 1;
        boolean foundSize = false;
        for (String methodName : List.of("size", "getSize")) {
            try {
                Method method = clazz.getMethod(methodName);
                Object result = method.invoke(config);
                if (result instanceof Number num) {
                    size = Math.max(1, num.intValue());
                    foundSize = true;
                    break;
                } else if (result instanceof IntSupplier supplier) {
                    size = Math.max(1, supplier.getAsInt());
                    foundSize = true;
                    break;
                }
            } catch (Throwable ignored) {
            }
        }

        if (!foundSize) {
            try {
                Field field = clazz.getDeclaredField("size");
                field.setAccessible(true);
                Object val = field.get(config);
                if (val instanceof Number num) {
                    size = Math.max(1, num.intValue());
                } else if (val instanceof IntSupplier supplier) {
                    size = Math.max(1, supplier.getAsInt());
                }
            } catch (Throwable ignored) {
            }
        }

        return new ExtractedOreConfig(targetBlocks, size);
    }

    private static List<ResourceLocation> parseTargetList(List<?> list) {
        List<ResourceLocation> result = new ArrayList<>();
        for (Object item : list) {
            if (item == null) continue;
            if (item instanceof OreConfiguration.TargetBlockState target) {
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(target.state.getBlock());
                if (id != null && !AIR_ID.equals(id)) result.add(id);
            } else if (item instanceof BlockState state) {
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                if (id != null && !AIR_ID.equals(id)) result.add(id);
            } else if (item instanceof Block block) {
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
                if (id != null && !AIR_ID.equals(id)) result.add(id);
            } else {
                ResourceLocation id = extractBlockIdFromObject(item);
                if (id != null && !AIR_ID.equals(id)) result.add(id);
            }
        }
        return result;
    }

    private static ResourceLocation extractBlockIdFromObject(Object item) {
        Class<?> clazz = item.getClass();
        for (String methodName : List.of("state", "getState", "block", "getBlock", "target", "getTarget")) {
            try {
                Method method = clazz.getMethod(methodName);
                Object val = method.invoke(item);
                if (val instanceof BlockState state) {
                    return BuiltInRegistries.BLOCK.getKey(state.getBlock());
                } else if (val instanceof Block block) {
                    return BuiltInRegistries.BLOCK.getKey(block);
                }
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static ExtractedOreConfig extractFromJson(ConfiguredFeature<?, ?> configured) {
        try {
            JsonObject config = null;
            try {
                @SuppressWarnings("unchecked")
                Codec<Object> codec = (Codec<Object>) (Object) configured.feature().configuredCodec().codec();
                JsonElement configElement = codec.encodeStart(JsonOps.INSTANCE, configured).result().orElse(null);
                if (configElement instanceof JsonObject obj) {
                    config = obj.has("config") && obj.get("config").isJsonObject()
                            ? obj.getAsJsonObject("config")
                            : obj;
                }
            } catch (Throwable ignored) {
            }

            if (config == null) {
                JsonElement encoded = ConfiguredFeature.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, configured)
                        .result().orElse(null);
                if (encoded instanceof JsonObject root) {
                    config = root.has("config") && root.get("config").isJsonObject()
                            ? root.getAsJsonObject("config")
                            : root;
                }
            }

            if (config == null) return null;

            JsonArray targetsArray = null;
            if (config.has("targets") && config.get("targets").isJsonArray()) {
                targetsArray = config.getAsJsonArray("targets");
            } else if (config.has("target_states") && config.get("target_states").isJsonArray()) {
                targetsArray = config.getAsJsonArray("target_states");
            }

            if (targetsArray == null || targetsArray.isEmpty()) return null;

            List<ResourceLocation> result = new ArrayList<>();
            for (JsonElement el : targetsArray) {
                ResourceLocation id = parseBlockIdFromJson(el);
                if (id != null && !AIR_ID.equals(id)) result.add(id);
            }
            if (result.isEmpty()) return null;

            int size = 1;
            if (config.has("size")) {
                size = (int) Math.max(1L, estimateIntProvider(config.get("size")));
            }
            return new ExtractedOreConfig(result, size);
        } catch (Throwable ignored) {
            return null;
        }
    }


    private static ResourceLocation parseBlockIdFromJson(JsonElement el) {
        if (el == null) return null;
        if (el.isJsonPrimitive()) {
            return ResourceLocation.tryParse(el.getAsString());
        }
        if (!(el instanceof JsonObject obj)) return null;
        if (obj.has("state")) {
            JsonElement stateEl = obj.get("state");
            if (stateEl.isJsonObject() && stateEl.getAsJsonObject().has("Name")) {
                return ResourceLocation.tryParse(stateEl.getAsJsonObject().get("Name").getAsString());
            } else if (stateEl.isJsonPrimitive()) {
                return ResourceLocation.tryParse(stateEl.getAsString());
            }
        }
        if (obj.has("block")) {
            JsonElement blockEl = obj.get("block");
            if (blockEl.isJsonPrimitive()) {
                return ResourceLocation.tryParse(blockEl.getAsString());
            } else if (blockEl.isJsonObject() && blockEl.getAsJsonObject().has("Name")) {
                return ResourceLocation.tryParse(blockEl.getAsJsonObject().get("Name").getAsString());
            }
        }
        return null;
    }

    private static PlacementEstimate estimatePlacement(List<PlacementModifier> modifiers) {
        long attempts = ATTEMPT_SCALE;
        int minY = Integer.MIN_VALUE;
        int maxY = Integer.MAX_VALUE;
        for (PlacementModifier modifier : modifiers) {
            if (isModifierDisabled(modifier)) {
                return PlacementEstimate.DISABLED;
            }

            if (modifier instanceof CountPlacement countPlacement) {
                long count = estimateCountPlacement(countPlacement);
                if (count > 0L) {
                    attempts = saturatingMultiply(ATTEMPT_SCALE, count);
                    continue;
                }
            } else if (modifier instanceof RarityFilter rarityFilter) {
                int chance = estimateRarityFilter(rarityFilter);
                if (chance > 0) {
                    attempts = Math.max(1L, ATTEMPT_SCALE / chance);
                    continue;
                }
            } else if (modifier instanceof HeightRangePlacement heightRange) {
                HeightBounds bounds = estimateHeightRange(heightRange);
                if (bounds != null) {
                    minY = bounds.minY();
                    maxY = bounds.maxY();
                    continue;
                }
            }

            JsonElement encoded = PlacementModifier.CODEC.encodeStart(JsonOps.INSTANCE, modifier)
                    .result().orElse(null);
            if (!(encoded instanceof JsonObject object)) continue;
            if (object.has("retro_gen") && object.get("retro_gen").getAsBoolean()) {
                return PlacementEstimate.DISABLED;
            }
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
        return new PlacementEstimate(attempts, minY, maxY, false);
    }

    private static boolean isModifierDisabled(PlacementModifier modifier) {
        Class<?> clazz = modifier.getClass();
        for (String methodName : List.of("retroGen", "isRetroGen")) {
            try {
                Method method = clazz.getMethod(methodName);
                Object res = method.invoke(modifier);
                if (Boolean.TRUE.equals(res)) return true;
            } catch (Throwable ignored) {
            }
        }
        for (String methodName : List.of("enabledSupplier", "getEnabledSupplier", "enabled", "isEnabled")) {
            try {
                Method method = clazz.getMethod(methodName);
                Object res = method.invoke(modifier);
                if (res instanceof BooleanSupplier supplier && !supplier.getAsBoolean()) {
                    return true;
                } else if (Boolean.FALSE.equals(res)) {
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static long estimateCountPlacement(CountPlacement countPlacement) {
        try {
            Field countField = CountPlacement.class.getDeclaredField("count");
            countField.setAccessible(true);
            Object countObj = countField.get(countPlacement);
            if (countObj instanceof IntProvider intProvider) {
                try {
                    Method getValue = intProvider.getClass().getMethod("getValue");
                    Object val = getValue.invoke(intProvider);
                    if (val instanceof Number num) return Math.max(1L, num.longValue());
                } catch (Throwable ignored) {
                }
                long min = intProvider.getMinValue();
                long max = intProvider.getMaxValue();
                return Math.max(1L, (min + max + 1L) / 2L);
            }
        } catch (Throwable ignored) {
        }
        return -1L;
    }

    private static int estimateRarityFilter(RarityFilter rarityFilter) {
        try {
            Field chanceField = RarityFilter.class.getDeclaredField("chance");
            chanceField.setAccessible(true);
            return Math.max(1, chanceField.getInt(rarityFilter));
        } catch (Throwable ignored) {
            return -1;
        }
    }

    private static HeightBounds estimateHeightRange(HeightRangePlacement heightRange) {
        try {
            Field heightField = HeightRangePlacement.class.getDeclaredField("height");
            heightField.setAccessible(true);
            Object heightObj = heightField.get(heightRange);
            if (heightObj == null) return null;

            Class<?> clazz = heightObj.getClass();
            Object minAnchor = null;
            Object maxAnchor = null;
            for (String fieldName : List.of("minInclusive", "min_inclusive")) {
                try {
                    Field f = clazz.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    minAnchor = f.get(heightObj);
                    if (minAnchor != null) break;
                } catch (Throwable ignored) {
                }
            }
            for (String fieldName : List.of("maxInclusive", "max_inclusive")) {
                try {
                    Field f = clazz.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    maxAnchor = f.get(heightObj);
                    if (maxAnchor != null) break;
                } catch (Throwable ignored) {
                }
            }
            if (minAnchor != null && maxAnchor != null) {
                int minY = resolveAnchor(minAnchor, Integer.MIN_VALUE);
                int maxY = resolveAnchor(maxAnchor, Integer.MAX_VALUE);
                return new HeightBounds(minY, maxY);
            }

            try {
                Field rangeField = clazz.getDeclaredField("range");
                rangeField.setAccessible(true);
                Object range = rangeField.get(heightObj);
                if (range != null) {
                    Method minMethod = range.getClass().getMethod("minInclusive");
                    Method maxMethod = range.getClass().getMethod("maxInclusive");
                    Object min = minMethod.invoke(range);
                    Object max = maxMethod.invoke(range);
                    if (min != null && max != null) {
                        int minY = resolveAnchor(min, Integer.MIN_VALUE);
                        int maxY = resolveAnchor(max, Integer.MAX_VALUE);
                        return new HeightBounds(minY, maxY);
                    }
                }
            } catch (Throwable ignored) {
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static int resolveAnchor(Object anchor, int fallback) {
        if (anchor instanceof VerticalAnchor.Absolute abs) {
            return abs.y();
        } else if (anchor instanceof VerticalAnchor.AboveBottom above) {
            return -64 + above.offset();
        } else if (anchor instanceof VerticalAnchor.BelowTop below) {
            return 319 - below.offset();
        } else if (anchor != null) {
            try {
                Method anchorTypeMethod = anchor.getClass().getMethod("anchorType");
                Method valueMethod = anchor.getClass().getMethod("value");
                Object anchorTypeObj = anchorTypeMethod.invoke(anchor);
                Object valueObj = valueMethod.invoke(anchor);
                int val = 0;
                if (valueObj instanceof Number num) {
                    val = num.intValue();
                } else if (valueObj != null) {
                    try {
                        Method getMethod = valueObj.getClass().getMethod("get");
                        Object res = getMethod.invoke(valueObj);
                        if (res instanceof Number num) val = num.intValue();
                    } catch (Throwable ignored) {
                    }
                }
                String typeName = anchorTypeObj != null ? anchorTypeObj.toString() : "";
                if ("ABSOLUTE".equals(typeName)) return val;
                if ("ABOVE_BOTTOM".equals(typeName)) return -64 + val;
                if ("BELOW_TOP".equals(typeName)) return 319 - val;
            } catch (Throwable ignored) {
            }
        }
        return fallback;
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
        int min = readAnchor(height.get("min_inclusive"), Integer.MIN_VALUE);
        int max = readAnchor(height.get("max_inclusive"), Integer.MAX_VALUE);
        return new HeightBounds(min, max);
    }

    private static int readAnchor(JsonElement element, int fallback) {
        if (!(element instanceof JsonObject object)) return fallback;
        if (object.has("absolute")) return object.get("absolute").getAsInt();
        if (object.has("above_bottom")) return -64 + object.get("above_bottom").getAsInt();
        if (object.has("below_top")) return 319 - object.get("below_top").getAsInt();
        return fallback;
    }

    private static long saturatingMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) return 0L;
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }

    private record ExtractedOreConfig(List<ResourceLocation> targets, int veinSize) {
    }

    private record PlacementEstimate(long attempts, int minY, int maxY, boolean disabled) {
        private static final PlacementEstimate DISABLED = new PlacementEstimate(0L, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
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