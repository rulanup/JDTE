package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.standardbuttons.ToggleButtonFactory;
import com.direwolf20.justdirethings.client.screens.widgets.GrayscaleButton;
import com.jdte.common.blockentities.ExtendedEnergyTransmitterBE;
import com.jdte.common.containers.ExtendedEnergyTransmitterContainer;
import com.jdte.common.network.data.ExtendedEnergyTransmitterSettingPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ExtendedEnergyTransmitterScreen extends BaseMachineScreen<ExtendedEnergyTransmitterContainer> {
    public boolean showParticles;

    public ExtendedEnergyTransmitterScreen(ExtendedEnergyTransmitterContainer container, Inventory inventory,
                                           Component name) {
        super(container, inventory, name);
        if (container.baseMachineBE instanceof ExtendedEnergyTransmitterBE transmitter) {
            showParticles = transmitter.showParticles;
        }
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(ToggleButtonFactory.SHOWPARTICLESBUTTON(
                getGuiLeft() + 116, topSectionTop + 62, showParticles, button -> {
                    showParticles = !showParticles;
                    ((GrayscaleButton) button).toggleActive();
                    saveSettings();
                }));
    }

    @Override
    public void addRedstoneButtons() {
        super.addRedstoneButtons();
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    public void addTickSpeedButton() {
    }

    @Override
    public void saveSettings() {
        super.saveSettings();
        PacketDistributor.sendToServer(new ExtendedEnergyTransmitterSettingPayload(showParticles));
    }
}
