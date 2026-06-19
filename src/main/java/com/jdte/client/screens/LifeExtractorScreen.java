package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.JDTE;
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
    private static final ResourceLocation MODE_BUTTON = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/mode_button.png");
    private int localMode;

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
        localMode = ((LifeExtractorContainer) container).getMode();
    }

    private boolean hasFilterUpgrade() {
        if (container.baseMachineBE instanceof com.jdte.common.blockentities.LifeExtractorBE extractor) {
            return com.jdte.common.upgrades.UpgradeHelper.countUpgrades(extractor, com.jdte.common.upgrades.UpgradeType.FILTER) > 0;
        }
        return false;
    }

    private int getModeButtonX() {
        return topSectionLeft - 22;
    }

    private int getModeButtonY() {
        return topSectionTop + 40;
    }

    private boolean isModeButtonClicked(double mouseX, double mouseY) {
        return MiscTools.inBounds(getModeButtonX(), getModeButtonY(), 16, 16, mouseX, mouseY);
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
        int x = getModeButtonX();
        int y = getModeButtonY();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.blit(MODE_BUTTON, x, y, localMode * 16, 0, 16, 16, 48, 16);
    }
}
