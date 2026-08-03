package com.jdte.common.minerals;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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

    public static final StreamCodec<RegistryFriendlyByteBuf, MineralSurveyData> STREAM_CODEC = StreamCodec.of(
            (buffer, survey) -> {
                buffer.writeVarInt(survey.schemaVersion());
                buffer.writeVarLong(survey.indexVersion());
                ResourceLocation.STREAM_CODEC.encode(buffer, survey.biomeId());
                ResourceLocation.STREAM_CODEC.encode(buffer, survey.dimensionId());
                MineralEntry.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, survey.entries());
            },
            buffer -> new MineralSurveyData(
                    buffer.readVarInt(),
                    buffer.readVarLong(),
                    ResourceLocation.STREAM_CODEC.decode(buffer),
                    ResourceLocation.STREAM_CODEC.decode(buffer),
                    MineralEntry.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer)));

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