package com.jdte.client.renderers;

import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class LargeGreenhouseBER implements BlockEntityRenderer<LargeGreenhouseBE, GreenhouseCropRenderState> {
    private final BlockModelResolver blockRenderer;

    public LargeGreenhouseBER(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.blockModelResolver();
    }

    @Override
    public GreenhouseCropRenderState createRenderState() {
        return new GreenhouseCropRenderState(LargeGreenhouseBE.INPUT_SLOTS);
    }

    @Override
    public void extractRenderState(LargeGreenhouseBE greenhouse, GreenhouseCropRenderState state, float partialTick,
                                   Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(greenhouse, state, partialTick, cameraPosition, breakProgress);
        state.extractCrops(greenhouse, greenhouse::getDisplayCropState, blockRenderer);
        state.facing = greenhouse.getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public void submit(GreenhouseCropRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        Direction facing = state.facing;
        if (!facing.getAxis().isHorizontal()) facing = Direction.NORTH;
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();

        for (int slot = 0; slot < LargeGreenhouseBE.INPUT_SLOTS; slot++) {
            int col = slot % 3;
            int row = slot / 3;
            if (row >= com.jdte.common.blocks.LargeGreenhouseStructure.DEPTH) continue;
            var crop = state.crops[slot];
            if (crop.isEmpty()) continue;
            double lateral = col - 1.0D;
            double depth = row;
            double x = 0.5D + right.getStepX() * lateral + back.getStepX() * depth;
            double z = 0.5D + right.getStepZ() * lateral + back.getStepZ() * depth;
            renderCrop(crop, x, z, poseStack, collector, state.lightCoords);
        }
    }

    private void renderCrop(net.minecraft.client.renderer.block.BlockModelRenderState crop, double x, double z, PoseStack poseStack,
                            SubmitNodeCollector collector, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(x, 0.25D, z);
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        crop.submit(poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}
