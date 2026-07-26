package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedInfusionMachineContainer extends InfusionMachineContainer {
    public AdvancedInfusionMachineContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public AdvancedInfusionMachineContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_INFUSION_MACHINE.get(), windowId, playerInventory, blockPos, JDTEBlocks.ADVANCED_INFUSION_MACHINE.get());
    }
}
