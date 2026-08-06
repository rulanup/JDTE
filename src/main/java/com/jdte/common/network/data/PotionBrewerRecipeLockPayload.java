package com.jdte.common.network.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record PotionBrewerRecipeLockPayload(BlockPos blockPos, boolean locked, boolean request) {
    public static PotionBrewerRecipeLockPayload decode(FriendlyByteBuf buf) {
        return new PotionBrewerRecipeLockPayload(buf.readBlockPos(), buf.readBoolean(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(locked);
        buf.writeBoolean(request);
    }
}
