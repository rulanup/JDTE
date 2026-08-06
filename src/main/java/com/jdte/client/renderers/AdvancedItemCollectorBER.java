package com.jdte.client.renderers;

import com.jdte.common.blockentities.AdvancedItemCollectorBE;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;

public class AdvancedItemCollectorBER extends AreaAffectingBER {
    public AdvancedItemCollectorBER(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        super.render(blockEntity, partialTicks, poseStack, buffers, packedLight, packedOverlay);
        if (!(blockEntity instanceof AdvancedItemCollectorBE) || blockEntity.getLevel() == null) return;

        long gameTime = blockEntity.getLevel().getGameTime();
        Direction direction = blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
        renderPortalCube(direction, poseStack.last().pose(), buffers.getBuffer(RenderType.endPortal()),
                gameTime, partialTicks);
    }

    private static void renderPortalCube(Direction direction, Matrix4f pose, VertexConsumer vertices,
                                         long gameTime, float partialTicks) {
        float animationTick = Math.floorMod(gameTime, 80L) + partialTicks;
        float pulse = Mth.cos(animationTick / 80.0F * (float) (Math.PI * 2.0)) * 0.25F + 0.25F;
        float zero = Mth.lerp(pulse, 0.46875F, 0.4375F);
        float one = Mth.lerp(pulse, 0.53125F, 0.5625F);
        float depth = one - zero;

        switch (direction) {
            case UP -> renderUp(pose, vertices, zero, one, depth);
            case DOWN -> renderDown(pose, vertices, zero, one, depth);
            case NORTH -> renderNorth(pose, vertices, zero, one, depth);
            case SOUTH -> renderSouth(pose, vertices, zero, one, depth);
            case EAST -> renderEast(pose, vertices, zero, one, depth);
            case WEST -> renderWest(pose, vertices, zero, one, depth);
        }
    }

    private static void renderUp(Matrix4f pose, VertexConsumer vertices, float zero, float one, float depth) {
        float face = 0.75F;
        renderFace(pose, vertices, zero, one, face, face + depth, one, one, one, one);
        renderFace(pose, vertices, zero, one, face + depth, face, zero, zero, zero, zero);
        renderFace(pose, vertices, one, one, face + depth, face, zero, one, one, zero);
        renderFace(pose, vertices, zero, zero, face, face + depth, zero, one, one, zero);
        renderFace(pose, vertices, zero, one, face, face, zero, zero, one, one);
        renderFace(pose, vertices, zero, one, face + depth, face + depth, one, one, zero, zero);
    }

    private static void renderDown(Matrix4f pose, VertexConsumer vertices, float zero, float one, float depth) {
        float face = 0.25F;
        renderFace(pose, vertices, zero, one, face, face - depth, one, one, one, one);
        renderFace(pose, vertices, zero, one, face - depth, face, zero, zero, zero, zero);
        renderFace(pose, vertices, one, one, face - depth, face, zero, one, one, zero);
        renderFace(pose, vertices, zero, zero, face, face - depth, zero, one, one, zero);
        renderFace(pose, vertices, zero, one, face, face, zero, zero, one, one);
        renderFace(pose, vertices, zero, one, face - depth, face - depth, one, one, zero, zero);
    }

    private static void renderNorth(Matrix4f pose, VertexConsumer vertices, float zero, float one, float depth) {
        float face = 0.25F;
        renderFace(pose, vertices, zero, one, zero, one, face, face, face, face);
        renderFace(pose, vertices, zero, one, one, zero, face - depth, face - depth, face - depth, face - depth);
        renderFace(pose, vertices, one, one, one, zero, face - depth, face, face, face - depth);
        renderFace(pose, vertices, zero, zero, zero, one, face - depth, face, face, face - depth);
        renderFace(pose, vertices, zero, one, zero, zero, face - depth, face - depth, face, face);
        renderFace(pose, vertices, zero, one, one, one, face, face, face - depth, face - depth);
    }

    private static void renderSouth(Matrix4f pose, VertexConsumer vertices, float zero, float one, float depth) {
        float face = 0.75F;
        renderFace(pose, vertices, zero, one, zero, one, face, face, face, face);
        renderFace(pose, vertices, zero, one, one, zero, face + depth, face + depth, face + depth, face + depth);
        renderFace(pose, vertices, zero, zero, zero, one, face + depth, face, face, face + depth);
        renderFace(pose, vertices, one, one, one, zero, face + depth, face, face, face + depth);
        renderFace(pose, vertices, zero, one, one, one, face, face, face + depth, face + depth);
        renderFace(pose, vertices, zero, one, zero, zero, face + depth, face + depth, face, face);
    }

    private static void renderEast(Matrix4f pose, VertexConsumer vertices, float zero, float one, float depth) {
        float face = 0.75F;
        renderFace(pose, vertices, face, face + depth, zero, one, one, one, one, one);
        renderFace(pose, vertices, face, face + depth, one, zero, zero, zero, zero, zero);
        renderFace(pose, vertices, face + depth, face + depth, one, zero, zero, one, one, zero);
        renderFace(pose, vertices, face, face, zero, one, zero, one, one, zero);
        renderFace(pose, vertices, face, face + depth, zero, zero, zero, zero, one, one);
        renderFace(pose, vertices, face, face + depth, one, one, one, one, zero, zero);
    }

    private static void renderWest(Matrix4f pose, VertexConsumer vertices, float zero, float one, float depth) {
        float face = 0.25F;
        renderFace(pose, vertices, face, face - depth, zero, one, one, one, one, one);
        renderFace(pose, vertices, face, face - depth, one, zero, zero, zero, zero, zero);
        renderFace(pose, vertices, face, face, zero, one, zero, one, one, zero);
        renderFace(pose, vertices, face - depth, face - depth, one, zero, zero, one, one, zero);
        renderFace(pose, vertices, face, face - depth, one, one, one, one, zero, zero);
        renderFace(pose, vertices, face, face - depth, zero, zero, zero, zero, one, one);
    }

    private static void renderFace(Matrix4f pose, VertexConsumer vertices,
                                   float x1, float x2, float y1, float y2,
                                   float z1, float z2, float z3, float z4) {
        vertices.addVertex(pose, x1, y1, z1);
        vertices.addVertex(pose, x2, y1, z2);
        vertices.addVertex(pose, x2, y2, z3);
        vertices.addVertex(pose, x1, y2, z4);
    }
}
