package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.BasicFluidSenderContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicFluidSenderScreen extends BaseMachineScreen<BasicFluidSenderContainer> {
    public BasicFluidSenderScreen(BasicFluidSenderContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
