package com.jdte.client.screens;

import com.jdte.common.containers.AdvancedItemReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedItemReceiverScreen extends ItemReceiverScreenBase<AdvancedItemReceiverContainer> {
    public AdvancedItemReceiverScreen(AdvancedItemReceiverContainer container, Inventory inv, Component name) {
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
