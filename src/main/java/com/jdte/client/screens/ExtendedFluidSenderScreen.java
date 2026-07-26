package com.jdte.client.screens;

import com.jdte.common.containers.ExtendedFluidSenderContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedFluidSenderScreen extends FluidSenderScreenBase<ExtendedFluidSenderContainer> {
    public ExtendedFluidSenderScreen(ExtendedFluidSenderContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
