package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedInfusionMachineContainer extends InfusionMachineContainer {
    public ExtendedInfusionMachineContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedInfusionMachineContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_INFUSION_MACHINE.get(), windowId, playerInventory, blockPos, JDTEBlocks.EXTENDED_INFUSION_MACHINE.get());
    }
}
