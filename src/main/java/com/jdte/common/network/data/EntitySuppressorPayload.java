package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record EntitySuppressorPayload(int mode, int target, boolean blacklist) {
    public static EntitySuppressorPayload decode(FriendlyByteBuf buf) {
        return new EntitySuppressorPayload(buf.readInt(), buf.readInt(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(mode);
        buf.writeInt(target);
        buf.writeBoolean(blacklist);
    }
}
