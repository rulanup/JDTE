package com.jdte.client.renderers;

import com.direwolf20.justdirethings.client.renderers.RenderHelpers;
import com.mojang.logging.LogUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.context.ContextKey;
import com.direwolf20.justdirethings.client.renderers.OurRenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import java.util.HashMap;
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
    private static final ContextKey<AreaPreviewRenderBatch> KEY =
            new ContextKey<>(Identifier.fromNamespaceAndPath("jdte", "area_previews"));
    private static final BlockDisplayContext MODEL_CONTEXT = BlockDisplayContext.create();
    private static final Set<BlockState> FAILED_BLUEPRINT_STATES = ConcurrentHashMap.newKeySet();
    private final List<Preview> previews = new ArrayList<>();
    private final List<ModelPreview> models = new ArrayList<>();

    private static AreaPreviewRenderBatch batch(LevelRenderState level) {
        var batch = level.getRenderData(KEY);
        if (batch == null) {
            batch = new AreaPreviewRenderBatch();
            level.setRenderData(KEY, batch);
        }
        return batch;
    }

    public static void enqueueMain(LevelRenderState level, AABB area, Direction selectedFace) {
        batch(level).previews.add(new Preview(area, false, selectedFace));
    }

    public static void enqueueOffset(LevelRenderState level, AABB area) {
        batch(level).previews.add(new Preview(area, true, null));
    }

    public static void enqueueBlueprint(LevelRenderState level, BlockPos origin, List<BlueprintBlock> blocks) {
        var resolver = Minecraft.getInstance().getBlockModelResolver();
        var batch = batch(level);
        var byState = new HashMap<BlockState, BlockModelRenderState>();
        for (var block : blocks) {
            BlockModelRenderState model = null;
            if (!FAILED_BLUEPRINT_STATES.contains(block.state())) {
                try {
                    model = byState.computeIfAbsent(block.state(), state -> {
                        var extracted = new BlockModelRenderState();
                        resolver.update(extracted, state, MODEL_CONTEXT);
                        return extracted;
                    });
                } catch (RuntimeException ex) {
                    if (FAILED_BLUEPRINT_STATES.add(block.state())) {
                        LOGGER.warn("Cannot extract Factory Package preview for {}", block.state(), ex);
                    }
                }
            }
            batch.models.add(new ModelPreview(origin.offset(block.relativePos()), model));
        }
    }

    public static void onRenderLevel(SubmitCustomGeometryEvent event) {
        var level = event.getLevelRenderState();
        var batch = level.getRenderData(KEY);
        if (batch == null) return;
        var camera = level.cameraRenderState.pos;
        var pose = event.getPoseStack();
        var collector = event.getSubmitNodeCollector();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        try {
            for (var preview : batch.previews) {
                collector.submitCustomGeometry(pose, OurRenderTypes.lines(), (snapshot, vertices) -> {
                    PoseStack lines = new PoseStack();
                    lines.last().set(snapshot);
                    RenderHelpers.renderLines(lines, preview.area(), preview.offset() ? OFFSET_LINE_COLOR : MAIN_LINE_COLOR,
                            type -> vertices);
                });
                submitBox(pose, collector, preview.area(), preview.offset() ? 0 : 1, 0,
                        preview.offset() ? 1 : 0, 0.125F);
                if (preview.selectedFace() != null) {
                    submitBox(pose, collector, faceHighlight(preview.area(), preview.selectedFace()), 1, 0.75F, 0.1F, 0.3F);
                }
            }
            for (var block : batch.models) {
                submitBox(pose, collector, new AABB(block.pos()), 0.15F, 0.85F, 1, 0.18F);
                if (block.model() == null) continue;
                pose.pushPose();
                try {
                    pose.translate(block.pos().getX(), block.pos().getY(), block.pos().getZ());
                    block.model().submitMultiLayer(pose, collector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
                } finally {
                    pose.popPose();
                }
            }
        } finally {
            pose.popPose();
        }
    }

    private static void submitBox(PoseStack pose, SubmitNodeCollector collector, AABB box,
                                  float red, float green, float blue, float alpha) {
        collector.submitCustomGeometry(pose, OurRenderTypes.TRANSPARENT_BOX, (snapshot, vertices) ->
                RenderHelpers.renderBoxSolid(snapshot, snapshot.pose(), type -> vertices,
                        box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha));
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

    private record ModelPreview(BlockPos pos, BlockModelRenderState model) {}

    public record BlueprintBlock(BlockPos relativePos, BlockState state) {}
}
