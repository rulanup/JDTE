package com.jdte.client.screens;

import com.jdte.common.containers.AdvancedFluidSenderContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedFluidSenderScreen extends FluidSenderScreenBase<AdvancedFluidSenderContainer> {
    public AdvancedFluidSenderScreen(AdvancedFluidSenderContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
