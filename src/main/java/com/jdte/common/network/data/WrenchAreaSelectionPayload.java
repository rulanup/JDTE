package com.jdte.common.network.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record WrenchAreaSelectionPayload(BlockPos machinePos, BlockPos firstCorner, BlockPos secondCorner) {
    public static WrenchAreaSelectionPayload decode(FriendlyByteBuf buf) {
        return new WrenchAreaSelectionPayload(buf.readBlockPos(), buf.readBlockPos(), buf.readBlockPos());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(machinePos);
        buf.writeBlockPos(firstCorner);
        buf.writeBlockPos(secondCorner);
    }
}
