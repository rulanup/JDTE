package com.jdte.client.renderers;

import com.jdte.common.blockentities.GreenhouseBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;

public class GreenhouseBER implements BlockEntityRenderer<GreenhouseBE> {
    private final BlockRenderDispatcher blockRenderer;

    public GreenhouseBER(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(GreenhouseBE greenhouse, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        for (int slot = 0; slot < GreenhouseBE.INPUT_SLOTS; slot++) {
            BlockState crop = greenhouse.getDisplayCropState(slot);
            if (crop == null) {
                continue;
            }
            double x = slot % 2 == 0 ? 0.12D : 0.55D;
            double z = slot / 2 == 0 ? 0.12D : 0.55D;
            poseStack.pushPose();
            poseStack.translate(x, 0.18D, z);
            poseStack.scale(0.33F, 0.33F, 0.33F);
            blockRenderer.renderSingleBlock(crop, poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
}
