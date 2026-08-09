package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ExtendedTimeFreezerContainer extends BaseMachineContainer {
    public ExtendedTimeFreezerContainer(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos());
    }

    public ExtendedTimeFreezerContainer(int id, Inventory inventory, BlockPos pos) {
        super(JDTEMenus.EXTENDED_TIME_FREEZER.get(), id, inventory, pos);
        addPlayerSlots(player.getInventory());
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.EXTENDED_TIME_FREEZER.get());
    }
}
