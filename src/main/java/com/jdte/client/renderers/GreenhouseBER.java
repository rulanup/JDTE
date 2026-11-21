package com.jdte.client.renderers;

import com.jdte.common.blockentities.GreenhouseBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class GreenhouseBER implements BlockEntityRenderer<GreenhouseBE, GreenhouseCropRenderState> {
    private final BlockModelResolver blockRenderer;

    public GreenhouseBER(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.blockModelResolver();
    }

    @Override
    public GreenhouseCropRenderState createRenderState() {
        return new GreenhouseCropRenderState(GreenhouseBE.INPUT_SLOTS);
    }

    @Override
    public void extractRenderState(GreenhouseBE greenhouse, GreenhouseCropRenderState state, float partialTick,
                                   Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(greenhouse, state, partialTick, cameraPosition, breakProgress);
        state.extractCrops(greenhouse, greenhouse::getDisplayCropState, blockRenderer);
    }

    @Override
    public void submit(GreenhouseCropRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        for (int slot = 0; slot < GreenhouseBE.INPUT_SLOTS; slot++) {
            var crop = state.crops[slot];
            if (crop.isEmpty()) {
                continue;
            }
            double x = slot % 2 == 0 ? 0.12D : 0.55D;
            double z = slot / 2 == 0 ? 0.12D : 0.55D;
            poseStack.pushPose();
            poseStack.translate(x, 0.18D, z);
            poseStack.scale(0.33F, 0.33F, 0.33F);
            crop.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
