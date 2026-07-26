package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class BasicFluidStabilizerContainer extends SimpleMachineContainer {
    public BasicFluidStabilizerContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public BasicFluidStabilizerContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.BASIC_FLUID_STABILIZER.get(), windowId, playerInventory, blockPos, JDTEBlocks.BASIC_FLUID_STABILIZER.get());
    }
}
