package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.BioExtractorContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public abstract class BioExtractorScreen<T extends BioExtractorContainer> extends BaseMachineScreen<T> {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");

    protected BioExtractorScreen(T container, Inventory inv, Component name) {
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
        int playerStart = Math.max(0, container.slots.size() - 36);
        for (int i = 0; i < playerStart; i++) {
            Slot slot = container.slots.get(i);
            guiGraphics.blitSprite(SLOT_SPRITE, leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18);
        }
    }
}
