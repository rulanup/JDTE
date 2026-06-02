package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.LifeExtractorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public abstract class LifeExtractorContainer extends BaseMachineContainer {
    protected ContainerData lifeExtractorData;

    protected LifeExtractorContainer(@Nullable MenuType<?> menuType, int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(menuType, windowId, playerInventory, blockPos);
        if (baseMachineBE instanceof LifeExtractorBE extractor) {
            lifeExtractorData = extractor.getLifeExtractorData();
            addDataSlots(lifeExtractorData);
        }
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
    }

    public int getMode() {
        return lifeExtractorData == null ? 0 : lifeExtractorData.get(0);
    }
}
