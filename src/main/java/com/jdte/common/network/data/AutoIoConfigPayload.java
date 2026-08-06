package com.jdte.common.network.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record AutoIoConfigPayload(BlockPos blockPos, int inputMask, int outputMask, boolean request) {
    public static AutoIoConfigPayload decode(FriendlyByteBuf buf) {
        return new AutoIoConfigPayload(buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeInt(inputMask);
        buf.writeInt(outputMask);
        buf.writeBoolean(request);
    }
}
