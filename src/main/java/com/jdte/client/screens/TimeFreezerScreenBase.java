package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.TimeFreezerBE;
import com.jdte.common.network.JDTEPacketHandler;
import com.jdte.common.network.data.TimeFreezerPayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class TimeFreezerScreenBase<T extends BaseMachineContainer> extends BaseMachineScreen<T> {
    private boolean timeFreezeEnabled = true;
    private boolean weatherFreezeEnabled = true;
    private Button timeButton;
    private Button weatherButton;

    protected TimeFreezerScreenBase(T container, Inventory inventory, Component title) {
        super(container, inventory, title);
        if (baseMachineBE instanceof TimeFreezerBE freezer) {
            timeFreezeEnabled = freezer.isTimeFreezeEnabled();
            weatherFreezeEnabled = freezer.isWeatherFreezeEnabled();
        }
    }

    @Override
    public void init() {
        super.init();
        int centerX = leftPos + 88;
        int buttonY = topSectionTop + 38;
        timeButton = addRenderableWidget(Button.builder(timeLabel(), button -> {
            timeFreezeEnabled = !timeFreezeEnabled;
            updateButtons();
            sendSettings();
        }).bounds(centerX - 76, buttonY, 70, 20).build());
        weatherButton = addRenderableWidget(Button.builder(weatherLabel(), button -> {
            weatherFreezeEnabled = !weatherFreezeEnabled;
            updateButtons();
            sendSettings();
        }).bounds(centerX + 6, buttonY, 70, 20).build());
    }

    private void updateButtons() {
        timeButton.setMessage(timeLabel());
        weatherButton.setMessage(weatherLabel());
    }

    private Component timeLabel() {
        return Component.translatable(timeFreezeEnabled
                ? "screen.jdte.time_freezer.time.enabled" : "screen.jdte.time_freezer.time.disabled");
    }

    private Component weatherLabel() {
        return Component.translatable(weatherFreezeEnabled
                ? "screen.jdte.time_freezer.weather.enabled" : "screen.jdte.time_freezer.weather.disabled");
    }

    private void sendSettings() {
        JDTEPacketHandler.CHANNEL.sendToServer(new TimeFreezerPayload(timeFreezeEnabled, weatherFreezeEnabled));
    }

    @Override
    public void addTickSpeedButton() {
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
}
