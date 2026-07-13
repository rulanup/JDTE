package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.AABB;

public record RangeBlockerSyncPayload(BlockPos blockPos, int mode, boolean blacklist, boolean active,
                                      double minX, double minY, double minZ,
                                      double maxX, double maxY, double maxZ) implements CustomPacketPayload {
    public static final Type<RangeBlockerSyncPayload> TYPE = new Type<>(JDTE.id("range_blocker_sync"));
    public static final StreamCodec<FriendlyByteBuf, RangeBlockerSyncPayload> STREAM_CODEC = StreamCodec.of(
            RangeBlockerSyncPayload::encode, RangeBlockerSyncPayload::decode);

    private static void encode(FriendlyByteBuf buf, RangeBlockerSyncPayload payload) {
        buf.writeBlockPos(payload.blockPos);
        buf.writeVarInt(payload.mode);
        buf.writeBoolean(payload.blacklist);
        buf.writeBoolean(payload.active);
        buf.writeDouble(payload.minX);
        buf.writeDouble(payload.minY);
        buf.writeDouble(payload.minZ);
        buf.writeDouble(payload.maxX);
        buf.writeDouble(payload.maxY);
        buf.writeDouble(payload.maxZ);
    }

    private static RangeBlockerSyncPayload decode(FriendlyByteBuf buf) {
        return new RangeBlockerSyncPayload(buf.readBlockPos(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public AABB area() { return new AABB(minX, minY, minZ, maxX, maxY, maxZ); }
    @Override public Type<RangeBlockerSyncPayload> type() { return TYPE; }
}
