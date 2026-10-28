package com.jdte.common.items;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.resources.Coal_T1;
import com.direwolf20.justdirethings.common.blocks.resources.CoalBlock_T1;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
public final class PortableFuelBurnSpeedHelper {
    private PortableFuelBurnSpeedHelper(){}
    public static int resolveBurnSpeedMultiplier(ItemStack stack){
        if(stack.getItem() instanceof Coal_T1 c)return c.getBurnSpeedMultiplier();
        if(stack.getItem() instanceof BlockItem b&&b.getBlock() instanceof CoalBlock_T1 c)return c.getBurnSpeedMultiplier();
        if(stack.getItem() instanceof LargeFuelCanisterItem)return LargeFuelCanisterItem.getBurnSpeedMultiplier(stack);
        if(stack.getItem() instanceof FuelCanister)return FuelCanister.getBurnSpeedMultiplier(stack);return 1;
    }
}
