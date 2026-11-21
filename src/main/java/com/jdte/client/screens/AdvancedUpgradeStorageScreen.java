package com.jdte.client.screens;

import net.minecraft.client.renderer.RenderPipelines;

import com.jdte.JDTE;
import com.jdte.common.containers.AdvancedUpgradeStorageContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedUpgradeStorageScreen extends AbstractContainerScreen<AdvancedUpgradeStorageContainer> {
    private static final Identifier GUI =
            Identifier.fromNamespaceAndPath(JDTE.MODID, "textures/gui/advanced_upgrade_storage.png");

    public AdvancedUpgradeStorageScreen(AdvancedUpgradeStorageContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 222);
        this.inventoryLabelY = 128;
        this.titleLabelY = 6;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = (width - imageWidth) / 2;
        int top = (height - imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GUI, left, top, 0, 0, imageWidth, imageHeight, 256, 256);
    }

}
