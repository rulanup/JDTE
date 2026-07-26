package com.jdte.client.screens;

import com.jdte.common.containers.ExtendedFluidReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedFluidReceiverScreen extends FluidReceiverScreenBase<ExtendedFluidReceiverContainer> {
    public ExtendedFluidReceiverScreen(ExtendedFluidReceiverContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
