package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class BasicFluidReceiverContainer extends FluidReceiverContainerBase {
    public BasicFluidReceiverContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public BasicFluidReceiverContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.BASIC_FLUID_RECEIVER.get(), windowId, playerInventory, blockPos,
                JDTEBlocks.BASIC_FLUID_RECEIVER.get());
    }
}
