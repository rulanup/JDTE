package com.jdte.common.containers;

import com.jdte.common.blockentities.CrystalIncubatorBE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class CrystalIncubatorContainer extends ExtendedItemReceiverContainer {
    public CrystalIncubatorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public CrystalIncubatorContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.CRYSTAL_INCUBATOR.get(), windowId, playerInventory, blockPos);
    }

    public int getMultiplier() {
        return baseMachineBE instanceof CrystalIncubatorBE incubator
                ? incubator.getMultiplier()
                : JDTEConfig.COMMON.crystalIncubatorMaxMultiplier.get();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.CRYSTAL_INCUBATOR.get());
    }
}
