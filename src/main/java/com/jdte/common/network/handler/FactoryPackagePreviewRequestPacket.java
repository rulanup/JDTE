package com.jdte.common.network.handler;

import com.jdte.common.factory.FactoryPackageStorage;
import com.jdte.common.factory.FactoryPackageStorage.BlockRecord;
import com.jdte.common.items.FactoryPackageItem;
import com.jdte.common.network.data.FactoryPackagePreviewChunkPayload;
import com.jdte.common.network.data.FactoryPackagePreviewChunkPayload.PreviewBlock;
import com.jdte.common.network.data.FactoryPackagePreviewRequestPayload;
import com.jdte.setup.JDTEConfig;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FactoryPackagePreviewRequestPacket {
    private static final int CHUNK_SIZE = 512;

    private FactoryPackagePreviewRequestPacket() {}

    public static void handle(FactoryPackagePreviewRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack held = FactoryPackageItem.isFilled(player.getMainHandItem())
                    ? player.getMainHandItem() : player.getOffhandItem();
            if (!FactoryPackageItem.getPackageId(held).filter(payload.packageId()::equals).isPresent()) return;
            MinecraftServer server = player.getServer();
            if (server == null) return;
            Util.ioPool().execute(() -> {
                try {
                    var data = FactoryPackageStorage.read(server, payload.packageId(), null,
                            JDTEConfig.COMMON.factoryPackerMaxUncompressedBytes.get(), server.registryAccess());
                    List<PreviewBlock> blocks = selectBlocks(data.blocks(),
                            JDTEConfig.COMMON.factoryPackerPreviewMaxBlocks.get());
                    int chunks = Math.max(1, (blocks.size() + CHUNK_SIZE - 1) / CHUNK_SIZE);
                    server.execute(() -> {
                        if (!player.isRemoved()) {
                            for (int index = 0; index < chunks; index++) {
                                int from = index * CHUNK_SIZE;
                                int to = Math.min(blocks.size(), from + CHUNK_SIZE);
                                PacketDistributor.sendToPlayer(player, new FactoryPackagePreviewChunkPayload(
                                        payload.packageId(), data.size(), index, chunks,
                                        List.copyOf(blocks.subList(from, to))));
                            }
                        }
                    });
                } catch (IOException ignored) {
                    // A claimed, consumed, or deleted package simply has no preview.
                }
            });
        });
    }

    private static List<PreviewBlock> selectBlocks(List<BlockRecord> records, int limit) {
        if (limit <= 0 || records.isEmpty()) return List.of();
        List<BlockRecord> ordered = new ArrayList<>(records);
        ordered.sort(Comparator.comparing((BlockRecord record) -> record.blockEntityData() == null));
        if (ordered.size() <= limit) return ordered.stream().map(FactoryPackagePreviewRequestPacket::previewBlock).toList();
        List<PreviewBlock> result = new ArrayList<>(limit);
        int machineCount = Math.min(limit, (int) ordered.stream().filter(r -> r.blockEntityData() != null).count());
        for (int i = 0; i < machineCount; i++) result.add(previewBlock(ordered.get(i)));
        int remaining = limit - machineCount;
        int ordinary = ordered.size() - machineCount;
        for (int i = 0; i < remaining; i++) {
            int index = machineCount + (int) ((long) i * ordinary / remaining);
            result.add(previewBlock(ordered.get(index)));
        }
        return List.copyOf(result);
    }

    private static PreviewBlock previewBlock(BlockRecord record) {
        return new PreviewBlock(record.relativePos(), Block.BLOCK_STATE_REGISTRY.getId(record.state()));
    }
}
