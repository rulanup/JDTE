package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RangeBlockerPayload(int mode, int target, boolean blacklist) implements CustomPacketPayload {
    public static final Type<RangeBlockerPayload> TYPE = new Type<>(JDTE.id("range_blocker"));
    public static final StreamCodec<FriendlyByteBuf, RangeBlockerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, RangeBlockerPayload::mode,
            ByteBufCodecs.INT, RangeBlockerPayload::target,
            ByteBufCodecs.BOOL, RangeBlockerPayload::blacklist,
            RangeBlockerPayload::new);

    @Override public Type<RangeBlockerPayload> type() { return TYPE; }
}
