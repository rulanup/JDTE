package com.jdte.client.screens;

import com.jdte.common.containers.AdvancedLifeExtractorContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedLifeExtractorScreen extends LifeExtractorScreen<AdvancedLifeExtractorContainer> {
    public AdvancedLifeExtractorScreen(AdvancedLifeExtractorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
