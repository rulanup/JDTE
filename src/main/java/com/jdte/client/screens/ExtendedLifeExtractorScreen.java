package com.jdte.client.screens;

import com.jdte.common.containers.ExtendedLifeExtractorContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedLifeExtractorScreen extends LifeExtractorScreen<ExtendedLifeExtractorContainer> {
    public ExtendedLifeExtractorScreen(ExtendedLifeExtractorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
