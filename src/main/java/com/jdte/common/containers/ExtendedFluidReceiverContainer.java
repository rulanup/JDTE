package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedFluidReceiverContainer extends FluidReceiverContainerBase {
    public ExtendedFluidReceiverContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedFluidReceiverContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_FLUID_RECEIVER.get(), windowId, playerInventory, blockPos,
                JDTEBlocks.EXTENDED_FLUID_RECEIVER.get());
    }
}
