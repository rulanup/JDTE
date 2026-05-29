package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.ExtendedItemReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedItemReceiverScreen extends BaseMachineScreen<ExtendedItemReceiverContainer> {
    public ExtendedItemReceiverScreen(ExtendedItemReceiverContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
