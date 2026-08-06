package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record LifeExtractorPayload(int mode) {
    public static LifeExtractorPayload decode(FriendlyByteBuf buf) {
        return new LifeExtractorPayload(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(mode);
    }
}
