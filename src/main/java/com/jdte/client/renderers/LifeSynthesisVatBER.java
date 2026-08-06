package com.jdte.client.renderers;

import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
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
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

/**
 * 生命合成舱纯客户端展示：观察窗进度液柱、中央培养基物品、养分输入流和生命流体输出流。
 * 不生成实体/粒子，不增加服务端 tick；液体几何合并为一次绘制，物品展示由方块实体缓存。
 */
public class LifeSynthesisVatBER implements BlockEntityRenderer<LifeSynthesisVatBE> {
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

    public LifeSynthesisVatBER(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LifeSynthesisVatBE vat, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Direction facing = vat.getBlockState().getValue(BlockStateProperties.FACING);
        if (!facing.getAxis().isHorizontal()) facing = Direction.NORTH;
        float progress = vat.getRenderProgress();
        boolean running = vat.getSyncedRunning();
        if (progress > 0.001F || running) {
            int progressColor = switch (vat.getSyncedTierCode()) {
                case 1 -> 0xCCB03A3A;
                case 2 -> 0xCC8B1A1A;
                case 3 -> 0xCC6A1A9B;
                default -> 0x66333333;
            };
            int nutrientColor = fluidColor(vat.getNutrientTank().getFluid(), DEFAULT_NUTRIENT_COLOR);
            float height = progress > 0.001F ? Math.max(0.05F, MAX_HEIGHT * progress) : 0.0F;
            renderProcessGeometry(poseStack, facing, progressColor, height, nutrientColor, running,
                    vat.getLevel() == null ? 0L : vat.getLevel().getGameTime(), partialTick);
        }
        if (running) renderInputItem(vat, poseStack, buffers, packedLight, packedOverlay, facing, partialTick);
        // 常亮状态光束：运行时红色、空闲时绿色，从结构底面中心向上到顶面
        renderBeam(poseStack, buffers, partialTick, vat, running ? BEAM_RUNNING_COLOR : BEAM_IDLE_COLOR);
    }

    private void renderProcessGeometry(PoseStack poseStack, Direction facing, int progressColor, float height,
                                       int nutrientColor, boolean running, long gameTime, float partialTick) {
        Direction right = facing.getClockWise();
        double centerX = 0.5D - facing.getStepX();
        double centerZ = 0.5D - facing.getStepZ();
        double pulse = 0.03D * Math.sin((gameTime + partialTick) * 0.25D);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        PoseStack.Pose pose = poseStack.last();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

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

        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderInputItem(LifeSynthesisVatBE vat, PoseStack poseStack, MultiBufferSource buffers,
                                 int packedLight, int packedOverlay, Direction facing, float partialTick) {
        ItemStack stack = vat.getRenderInputItem();
        if (stack.isEmpty()) return;
        long gameTime = vat.getLevel() == null ? 0L : vat.getLevel().getGameTime();
        float animation = gameTime + partialTick;
        poseStack.pushPose();
        poseStack.translate(0.5D - facing.getStepX(), 1.08D + Math.sin(animation * 0.12D) * 0.05D,
                0.5D - facing.getStepZ());
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED,
                packedLight, packedOverlay == 0 ? OverlayTexture.NO_OVERLAY : packedOverlay,
                poseStack, buffers, vat.getLevel(), 0);
        poseStack.popPose();
    }

    private static int fluidColor(FluidStack stack, int fallback) {
        if (stack.isEmpty()) return fallback;
        int color = IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(stack);
        return 0xAA000000 | color & 0x00FFFFFF;
    }

    private static void addBox(BufferBuilder buffer, PoseStack.Pose pose, double centerX, double minY,
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

    private static void quad(BufferBuilder buffer, PoseStack.Pose pose,
                             double x1, double y1, double z1, double x2, double y2, double z2,
                             double x3, double y3, double z3, double x4, double y4, double z4, int color) {
        addVertex(buffer, pose, x1, y1, z1, color);
        addVertex(buffer, pose, x2, y2, z2, color);
        addVertex(buffer, pose, x3, y3, z3, color);
        addVertex(buffer, pose, x4, y4, z4, color);
    }

    /** 原版信标光束渲染器：调用 MC 自带实现（穿透方块、带光晕与动画），颜色按运行状态切换。 */
    private void renderBeam(PoseStack poseStack, MultiBufferSource buffers, float partialTick, LifeSynthesisVatBE vat, int color) {
        Direction facing = vat.getBlockState().getValue(BlockStateProperties.FACING);
        if (!facing.getAxis().isHorizontal()) facing = Direction.NORTH;
        long gameTime = vat.getLevel() != null ? vat.getLevel().getGameTime() : 0L;
        poseStack.pushPose();
        // 3x3x2 结构中心在控制器向后一格：光束从结构底面中心垂直向上
        poseStack.translate(-facing.getStepX(), 0, -facing.getStepZ());
        BeaconRenderer.renderBeaconBeam(poseStack, buffers, BeaconRenderer.BEAM_LOCATION, partialTick, 1.0F,
                gameTime, 0, BEAM_HEIGHT, beaconColor(color), BEAM_RADIUS, BEAM_GLOW_RADIUS);
        poseStack.popPose();
    }

    private static void addVertex(BufferBuilder buffer, PoseStack.Pose pose, double x, double y, double z, int color) {
        buffer.vertex(pose.pose(), (float) x, (float) y, (float) z).color(color).endVertex();
    }

    private static float[] beaconColor(int color) {
        return new float[]{
                (color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F
        };
    }

    public AABB getRenderBoundingBox(LifeSynthesisVatBE vat) {
        BlockPos pos = vat.getBlockPos();
        return new AABB(pos).inflate(2.0D, 1.0D, 2.0D);
    }

    @Override
    public boolean shouldRenderOffScreen(LifeSynthesisVatBE vat) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }
}
