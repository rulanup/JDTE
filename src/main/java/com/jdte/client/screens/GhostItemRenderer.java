package com.jdte.client.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

final class GhostItemRenderer {
    private GhostItemRenderer() {}

    static void draw(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int overlay) {
        graphics.fakeItem(stack, x, y);
        graphics.fill(x, y, x + 16, y + 16, overlay);
    }
}
