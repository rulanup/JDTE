package com.jdte.client.renderers;

import com.direwolf20.justdirethings.client.blockentityrenders.baseber.AreaAffectingBER.AreaAffectingRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class RotatingMachineItemRenderer<T extends BlockEntity>
        extends AreaAffectingBER<T, RotatingMachineItemRenderer.State> {
    public static final class State extends AreaAffectingRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        Direction facing;
        float angle;
    }

    private final Item item;
    private final ItemModelResolver models;

    public RotatingMachineItemRenderer(BlockEntityRendererProvider.Context context, Item item) {
        super(context);
        this.item = item;
        models = context.itemModelResolver();
    }

    @Override public State createRenderState() { return new State(); }

    @Override public void extractRenderState(T blockEntity, State state, float partialTick, Vec3 cameraPosition,
                                              ModelFeatureRenderer.CrumblingOverlay overlay) {
        super.extractRenderState(blockEntity, state, partialTick, cameraPosition, overlay);
        state.facing = blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
        state.angle = (System.currentTimeMillis() / 15L) % 360;
        models.updateForTopItem(state.item, item.getDefaultInstance(), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
    }

    @Override public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, pose, collector, camera);
        Direction direction = state.facing;
        pose.pushPose();
        pose.translate(0.5F + direction.getStepX() * 0.3F, 0.5F + direction.getStepY() * 0.3F,
                0.5F + direction.getStepZ() * 0.3F);
        pose.mulPose(Axis.XP.rotationDegrees(direction.getStepZ() * -90));
        pose.mulPose(Axis.ZP.rotationDegrees(direction.getStepX() * 90));
        pose.mulPose(Axis.XP.rotationDegrees(direction.getStepY() == 1 ? 0 : 180));
        pose.mulPose(Axis.YP.rotationDegrees(state.angle));
        pose.scale(0.15F, 0.15F, 0.15F);
        state.item.submit(pose, collector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
