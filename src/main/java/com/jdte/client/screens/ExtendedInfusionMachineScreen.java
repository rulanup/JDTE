package com.jdte.client.screens;

import com.jdte.common.containers.ExtendedInfusionMachineContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedInfusionMachineScreen extends InfusionMachineScreen<ExtendedInfusionMachineContainer> {
    public ExtendedInfusionMachineScreen(ExtendedInfusionMachineContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
