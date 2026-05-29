package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.BasicGlueActivatorContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicGlueActivatorScreen extends BaseMachineScreen<BasicGlueActivatorContainer> {
    public BasicGlueActivatorScreen(BasicGlueActivatorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
