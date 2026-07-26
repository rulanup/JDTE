package com.jdte.common.containers;

import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedBioCrusherContainer extends BioCrusherContainer {
    public AdvancedBioCrusherContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_BIO_CRUSHER.get(), windowId, playerInventory, blockPos);
    }

    public AdvancedBioCrusherContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }
}
