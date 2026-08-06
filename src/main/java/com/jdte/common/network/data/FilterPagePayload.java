package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FilterPagePayload(int page) implements CustomPacketPayload {
    public static final Type<FilterPagePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "filter_page"));

    @Override
    public Type<FilterPagePayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, FilterPagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, FilterPagePayload::page,
            FilterPagePayload::new
    );
}
