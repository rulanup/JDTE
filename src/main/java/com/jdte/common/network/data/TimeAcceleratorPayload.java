package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record TimeAcceleratorPayload(int multiplier) {
    public static TimeAcceleratorPayload decode(FriendlyByteBuf buf) {
        return new TimeAcceleratorPayload(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(multiplier);
    }
}
