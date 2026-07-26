package com.jdte.client.screens;

import com.jdte.common.containers.BasicFluidReceiverContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicFluidReceiverScreen extends FluidReceiverScreenBase<BasicFluidReceiverContainer> {
    public BasicFluidReceiverScreen(BasicFluidReceiverContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
