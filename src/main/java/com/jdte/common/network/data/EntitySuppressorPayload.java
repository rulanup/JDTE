package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EntitySuppressorPayload(int mode, int target, boolean blacklist) implements CustomPacketPayload {
    public static final Type<EntitySuppressorPayload> TYPE = new Type<>(JDTE.id("entity_suppressor"));
    public static final StreamCodec<FriendlyByteBuf, EntitySuppressorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, EntitySuppressorPayload::mode,
            ByteBufCodecs.INT, EntitySuppressorPayload::target,
            ByteBufCodecs.BOOL, EntitySuppressorPayload::blacklist,
            EntitySuppressorPayload::new);
    @Override public Type<EntitySuppressorPayload> type() { return TYPE; }
}
