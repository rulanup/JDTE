package com.jdte.client.renderers;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public class AreaAffectingBER implements BlockEntityRenderer<BlockEntity> {
    public AreaAffectingBER(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlockEntity blockentity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
        if (blockentity instanceof AreaAffectingBE areaAffectingBE) {
            if (areaAffectingBE.getAreaAffectingData().renderArea) {
                BlockPos blockPos = blockentity.getBlockPos();
                AreaPreviewRenderBatch.enqueueMain(areaAffectingBE.getAABB(BlockPos.ZERO).move(blockPos));
                if (areaAffectingBE.getAreaAffectingData().xRadius > 0 || areaAffectingBE.getAreaAffectingData().yRadius > 0 || areaAffectingBE.getAreaAffectingData().zRadius > 0) {
                    AreaPreviewRenderBatch.enqueueOffset(areaAffectingBE.getAABBOffsetOnly(BlockPos.ZERO).move(blockPos));
                }
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntity blockEntity) {
        return AABB.encapsulatingFullBlocks(blockEntity.getBlockPos().above(10).north(10).east(10), blockEntity.getBlockPos().below(10).south(10).west(10));
    }
}
