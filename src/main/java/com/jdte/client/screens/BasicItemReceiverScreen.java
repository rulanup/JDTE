package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.BasicItemReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicItemReceiverScreen extends BaseMachineScreen<BasicItemReceiverContainer> {
    public BasicItemReceiverScreen(BasicItemReceiverContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
