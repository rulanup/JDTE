package com.jdte.common.containers;

import com.jdte.common.blockentities.ExtendedBioExtractorBE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ExtendedBioExtractorContainer extends BioExtractorContainer {
    public ExtendedBioExtractorContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_BIO_EXTRACTOR.get(), windowId, playerInventory, blockPos, JDTEBlocks.EXTENDED_BIO_EXTRACTOR.get());
    }

    public ExtendedBioExtractorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    @Override
    public void addMachineSlots() {
        if (baseMachineBE instanceof ExtendedBioExtractorBE extractor) {
            machineHandler = extractor.getMachineHandler();
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 9; col++) {
                    int slotIndex = row * 9 + col;
                    addSlot(new SlotItemHandler(machineHandler, slotIndex, 8 + col * 18, 24 + row * 18));
                }
            }
        }
    }
}
