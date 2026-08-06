package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record FactoryPackerStartPayload() {
    public static FactoryPackerStartPayload decode(FriendlyByteBuf buf) {
        return new FactoryPackerStartPayload();
    }

    public void encode(FriendlyByteBuf buf) {
    }
}
