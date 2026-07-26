package com.jdte.client.screens;

import com.jdte.common.containers.AdvancedFluidReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedFluidReceiverScreen extends FluidReceiverScreenBase<AdvancedFluidReceiverContainer> {
    public AdvancedFluidReceiverScreen(AdvancedFluidReceiverContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
