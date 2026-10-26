package com.jdte.common.minerals;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record MineralSurveyData(
        int schemaVersion,
        long indexVersion,
        ResourceLocation biomeId,
        ResourceLocation dimensionId,
        List<MineralEntry> entries
) {
    public static final int CURRENT_SCHEMA = 1;

    public static final Codec<MineralSurveyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema", CURRENT_SCHEMA).forGetter(MineralSurveyData::schemaVersion),
            Codec.LONG.optionalFieldOf("index_version", 0L).forGetter(MineralSurveyData::indexVersion),
            ResourceLocation.CODEC.fieldOf("biome").forGetter(MineralSurveyData::biomeId),
            ResourceLocation.CODEC.fieldOf("dimension").forGetter(MineralSurveyData::dimensionId),
            MineralEntry.CODEC.listOf().fieldOf("entries").forGetter(MineralSurveyData::entries)
    ).apply(instance, MineralSurveyData::new));

    public static void write(FriendlyByteBuf buffer, MineralSurveyData survey) {
        buffer.writeVarInt(survey.schemaVersion());
        buffer.writeVarLong(survey.indexVersion());
        buffer.writeResourceLocation(survey.biomeId());
        buffer.writeResourceLocation(survey.dimensionId());
        buffer.writeCollection(survey.entries(), MineralEntry::write);
    }

    public static MineralSurveyData read(FriendlyByteBuf buffer) {
        return new MineralSurveyData(buffer.readVarInt(), buffer.readVarLong(),
                buffer.readResourceLocation(), buffer.readResourceLocation(), buffer.readList(MineralEntry::read));
    }

    public MineralSurveyData {
        entries = List.copyOf(entries);
    }

    public static MineralSurveyData create(long indexVersion, ResourceLocation biomeId,
                                           ResourceLocation dimensionId, List<MineralEntry> entries) {
        return new MineralSurveyData(CURRENT_SCHEMA, indexVersion, biomeId, dimensionId, entries);
    }

    public long totalWeight() {
        long total = 0L;
        for (MineralEntry entry : entries) {
            total = saturatingAdd(total, entry.weight());
        }
        return total;
    }

    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }
}
