package com.jdte.client.screens.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Renders oversized greenhouse output counts at half the vanilla decoration scale. */
public final class GreenhouseSlotCountRenderer {
    private static final float COUNT_SCALE = 0.5F;

    private GreenhouseSlotCountRenderer() {
    }

    public static void render(GuiGraphics graphics, Font font, ItemStack stack, Slot slot,
                              int imageWidth, String countLabel) {
        int seed = slot.x + slot.y * imageWidth;
        graphics.renderItem(stack, slot.x, slot.y, seed);

        // Preserve durability/cooldown decorations without drawing the vanilla-sized count.
        graphics.renderItemDecorations(font, stack.copyWithCount(1), slot.x, slot.y, null);
        String count = countLabel != null ? countLabel : Integer.toString(stack.getCount());
        graphics.pose().pushPose();
        graphics.pose().translate(slot.x + 17.0F, slot.y + 12.0F, 250.0F);
        graphics.pose().scale(COUNT_SCALE, COUNT_SCALE, 1.0F);
        graphics.drawString(font, count, -font.width(count), 0, 0xFFFFFF, true);
        graphics.pose().popPose();
    }
}
