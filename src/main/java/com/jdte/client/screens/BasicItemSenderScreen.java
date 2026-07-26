package com.jdte.client.screens;

import com.jdte.common.containers.BasicItemSenderContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicItemSenderScreen extends ItemSenderScreenBase<BasicItemSenderContainer> {
    public BasicItemSenderScreen(BasicItemSenderContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    protected boolean useBasicLayout() {
        return true;
    }
}
