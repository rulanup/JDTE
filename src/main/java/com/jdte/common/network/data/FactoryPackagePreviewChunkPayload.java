package com.jdte.common.network.data;

import com.jdte.JDTE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record FactoryPackagePreviewChunkPayload(UUID packageId, Vec3i size, int chunkIndex, int chunkCount,
                                                List<PreviewBlock> blocks) implements CustomPacketPayload {
    public static final Type<FactoryPackagePreviewChunkPayload> TYPE =
            new Type<>(JDTE.id("factory_package_preview_chunk"));
    public static final StreamCodec<FriendlyByteBuf, FactoryPackagePreviewChunkPayload> STREAM_CODEC =
            StreamCodec.of(FactoryPackagePreviewChunkPayload::encode, FactoryPackagePreviewChunkPayload::decode);

    private static void encode(FriendlyByteBuf buf, FactoryPackagePreviewChunkPayload payload) {
        buf.writeUUID(payload.packageId());
        buf.writeVarInt(payload.size().getX());
        buf.writeVarInt(payload.size().getY());
        buf.writeVarInt(payload.size().getZ());
        buf.writeVarInt(payload.chunkIndex());
        buf.writeVarInt(payload.chunkCount());
        buf.writeVarInt(payload.blocks().size());
        for (PreviewBlock block : payload.blocks()) {
            buf.writeBlockPos(block.relativePos());
            buf.writeVarInt(block.stateId());
        }
    }

    private static FactoryPackagePreviewChunkPayload decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        Vec3i size = new Vec3i(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        int index = buf.readVarInt();
        int count = buf.readVarInt();
        int blockCount = buf.readVarInt();
        List<PreviewBlock> blocks = new ArrayList<>(blockCount);
        for (int i = 0; i < blockCount; i++) {
            blocks.add(new PreviewBlock(buf.readBlockPos(), buf.readVarInt()));
        }
        return new FactoryPackagePreviewChunkPayload(id, size, index, count, List.copyOf(blocks));
    }

    @Override public Type<FactoryPackagePreviewChunkPayload> type() { return TYPE; }

    public record PreviewBlock(BlockPos relativePos, int stateId) {}
}
