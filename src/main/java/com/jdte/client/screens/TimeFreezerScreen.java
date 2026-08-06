package com.jdte.client.screens;

import com.jdte.common.containers.TimeFreezerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TimeFreezerScreen extends TimeFreezerScreenBase<TimeFreezerContainer> {
    public TimeFreezerScreen(TimeFreezerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
