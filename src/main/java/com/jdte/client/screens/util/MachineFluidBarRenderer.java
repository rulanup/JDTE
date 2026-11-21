package com.jdte.client.screens.util;

import com.jdte.JDTE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;

public final class MachineFluidBarRenderer {
    private static final Identifier FLUID_BAR = Identifier.fromNamespaceAndPath(JDTE.MODID, "textures/gui/fluid_bar.png");

    private MachineFluidBarRenderer() {
    }

    public static void renderBar(GuiGraphicsExtractor guiGraphics, FluidStack fluidStack, int fluidAmount, int maxMb, int x, int y) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FLUID_BAR, x, y, 0, 0, 18, 72, 36, 72);
        int remaining = MachineBarMath.scaleClamped(fluidAmount, maxMb, 70);
        if (remaining > 0) {
            renderFluid(guiGraphics, fluidStack, x + 1, y + 72 - 1, 16, remaining);
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FLUID_BAR, x, y, 18, 0, 18, 72, 36, 72);
    }

    public static void renderFluid(GuiGraphicsExtractor guiGraphics, FluidStack fluidStack, int startX, int startY, int width, int height) {
        if (fluidStack.isEmpty() || height <= 0) return;

        FluidModel model = fluidModel(fluidStack);
        TextureAtlasSprite sprite = model.stillMaterial().sprite();
        int color = tint(model, fluidStack) | 0xFF000000;
        int tileWidth = sprite.contents().width();
        int tileHeight = sprite.contents().height();
        for (int dy = 0; dy < height; dy += tileHeight) {
            int h = Math.min(tileHeight, height - dy);
            int y = startY - dy - h;
            for (int dx = 0; dx < width; dx += tileWidth) {
                int w = Math.min(tileWidth, width - dx);
                int x = startX + dx;
                guiGraphics.enableScissor(x, y, x + w, y + h);
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, tileWidth, tileHeight, color);
                guiGraphics.disableScissor();
            }
        }
    }

    public static void renderStretchedFluid(GuiGraphicsExtractor graphics, FluidStack stack, int x, int bottom,
                                            int width, int height) {
        if (stack.isEmpty() || height <= 0) return;
        FluidModel model = fluidModel(stack);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, model.stillMaterial().sprite(),
                x, bottom - height, width, height, tint(model, stack));
    }

    private static FluidModel fluidModel(FluidStack stack) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(stack.getFluid().defaultFluidState());
    }

    private static int tint(FluidModel model, FluidStack stack) {
        var source = model.fluidTintSource();
        return source == null ? -1 : source.colorAsStack(stack);
    }
}
