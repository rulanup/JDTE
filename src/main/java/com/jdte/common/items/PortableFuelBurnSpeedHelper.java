package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blocks.resources.CoalBlock_T1;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.resources.Coal_T1;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
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

    /**
     * Returns the smelting fuel time used by a portable generator. Some JDT
     * coal blocks expose their custom multiplier but do not inherit the
     * vanilla fuel tag, so their burn time must use the matching vanilla coal
     * value as a fallback.
     */
    public static int resolveBurnTime(ItemStack fuelStack) {
        int burnTime = fuelStack.getBurnTime(RecipeType.SMELTING);
        if (burnTime > 0) {
            return burnTime;
        }
        Item item = fuelStack.getItem();
        if (item instanceof Coal_T1 || item == Items.COAL) {
            return 1_600;
        }
        if (item == Items.COAL_BLOCK
                || item instanceof BlockItem blockItem && blockItem.getBlock() instanceof CoalBlock_T1) {
            return 16_000;
        }
        return 0;
    }
}
