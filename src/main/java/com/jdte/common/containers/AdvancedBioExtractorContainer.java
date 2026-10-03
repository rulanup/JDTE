package com.jdte.common.containers;

import com.jdte.common.blockentities.AdvancedBioExtractorBE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.SlotItemHandler;

public class AdvancedBioExtractorContainer extends BioExtractorContainer {
    public AdvancedBioExtractorContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_BIO_EXTRACTOR.get(), windowId, playerInventory, blockPos, JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get());
    }

    public AdvancedBioExtractorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    @Override
    public void addMachineSlots() {
        if (baseMachineBE instanceof AdvancedBioExtractorBE extractor) {
            machineHandler = extractor.getMachineHandler();
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int slotIndex = row * 3 + col;
                    addSlot(new SlotItemHandler(machineHandler, slotIndex, 62 + col * 18, 20 + row * 18));
                }
            }
        }
    }
}
