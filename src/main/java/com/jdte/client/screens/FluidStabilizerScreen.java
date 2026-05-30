package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.FluidStabilizerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FluidStabilizerScreen extends BaseMachineScreen<FluidStabilizerContainer> {
    public FluidStabilizerScreen(FluidStabilizerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
