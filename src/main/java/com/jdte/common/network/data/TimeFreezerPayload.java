package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TimeFreezerPayload(boolean timeFreezeEnabled, boolean weatherFreezeEnabled) implements CustomPacketPayload {
    public static final Type<TimeFreezerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "time_freezer"));

    @Override
    public Type<TimeFreezerPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, TimeFreezerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, TimeFreezerPayload::timeFreezeEnabled,
            ByteBufCodecs.BOOL, TimeFreezerPayload::weatherFreezeEnabled,
            TimeFreezerPayload::new
    );
}
