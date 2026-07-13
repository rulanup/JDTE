package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.AdvancedItemCollectorContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedItemCollectorScreen extends BaseMachineScreen<AdvancedItemCollectorContainer> {
    public AdvancedItemCollectorScreen(AdvancedItemCollectorContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
