package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record FactoryPackageRotatePayload(int delta) {
    public static FactoryPackageRotatePayload decode(FriendlyByteBuf buf) {
        return new FactoryPackageRotatePayload(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(delta);
    }
}
