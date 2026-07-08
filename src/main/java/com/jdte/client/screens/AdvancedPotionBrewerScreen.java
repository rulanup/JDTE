package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.containers.AdvancedPotionBrewerContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedPotionBrewerScreen extends BaseMachineScreen<AdvancedPotionBrewerContainer> {
    private static final ResourceLocation BREWING_STAND_BG = ResourceLocation.withDefaultNamespace("textures/gui/container/brewing_stand.png");

    public AdvancedPotionBrewerScreen(AdvancedPotionBrewerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        int x = getGuiLeft();
        int y = getGuiTop();

        AdvancedPotionBrewerContainer brewerContainer = (AdvancedPotionBrewerContainer) container;

        guiGraphics.blit(BREWING_STAND_BG, x + 7, y + 7, 0, 0, 130, 69);

        renderFuelBar(guiGraphics, x, y, brewerContainer);
        renderBrewBubbles(guiGraphics, x, y, brewerContainer);
        renderBrewArrow(guiGraphics, x, y, brewerContainer);
    }

    private void renderFuelBar(GuiGraphics guiGraphics, int x, int y, AdvancedPotionBrewerContainer brewerContainer) {
        int fuel = brewerContainer.getFuel();
        if (fuel > 0) {
            int fuelHeight = (fuel * 13) / AdvancedPotionBrewerBE.FUEL_PER_BLAZE;
            if (fuelHeight > 0) {
                guiGraphics.blit(BREWING_STAND_BG, x + 18, y + 70 - fuelHeight, 176, 29 - fuelHeight, 18, fuelHeight);
            }
        }
    }

    private void renderBrewBubbles(GuiGraphics guiGraphics, int x, int y, AdvancedPotionBrewerContainer brewerContainer) {
        int progress = brewerContainer.getBrewProgress();
        int maxProgress = brewerContainer.getBrewProgressMax();
        if (maxProgress > 0 && progress > 0) {
            int bubbleHeight = (int) ((float) progress / maxProgress * 28.0F);
            if (bubbleHeight > 0) {
                guiGraphics.blit(BREWING_STAND_BG, x + 60, y + 48 - bubbleHeight, 185, 29 - bubbleHeight, 12, bubbleHeight);
            }
        }
    }

    private void renderBrewArrow(GuiGraphics guiGraphics, int x, int y, AdvancedPotionBrewerContainer brewerContainer) {
        int progress = brewerContainer.getBrewProgress();
        int maxProgress = brewerContainer.getBrewProgressMax();
        if (maxProgress > 0 && progress > 0) {
            int arrowHeight = (progress * 28) / maxProgress;
            if (arrowHeight > 0) {
                guiGraphics.blit(BREWING_STAND_BG, x + 92, y + 48 - arrowHeight, 176, 56 - arrowHeight, 9, arrowHeight);
            }
        }
    }
}
