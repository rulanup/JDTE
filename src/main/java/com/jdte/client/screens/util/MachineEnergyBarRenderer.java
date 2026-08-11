package com.jdte.client.screens.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class MachineEnergyBarRenderer {
    private static final ResourceLocation POWER_BAR = ResourceLocation.fromNamespaceAndPath(
            "justdirethings", "textures/gui/powerbar.png");
    private static final int FILL_PIXELS = 70;

    private MachineEnergyBarRenderer() {
    }

    public static void renderBar(GuiGraphics graphics, int energy, int capacity, int x, int y) {
        int fill = MachineBarMath.scaleClamped(energy, capacity, FILL_PIXELS);
        graphics.blit(POWER_BAR, x, y, 0, 0, 18, 72, 36, 72);
        if (fill > 0) {
            graphics.blit(POWER_BAR, x + 1, y + 70 - fill,
                    19, 69 - fill, 17, fill + 1, 36, 72);
        }
    }
}