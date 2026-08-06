package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record GelGeneratorPayload(boolean autoBalanceInputs) {
    public static GelGeneratorPayload decode(FriendlyByteBuf buf) {
        return new GelGeneratorPayload(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(autoBalanceInputs);
    }
}
