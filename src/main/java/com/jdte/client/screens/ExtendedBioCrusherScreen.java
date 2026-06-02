package com.jdte.client.screens;

import com.jdte.common.containers.ExtendedBioCrusherContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedBioCrusherScreen extends BioCrusherScreen<ExtendedBioCrusherContainer> {
    public ExtendedBioCrusherScreen(ExtendedBioCrusherContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
