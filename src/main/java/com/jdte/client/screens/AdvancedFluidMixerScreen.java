package com.jdte.client.screens;

import com.jdte.common.containers.AdvancedFluidMixerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedFluidMixerScreen extends FluidMixerScreen<AdvancedFluidMixerContainer> {
    public AdvancedFluidMixerScreen(AdvancedFluidMixerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
