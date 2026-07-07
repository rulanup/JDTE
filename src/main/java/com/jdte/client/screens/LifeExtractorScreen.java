package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.JDTE;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.LifeExtractorBE;
import com.jdte.common.containers.LifeExtractorContainer;
import com.jdte.common.network.data.LifeExtractorPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class LifeExtractorScreen<T extends LifeExtractorContainer> extends BaseMachineScreen<T> {
    private static final ResourceLocation SCANNER_ICON = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/mobscanner.png");
    private static final ResourceLocation GLOWING_ICON = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/glowing.png");
    private static final ResourceLocation NIGHTVISION_ICON = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/nightvision.png");

    private static final Component TOOLTIP_HOSTILE = Component.translatable("jdte.screen.life_extractor.mode.hostile");
    private static final Component TOOLTIP_FRIENDLY = Component.translatable("jdte.screen.life_extractor.mode.friendly");
    private static final Component TOOLTIP_ALL = Component.translatable("jdte.screen.life_extractor.mode.all");

    private int localMode;
    private int modeBtnX;
    private int modeBtnY;

    protected LifeExtractorScreen(T container, Inventory inv, Component name) {
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
        if (!hasFilterUpgrade()) {
            renderModeButton(guiGraphics);
        }
    }

    @Override
    public void init() {
        super.init();
        var config = GuiUpgradeLayoutConfig.getInstance();
        modeBtnX = leftPos + config.getLifeExtractorModeButtonX();
        modeBtnY = topSectionTop + config.getLifeExtractorModeButtonY();
        localMode = ((LifeExtractorContainer) container).getMode();
    }

    private boolean hasFilterUpgrade() {
        if (container.baseMachineBE instanceof LifeExtractorBE extractor) {
            return com.jdte.common.upgrades.UpgradeHelper.countUpgrades(extractor, com.jdte.common.upgrades.UpgradeType.FILTER) > 0;
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
            PacketDistributor.sendToServer(new LifeExtractorPayload(localMode));
            net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderModeButton(GuiGraphics guiGraphics) {
        ResourceLocation icon = switch (localMode) {
            case LifeExtractorBE.MODE_HOSTILE -> SCANNER_ICON;
            case LifeExtractorBE.MODE_FRIENDLY -> GLOWING_ICON;
            default -> NIGHTVISION_ICON;
        };
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blit(icon, modeBtnX, modeBtnY, 0, 0, 16, 16, 16, 16);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        if (!hasFilterUpgrade() && isModeButtonClicked(mouseX, mouseY)) {
            Component tooltip = switch (localMode) {
                case LifeExtractorBE.MODE_HOSTILE -> TOOLTIP_HOSTILE;
                case LifeExtractorBE.MODE_FRIENDLY -> TOOLTIP_FRIENDLY;
                default -> TOOLTIP_ALL;
            };
            guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
        }
    }
}
