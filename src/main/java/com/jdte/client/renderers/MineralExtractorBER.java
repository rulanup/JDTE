package com.jdte.client.renderers;

import com.jdte.common.blockentities.LargeMineralExtractorBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blocks.MineralExtractorBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class MineralExtractorBER implements BlockEntityRenderer<MineralExtractorBE> {
    private static final float CORE_WIDTH = 0.375F;
    private static final float CORE_HEIGHT = 0.3125F;
    private static final float ROTATION_SPEED = 2.0F;

    public MineralExtractorBER(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MineralExtractorBE extractor, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (extractor.getLevel() == null) return;
        Direction facing = extractor.getBlockState().getValue(BlockStateProperties.FACING);
        float angle = (extractor.getLevel().getGameTime() + partialTick) * ROTATION_SPEED % 360.0F;

        poseStack.pushPose();
        if (extractor instanceof LargeMineralExtractorBE) {
            // The controller is at the front-center. Move one block toward
            // the structure's depth so the enlarged core stays at its center.
            Direction centerOffset = facing.getOpposite();
            poseStack.translate(
                    0.5D + centerOffset.getStepX(),
                    1.0625D,
                    0.5D + centerOffset.getStepZ());
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.scale(CORE_WIDTH * 3.0F, CORE_HEIGHT * 2.0F, CORE_WIDTH * 3.0F);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
        } else {
            Direction horizontal = extractor.getBlockState().getValue(MineralExtractorBlock.HORIZONTAL_FACING);
            poseStack.translate(0.5D, 0.5D, 0.5D);
            orientFromUp(poseStack, facing, horizontal);
            poseStack.translate(0.0D, 0.03125D, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.scale(CORE_WIDTH, CORE_HEIGHT, CORE_WIDTH);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
        }
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState(), poseStack, buffers, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void orientFromUp(PoseStack poseStack, Direction facing, Direction horizontal) {
        if (!facing.getAxis().isHorizontal()) {
            // 与 blockstate 的 y 旋转保持一致，模型控制器基准位于 west 面。
            int index = Math.floorMod(horizontal.get2DDataValue() - 1, 4);
            poseStack.mulPose(Axis.YP.rotationDegrees(index * 90.0F));
            if (facing == Direction.DOWN) poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            return;
        }
        switch (facing) {
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case SOUTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }
            case EAST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }
            case UP, DOWN -> { }
        }
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}
