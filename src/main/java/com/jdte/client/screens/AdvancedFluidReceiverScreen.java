package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.AdvancedFluidReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedFluidReceiverScreen extends BaseMachineScreen<AdvancedFluidReceiverContainer> {
    public AdvancedFluidReceiverScreen(AdvancedFluidReceiverContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
