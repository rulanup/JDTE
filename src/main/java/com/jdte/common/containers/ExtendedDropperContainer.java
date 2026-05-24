package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.DropperT2Container;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ExtendedDropperContainer extends DropperT2Container {
    public ExtendedDropperContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedDropperContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(windowId, playerInventory, blockPos);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.EXTENDED_DROPPER.get());
    }
}
