package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record FactoryPackagePreviewRequestPayload(UUID packageId) {
    public static FactoryPackagePreviewRequestPayload decode(FriendlyByteBuf buf) {
        return new FactoryPackagePreviewRequestPayload(buf.readUUID());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(packageId);
    }
}
