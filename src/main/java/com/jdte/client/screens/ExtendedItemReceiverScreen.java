package com.jdte.client.screens;

import com.jdte.common.containers.ExtendedItemReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedItemReceiverScreen<T extends ExtendedItemReceiverContainer> extends ItemReceiverScreenBase<T> {
    public ExtendedItemReceiverScreen(T container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    protected boolean useBasicLayout() {
        return false;
    }
}
