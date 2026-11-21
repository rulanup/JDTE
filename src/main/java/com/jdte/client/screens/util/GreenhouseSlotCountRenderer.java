package com.jdte.client.screens.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Renders oversized greenhouse output counts at half the vanilla decoration scale. */
public final class GreenhouseSlotCountRenderer {
    private static final float COUNT_SCALE = 0.5F;

    private GreenhouseSlotCountRenderer() {
    }

    public static void extractRenderState(GuiGraphicsExtractor graphics, Font font, ItemStack stack, Slot slot,
                              int imageWidth, String countLabel) {
        int seed = slot.x + slot.y * imageWidth;
        if (slot.isFake()) graphics.fakeItem(stack, slot.x, slot.y, seed);
        else graphics.item(stack, slot.x, slot.y, seed);

        // Preserve durability/cooldown decorations without drawing the vanilla-sized count.
        graphics.itemDecorations(font, stack.copyWithCount(1), slot.x, slot.y, null);
        String count = countLabel != null ? countLabel : Integer.toString(stack.getCount());
        graphics.pose().pushMatrix();
        graphics.pose().translate(slot.x + 17.0F, slot.y + 12.0F);
        graphics.pose().scale(COUNT_SCALE, COUNT_SCALE);
        graphics.text(font, count, -font.width(count), 0, 0xFFFFFFFF, true);
        graphics.pose().popMatrix();
    }
}
