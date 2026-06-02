package com.jdte.common.containers;

import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class AdvancedBioCrusherContainer extends BioCrusherContainer {
    public AdvancedBioCrusherContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_BIO_CRUSHER.get(), windowId, playerInventory, blockPos);
    }

    public AdvancedBioCrusherContainer(int windowId, Inventory playerInventory, net.minecraft.network.FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
