package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.containers.BioCrusherContainer;
import com.jdte.common.network.data.BioCrusherPayload;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class BioCrusherScreen<T extends BioCrusherContainer> extends BaseMachineScreen<T> {
    private static final ResourceLocation SCANNER_ICON = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/mobscanner.png");
    private static final ResourceLocation GLOWING_ICON = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/glowing.png");
    private static final ResourceLocation NIGHTVISION_ICON = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/nightvision.png");

    private static final Component TOOLTIP_HOSTILE = Component.translatable("jdte.screen.bio_crusher.mode.hostile");
    private static final Component TOOLTIP_FRIENDLY = Component.translatable("jdte.screen.bio_crusher.mode.friendly");
    private static final Component TOOLTIP_ALL = Component.translatable("jdte.screen.bio_crusher.mode.all");

    private int localMode;
    private int modeBtnX;
    private int modeBtnY;

    protected BioCrusherScreen(T container, Inventory inv, Component name) {
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
        renderProgressBar(guiGraphics);
        if (!hasFilterUpgrade()) {
            renderModeButton(guiGraphics);
        }
    }

    @Override
    public void init() {
        super.init();
        var config = GuiUpgradeLayoutConfig.getInstance();
        modeBtnX = leftPos + config.getBioCrusherModeButtonX();
        modeBtnY = topSectionTop + config.getBioCrusherModeButtonY();
        localMode = ((BioCrusherContainer) container).getMode();
    }

    private boolean hasFilterUpgrade() {
        if (container.baseMachineBE instanceof BioCrusherBE crusher) {
            return UpgradeHelper.countUpgrades(crusher, UpgradeType.FILTER) > 0;
        }
        return false;
    }

    private boolean isModeButtonClicked(double mouseX, double mouseY) {
        return MiscTools.inBounds(modeBtnX, modeBtnY, 16, 16, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !hasFilterUpgrade() && isModeButtonClicked(mouseX, mouseY)) {
            localMode = (localMode + 1) % 3;
            PacketDistributor.sendToServer(new BioCrusherPayload(localMode));
            net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderModeButton(GuiGraphics guiGraphics) {
        ResourceLocation icon = switch (localMode) {
            case BioCrusherBE.MODE_HOSTILE -> SCANNER_ICON;
            case BioCrusherBE.MODE_FRIENDLY -> GLOWING_ICON;
            default -> NIGHTVISION_ICON;
        };
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blit(icon, modeBtnX, modeBtnY, 0, 0, 16, 16, 16, 16);
    }

    private void renderProgressBar(GuiGraphics guiGraphics) {
        BioCrusherContainer bcContainer = (BioCrusherContainer) container;
        int progress = bcContainer.getProgress();
        int processTime = bcContainer.getProcessTime();

        if (processTime > 0 && progress > 0) {
            int progressBarWidth = (int) (24.0f * progress / processTime);
            guiGraphics.fill(topSectionLeft + 79, topSectionTop + 35, topSectionLeft + 79 + progressBarWidth, topSectionTop + 35 + 16, 0xFF00FF00);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        if (!hasFilterUpgrade() && isModeButtonClicked(mouseX, mouseY)) {
            Component tooltip = switch (localMode) {
                case BioCrusherBE.MODE_HOSTILE -> TOOLTIP_HOSTILE;
                case BioCrusherBE.MODE_FRIENDLY -> TOOLTIP_FRIENDLY;
                default -> TOOLTIP_ALL;
            };
            guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
        }
    }
}
