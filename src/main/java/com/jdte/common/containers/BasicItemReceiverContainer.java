package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class BasicItemReceiverContainer extends ItemReceiverContainerBase {
    public BasicItemReceiverContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public BasicItemReceiverContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.BASIC_ITEM_RECEIVER.get(), windowId, playerInventory, blockPos,
                JDTEBlocks.BASIC_ITEM_RECEIVER.get());
    }

    @Override
    protected boolean useBasicLayout() {
        return true;
    }
}
