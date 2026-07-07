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

public class AdvancedItemSenderContainer extends BaseMachineContainer {
    public AdvancedItemSenderContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public AdvancedItemSenderContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_ITEM_SENDER.get(), windowId, playerInventory, blockPos);
        addPlayerSlots(player.getInventory());
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        var config = GuiUpgradeLayoutConfig.getInstance();
        int startX = config.getItemSenderSlotStartX();
        int startY = config.getItemSenderSlotStartY();
        int spacing = config.getItemSenderSlotSpacing();
        int count = config.getItemSenderSlotCount();
        for (int i = 0; i < count; i++) {
            addSlot(new SlotItemHandler(machineHandler, i, startX + i * spacing, startY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.ADVANCED_ITEM_SENDER.get());
    }
}
