package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MineralExtractorOutputPagePayload(int page) implements CustomPacketPayload {
    public static final Type<MineralExtractorOutputPagePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "mineral_extractor_output_page"));
    public static final StreamCodec<FriendlyByteBuf, MineralExtractorOutputPagePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, MineralExtractorOutputPagePayload::page,
                    MineralExtractorOutputPagePayload::new);

    @Override
    public Type<MineralExtractorOutputPagePayload> type() {
        return TYPE;
    }
}