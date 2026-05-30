package com.jdte.client.screens;

import com.jdte.common.containers.ExtendedGelGeneratorContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedGelGeneratorScreen extends GelGeneratorScreen<ExtendedGelGeneratorContainer> {
    public ExtendedGelGeneratorScreen(ExtendedGelGeneratorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
