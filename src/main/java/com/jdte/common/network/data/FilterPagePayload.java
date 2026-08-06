package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record FilterPagePayload(int page) {
    public static FilterPagePayload decode(FriendlyByteBuf buf) {
        return new FilterPagePayload(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(page);
    }
}
