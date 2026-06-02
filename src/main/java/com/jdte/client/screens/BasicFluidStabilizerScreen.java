package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.BasicFluidStabilizerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicFluidStabilizerScreen extends BaseMachineScreen<BasicFluidStabilizerContainer> {
    public BasicFluidStabilizerScreen(BasicFluidStabilizerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }
}
