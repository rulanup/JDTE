package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.InfusionMachineBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public abstract class InfusionMachineContainer extends BaseMachineContainer {
    protected ContainerData infusionData;

    protected InfusionMachineContainer(@Nullable MenuType<?> menuType, int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(menuType, windowId, playerInventory, blockPos);
        if (baseMachineBE instanceof InfusionMachineBE infusion) {
            infusionData = infusion.getInfusionData();
            addDataSlots(infusionData);
        }
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        addSlot(new SlotItemHandler(machineHandler, InfusionMachineBE.INPUT_SLOT, 56, 35));
        addSlot(new SlotItemHandler(machineHandler, InfusionMachineBE.OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean mayPickup(net.minecraft.world.entity.player.Player player) {
                return true;
            }
        });
    }

    public int getProgress() {
        return infusionData == null ? 0 : infusionData.get(0);
    }

    public int getProgressMax() {
        return infusionData == null ? 1 : Math.max(1, infusionData.get(1));
    }
}
