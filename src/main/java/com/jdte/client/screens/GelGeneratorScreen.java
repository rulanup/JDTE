package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.jdte.common.containers.GelGeneratorContainer;
import com.jdte.common.network.data.GelGeneratorPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;

public abstract class GelGeneratorScreen<T extends GelGeneratorContainer> extends BaseMachineScreen<T> {
    private static final int SPLIT_FLUID_INNER_HEIGHT = 34;
    private Button autoBalanceButton;
    private boolean autoBalanceLocal;

    protected GelGeneratorScreen(T container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    public int getFluidBarOffset() {
        return 204;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        renderSplitFluidTank(guiGraphics);
        renderProgressArrow(guiGraphics);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        renderOutputFluidTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void init() {
        super.init();
        renderables.forEach(renderable -> {
            if (renderable instanceof net.minecraft.client.gui.components.AbstractWidget widget
                    && widget.getY() >= topSectionTop + 40
                    && widget.getY() <= topSectionTop + 62) {
                widget.setY(widget.getY() + 12);
            }
        });
        autoBalanceLocal = ((GelGeneratorContainer) container).isAutoBalanceInputs();
        autoBalanceButton = addRenderableWidget(Button.builder(getAutoBalanceText(autoBalanceLocal), button -> toggleAutoBalance())
                .bounds(topSectionLeft - 64, topSectionTop + 82, 60, 18)
                .build());
    }

    private void toggleAutoBalance() {
        autoBalanceLocal = !autoBalanceLocal;
        PacketDistributor.sendToServer(new GelGeneratorPayload(autoBalanceLocal));
        if (autoBalanceButton != null) {
            autoBalanceButton.setMessage(getAutoBalanceText(autoBalanceLocal));
        }
    }

    private Component getAutoBalanceText(boolean enabled) {
        return Component.translatable(enabled ? "jdte.screen.auto_balance.on" : "jdte.screen.auto_balance.off");
    }

    private void renderSplitFluidTank(GuiGraphics guiGraphics) {
        int x = topSectionLeft + getFluidBarOffset();
        int y = topSectionTop + 5;
        GelGeneratorContainer gelContainer = (GelGeneratorContainer) container;
        int capacity = Math.max(1, gelContainer.getOutputFluidCapacity());

        guiGraphics.blit(FLUIDBAR, x, y, 0, 0, 18, 72, 36, 72);

        int inputHeight = Math.min(SPLIT_FLUID_INNER_HEIGHT, (gelContainer.getFluidAmount() * SPLIT_FLUID_INNER_HEIGHT) / capacity);
        if (inputHeight > 0) {
            renderFluidStack(guiGraphics, gelContainer.getFluidStack(), x + 1, y + 1 + SPLIT_FLUID_INNER_HEIGHT, 16, inputHeight);
        }

        int outputHeight = Math.min(SPLIT_FLUID_INNER_HEIGHT, (gelContainer.getOutputFluidAmount() * SPLIT_FLUID_INNER_HEIGHT) / capacity);
        if (outputHeight > 0) {
            renderFluidStack(guiGraphics, gelContainer.getOutputFluidStack(), x + 1, y + 71, 16, outputHeight);
        }

        guiGraphics.fill(x + 1, y + 36, x + 17, y + 37, 0xFF404040);
        guiGraphics.blit(FLUIDBAR, x, y, 18, 0, 18, 72, 36, 72);
    }

    private void renderOutputFluidTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = topSectionLeft + getFluidBarOffset();
        int y = topSectionTop + 41;
        if (!MiscTools.inBounds(x, y, 18, 36, mouseX, mouseY)) {
            return;
        }

        GelGeneratorContainer gelContainer = (GelGeneratorContainer) container;
        FluidStack fluidStack = gelContainer.getOutputFluidStack();
        int maxMb = gelContainer.getOutputFluidCapacity();
        guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                Component.translatable("justdirethings.screen.fluid", fluidStack.getHoverName(), MagicHelpers.withSuffix(gelContainer.getOutputFluidAmount()), MagicHelpers.withSuffix(maxMb))
        )), mouseX, mouseY);
    }

    private void renderFluidStack(GuiGraphics guiGraphics, FluidStack fluidStack, int startX, int startY, int width, int height) {
        if (fluidStack.isEmpty() || height <= 0) return;

        Fluid fluid = fluidStack.getFluid();
        net.minecraft.resources.ResourceLocation fluidStill = IClientFluidTypeExtensions.of(fluid).getStillTexture();
        TextureAtlasSprite fluidStillSprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
        int fluidColor = IClientFluidTypeExtensions.of(fluid).getTintColor(fluidStack);

        float red = (float) (fluidColor >> 16 & 255) / 255.0F;
        float green = (float) (fluidColor >> 8 & 255) / 255.0F;
        float blue = (float) (fluidColor & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        RenderSystem.setShaderColor(red, green, blue, 1.0f);

        int zLevel = 0;
        float uMin = fluidStillSprite.getU0();
        float uMax = fluidStillSprite.getU1();
        float vMin = fluidStillSprite.getV0();
        float vMax = fluidStillSprite.getV1();
        int textureWidth = fluidStillSprite.contents().width();
        int textureHeight = fluidStillSprite.contents().height();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder vertexBuffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        int yOffset = 0;
        while (yOffset < height) {
            int drawHeight = Math.min(textureHeight, height - yOffset);
            int drawY = startY - yOffset - drawHeight;
            float vMaxAdjusted = vMin + (vMax - vMin) * ((float) drawHeight / textureHeight);

            int xOffset = 0;
            while (xOffset < width) {
                int drawWidth = Math.min(textureWidth, width - xOffset);
                float uMaxAdjusted = uMin + (uMax - uMin) * ((float) drawWidth / textureWidth);

                vertexBuffer.addVertex(poseStack.last().pose(), startX + xOffset, drawY + drawHeight, zLevel).setUv(uMin, vMaxAdjusted);
                vertexBuffer.addVertex(poseStack.last().pose(), startX + xOffset + drawWidth, drawY + drawHeight, zLevel).setUv(uMaxAdjusted, vMaxAdjusted);
                vertexBuffer.addVertex(poseStack.last().pose(), startX + xOffset + drawWidth, drawY, zLevel).setUv(uMaxAdjusted, vMin);
                vertexBuffer.addVertex(poseStack.last().pose(), startX + xOffset, drawY, zLevel).setUv(uMin, vMin);

                xOffset += drawWidth;
            }
            yOffset += drawHeight;
        }

        BufferUploader.drawWithShader(vertexBuffer.buildOrThrow());
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.applyModelViewMatrix();
    }

    private void renderProgressArrow(GuiGraphics guiGraphics) {
        int x = getGuiLeft() + 105;
        int y = getGuiTop() + 30;
        GelGeneratorContainer gelContainer = (GelGeneratorContainer) container;
        int progressWidth = (gelContainer.getGelProgress() * 24) / gelContainer.getGelProgressMax();

        drawArrow(guiGraphics, x, y, 24, 0xFF2B2B2B);
        drawArrow(guiGraphics, x + 1, y + 1, 22, 0xFF8A8A8A);
        if (progressWidth > 0) {
            drawArrow(guiGraphics, x + 1, y + 1, Math.min(22, progressWidth), 0xFF3DBB57);
        }
    }

    private void drawArrow(GuiGraphics guiGraphics, int x, int y, int width, int color) {
        int clampedWidth = Math.max(0, Math.min(24, width));
        if (clampedWidth <= 0) return;

        int bodyWidth = Math.min(16, clampedWidth);
        if (bodyWidth > 0) {
            guiGraphics.fill(x, y + 5, x + bodyWidth, y + 10, color);
        }
        int headWidth = clampedWidth - 16;
        if (headWidth <= 0) return;

        drawHeadPart(guiGraphics, x + 16, y + 4, Math.min(headWidth, 2), 7, color);
        if (headWidth > 2) drawHeadPart(guiGraphics, x + 18, y + 3, Math.min(headWidth - 2, 2), 9, color);
        if (headWidth > 4) drawHeadPart(guiGraphics, x + 20, y + 2, Math.min(headWidth - 4, 2), 11, color);
        if (headWidth > 6) drawHeadPart(guiGraphics, x + 22, y + 1, Math.min(headWidth - 6, 2), 13, color);
    }

    private void drawHeadPart(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        if (width > 0) {
            guiGraphics.fill(x, y, x + width, y + height, color);
        }
    }
}
