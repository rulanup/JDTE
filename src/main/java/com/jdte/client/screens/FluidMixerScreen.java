package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.client.screens.util.MachineBarMath;
import com.jdte.common.containers.FluidMixerContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;

/**
 * 流体混合器 GUI 基类。
 *
 * <p>类似 Mek Chemical Infuser 的水平布局：
 * 左侧输入罐 A、中间催化剂槽 + 进度箭头、右侧输入罐 B、
 * 下方居中输出罐。</p>
 */
public abstract class FluidMixerScreen<T extends FluidMixerContainer> extends BaseMachineScreen<T> {
    private static final int FLUID_INNER_HEIGHT = 70;
    /** 隐藏 BaseMachineScreen 的默认流体条（我们自行渲染三个罐）。 */
    private static final int HIDDEN_BASE_FLUID_BAR_OFFSET = -10000;

    public static final int TANK_A_X = 32;
    public static final int TANK_B_X = 56;
    public static final int CATALYST_X = 94;
    public static final int CATALYST_Y = 14;
    public static final int ARROW_X = 91;
    public static final int ARROW_Y = 42;
    public static final int ARROW_WIDTH = 24;
    public static final int ARROW_HEIGHT = 17;
    public static final int TANK_OUT_X = 130;
    public static final int TANKS_Y = 5;
    public static final int REDSTONE_X = 160;
    public static final int REDSTONE_Y = 33;

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(com.jdte.JDTE.MODID, "textures/gui/mixer_arrow.png");

    protected FluidMixerScreen(T container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    public int getFluidBarOffset() {
        return HIDDEN_BASE_FLUID_BAR_OFFSET;
    }

    @Override
    public void addTickSpeedButton() {
    }

    @Override
    public void addRedstoneButtons() {
        addRenderableWidget(com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory.REDSTONEBUTTON(
                getGuiLeft() + REDSTONE_X,
                topSectionTop + REDSTONE_Y,
                redstoneMode.ordinal(),
                b -> {
                    redstoneMode = com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.values()[((com.direwolf20.justdirethings.client.screens.widgets.ToggleButton) b).getTexturePosition()];
                    saveSettings();
                }));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        renderSlotBackground(guiGraphics);
        renderFlowIndicator(guiGraphics);
        renderFluidTanks(guiGraphics);
        renderProgressArrow(guiGraphics);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        renderFluidTooltips(guiGraphics, mouseX, mouseY);
        renderSlotAndArrowTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderSlotBackground(GuiGraphics guiGraphics) {
        guiGraphics.blitSprite(SLOT_SPRITE, topSectionLeft + CATALYST_X - 1, topSectionTop + CATALYST_Y - 1, 18, 18);
    }

    private void renderFlowIndicator(GuiGraphics guiGraphics) {
        int cx = topSectionLeft + 103;
        int cy = topSectionTop + 34;
        guiGraphics.fill(cx - 3, cy, cx + 4, cy + 1, 0xFF4A4A52);
        guiGraphics.fill(cx - 2, cy + 1, cx + 3, cy + 2, 0xFF656570);
        guiGraphics.fill(cx - 1, cy + 2, cx + 2, cy + 3, 0xFF858595);
        guiGraphics.fill(cx, cy + 3, cx + 1, cy + 4, 0xFFA5A5BA);
    }

    private void renderFluidTanks(GuiGraphics guiGraphics) {
        FluidMixerContainer mixerContainer = getMenu();
        int capacity = Math.max(1, mixerContainer.getFluidCapacity());

        // 输入罐 A
        renderTank(guiGraphics,
                topSectionLeft + TANK_A_X, topSectionTop + TANKS_Y,
                container.getFluidStack(), container.getFluidAmount(), capacity);

        // 输入罐 B
        renderTank(guiGraphics,
                topSectionLeft + TANK_B_X, topSectionTop + TANKS_Y,
                mixerContainer.getInputBFluidStack(), mixerContainer.getInputBFluidAmount(), capacity);

        // 输出罐
        renderTank(guiGraphics,
                topSectionLeft + TANK_OUT_X, topSectionTop + TANKS_Y,
                mixerContainer.getOutputFluidStack(), mixerContainer.getOutputFluidAmount(), capacity);
    }

    private void renderTank(GuiGraphics guiGraphics, int x, int y, FluidStack fluidStack, int amount, int capacity) {
        guiGraphics.blit(FLUIDBAR, x, y, 0, 0, 18, 72, 36, 72);

        int fluidHeight = MachineBarMath.scaleClamped(amount, capacity, FLUID_INNER_HEIGHT);
        if (fluidHeight > 0) {
            renderFluidStack(guiGraphics, fluidStack, x + 1, y + 71, 16, fluidHeight);
        }

        guiGraphics.blit(FLUIDBAR, x, y, 18, 0, 18, 72, 36, 72);
    }

    private void renderFluidTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        FluidMixerContainer mixerContainer = getMenu();
        int maxMb = mixerContainer.getFluidCapacity();

        // 输入罐 A
        int ax = topSectionLeft + TANK_A_X;
        int ay = topSectionTop + TANKS_Y;
        if (MiscTools.inBounds(ax, ay, 18, 72, mouseX, mouseY)) {
            FluidStack fs = container.getFluidStack();
            guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                    Component.translatable("jdte.screen.fluid_mixer.input_a").withStyle(net.minecraft.ChatFormatting.GOLD),
                    Component.translatable("justdirethings.screen.fluid", fs.getHoverName(),
                            MagicHelpers.withSuffix(container.getFluidAmount()),
                            MagicHelpers.withSuffix(maxMb))
            )), mouseX, mouseY);
            return;
        }

        // 输入罐 B
        int bx = topSectionLeft + TANK_B_X;
        int by = topSectionTop + TANKS_Y;
        if (MiscTools.inBounds(bx, by, 18, 72, mouseX, mouseY)) {
            FluidStack fs = mixerContainer.getInputBFluidStack();
            guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                    Component.translatable("jdte.screen.fluid_mixer.input_b").withStyle(net.minecraft.ChatFormatting.AQUA),
                    Component.translatable("justdirethings.screen.fluid", fs.getHoverName(),
                            MagicHelpers.withSuffix(mixerContainer.getInputBFluidAmount()),
                            MagicHelpers.withSuffix(maxMb))
            )), mouseX, mouseY);
            return;
        }

        // 输出罐
        int ox = topSectionLeft + TANK_OUT_X;
        int oy = topSectionTop + TANKS_Y;
        if (MiscTools.inBounds(ox, oy, 18, 72, mouseX, mouseY)) {
            FluidStack fs = mixerContainer.getOutputFluidStack();
            guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                    Component.translatable("jdte.screen.fluid_mixer.output").withStyle(net.minecraft.ChatFormatting.GREEN),
                    Component.translatable("justdirethings.screen.fluid", fs.getHoverName(),
                            MagicHelpers.withSuffix(mixerContainer.getOutputFluidAmount()),
                            MagicHelpers.withSuffix(maxMb))
            )), mouseX, mouseY);
        }
    }

    private void renderSlotAndArrowTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 催化剂槽提示（槽位为空时）
        if (MiscTools.inBounds(topSectionLeft + CATALYST_X, topSectionTop + CATALYST_Y, 18, 18, mouseX, mouseY)) {
            if (getMenu().slots.get(0).getItem().isEmpty()) {
                guiGraphics.renderTooltip(font, Component.translatable("jdte.slot.fluid_mixer_catalyst")
                        .withStyle(net.minecraft.ChatFormatting.GRAY), mouseX, mouseY);
                return;
            }
        }

        // 进度箭头提示
        if (MiscTools.inBounds(topSectionLeft + ARROW_X, topSectionTop + ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT, mouseX, mouseY)) {
            FluidMixerContainer mixerContainer = getMenu();
            int progressMax = mixerContainer.getProgressMax();
            int progress = mixerContainer.getProgress();
            int percent = (progressMax > 0) ? (progress * 100 / progressMax) : 0;
            guiGraphics.renderTooltip(font, Component.translatable("jdte.screen.fluid_mixer.progress", percent)
                    .withStyle(net.minecraft.ChatFormatting.AQUA), mouseX, mouseY);
        }
    }

    private void renderProgressArrow(GuiGraphics guiGraphics) {
        int x = topSectionLeft + ARROW_X;
        int y = topSectionTop + ARROW_Y;
        FluidMixerContainer mixerContainer = getMenu();
        int progressMax = mixerContainer.getProgressMax();
        int progress = mixerContainer.getProgress();
        int progressWidth = (progressMax > 0 && progress > 0)
                ? Math.clamp((progress * ARROW_WIDTH) / progressMax, 0, ARROW_WIDTH)
                : 0;

        // 底层空箭头
        guiGraphics.blit(ARROW_TEXTURE, x, y, 0, 0, ARROW_WIDTH, ARROW_HEIGHT, 24, 34);

        // 填充进度
        if (progressWidth > 0) {
            guiGraphics.blit(ARROW_TEXTURE, x, y, 0, 17, progressWidth, ARROW_HEIGHT, 24, 34);
        }
    }

    private void renderFluidStack(GuiGraphics guiGraphics, FluidStack fluidStack, int startX, int startY,
                                  int width, int height) {
        if (fluidStack.isEmpty() || height <= 0) return;

        Fluid fluid = fluidStack.getFluid();
        ResourceLocation fluidStill = IClientFluidTypeExtensions.of(fluid).getStillTexture();
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
        int fluidColor = IClientFluidTypeExtensions.of(fluid).getTintColor(fluidStack);

        float red = (float) (fluidColor >> 16 & 255) / 255.0F;
        float green = (float) (fluidColor >> 8 & 255) / 255.0F;
        float blue = (float) (fluidColor & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        RenderSystem.setShaderColor(red, green, blue, 1.0f);

        float uMin = sprite.getU0();
        float uMax = sprite.getU1();
        float vMin = sprite.getV0();
        float vMax = sprite.getV1();
        int textureHeight = sprite.contents().height();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder vertexBuffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        int currentY = startY;
        int remaining = height;
        while (remaining > 0) {
            int drawHeight = Math.min(remaining, textureHeight);
            float adjustedVMax = vMin + (vMax - vMin) * ((float) drawHeight / textureHeight);

            vertexBuffer.addVertex(poseStack.last().pose(), startX, currentY, 0)
                    .setUv(uMin, adjustedVMax);
            vertexBuffer.addVertex(poseStack.last().pose(), startX + width, currentY, 0)
                    .setUv(uMax, adjustedVMax);
            vertexBuffer.addVertex(poseStack.last().pose(), startX + width, currentY - drawHeight, 0)
                    .setUv(uMax, vMin);
            vertexBuffer.addVertex(poseStack.last().pose(), startX, currentY - drawHeight, 0)
                    .setUv(uMin, vMin);

            currentY -= drawHeight;
            remaining -= drawHeight;
        }

        BufferUploader.drawWithShader(vertexBuffer.buildOrThrow());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}
