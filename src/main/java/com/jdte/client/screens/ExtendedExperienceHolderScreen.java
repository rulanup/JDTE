package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.util.ExperienceUtils;
import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.common.containers.ExtendedExperienceHolderContainer;
import com.jdte.common.network.data.ExtendedExperienceHolderPayload;
import com.jdte.common.network.data.ExtendedExperienceHolderSettingsPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ExtendedExperienceHolderScreen extends BaseMachineScreen<ExtendedExperienceHolderContainer> {
    private ExtendedExperienceHolderBE experienceHolderBE;
    private int targetExp;
    private boolean ownerOnly;
    private boolean collectExp;
    public boolean showParticles = true;
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_BAR_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");

    public ExtendedExperienceHolderScreen(
            ExtendedExperienceHolderContainer container, Inventory inventory, Component name) {
        super(container, inventory, name);
        if (container.baseMachineBE instanceof ExtendedExperienceHolderBE experienceHolder) {
            this.experienceHolderBE = experienceHolder;
            this.targetExp = experienceHolder.targetExp;
            this.ownerOnly = experienceHolder.ownerOnly;
            this.collectExp = experienceHolder.collectExp;
            this.showParticles = experienceHolder.showParticles;
        }
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(ToggleButtonFactory.STOREEXPBUTTON(
                topSectionLeft + (topSectionWidth / 2) + 15, topSectionTop + 62, true, button -> {
                    int amount = 1;
                    if (Screen.hasControlDown()) {
                        amount = -1;
                    } else if (Screen.hasShiftDown()) {
                        amount *= 10;
                    }
                    PacketDistributor.sendToServer(new ExtendedExperienceHolderPayload(
                            experienceHolderBE.getBlockPos(), true, amount));
                }));
        addRenderableWidget(ToggleButtonFactory.EXTRACTEXPBUTTON(
                topSectionLeft + (topSectionWidth / 2) - 15 - 18, topSectionTop + 62, true, button -> {
                    int amount = 1;
                    if (Screen.hasControlDown()) {
                        amount = -1;
                    } else if (Screen.hasShiftDown()) {
                        amount *= 10;
                    }
                    PacketDistributor.sendToServer(new ExtendedExperienceHolderPayload(
                            experienceHolderBE.getBlockPos(), false, amount));
                }));
        addRenderableWidget(ToggleButtonFactory.TARGETEXPBUTTON(
                topSectionLeft + (topSectionWidth / 2) - 15 - 42, topSectionTop + 64, targetExp, button -> {
                    targetExp = ((NumberButton) button).getValue();
                    saveSettings();
                }));
        addRenderableWidget(ToggleButtonFactory.OWNERONLYBUTTON(
                topSectionLeft + (topSectionWidth / 2) - 15 - 60, topSectionTop + 62, ownerOnly, button -> {
                    ownerOnly = !ownerOnly;
                    ((GrayscaleButton) button).toggleActive();
                    saveSettings();
                }));
        addRenderableWidget(ToggleButtonFactory.COLLECTEXPBUTTON(
                topSectionLeft + (topSectionWidth / 2) + 15, topSectionTop + 42, collectExp, button -> {
                    collectExp = !collectExp;
                    ((GrayscaleButton) button).toggleActive();
                    saveSettings();
                }));
        addRenderableWidget(ToggleButtonFactory.SHOWPARTICLESBUTTON(
                topSectionLeft + (topSectionWidth / 2) + 31, topSectionTop + 42, showParticles, button -> {
                    showParticles = !showParticles;
                    ((GrayscaleButton) button).toggleActive();
                    saveSettings();
                }));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        renderXPBar(guiGraphics);
    }

    public void renderXPBar(GuiGraphics guiGraphics) {
        int barX = topSectionLeft + (topSectionWidth / 2) - (182 / 2);
        int barY = topSectionTop + topSectionHeight - 15;
        guiGraphics.blitSprite(EXPERIENCE_BAR_BACKGROUND_SPRITE, barX, barY, 182, 5);
        int partialAmount = (int) (ExperienceUtils.getProgressToNextLevel(experienceHolderBE.exp) * 183.0F);
        if (partialAmount > 0) {
            guiGraphics.blitSprite(EXPERIENCE_BAR_PROGRESS_SPRITE,
                    182, 5, 0, 0, barX, barY, partialAmount, 5);
        }
        String level = String.valueOf(ExperienceUtils.getLevelFromTotalExperience(experienceHolderBE.exp));
        int textX = topSectionLeft + (topSectionWidth / 2) - font.width(level) / 2;
        int textY = topSectionTop + 62 + (font.lineHeight / 2);
        guiGraphics.drawString(font, level, textX + 1, textY, 0, false);
        guiGraphics.drawString(font, level, textX - 1, textY, 0, false);
        guiGraphics.drawString(font, level, textX, textY + 1, 0, false);
        guiGraphics.drawString(font, level, textX, textY - 1, 0, false);
        guiGraphics.drawString(font, level, textX, textY, 8453920, false);
    }

    @Override
    public void saveSettings() {
        super.saveSettings();
        PacketDistributor.sendToServer(new ExtendedExperienceHolderSettingsPayload(
                experienceHolderBE.getBlockPos(), targetExp, ownerOnly, collectExp, showParticles));
    }
}
