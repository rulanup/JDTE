package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blocks.resources.CoalBlock_T1;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.resources.Coal_T1;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class PortableFuelBurnSpeedHelper {
    private PortableFuelBurnSpeedHelper() {
    }

    public static int resolveBurnSpeedMultiplier(ItemStack fuelStack) {
        Item item = fuelStack.getItem();
        if (item instanceof Coal_T1 direCoal) {
            return direCoal.getBurnSpeedMultiplier();
        }
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof CoalBlock_T1 coalBlock) {
                return coalBlock.getBurnSpeedMultiplier();
            }
        }
        if (item instanceof LargeFuelCanisterItem) {
            return LargeFuelCanisterItem.getBurnSpeedMultiplier(fuelStack);
        }
        if (item instanceof FuelCanister) {
            return FuelCanister.getBurnSpeedMultiplier(fuelStack);
        }
        return 1;
    }
}
