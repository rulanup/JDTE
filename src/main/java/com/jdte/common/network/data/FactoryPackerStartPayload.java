package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FactoryPackerStartPayload() implements CustomPacketPayload {
    public static final Type<FactoryPackerStartPayload> TYPE = new Type<>(JDTE.id("factory_packer_start"));
    public static final StreamCodec<FriendlyByteBuf, FactoryPackerStartPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> { }, buffer -> new FactoryPackerStartPayload());

    @Override public Type<FactoryPackerStartPayload> type() { return TYPE; }
}
