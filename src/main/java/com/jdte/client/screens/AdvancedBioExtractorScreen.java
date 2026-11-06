package com.jdte.client.screens;

import com.jdte.common.containers.AdvancedBioExtractorContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedBioExtractorScreen extends BioExtractorScreen<AdvancedBioExtractorContainer> {
    public AdvancedBioExtractorScreen(AdvancedBioExtractorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
