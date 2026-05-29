package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.ExtendedItemSenderContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedItemSenderScreen extends BaseMachineScreen<ExtendedItemSenderContainer> {
    public ExtendedItemSenderScreen(ExtendedItemSenderContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
