package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ExtendedItemReceiverContainer extends BaseMachineContainer {
    public ExtendedItemReceiverContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedItemReceiverContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_ITEM_RECEIVER.get(), windowId, playerInventory, blockPos);
        addPlayerSlots(player.getInventory());
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        var config = GuiUpgradeLayoutConfig.getInstance();
        int startX = config.getItemReceiverSlotStartX();
        int startY = config.getItemReceiverSlotStartY();
        int spacing = config.getItemReceiverSlotSpacing();
        int count = config.getItemReceiverSlotCount();
        for (int i = 0; i < count; i++) {
            addSlot(new SlotItemHandler(machineHandler, i, startX + i * spacing, startY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.EXTENDED_ITEM_RECEIVER.get());
    }
}
