package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedLifeExtractorContainer extends LifeExtractorContainer {
    public AdvancedLifeExtractorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public AdvancedLifeExtractorContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_LIFE_EXTRACTOR.get(), windowId, playerInventory, blockPos, JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get());
    }
}
