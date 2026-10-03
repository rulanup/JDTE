package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedFluidMixerContainer extends FluidMixerContainer {
    public ExtendedFluidMixerContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedFluidMixerContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_FLUID_MIXER.get(), windowId, playerInventory, blockPos, JDTEBlocks.EXTENDED_FLUID_MIXER.get());
    }
}
