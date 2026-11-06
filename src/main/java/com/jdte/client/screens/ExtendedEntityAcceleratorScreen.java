package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.client.screens.widgets.NumberButton;
import com.jdte.common.containers.ExtendedEntityAcceleratorContainer;
import com.jdte.common.network.data.TimeAcceleratorPayload;
import com.jdte.setup.JDTEConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import com.jdte.common.network.JDTEPacketHandler;

public class ExtendedEntityAcceleratorScreen extends BaseMachineScreen<ExtendedEntityAcceleratorContainer> {
    private int multiplier;
    private NumberButton multiplierButton;

    public ExtendedEntityAcceleratorScreen(ExtendedEntityAcceleratorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.multiplier = container.getMultiplier();
    }

    @Override
    public void init() {
        super.init();
        multiplierButton=addRenderableWidget(new NumberButton(getGuiLeft() + 65, topSectionTop + 44, 34, 12, multiplier, 1,
                JDTEConfig.COMMON.entityAccelerator.extendedMaxMultiplier.get(),
                Component.translatable("jdte.screen.multiplier"), b -> {
            multiplier = ((NumberButton) b).getValue();
            JDTEPacketHandler.CHANNEL.sendToServer(new TimeAcceleratorPayload(multiplier));
        }));
    }

    @Override protected void containerTick(){super.containerTick();if(multiplierButton!=null)multiplierButton.setValue(((ExtendedEntityAcceleratorContainer)container).getMultiplier());}

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
