package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.neoforged.neoforge.items.SlotItemHandler;

public class AdvancedPotionBrewerContainer extends BaseMachineContainer {
    public final ContainerData brewerData;

    public AdvancedPotionBrewerContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public AdvancedPotionBrewerContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_POTION_BREWER.get(), windowId, playerInventory, blockPos);
        if (baseMachineBE instanceof AdvancedPotionBrewerBE brewer) {
            brewerData = brewer.brewerData;
            addDataSlots(brewerData);
        } else {
            brewerData = null;
        }
        addPlayerSlots(player.getInventory());
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        addSlot(new SlotItemHandler(machineHandler, AdvancedPotionBrewerBE.BOTTLE_SLOT_0, 56, 47));
        addSlot(new SlotItemHandler(machineHandler, AdvancedPotionBrewerBE.BOTTLE_SLOT_1, 79, 58));
        addSlot(new SlotItemHandler(machineHandler, AdvancedPotionBrewerBE.BOTTLE_SLOT_2, 102, 47));
        addSlot(new SlotItemHandler(machineHandler, AdvancedPotionBrewerBE.INGREDIENT_SLOT, 79, 17));
        addSlot(new SlotItemHandler(machineHandler, AdvancedPotionBrewerBE.FUEL_SLOT, 17, 17));
    }

    public int getFuel() {
        return brewerData == null ? 0 : brewerData.get(2);
    }

    public int getBrewProgress() {
        return brewerData == null ? 0 : brewerData.get(0);
    }

    public int getBrewProgressMax() {
        return brewerData == null ? 1 : Math.max(1, brewerData.get(1));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.ADVANCED_POTION_BREWER.get());
    }
}
