package com.jdte.client.screens;

import com.jdte.common.containers.BasicItemReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicItemReceiverScreen extends ItemReceiverScreenBase<BasicItemReceiverContainer> {
    public BasicItemReceiverScreen(BasicItemReceiverContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    protected boolean useBasicLayout() {
        return true;
    }
}
