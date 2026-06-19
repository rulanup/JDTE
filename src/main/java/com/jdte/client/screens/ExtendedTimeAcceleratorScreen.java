package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.jdte.common.blockentities.AdvancedTimeAcceleratorBE;
import com.jdte.common.containers.ExtendedTimeAcceleratorContainer;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ExtendedTimeAcceleratorScreen extends BaseMachineScreen<ExtendedTimeAcceleratorContainer> {
    private int multiplier;

    public ExtendedTimeAcceleratorScreen(ExtendedTimeAcceleratorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.multiplier = container.getMultiplier();
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(new NumberButton(getGuiLeft() + 65, topSectionTop + 44, 34, 12, multiplier, 1, com.jdte.setup.JDTEConfig.COMMON.advancedTimeAcceleratorMaxMultiplier.get(), Component.translatable("jdte.screen.multiplier"), b -> {
            multiplier = ((NumberButton) b).getValue();
            PacketDistributor.sendToServer(new TimeAcceleratorPayload(multiplier));
        }));
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
