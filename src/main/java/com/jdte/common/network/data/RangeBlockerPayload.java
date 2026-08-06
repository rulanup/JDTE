package com.jdte.common.network.data;

import net.minecraft.network.FriendlyByteBuf;

public record RangeBlockerPayload(int mode, int target, boolean blacklist) {
    public static RangeBlockerPayload decode(FriendlyByteBuf buf) {
        return new RangeBlockerPayload(buf.readInt(), buf.readInt(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(mode);
        buf.writeInt(target);
        buf.writeBoolean(blacklist);
    }
}
