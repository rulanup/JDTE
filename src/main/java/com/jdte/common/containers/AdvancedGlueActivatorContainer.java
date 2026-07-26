package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedGlueActivatorContainer extends SimpleMachineContainer {
    public AdvancedGlueActivatorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public AdvancedGlueActivatorContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.ADVANCED_GLUE_ACTIVATOR.get(), windowId, playerInventory, blockPos, JDTEBlocks.ADVANCED_GLUE_ACTIVATOR.get());
    }
}
