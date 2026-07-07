package com.jdte.client.screens;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.jdte.common.containers.ExtendedFluidStabilizerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedFluidStabilizerScreen extends BaseMachineScreen<ExtendedFluidStabilizerContainer> {
    public ExtendedFluidStabilizerScreen(ExtendedFluidStabilizerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    public void setTopSection() {
        extraWidth = 60;
        extraHeight = 0;
    }

    @Override
    public int getFluidBarOffset() {
        return 204;
    }
}
