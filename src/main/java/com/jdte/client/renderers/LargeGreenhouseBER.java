package com.jdte.client.renderers;

import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class LargeGreenhouseBER implements BlockEntityRenderer<LargeGreenhouseBE> {
    private final BlockRenderDispatcher blockRenderer;

    public LargeGreenhouseBER(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(LargeGreenhouseBE greenhouse, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (greenhouse.getLevel() != null && !com.jdte.common.greenhouse.GreenhouseMatrixRenderRegistry.shouldRender(
                greenhouse.getLevel(), greenhouse.getBlockPos())) return;
        Direction facing = greenhouse.getBlockState().getValue(BlockStateProperties.FACING);
        if (!facing.getAxis().isHorizontal()) facing = Direction.NORTH;
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();

        for (int slot = 0; slot < LargeGreenhouseBE.INPUT_SLOTS; slot++) {
            int col = slot % 3;
            int row = slot / 3;
            if (row >= com.jdte.common.blocks.LargeGreenhouseStructure.DEPTH) continue;
            BlockState crop = greenhouse.getDisplayCropState(slot);
            if (crop == null) continue;
            double lateral = col - 1.0D;
            double depth = row;
            double x = 0.5D + right.getStepX() * lateral + back.getStepX() * depth;
            double z = 0.5D + right.getStepZ() * lateral + back.getStepZ() * depth;
            renderCrop(crop, x, z, poseStack, buffers, packedLight);
        }
    }

    private void renderCrop(BlockState crop, double x, double z, PoseStack poseStack,
                            MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(x, 0.25D, z);
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        blockRenderer.renderSingleBlock(crop, poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(LargeGreenhouseBE greenhouse) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}
