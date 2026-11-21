package com.jdte.client.screens;

import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.client.input.MouseButtonEvent;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.JDTE;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.LifeExtractorBE;
import com.jdte.common.containers.LifeExtractorContainer;
import com.jdte.common.network.data.LifeExtractorPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public abstract class LifeExtractorScreen<T extends LifeExtractorContainer> extends BaseMachineScreen<T> {
    private static final Identifier SCANNER_ICON = Identifier.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/mobscanner.png");
    private static final Identifier GLOWING_ICON = Identifier.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/glowing.png");
    private static final Identifier NIGHTVISION_ICON = Identifier.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/nightvision.png");

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
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
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
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0 && !hasFilterUpgrade() && isModeButtonClicked(mouseX, mouseY)) {
            localMode = (localMode + 1) % 3;
            ClientPacketDistributor.sendToServer(new LifeExtractorPayload(localMode));
            net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void renderModeButton(GuiGraphicsExtractor guiGraphics) {
        Identifier icon = switch (localMode) {
            case LifeExtractorBE.MODE_HOSTILE -> SCANNER_ICON;
            case LifeExtractorBE.MODE_FRIENDLY -> GLOWING_ICON;
            default -> NIGHTVISION_ICON;
        };
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon, modeBtnX, modeBtnY, 0, 0, 16, 16, 16, 16);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);
        if (!hasFilterUpgrade() && isModeButtonClicked(mouseX, mouseY)) {
            Component tooltip = switch (localMode) {
                case LifeExtractorBE.MODE_HOSTILE -> TOOLTIP_HOSTILE;
                case LifeExtractorBE.MODE_FRIENDLY -> TOOLTIP_FRIENDLY;
                default -> TOOLTIP_ALL;
            };
            guiGraphics.setTooltipForNextFrame(font, tooltip, mouseX, mouseY);
        }
    }
}
