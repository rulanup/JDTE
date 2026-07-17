package com.jdte.client;

import com.jdte.client.renderers.AreaPreviewRenderBatch;
import com.jdte.client.renderers.AreaPreviewRenderBatch.BlueprintBlock;
import com.jdte.common.items.FactoryPackageItem;
import com.jdte.common.network.data.FactoryPackagePreviewChunkPayload;
import com.jdte.common.network.data.FactoryPackagePreviewRequestPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FactoryPackagePreviewClient {
    private static final int MAX_CACHE_ENTRIES = 8;
    private static final Map<UUID, CachedPreview> CACHE = new LinkedHashMap<>() {
        @Override protected boolean removeEldestEntry(Map.Entry<UUID, CachedPreview> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    };
    private static final Map<UUID, Assembly> ASSEMBLIES = new LinkedHashMap<>();
    private static UUID requestedId;
    private static long nextRequestTime;

    private FactoryPackagePreviewClient() {}

    public static void acceptChunk(FactoryPackagePreviewChunkPayload payload) {
        if (payload.chunkCount() <= 0 || payload.chunkCount() > 64 || payload.chunkIndex() < 0
                || payload.chunkIndex() >= payload.chunkCount()) return;
        Assembly assembly = ASSEMBLIES.compute(payload.packageId(), (id, current) ->
                current == null || current.chunkCount != payload.chunkCount()
                        ? new Assembly(payload.size(), payload.chunkCount()) : current);
        assembly.accept(payload.chunkIndex(), payload.blocks());
        if (assembly.complete()) {
            CACHE.put(payload.packageId(), new CachedPreview(assembly.size, assembly.flatten()));
            ASSEMBLIES.remove(payload.packageId());
        }
    }

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        Preview preview = getPreview(Minecraft.getInstance());
        if (preview == null) return;
        BlockPos max = preview.origin().offset(preview.rotatedSize()).offset(-1, -1, -1);
        AreaPreviewRenderBatch.enqueueOffset(AABB.encapsulatingFullBlocks(preview.origin(), max));
        if (!preview.blocks().isEmpty()) {
            AreaPreviewRenderBatch.enqueueBlueprint(preview.origin(), preview.blocks());
        }
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) return;
        Preview preview = getPreview(minecraft);
        if (preview == null) return;
        Vec3i size = preview.rotatedSize();
        Component text = Component.translatable("message.jdte.factory_package.preview",
                size.getX(), size.getY(), size.getZ(), preview.origin().getX(),
                preview.origin().getY(), preview.origin().getZ());
        event.getGuiGraphics().drawCenteredString(minecraft.font, text,
                event.getGuiGraphics().guiWidth() / 2,
                event.getGuiGraphics().guiHeight() / 2 + 28, 0x55FFFF);
    }

    private static Preview getPreview(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) return null;
        ItemStack stack = heldPackage(player);
        var id = FactoryPackageItem.getPackageId(stack);
        var size = FactoryPackageItem.getSize(stack);
        if (id.isEmpty() || size.isEmpty()) return null;
        requestIfNeeded(id.get());
        int rotation = FactoryPackageItem.getRotation(stack);
        Vec3i rotatedSize = FactoryPackageItem.getRotatedSize(stack);
        BlockPos origin;
        var target = FactoryPackageItem.getPlacementTarget(stack);
        if (target.isPresent() && target.get().dimension().equals(minecraft.level.dimension().location())) {
            origin = target.get().origin();
        } else if (minecraft.hitResult instanceof BlockHitResult hit
                && hit.getType() == HitResult.Type.BLOCK) {
            origin = FactoryPackageItem.getPlacementOrigin(hit.getBlockPos(), hit.getDirection(), rotatedSize);
        } else {
            return null;
        }
        CachedPreview cached = CACHE.get(id.get());
        List<BlueprintBlock> blocks = cached == null ? List.of() : cached.blocks(rotation);
        return new Preview(origin, rotatedSize, blocks);
    }

    private static void requestIfNeeded(UUID id) {
        if (CACHE.containsKey(id)) return;
        long now = System.currentTimeMillis();
        if (id.equals(requestedId) && now < nextRequestTime) return;
        requestedId = id;
        nextRequestTime = now + 2000L;
        PacketDistributor.sendToServer(new FactoryPackagePreviewRequestPayload(id));
    }

    private static ItemStack heldPackage(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (FactoryPackageItem.isFilled(mainHand)) return mainHand;
        ItemStack offHand = player.getOffhandItem();
        return FactoryPackageItem.isFilled(offHand) ? offHand : ItemStack.EMPTY;
    }

    private static BlockPos rotate(BlockPos pos, Vec3i size, int rotation) {
        return switch (Math.floorMod(rotation, 4)) {
            case 1 -> new BlockPos(size.getZ() - 1 - pos.getZ(), pos.getY(), pos.getX());
            case 2 -> new BlockPos(size.getX() - 1 - pos.getX(), pos.getY(), size.getZ() - 1 - pos.getZ());
            case 3 -> new BlockPos(pos.getZ(), pos.getY(), size.getX() - 1 - pos.getX());
            default -> pos;
        };
    }

    private static Rotation rotation(int quarterTurns) {
        return switch (Math.floorMod(quarterTurns, 4)) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private record Preview(BlockPos origin, Vec3i rotatedSize, List<BlueprintBlock> blocks) {}

    private static final class CachedPreview {
        private final Vec3i size;
        private final List<FactoryPackagePreviewChunkPayload.PreviewBlock> sourceBlocks;
        private final List<BlueprintBlock>[] rotatedBlocks = new List[4];

        private CachedPreview(Vec3i size, List<FactoryPackagePreviewChunkPayload.PreviewBlock> sourceBlocks) {
            this.size = size;
            this.sourceBlocks = sourceBlocks;
        }

        private List<BlueprintBlock> blocks(int quarterTurns) {
            int index = Math.floorMod(quarterTurns, 4);
            if (rotatedBlocks[index] == null) {
                Rotation stateRotation = rotation(index);
                rotatedBlocks[index] = sourceBlocks.stream().map(block -> {
                    BlockState state = Block.BLOCK_STATE_REGISTRY.byId(block.stateId());
                    if (state == null) state = Blocks.AIR.defaultBlockState();
                    return new BlueprintBlock(rotate(block.relativePos(), size, index), state.rotate(stateRotation));
                }).filter(block -> !block.state().isAir()).toList();
            }
            return rotatedBlocks[index];
        }
    }

    private static final class Assembly {
        private final Vec3i size;
        private final int chunkCount;
        private final List<FactoryPackagePreviewChunkPayload.PreviewBlock>[] chunks;

        private Assembly(Vec3i size, int chunkCount) {
            this.size = size;
            this.chunkCount = chunkCount;
            this.chunks = new List[chunkCount];
        }

        private void accept(int index, List<FactoryPackagePreviewChunkPayload.PreviewBlock> blocks) {
            chunks[index] = List.copyOf(blocks);
        }
        private boolean complete() {
            for (List<FactoryPackagePreviewChunkPayload.PreviewBlock> chunk : chunks) if (chunk == null) return false;
            return true;
        }
        private List<FactoryPackagePreviewChunkPayload.PreviewBlock> flatten() {
            List<FactoryPackagePreviewChunkPayload.PreviewBlock> result = new ArrayList<>();
            for (List<FactoryPackagePreviewChunkPayload.PreviewBlock> chunk : chunks) result.addAll(chunk);
            return List.copyOf(result);
        }
    }
}
