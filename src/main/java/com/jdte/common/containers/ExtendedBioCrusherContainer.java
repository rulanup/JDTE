package com.jdte.common.containers;

import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedBioCrusherContainer extends BioCrusherContainer {
    public ExtendedBioCrusherContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_BIO_CRUSHER.get(), windowId, playerInventory, blockPos);
    }

    public ExtendedBioCrusherContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }
}
