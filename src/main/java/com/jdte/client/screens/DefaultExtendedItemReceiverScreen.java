package com.jdte.client.screens;

import com.jdte.common.containers.ExtendedItemReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DefaultExtendedItemReceiverScreen extends ExtendedItemReceiverScreen<ExtendedItemReceiverContainer> {
    public DefaultExtendedItemReceiverScreen(ExtendedItemReceiverContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }
}
