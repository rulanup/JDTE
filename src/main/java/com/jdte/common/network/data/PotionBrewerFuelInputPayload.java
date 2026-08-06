package com.jdte.common.network.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record PotionBrewerFuelInputPayload(BlockPos blockPos, boolean enabled) {
    public static PotionBrewerFuelInputPayload decode(FriendlyByteBuf buf) {
        return new PotionBrewerFuelInputPayload(buf.readBlockPos(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(enabled);
    }
}
