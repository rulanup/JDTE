package com.jdte.client.screens;

import com.jdte.common.containers.BasicFluidSenderContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicFluidSenderScreen extends FluidSenderScreenBase<BasicFluidSenderContainer> {
    public BasicFluidSenderScreen(BasicFluidSenderContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
