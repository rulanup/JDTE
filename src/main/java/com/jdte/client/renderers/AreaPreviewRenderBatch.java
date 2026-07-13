package com.jdte.client.renderers;

import com.direwolf20.justdirethings.client.renderers.RenderHelpers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class AreaPreviewRenderBatch {
    private static final Color MAIN_LINE_COLOR = Color.GREEN;
    private static final Color OFFSET_LINE_COLOR = Color.WHITE;
    private static final List<Preview> PREVIEWS = new ArrayList<>();

    private AreaPreviewRenderBatch() {
    }

    public static void enqueueMain(AABB area) {
        PREVIEWS.add(new Preview(area, false));
    }

    public static void enqueueOffset(AABB area) {
        PREVIEWS.add(new Preview(area, true));
    }

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES || PREVIEWS.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        poseStack.pushPose();
        var camera = event.getCamera().getPosition();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = poseStack.last().pose();
        try {
            for (Preview preview : PREVIEWS) {
                RenderHelpers.renderLines(poseStack, preview.area(),
                        preview.offset() ? OFFSET_LINE_COLOR : MAIN_LINE_COLOR, buffers);
            }
            for (Preview preview : PREVIEWS) {
                if (preview.offset()) {
                    RenderHelpers.renderBoxSolid(poseStack, matrix, buffers, preview.area(), 0, 0, 1, 0.125F);
                } else {
                    RenderHelpers.renderBoxSolid(poseStack, matrix, buffers, preview.area(), 1, 0, 0, 0.125F);
                }
            }
            buffers.endBatch();
        } finally {
            PREVIEWS.clear();
            poseStack.popPose();
        }
    }

    private record Preview(AABB area, boolean offset) {
    }
}
