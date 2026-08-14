package com.jdte.client.screens.util;

import com.jdte.JDTE;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public final class MachineFluidBarRenderer {
    private static final ResourceLocation FLUID_BAR = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluidbar.png");

    private MachineFluidBarRenderer() {
    }

    public static void renderBar(GuiGraphics guiGraphics, FluidStack fluidStack, int fluidAmount, int maxMb, int x, int y) {
        renderBar(guiGraphics, fluidStack, fluidAmount, maxMb, false, x, y);
    }

    public static void renderBar(GuiGraphics guiGraphics, FluidStack fluidStack, int fluidAmount, int maxMb,
                                 boolean configuredGhost, int x, int y) {
        guiGraphics.blit(FLUID_BAR, x, y, 0, 0, 18, 72, 36, 72);
        int remaining = MachineBarMath.scaleClamped(fluidAmount, maxMb, 70);
        if (remaining > 0) {
            renderFluid(guiGraphics, fluidStack, x + 1, y + 72 - 1, 16, remaining);
        } else if (configuredGhost && !fluidStack.isEmpty()) {
            RenderSystem.enableBlend();
            renderFluid(guiGraphics, fluidStack, x + 1, y + 44, 16, 16, 0.35F);
            RenderSystem.disableBlend();
        }
        guiGraphics.blit(FLUID_BAR, x, y, 18, 0, 18, 72, 36, 72);
    }

    public static void renderFluid(GuiGraphics guiGraphics, FluidStack fluidStack, int startX, int startY, int width, int height) {
        renderFluid(guiGraphics, fluidStack, startX, startY, width, height, 1.0F);
    }

    private static void renderFluid(GuiGraphics guiGraphics, FluidStack fluidStack, int startX, int startY,
                                    int width, int height, float alpha) {
        if (fluidStack.isEmpty() || height <= 0) return;

        Fluid fluid = fluidStack.getFluid();
        ResourceLocation fluidStill = IClientFluidTypeExtensions.of(fluid).getStillTexture();
        TextureAtlasSprite fluidStillSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
        int fluidColor = IClientFluidTypeExtensions.of(fluid).getTintColor(fluidStack);

        float red = (float) (fluidColor >> 16 & 255) / 255.0F;
        float green = (float) (fluidColor >> 8 & 255) / 255.0F;
        float blue = (float) (fluidColor & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        RenderSystem.setShaderColor(red, green, blue, alpha);

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
