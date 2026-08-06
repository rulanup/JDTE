package com.jdte.common.containers;

import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;

public final class LargeMineralExtractorContainer extends MineralExtractorContainer {
    public LargeMineralExtractorContainer(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos());
    }

    public LargeMineralExtractorContainer(int id, Inventory inventory, BlockPos pos) {
        super(JDTEMenus.LARGE_MINERAL_EXTRACTOR.get(), id, inventory, pos);
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player,
                JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get());
    }
}