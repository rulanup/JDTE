package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record TimeFreezerPayload(boolean timeFreezeEnabled, boolean weatherFreezeEnabled) {
    public static TimeFreezerPayload decode(FriendlyByteBuf buf) {
        return new TimeFreezerPayload(buf.readBoolean(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(timeFreezeEnabled);
        buf.writeBoolean(weatherFreezeEnabled);
    }
}
