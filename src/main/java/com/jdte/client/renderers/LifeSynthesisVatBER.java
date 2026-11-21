package com.jdte.client.renderers;

import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * 生命合成舱纯客户端展示：观察窗进度液柱、中央培养基物品、养分输入流和生命流体输出流。
 * 不生成实体/粒子，不增加服务端 tick；液体几何合并为一次绘制，物品展示由方块实体缓存。
 */
public class LifeSynthesisVatBER implements BlockEntityRenderer<LifeSynthesisVatBE, LifeSynthesisVatBER.State> {
    private static final float WIDTH = 0.6F;
    private static final float MAX_HEIGHT = 0.85F;
    private static final float BOTTOM = 0.12F;
    private static final float OFFSET = 0.501F;
    /** 原版信标光束：从结构底面中心向上 2 格（穿透方块，带光晕动画），宽度参考原版信标。 */
    /** 光束高度：从结构底面到顶面（2 格），不超出模型范围。 */
    private static final int BEAM_HEIGHT = 2;
    /** 运行时红色，空闲（待机）时绿色，作为机器状态指示。 */
    private static final int BEAM_RUNNING_COLOR = 0xFFFF3B3B;
    private static final int BEAM_IDLE_COLOR = 0xFF55FF55;
    private static final float BEAM_RADIUS = 0.2F;
    private static final float BEAM_GLOW_RADIUS = 0.25F;

    private static final int LIFE_FLUID_COLOR = 0xBBDC143C;
    private static final int DEFAULT_NUTRIENT_COLOR = 0xAA4B8BD8;
    private static final float ITEM_SCALE = 0.42F;
    private static final int VIEW_DISTANCE = 24;

    public static final class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        Direction facing;
        boolean running;
        float height;
        int progressColor;
        int nutrientColor;
        double animationTime;
        float beamTime;
    }

    private final ItemModelResolver models;

    public LifeSynthesisVatBER(BlockEntityRendererProvider.Context context) {
        models = context.itemModelResolver();
    }

    @Override public State createRenderState() { return new State(); }

    @Override public void extractRenderState(LifeSynthesisVatBE vat, State state, float partialTick,
                                             Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(vat, state, partialTick, camera, overlay);
        Direction facing = vat.getBlockState().getValue(BlockStateProperties.FACING);
        state.facing = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        long gameTime = vat.getLevel() == null ? 0L : vat.getLevel().getGameTime();
        state.animationTime = (double) gameTime + partialTick;
        state.beamTime = Math.floorMod(gameTime, 40) + partialTick;
        state.running = vat.getSyncedRunning();
        float progress = vat.getRenderProgress();
        state.height = progress > 0.001F ? Math.max(0.05F, MAX_HEIGHT * progress) : 0;
        state.progressColor = switch (vat.getSyncedTierCode()) {
                case 1 -> 0xCCB03A3A;
                case 2 -> 0xCC8B1A1A;
                case 3 -> 0xCC6A1A9B;
                default -> 0x66333333;
            };
        state.nutrientColor = fluidColor(vat.getNutrientTank().getFluid(), DEFAULT_NUTRIENT_COLOR);
        models.updateForTopItem(state.item, state.running ? vat.getRenderInputItem() : ItemStack.EMPTY,
                ItemDisplayContext.FIXED, vat.getLevel(), null, 0);
    }

    @Override public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.running || state.height > 0) {
            collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(),
                    (pose, buffer) -> renderProcessGeometry(pose, buffer, state.facing, state.progressColor,
                            state.height, state.nutrientColor, state.running, state.animationTime));
        }
        Direction facing = state.facing;
        poseStack.pushPose();
        poseStack.translate(-facing.getStepX(), 0, -facing.getStepZ());
        BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1F, state.beamTime,
                0, BEAM_HEIGHT, state.running ? BEAM_RUNNING_COLOR : BEAM_IDLE_COLOR, BEAM_RADIUS, BEAM_GLOW_RADIUS);
        if (state.running) {
            poseStack.translate(0.5D, 1.08D + Math.sin(state.animationTime * 0.12D) * 0.05D, 0.5D);
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }
        poseStack.popPose();
    }

    private void renderProcessGeometry(PoseStack.Pose pose, VertexConsumer buffer, Direction facing, int progressColor,
                                       float height, int nutrientColor, boolean running, double animationTime) {
        Direction right = facing.getClockWise();
        double centerX = 0.5D - facing.getStepX();
        double centerZ = 0.5D - facing.getStepZ();
        double pulse = 0.03D * Math.sin(animationTime * 0.25D);

        if (height > 0.0F) {
            double windowX = 0.5D + facing.getStepX() * OFFSET;
            double windowZ = 0.5D + facing.getStepZ() * OFFSET;
            float halfWidth = WIDTH / 2.0F;
            float top = BOTTOM + height;
            addVertex(buffer, pose, windowX + right.getStepX() * halfWidth, 0.5D + top,
                    windowZ + right.getStepZ() * halfWidth, progressColor);
            addVertex(buffer, pose, windowX - right.getStepX() * halfWidth, 0.5D + top,
                    windowZ - right.getStepZ() * halfWidth, progressColor);
            addVertex(buffer, pose, windowX - right.getStepX() * halfWidth, 0.5D + BOTTOM,
                    windowZ - right.getStepZ() * halfWidth, progressColor);
            addVertex(buffer, pose, windowX + right.getStepX() * halfWidth, 0.5D + BOTTOM,
                    windowZ + right.getStepZ() * halfWidth, progressColor);
        }

        if (running) {
            double leftX = centerX - right.getStepX() * 0.62D;
            double leftZ = centerZ - right.getStepZ() * 0.62D;
            double rightX = centerX + right.getStepX() * 0.62D;
            double rightZ = centerZ + right.getStepZ() * 0.62D;
            addBox(buffer, pose, leftX, 0.30D, leftZ, 0.20D, 0.16D, 0.20D, nutrientColor);
            addBox(buffer, pose, leftX, 0.46D, leftZ, 0.075D, 0.72D + pulse, 0.075D, nutrientColor);
            addBox(buffer, pose, rightX, 0.30D, rightZ, 0.20D, 0.16D, 0.20D, LIFE_FLUID_COLOR);
            addBox(buffer, pose, rightX, 0.46D, rightZ, 0.075D, 0.72D - pulse, 0.075D, LIFE_FLUID_COLOR);
            addBox(buffer, pose, centerX - right.getStepX() * 0.31D, 1.12D + pulse, centerZ - right.getStepZ() * 0.31D,
                    Math.abs(right.getStepX()) * 0.62D + 0.075D, 0.075D,
                    Math.abs(right.getStepZ()) * 0.62D + 0.075D, nutrientColor);
            addBox(buffer, pose, centerX + right.getStepX() * 0.31D, 1.12D - pulse, centerZ + right.getStepZ() * 0.31D,
                    Math.abs(right.getStepX()) * 0.62D + 0.075D, 0.075D,
                    Math.abs(right.getStepZ()) * 0.62D + 0.075D, LIFE_FLUID_COLOR);
        }

    }

    private static int fluidColor(FluidStack stack, int fallback) {
        if (stack.isEmpty()) return fallback;
        var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(stack.getFluid().defaultFluidState());
        var tint = model.fluidTintSource();
        int color = tint == null ? 0xFFFFFFFF : tint.colorAsStack(stack);
        return 0xAA000000 | color & 0x00FFFFFF;
    }

    private static void addBox(VertexConsumer buffer, PoseStack.Pose pose, double centerX, double minY,
                               double centerZ, double sizeX, double height, double sizeZ, int color) {
        double minX = centerX - sizeX / 2.0D;
        double maxX = centerX + sizeX / 2.0D;
        double maxY = minY + height;
        double minZ = centerZ - sizeZ / 2.0D;
        double maxZ = centerZ + sizeZ / 2.0D;
        quad(buffer, pose, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, color);
        quad(buffer, pose, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, color);
        quad(buffer, pose, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, color);
        quad(buffer, pose, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, color);
        quad(buffer, pose, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, color);
        quad(buffer, pose, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, color);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                             double x1, double y1, double z1, double x2, double y2, double z2,
                             double x3, double y3, double z3, double x4, double y4, double z4, int color) {
        addVertex(buffer, pose, x1, y1, z1, color);
        addVertex(buffer, pose, x2, y2, z2, color);
        addVertex(buffer, pose, x3, y3, z3, color);
        addVertex(buffer, pose, x4, y4, z4, color);
    }

    private static void addVertex(VertexConsumer buffer, PoseStack.Pose pose, double x, double y, double z, int color) {
        buffer.addVertex(pose.pose(), (float) x, (float) y, (float) z).setColor(color);
    }

    @Override
    public AABB getRenderBoundingBox(LifeSynthesisVatBE vat) {
        BlockPos pos = vat.getBlockPos();
        return new AABB(pos).inflate(2.0D, 1.0D, 2.0D);
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }
}
