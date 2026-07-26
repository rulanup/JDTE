package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.SlotItemHandler;

public abstract class ItemReceiverContainerBase extends BaseMachineContainer {
    private final Block machineBlock;

    protected ItemReceiverContainerBase(MenuType<?> menuType, int windowId, Inventory playerInventory,
                                        BlockPos blockPos, Block machineBlock) {
        super(menuType, windowId, playerInventory, blockPos);
        this.machineBlock = machineBlock;
        addPlayerSlots(player.getInventory());
    }

    protected abstract boolean useBasicLayout();

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        var config = GuiUpgradeLayoutConfig.getInstance();
        boolean basic = useBasicLayout();
        int startX = basic ? config.getBasicItemReceiverSlotStartX() : config.getItemReceiverSlotStartX();
        int startY = basic ? config.getBasicItemReceiverSlotStartY() : config.getItemReceiverSlotStartY();
        int spacing = basic ? config.getBasicItemReceiverSlotSpacing() : config.getItemReceiverSlotSpacing();
        int count = basic ? config.getBasicItemReceiverSlotCount() : config.getItemReceiverSlotCount();
        for (int i = 0; i < count; i++) {
            addSlot(new SlotItemHandler(machineHandler, i, startX + i * spacing, startY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, machineBlock);
    }
}
