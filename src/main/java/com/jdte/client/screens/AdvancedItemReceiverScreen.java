package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.AdvancedItemReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedItemReceiverScreen extends BaseMachineScreen<AdvancedItemReceiverContainer> {
    public AdvancedItemReceiverScreen(AdvancedItemReceiverContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
