package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record FactoryPackagePreviewRequestPayload(UUID packageId) implements CustomPacketPayload {
    public static final Type<FactoryPackagePreviewRequestPayload> TYPE =
            new Type<>(JDTE.id("factory_package_preview_request"));
    public static final StreamCodec<FriendlyByteBuf, FactoryPackagePreviewRequestPayload> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> buf.writeUUID(payload.packageId()),
                    buf -> new FactoryPackagePreviewRequestPayload(buf.readUUID()));

    @Override public Type<FactoryPackagePreviewRequestPayload> type() { return TYPE; }
}
