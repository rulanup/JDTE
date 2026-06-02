package com.jdte.client.screens;

import com.jdte.common.containers.AdvancedInfusionMachineContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedInfusionMachineScreen extends InfusionMachineScreen<AdvancedInfusionMachineContainer> {
    public AdvancedInfusionMachineScreen(AdvancedInfusionMachineContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
