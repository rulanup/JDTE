package com.jdte.common.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record GreenhouseCropDefinition(List<ItemStack> outputs, ResourceLocation displayBlock,
                                       ResourceLocation harvestBlock, boolean useLootTable,
                                       int growthWork, int timeFluid) {
    public GreenhouseCropDefinition {
        outputs = outputs.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
        harvestBlock = harvestBlock == null ? displayBlock : harvestBlock;
        growthWork = Math.max(1, growthWork);
        timeFluid = Math.max(1, timeFluid);
    }

    @Override
    public List<ItemStack> outputs() {
        return outputs.stream().map(ItemStack::copy).toList();
    }
}
