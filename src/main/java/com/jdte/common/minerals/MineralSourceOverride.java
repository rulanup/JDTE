package com.jdte.common.minerals;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public record MineralSourceOverride(
        List<ResourceLocation> biomes,
        List<ResourceLocation> biomeTags,
        List<MineralEntry> entries,
        boolean replace
) {
    public MineralSourceOverride {
        biomes = List.copyOf(biomes);
        biomeTags = List.copyOf(biomeTags);
        entries = List.copyOf(entries);
    }

    public boolean matches(ResourceLocation biomeId, Holder<Biome> biome) {
        if (biomes.contains(biomeId)) return true;
        return biomeTags.stream().anyMatch(id -> biome.is(TagKey.create(Registries.BIOME, id)));
    }
}