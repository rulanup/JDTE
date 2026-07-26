package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ExtendedItemReceiverContainer extends ItemReceiverContainerBase {
    public ExtendedItemReceiverContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedItemReceiverContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        this(JDTEMenus.EXTENDED_ITEM_RECEIVER.get(), windowId, playerInventory, blockPos);
    }

    protected ExtendedItemReceiverContainer(MenuType<?> menuType, int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(menuType, windowId, playerInventory, blockPos, JDTEBlocks.EXTENDED_ITEM_RECEIVER.get());
    }

    @Override
    protected boolean useBasicLayout() {
        return false;
    }
}
