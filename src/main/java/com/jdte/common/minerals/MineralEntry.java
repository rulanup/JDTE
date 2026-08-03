package com.jdte.common.minerals;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record MineralEntry(
        ResourceLocation oreId,
        long weight,
        int minY,
        int maxY,
        int veinSize,
        Confidence confidence
) {
    public static final Codec<MineralEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("ore").forGetter(MineralEntry::oreId),
            Codec.LONG.fieldOf("weight").forGetter(MineralEntry::weight),
            Codec.INT.optionalFieldOf("min_y", Integer.MIN_VALUE).forGetter(MineralEntry::minY),
            Codec.INT.optionalFieldOf("max_y", Integer.MAX_VALUE).forGetter(MineralEntry::maxY),
            Codec.INT.optionalFieldOf("vein_size", 1).forGetter(MineralEntry::veinSize),
            Confidence.CODEC.optionalFieldOf("confidence", Confidence.ESTIMATED).forGetter(MineralEntry::confidence)
    ).apply(instance, MineralEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MineralEntry> STREAM_CODEC = StreamCodec.of(
            (buffer, entry) -> {
                ResourceLocation.STREAM_CODEC.encode(buffer, entry.oreId());
                buffer.writeVarLong(entry.weight());
                buffer.writeVarInt(entry.minY());
                buffer.writeVarInt(entry.maxY());
                buffer.writeVarInt(entry.veinSize());
                buffer.writeEnum(entry.confidence());
            },
            buffer -> new MineralEntry(
                    ResourceLocation.STREAM_CODEC.decode(buffer),
                    buffer.readVarLong(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readEnum(Confidence.class)));

    public MineralEntry {
        weight = Math.max(1L, weight);
        if (minY > maxY) {
            int swap = minY;
            minY = maxY;
            maxY = swap;
        }
        veinSize = Math.max(1, veinSize);
        confidence = confidence == null ? Confidence.ESTIMATED : confidence;
    }

    public enum Confidence {
        EXACT,
        ESTIMATED,
        DATA_PACK;

        public static final Codec<Confidence> CODEC = Codec.STRING.xmap(
                value -> Confidence.valueOf(value.toUpperCase(java.util.Locale.ROOT)),
                value -> value.name().toLowerCase(java.util.Locale.ROOT));
    }
}