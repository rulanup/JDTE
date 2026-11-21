package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BioCrusherPayload(int mode) implements CustomPacketPayload {
    public static final Type<BioCrusherPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(JDTE.MODID, "bio_crusher"));

    @Override
    public Type<BioCrusherPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, BioCrusherPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, BioCrusherPayload::mode,
            BioCrusherPayload::new
    );
}
