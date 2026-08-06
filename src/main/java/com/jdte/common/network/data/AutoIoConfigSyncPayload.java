package com.jdte.common.network.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record AutoIoConfigSyncPayload(BlockPos blockPos, int inputMask, int outputMask) {
    public static AutoIoConfigSyncPayload decode(FriendlyByteBuf buf) {
        return new AutoIoConfigSyncPayload(buf.readBlockPos(), buf.readInt(), buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeInt(inputMask);
        buf.writeInt(outputMask);
    }
}
