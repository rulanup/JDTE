package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class BasicItemSenderContainer extends ItemSenderContainerBase {
    public BasicItemSenderContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public BasicItemSenderContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.BASIC_ITEM_SENDER.get(), windowId, playerInventory, blockPos,
                JDTEBlocks.BASIC_ITEM_SENDER.get());
    }

    @Override
    protected boolean useBasicLayout() {
        return true;
    }
}
