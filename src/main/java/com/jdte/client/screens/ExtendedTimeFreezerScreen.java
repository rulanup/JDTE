package com.jdte.client.screens;

import com.jdte.common.containers.ExtendedTimeFreezerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedTimeFreezerScreen extends TimeFreezerScreenBase<ExtendedTimeFreezerContainer> {
    public ExtendedTimeFreezerScreen(ExtendedTimeFreezerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
