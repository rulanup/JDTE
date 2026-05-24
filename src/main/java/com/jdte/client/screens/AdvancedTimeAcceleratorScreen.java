package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.jdte.common.blockentities.AdvancedTimeAcceleratorBE;
import com.jdte.common.containers.AdvancedTimeAcceleratorContainer;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class AdvancedTimeAcceleratorScreen extends BaseMachineScreen<AdvancedTimeAcceleratorContainer> {
    private int multiplier;

    public AdvancedTimeAcceleratorScreen(AdvancedTimeAcceleratorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.multiplier = container.getMultiplier();
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(new NumberButton(getGuiLeft() + 65, topSectionTop + 44, 34, 12, multiplier, 1, AdvancedTimeAcceleratorBE.MAX_MULTIPLIER, Component.translatable("jdte.screen.multiplier"), b -> {
            multiplier = ((NumberButton) b).getValue();
            PacketDistributor.sendToServer(new TimeAcceleratorPayload(multiplier));
        }));
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }
}
