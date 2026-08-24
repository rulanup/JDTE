package com.jdte.client.entityrenders;

import com.direwolf20.justdirethings.client.renderers.RenderHelpers;
import com.jdte.common.entities.UltimateTimeWandEntity;
import com.jdte.common.items.UltimateTimeWandData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class UltimateTimeWandRenderer extends EntityRenderer<UltimateTimeWandEntity> {
    public UltimateTimeWandRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(UltimateTimeWandEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockPos blockPos = entity.blockPosition();
        if (entity.level().getBlockState(blockPos).isAir()) {
            return;
        }

        int totalTime = entity.getTotalTime();
        if (totalTime <= 0) {
            return;
        }
        int remainingTime = Math.max(0, Math.min(totalTime, entity.getRemainingTime()));
        float timeProgress = clampProgress((float) remainingTime / totalTime);
        int multiplier = UltimateTimeWandData.multiplierForExponent(entity.getExponent());

        poseStack.pushPose();
        renderProgressBarOnSide(poseStack, buffer, 0.3f, multiplier / 4.0f,
                0.0f, 1.0f, 0.0f, 0.5f, packedLight);
        renderProgressBarOnSide(poseStack, buffer, 0.6f, timeProgress,
                1.0f, 0.0f, 0.0f, 0.5f, packedLight);

        String timeRate = "x" + multiplier;
        String timeRemains = String.format("%.2f", remainingTime / 20.0f) + "s";
        float paddingLeftRight = 0.13F;
        drawText(poseStack, buffer, timeRate, new Vector3f(-paddingLeftRight, 0.39f, 0.51F), Axis.YP.rotationDegrees(0), ChatFormatting.WHITE.getColor());
        drawText(poseStack, buffer, timeRate, new Vector3f(paddingLeftRight, 0.39f, -0.51F), Axis.YP.rotationDegrees(180F), ChatFormatting.WHITE.getColor());
        drawText(poseStack, buffer, timeRate, new Vector3f(0.51F, 0.39f, paddingLeftRight), Axis.YP.rotationDegrees(90F), ChatFormatting.WHITE.getColor());
        drawText(poseStack, buffer, timeRate, new Vector3f(-0.51F, 0.39f, -paddingLeftRight), Axis.YP.rotationDegrees(-90F), ChatFormatting.WHITE.getColor());
        drawText(poseStack, buffer, timeRate, new Vector3f(-paddingLeftRight, 1.01F, 0.5f - 0.39f), Axis.XP.rotationDegrees(90F), ChatFormatting.WHITE.getColor());
        drawText(poseStack, buffer, timeRate, new Vector3f(-paddingLeftRight, -0.01F, -0.5f + 0.39f), Axis.XP.rotationDegrees(-90F), ChatFormatting.WHITE.getColor());

        drawText(poseStack, buffer, timeRemains, new Vector3f(-paddingLeftRight, 0.69f, 0.51F), Axis.YP.rotationDegrees(0), ChatFormatting.WHITE.getColor());
        drawText(poseStack, buffer, timeRemains, new Vector3f(paddingLeftRight, 0.69f, -0.51F), Axis.YP.rotationDegrees(180F), ChatFormatting.WHITE.getColor());
        drawText(poseStack, buffer, timeRemains, new Vector3f(0.51F, 0.69f, paddingLeftRight), Axis.YP.rotationDegrees(90F), ChatFormatting.WHITE.getColor());
        drawText(poseStack, buffer, timeRemains, new Vector3f(-0.51F, 0.69f, -paddingLeftRight), Axis.YP.rotationDegrees(-90F), ChatFormatting.WHITE.getColor());
        drawText(poseStack, buffer, timeRemains, new Vector3f(-paddingLeftRight, 1.01F, 0.5f - 0.69f), Axis.XP.rotationDegrees(90F), ChatFormatting.WHITE.getColor());
        drawText(poseStack, buffer, timeRemains, new Vector3f(-paddingLeftRight, -0.01F, -0.5f + 0.69f), Axis.XP.rotationDegrees(-90F), ChatFormatting.WHITE.getColor());
        poseStack.popPose();
    }

    private static float clampProgress(float progress) {
        return Math.max(0.0f, Math.min(1.0f, progress));
    }

    private void renderProgressBarOnSide(PoseStack poseStack, MultiBufferSource buffer, float yStart,
                                         float progress, float r, float g, float b, float a, int packedLight) {
        float barWidth = 0.8f;
        float barHeight = 0.1f;
        float barProgress = barWidth * clampProgress(progress);
        renderBarFace(poseStack, buffer, -0.4f, yStart, 0.5f, barProgress, barHeight, r, g, b, a, packedLight, new Quaternionf().rotateY(0.0f));
        renderBarFace(poseStack, buffer, -0.4f, yStart, 0.5f, barProgress, barHeight, r, g, b, a, packedLight, new Quaternionf().rotateY((float) Math.PI));
        renderBarFace(poseStack, buffer, -0.4f, yStart, 0.5f, barProgress, barHeight, r, g, b, a, packedLight, new Quaternionf().rotateY((float) Math.PI / 2.0f));
        renderBarFace(poseStack, buffer, -0.4f, yStart, 0.5f, barProgress, barHeight, r, g, b, a, packedLight, new Quaternionf().rotateY((float) -Math.PI / 2.0f));
        renderBarFace(poseStack, buffer, -0.4f, yStart - 0.5f, 1f, barProgress, barHeight, r, g, b, a, packedLight, new Quaternionf().rotateX((float) -Math.PI / 2.0f));
        renderBarFace(poseStack, buffer, -0.4f, yStart - 0.5f, 0f, barProgress, barHeight, r, g, b, a, packedLight, new Quaternionf().rotateX((float) Math.PI / 2.0f));
    }

    private void renderBarFace(PoseStack poseStack, MultiBufferSource buffer, float xStart, float yStart,
                               float zStart, float barWidth, float barHeight, float r, float g, float b,
                               float a, int packedLight, Quaternionf rotation) {
        poseStack.pushPose();
        poseStack.mulPose(rotation);
        RenderHelpers.renderBoxSolid(poseStack, poseStack.last().pose(), buffer,
                new AABB(xStart, yStart, zStart, xStart + barWidth, yStart + barHeight, zStart), r, g, b, a);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(UltimateTimeWandEntity entity) {
        return null;
    }

    private void drawText(PoseStack poseStack, MultiBufferSource source, String text,
                          Vector3f translateVector, Quaternionf rotate, int color) {
        poseStack.pushPose();
        poseStack.translate(translateVector.x(), translateVector.y(), translateVector.z());
        poseStack.scale(0.01F, -0.01F, 0.01F);
        poseStack.mulPose(rotate);
        getFont().drawInBatch(text, 0, 0, -1, false, poseStack.last().pose(), source,
                Font.DisplayMode.NORMAL, 0, color);
        poseStack.popPose();
    }
}
