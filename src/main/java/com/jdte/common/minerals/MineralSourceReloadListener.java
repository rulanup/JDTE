package com.jdte.common.minerals;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MineralSourceReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile List<MineralSourceOverride> overrides = List.of();

    public MineralSourceReloadListener() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("jdte/mineral_sources"));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager manager,
                         ProfilerFiller profiler) {
        List<MineralSourceOverride> parsed = new ArrayList<>();
        resources.forEach((id, json) -> {
            try {
                parsed.add(parse(GsonHelper.convertToJsonObject(json, id.toString())));
            } catch (RuntimeException exception) {
                LOGGER.error("Failed to parse mineral source {}", id, exception);
            }
        });
        overrides = List.copyOf(parsed);
    }

    public static List<MineralSourceOverride> overrides() {
        return overrides;
    }

    private static MineralSourceOverride parse(JsonObject root) {
        List<Identifier> biomes = readIds(root, "biomes");
        List<Identifier> tags = readIds(root, "biome_tags");
        if (biomes.isEmpty() && tags.isEmpty()) {
            throw new JsonParseException("Expected at least one biome or biome tag");
        }
        List<MineralEntry> entries = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(root, "entries")) {
            JsonObject entry = element.getAsJsonObject();
            Identifier ore = Identifier.parse(GsonHelper.getAsString(entry, "ore"));
            long weight = GsonHelper.getAsLong(entry, "weight", 1L);
            int minY = GsonHelper.getAsInt(entry, "min_y", Integer.MIN_VALUE);
            int maxY = GsonHelper.getAsInt(entry, "max_y", Integer.MAX_VALUE);
            int vein = GsonHelper.getAsInt(entry, "vein_size", 1);
            entries.add(new MineralEntry(ore, weight, minY, maxY, vein, MineralEntry.Confidence.DATA_PACK));
        }
        return new MineralSourceOverride(biomes, tags, entries, GsonHelper.getAsBoolean(root, "replace", false));
    }

    private static List<Identifier> readIds(JsonObject root, String key) {
        if (!root.has(key)) return List.of();
        List<Identifier> result = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(root, key)) {
            result.add(Identifier.parse(element.getAsString()));
        }
        return result;
    }
}
