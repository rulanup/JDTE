package com.jdte.client.screens;

import com.jdte.common.containers.AdvancedBioCrusherContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedBioCrusherScreen extends BioCrusherScreen<AdvancedBioCrusherContainer> {
    public AdvancedBioCrusherScreen(AdvancedBioCrusherContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
