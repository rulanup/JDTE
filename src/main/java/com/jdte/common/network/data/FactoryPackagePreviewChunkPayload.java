package com.jdte.common.network.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record FactoryPackagePreviewChunkPayload(UUID packageId, Vec3i size, int chunkIndex, int chunkCount,
                                                List<PreviewBlock> blocks) {
    public static FactoryPackagePreviewChunkPayload decode(FriendlyByteBuf buf) {
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

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(packageId);
        buf.writeVarInt(size.getX());
        buf.writeVarInt(size.getY());
        buf.writeVarInt(size.getZ());
        buf.writeVarInt(chunkIndex);
        buf.writeVarInt(chunkCount);
        buf.writeVarInt(blocks.size());
        for (PreviewBlock block : blocks) {
            buf.writeBlockPos(block.relativePos());
            buf.writeVarInt(block.stateId());
        }
    }

    public record PreviewBlock(BlockPos relativePos, int stateId) {
    }
}
