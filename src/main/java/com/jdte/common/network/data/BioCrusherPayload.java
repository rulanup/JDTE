package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record BioCrusherPayload(int mode) {
    public static BioCrusherPayload decode(FriendlyByteBuf buf) {
        return new BioCrusherPayload(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(mode);
    }
}
