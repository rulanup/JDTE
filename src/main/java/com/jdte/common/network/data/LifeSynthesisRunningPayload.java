package com.jdte.common.network.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record LifeSynthesisRunningPayload(BlockPos blockPos, boolean running) {
    public static LifeSynthesisRunningPayload decode(FriendlyByteBuf buf) {
        return new LifeSynthesisRunningPayload(buf.readBlockPos(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(running);
    }
}
