package com.jdte.common.network.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record WrenchAreaAdjustPayload(BlockPos blockPos, int delta) {
    public static WrenchAreaAdjustPayload decode(FriendlyByteBuf buf) {
        return new WrenchAreaAdjustPayload(buf.readBlockPos(), buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeInt(delta);
    }
}
