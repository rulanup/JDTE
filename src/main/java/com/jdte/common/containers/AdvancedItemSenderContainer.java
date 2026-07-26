package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedItemSenderContainer extends ItemSenderContainerBase {
    public AdvancedItemSenderContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public AdvancedItemSenderContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_ITEM_SENDER.get(), windowId, playerInventory, blockPos,
                JDTEBlocks.ADVANCED_ITEM_SENDER.get());
    }

    @Override
    protected boolean useBasicLayout() {
        return false;
    }
}
