package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.util.MagicHelpers;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.blockentities.LifeExtractorBE;
import com.jdte.common.containers.LifeExtractorContainer;
import com.jdte.common.network.data.LifeExtractorPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
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

public abstract class LifeExtractorScreen<T extends LifeExtractorContainer> extends BaseMachineScreen<T> {
    private Button modeButton;
    private int localMode;
    private static final int FLUID_INNER_HEIGHT = 52;

    protected LifeExtractorScreen(T container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        renderFluidTank(guiGraphics);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        renderFluidTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void init() {
        super.init();
        localMode = ((LifeExtractorContainer) container).getMode();
        boolean hasFilterUpgrade = hasFilterUpgrade();
        if (!hasFilterUpgrade) {
            modeButton = addRenderableWidget(Button.builder(getModeText(localMode), button -> toggleMode())
                    .bounds(topSectionLeft - 64, topSectionTop + 40, 60, 18)
                    .build());
        }
    }

    private boolean hasFilterUpgrade() {
        if (container.baseMachineBE instanceof com.jdte.common.blockentities.LifeExtractorBE extractor) {
            return com.jdte.common.upgrades.UpgradeHelper.countUpgrades(extractor, com.jdte.common.upgrades.UpgradeType.FILTER) > 0;
        }
        return false;
    }

    private void toggleMode() {
        localMode = (localMode + 1) % 3;
        PacketDistributor.sendToServer(new LifeExtractorPayload(localMode));
        if (modeButton != null) {
            modeButton.setMessage(getModeText(localMode));
        }
    }

    private Component getModeText(int mode) {
        return switch (mode) {
            case LifeExtractorBE.MODE_HOSTILE -> Component.translatable("jdte.screen.mode.hostile");
            case LifeExtractorBE.MODE_FRIENDLY -> Component.translatable("jdte.screen.mode.friendly");
            case LifeExtractorBE.MODE_ALL -> Component.translatable("jdte.screen.mode.all");
            default -> Component.translatable("jdte.screen.mode.hostile");
        };
    }

    private void renderFluidTank(GuiGraphics guiGraphics) {
        int x = topSectionLeft + getFluidBarOffset();
        int y = topSectionTop + 5;
        LifeExtractorContainer leContainer = (LifeExtractorContainer) container;
        int capacity = Math.max(1, baseMachineBE instanceof LifeExtractorBE le ? le.getMaxMB() : LifeExtractorBE.BASE_FLUID_CAPACITY);

        guiGraphics.blit(FLUIDBAR, x, y, 0, 0, 18, 56, 36, 72);

        int fluidHeight = Math.min(FLUID_INNER_HEIGHT, (leContainer.getFluidAmount() * FLUID_INNER_HEIGHT) / capacity);
        if (fluidHeight > 0) {
            renderFluidStack(guiGraphics, leContainer.getFluidStack(), x + 1, y + 1 + FLUID_INNER_HEIGHT, 16, fluidHeight);
        }

        guiGraphics.blit(FLUIDBAR, x, y, 18, 0, 18, 56, 36, 72);
    }

    private void renderFluidTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = topSectionLeft + getFluidBarOffset();
        int y = topSectionTop + 5;
        if (!MiscTools.inBounds(x, y, 18, 56, mouseX, mouseY)) {
            return;
        }

        LifeExtractorContainer leContainer = (LifeExtractorContainer) container;
        FluidStack fluidStack = leContainer.getFluidStack();
        int maxMb = baseMachineBE instanceof LifeExtractorBE le ? le.getMaxMB() : LifeExtractorBE.BASE_FLUID_CAPACITY;
        guiGraphics.renderTooltip(font, Language.getInstance().getVisualOrder(Arrays.asList(
                Component.translatable("justdirethings.screen.fluid", fluidStack.getHoverName(), MagicHelpers.withSuffix(leContainer.getFluidAmount()), MagicHelpers.withSuffix(maxMb))
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
}
