package com.jdte.common.recipes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record GreenhouseCropDefinition(List<ItemStack> outputs, ResourceLocation displayBlock,
                                       ResourceLocation harvestBlock, boolean useLootTable,
                                       int growthWork, int timeFluid, HarvestGenerator harvestGenerator) {
    public GreenhouseCropDefinition(List<ItemStack> outputs, ResourceLocation displayBlock,
                                    ResourceLocation harvestBlock, boolean useLootTable,
                                    int growthWork, int timeFluid) {
        this(outputs, displayBlock, harvestBlock, useLootTable, growthWork, timeFluid, null);
    }
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

    public List<ItemStack> generateHarvest(ServerLevel level, BlockPos pos, ItemStack tool) {
        return harvestGenerator == null ? outputs() : harvestGenerator.generate(level, pos, tool);
    }

    @FunctionalInterface
    public interface HarvestGenerator {
        List<ItemStack> generate(ServerLevel level, BlockPos pos, ItemStack tool);
    }
}
