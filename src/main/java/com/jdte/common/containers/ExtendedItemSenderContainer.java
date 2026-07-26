package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ExtendedItemSenderContainer extends ItemSenderContainerBase {
    public ExtendedItemSenderContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedItemSenderContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_ITEM_SENDER.get(), windowId, playerInventory, blockPos,
                JDTEBlocks.EXTENDED_ITEM_SENDER.get());
    }

    @Override
    protected boolean useBasicLayout() {
        return false;
    }
}
