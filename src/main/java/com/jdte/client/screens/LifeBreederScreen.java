package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory.TextureLocalization;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.direwolf20.justdirethings.client.screens.widgets.ToggleButton;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.containers.LifeBreederContainer;
import com.jdte.common.network.data.LifeBreederModePayload;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class LifeBreederScreen extends BaseMachineScreen<LifeBreederContainer> {
    private static final List<TextureLocalization> MODE_TEXTURES = List.of(
            texture("textures/item/abilityupgrades/deathprotection.png", "screen.jdte.life_breeder.mode.0"),
            texture("textures/gui/buttons/passivemob.png", "screen.jdte.life_breeder.mode.1"),
            texture("textures/item/abilityupgrades/orexray.png", "screen.jdte.life_breeder.mode.2"));
    private static final Component FEED_TOOLTIP = Component.translatable("jdte.slot.life_breeder_feed");
    private static final Component OUTPUT_TOOLTIP = Component.translatable("jdte.slot.life_breeder_output");

    private final LifeBreederContainer breederContainer;
    private NumberButton multiplierButton;
    private ToggleButton modeButton;

    public LifeBreederScreen(LifeBreederContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        breederContainer = container;
    }

    @Override public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        renderEnergyBar(graphics);
        for (int index = 0; index < Math.min(LifeBreederBE.TOTAL_SLOTS, container.slots.size()); index++) {
            Slot slot = container.slots.get(index);
            graphics.blitSprite(ResourceLocation.withDefaultNamespace("container/slot"),
                    getGuiLeft() + slot.x - 1, getGuiTop() + slot.y - 1, 18, 18);
        }
    }

    private void renderEnergyBar(GuiGraphics graphics) {
        int maxEnergy = Math.max(1, breederContainer.getBreeder().getMaxEnergy());
        int fill = Math.clamp((int) ((long) breederContainer.getEnergy() * 70L / maxEnergy), 0, 70);
        int x = topSectionLeft + getEnergyBarOffset();
        int y = topSectionTop + 5;
        graphics.blit(POWERBAR, x, y, 0, 0, 18, 72, 36, 72);
        graphics.blit(POWERBAR, x + 1, y + 70 - fill, 19, 69 - fill, 17, fill + 1, 36, 72);
    }

    @Override public void init() {
        super.init();
        modeButton = addRenderableWidget(new ToggleButton(
                leftPos + 44, topSectionTop + 62, 16, 16, MODE_TEXTURES,
                breederContainer.getMode(), button -> {
                    int mode = ((ToggleButton) button).getTexturePosition();
                    PacketDistributor.sendToServer(new LifeBreederModePayload(mode));
                }));
    }

    @Override public void addFilterButtons() {
        addRenderableWidget(ToggleButtonFactory.ALLOWLISTBUTTON(
                leftPos + 44, topSectionTop + 44, filterData.allowlist, button -> {
                    filterData.allowlist = !filterData.allowlist;
                    saveSettings();
                }));
    }

    @Override public void addTickSpeedButton() {
        multiplierButton = new NumberButton(
                getGuiLeft() + 140, topSectionTop + 44, 24, 12,
                breederContainer.getMultiplier(), 1, breederContainer.getMaxMultiplier(),
                Component.translatable("jdte.screen.life_breeder.multiplier"), button ->
                PacketDistributor.sendToServer(new TimeAcceleratorPayload(((NumberButton) button).getValue())));
        addRenderableWidget(multiplierButton);
    }

    @Override protected void containerTick() {
        super.containerTick();
        if (multiplierButton != null) {
            multiplierButton.max = breederContainer.getMaxMultiplier();
            multiplierButton.setValue(Math.min(breederContainer.getMultiplier(), multiplierButton.max));
        }
        if (modeButton != null && modeButton.getTexturePosition() != breederContainer.getMode()) {
            modeButton.setTexturePosition(breederContainer.getMode());
        }
    }

    @Override protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot == null || hoveredSlot.hasItem()) return;
        int index = container.slots.indexOf(hoveredSlot);
        if (index >= 0 && index < LifeBreederBE.FEED_SLOTS) {
            graphics.renderTooltip(font, FEED_TOOLTIP, mouseX, mouseY);
        } else if (index >= LifeBreederBE.FEED_SLOTS && index < LifeBreederBE.TOTAL_SLOTS) {
            graphics.renderTooltip(font, OUTPUT_TOOLTIP, mouseX, mouseY);
        }
    }

    @Override public int getFluidBarOffset() { return 204; }

    private static TextureLocalization texture(String path, String translation) {
        return new TextureLocalization(ResourceLocation.fromNamespaceAndPath("justdirethings", path),
                Component.translatable(translation));
    }
}
