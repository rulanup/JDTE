package com.jdte.client.renderers;

import com.direwolf20.justdirethings.client.renderers.RenderHelpers;
import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.slf4j.Logger;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class AreaPreviewRenderBatch {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Color MAIN_LINE_COLOR = Color.GREEN;
    private static final Color OFFSET_LINE_COLOR = Color.WHITE;
    private static final List<Preview> PREVIEWS = new ArrayList<>();
    private static final List<Blueprint> BLUEPRINTS = new ArrayList<>();
    private static final Set<BlockState> FAILED_BLUEPRINT_STATES = ConcurrentHashMap.newKeySet();

    private AreaPreviewRenderBatch() {
    }

    public static void enqueueMain(AABB area) {
        enqueueMain(area, null);
    }

    public static void enqueueMain(AABB area, Direction selectedFace) {
        PREVIEWS.add(new Preview(area, false, selectedFace));
    }

    public static void enqueueOffset(AABB area) {
        PREVIEWS.add(new Preview(area, true, null));
    }

    public static void enqueueBlueprint(BlockPos origin, List<BlueprintBlock> blocks) {
        if (!blocks.isEmpty()) BLUEPRINTS.add(new Blueprint(origin, blocks));
    }

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES
                || PREVIEWS.isEmpty() && BLUEPRINTS.isEmpty()) {
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
                if (preview.selectedFace() != null) {
                    RenderHelpers.renderBoxSolid(poseStack, matrix, buffers,
                            faceHighlight(preview.area(), preview.selectedFace()), 1.0F, 0.75F, 0.1F, 0.3F);
                }
            }
            for (Blueprint blueprint : BLUEPRINTS) {
                poseStack.pushPose();
                poseStack.translate(blueprint.origin().getX(), blueprint.origin().getY(), blueprint.origin().getZ());
                Matrix4f blueprintMatrix = poseStack.last().pose();
                for (BlueprintBlock block : blueprint.blocks()) {
                    AABB bounds = new AABB(block.relativePos());
                    RenderHelpers.renderBoxSolid(poseStack, blueprintMatrix, buffers, bounds,
                            0.15F, 0.85F, 1.0F, 0.18F);
                }
                for (BlueprintBlock block : blueprint.blocks()) {
                    if (FAILED_BLUEPRINT_STATES.contains(block.state())) continue;
                    poseStack.pushPose();
                    try {
                        poseStack.translate(block.relativePos().getX(), block.relativePos().getY(), block.relativePos().getZ());
                        renderBlueprintBlock(minecraft, poseStack, buffers, block.state());
                    } catch (Throwable throwable) {
                        if (FAILED_BLUEPRINT_STATES.add(block.state())) {
                            LOGGER.warn("Skipping unsafe Factory Package preview model for {}",
                                    block.state(), throwable);
                        }
                    } finally {
                        poseStack.popPose();
                    }
                }
                poseStack.popPose();
            }
            buffers.endBatch();
        } finally {
            PREVIEWS.clear();
            BLUEPRINTS.clear();
            poseStack.popPose();
        }
    }

    private static AABB faceHighlight(AABB area, Direction face) {
        double thickness = 0.015625D;
        return switch (face) {
            case WEST -> new AABB(area.minX - thickness, area.minY, area.minZ,
                    area.minX + thickness, area.maxY, area.maxZ);
            case EAST -> new AABB(area.maxX - thickness, area.minY, area.minZ,
                    area.maxX + thickness, area.maxY, area.maxZ);
            case DOWN -> new AABB(area.minX, area.minY - thickness, area.minZ,
                    area.maxX, area.minY + thickness, area.maxZ);
            case UP -> new AABB(area.minX, area.maxY - thickness, area.minZ,
                    area.maxX, area.maxY + thickness, area.maxZ);
            case NORTH -> new AABB(area.minX, area.minY, area.minZ - thickness,
                    area.maxX, area.maxY, area.minZ + thickness);
            case SOUTH -> new AABB(area.minX, area.minY, area.maxZ - thickness,
                    area.maxX, area.maxY, area.maxZ + thickness);
        };
    }

    private record Preview(AABB area, boolean offset, Direction selectedFace) {
    }

    private record Blueprint(BlockPos origin, List<BlueprintBlock> blocks) {
    }

    public record BlueprintBlock(BlockPos relativePos, BlockState state) {}

    private static void renderBlueprintBlock(Minecraft minecraft, PoseStack poseStack,
                                             MultiBufferSource buffers, BlockState state) {
        if (state.getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
            ItemStack stack = new ItemStack(state.getBlock());
            if (!stack.isEmpty()) {
                poseStack.translate(0.5F, 0.5F, 0.5F);
                minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED,
                        LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, buffers,
                        minecraft.level, 0);
            }
            return;
        }
        minecraft.getBlockRenderer().renderSingleBlock(state, poseStack, buffers,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }
}
