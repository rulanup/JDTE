package com.jdte.client.screens;

import com.jdte.common.containers.ExtendedFluidMixerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedFluidMixerScreen extends FluidMixerScreen<ExtendedFluidMixerContainer> {
    public ExtendedFluidMixerScreen(ExtendedFluidMixerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
