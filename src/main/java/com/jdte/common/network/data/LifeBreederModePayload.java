package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record LifeBreederModePayload(int mode) {
    public static LifeBreederModePayload decode(FriendlyByteBuf buf) {
        return new LifeBreederModePayload(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(mode);
    }
}
