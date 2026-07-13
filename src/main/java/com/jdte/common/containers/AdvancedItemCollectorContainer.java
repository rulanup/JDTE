package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class AdvancedItemCollectorContainer extends BaseMachineContainer {
    public AdvancedItemCollectorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public AdvancedItemCollectorContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_ITEM_COLLECTOR.get(), windowId, playerInventory, blockPos);
        addPlayerSlots(playerInventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player,
                JDTEBlocks.ADVANCED_ITEM_COLLECTOR.get());
    }
}
