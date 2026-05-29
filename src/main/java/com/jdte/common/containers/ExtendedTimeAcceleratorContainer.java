package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.AdvancedTimeAcceleratorBE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ExtendedTimeAcceleratorContainer extends BaseMachineContainer {
    public ExtendedTimeAcceleratorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedTimeAcceleratorContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_TIME_ACCELERATOR.get(), windowId, playerInventory, blockPos);
        addPlayerSlots(player.getInventory());
    }

    public int getMultiplier() {
        return baseMachineBE instanceof AdvancedTimeAcceleratorBE accelerator ? accelerator.getMultiplier() : 4;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get());
    }
}
