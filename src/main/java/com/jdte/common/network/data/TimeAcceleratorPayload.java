package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TimeAcceleratorPayload(int multiplier) implements CustomPacketPayload {
    public static final Type<TimeAcceleratorPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(JDTE.MODID, "time_accelerator"));

    @Override
    public Type<TimeAcceleratorPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, TimeAcceleratorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, TimeAcceleratorPayload::multiplier,
            TimeAcceleratorPayload::new
    );
}
