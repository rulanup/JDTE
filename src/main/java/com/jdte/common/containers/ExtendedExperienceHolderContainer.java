package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class ExtendedExperienceHolderContainer extends BaseMachineContainer {
    public ExtendedExperienceHolderContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedExperienceHolderContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_EXPERIENCE_HOLDER.get(), windowId, playerInventory, blockPos);
        addPlayerSlots(playerInventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player,
                JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return super.quickMoveStack(player, index);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }
}
