package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.BasicFluidReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicFluidReceiverScreen extends BaseMachineScreen<BasicFluidReceiverContainer> {
    public BasicFluidReceiverScreen(BasicFluidReceiverContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
