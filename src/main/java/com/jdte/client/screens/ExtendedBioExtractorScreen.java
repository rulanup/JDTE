package com.jdte.client.screens;

import com.jdte.common.containers.ExtendedBioExtractorContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedBioExtractorScreen extends BioExtractorScreen<ExtendedBioExtractorContainer> {
    public ExtendedBioExtractorScreen(ExtendedBioExtractorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
