package com.jdte.common.network.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;

public record EntitySuppressorSyncPayload(BlockPos blockPos, int mode, int target, boolean blacklist,
                                          boolean particleActive, boolean entitySuppressionActive,
                                          boolean renderingSuppressionActive,
                                          double minX, double minY, double minZ,
                                          double maxX, double maxY, double maxZ) {
    public static EntitySuppressorSyncPayload decode(FriendlyByteBuf buf) {
        return new EntitySuppressorSyncPayload(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeVarInt(mode);
        buf.writeVarInt(target);
        buf.writeBoolean(blacklist);
        buf.writeBoolean(particleActive);
        buf.writeBoolean(entitySuppressionActive);
        buf.writeBoolean(renderingSuppressionActive);
        buf.writeDouble(minX);
        buf.writeDouble(minY);
        buf.writeDouble(minZ);
        buf.writeDouble(maxX);
        buf.writeDouble(maxY);
        buf.writeDouble(maxZ);
    }

    public AABB area() {
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
