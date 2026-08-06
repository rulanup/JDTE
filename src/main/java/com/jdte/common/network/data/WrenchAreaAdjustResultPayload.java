package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record WrenchAreaAdjustResultPayload(double radius, double maxRadius) {
    public static WrenchAreaAdjustResultPayload decode(FriendlyByteBuf buf) {
        return new WrenchAreaAdjustResultPayload(buf.readDouble(), buf.readDouble());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(radius);
        buf.writeDouble(maxRadius);
    }
}
