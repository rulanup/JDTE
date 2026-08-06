package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LifeBreederModePayload(int mode) implements CustomPacketPayload {
    public static final Type<LifeBreederModePayload> TYPE = new Type<>(JDTE.id("life_breeder_mode"));
    public static final StreamCodec<FriendlyByteBuf, LifeBreederModePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, LifeBreederModePayload::mode, LifeBreederModePayload::new);
    @Override public Type<LifeBreederModePayload> type() { return TYPE; }
}
