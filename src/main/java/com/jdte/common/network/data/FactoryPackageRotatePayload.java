package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FactoryPackageRotatePayload(int delta) implements CustomPacketPayload {
    public static final Type<FactoryPackageRotatePayload> TYPE = new Type<>(JDTE.id("factory_package_rotate"));
    public static final StreamCodec<FriendlyByteBuf, FactoryPackageRotatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, FactoryPackageRotatePayload::delta, FactoryPackageRotatePayload::new);

    @Override public Type<FactoryPackageRotatePayload> type() { return TYPE; }
}
