package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.blockentities.ChargedLightningRodMode;
import com.jdte.common.containers.ChargedLightningRodContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ChargedLightningRodScreen extends BaseMachineScreen<ChargedLightningRodContainer> {
    private Button renderButton;
    private Button soundButton;

    public ChargedLightningRodScreen(ChargedLightningRodContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public void init() {
        super.init();
        renderButton = addRenderableWidget(Button.builder(renderLabel(), button -> sendButton(0))
                .bounds(leftPos + 8, topSectionTop + 42, 76, 20)
                .tooltip(Tooltip.create(Component.translatable("jdte.screen.charged_lightning_rod.render_tooltip")))
                .build());
        soundButton = addRenderableWidget(Button.builder(soundLabel(), button -> sendButton(1))
                .bounds(leftPos + 92, topSectionTop + 42, 76, 20)
                .tooltip(Tooltip.create(Component.translatable("jdte.screen.charged_lightning_rod.sound_tooltip")))
                .build());
    }

    private void sendButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private Component renderLabel() {
        return Component.translatable(menu.isRenderEnabled()
                ? "jdte.screen.charged_lightning_rod.render_on" : "jdte.screen.charged_lightning_rod.render_off");
    }

    private Component soundLabel() {
        return Component.translatable(menu.isSoundEnabled()
                ? "jdte.screen.charged_lightning_rod.sound_on" : "jdte.screen.charged_lightning_rod.sound_off");
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        renderButton.setMessage(renderLabel());
        soundButton.setMessage(soundLabel());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int labelY = topSectionTop - topPos + 6;
        ChargedLightningRodMode mode = menu.getMode();
        graphics.drawString(font, Component.translatable("jdte.screen.charged_lightning_rod.cost", mode.energyPerStrike),
                8, labelY, 0x404040, false);
        graphics.drawString(font, Component.translatable(mode == ChargedLightningRodMode.NORMAL
                        ? "jdte.screen.charged_lightning_rod.normal_rate" : "jdte.screen.charged_lightning_rod.boosted_rate",
                mode == ChargedLightningRodMode.NORMAL ? mode.intervalTicks / 20 : mode.strikesPerCycle),
                8, labelY + 11, 0x404040, false);
        graphics.drawString(font, Component.translatable(menu.isWeatherAllowed()
                        ? "jdte.screen.charged_lightning_rod.weather_ready" : "jdte.screen.charged_lightning_rod.waiting_storm"),
                8, labelY + 22, menu.isWeatherAllowed() ? 0x306030 : 0x993333, false);
    }

    @Override public void addTickSpeedButton() { }
    @Override public void setTopSection() { extraWidth = 60; extraHeight = 0; }
}
