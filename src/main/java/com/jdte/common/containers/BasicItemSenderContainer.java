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

public class BasicItemSenderContainer extends BaseMachineContainer {
    public BasicItemSenderContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public BasicItemSenderContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.BASIC_ITEM_SENDER.get(), windowId, playerInventory, blockPos);
        addPlayerSlots(player.getInventory());
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        var config = GuiUpgradeLayoutConfig.getInstance();
        int startX = config.getBasicItemSenderSlotStartX();
        int startY = config.getBasicItemSenderSlotStartY();
        int spacing = config.getBasicItemSenderSlotSpacing();
        int count = config.getBasicItemSenderSlotCount();
        for (int i = 0; i < count; i++) {
            addSlot(new SlotItemHandler(machineHandler, i, startX + i * spacing, startY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.BASIC_ITEM_SENDER.get());
    }
}
