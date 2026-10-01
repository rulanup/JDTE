package com.jdte.client.screens;

import com.jdte.JDTE;
import com.jdte.common.containers.AdvancedUpgradeStorageContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedUpgradeStorageScreen extends AbstractContainerScreen<AdvancedUpgradeStorageContainer> {
    private static final ResourceLocation GUI =
            ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/advanced_upgrade_storage.png");

    public AdvancedUpgradeStorageScreen(AdvancedUpgradeStorageContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = 128;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = (width - imageWidth) / 2;
        int top = (height - imageHeight) / 2;
        guiGraphics.blit(GUI, left, top, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
