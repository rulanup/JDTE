package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.blocks.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class JDTEBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(JDTE.MODID);

    public static final DeferredHolder<Block, BasicTimeAcceleratorBlock> BASIC_TIME_ACCELERATOR = BLOCKS.register("basic_time_accelerator", BasicTimeAcceleratorBlock::new);
    public static final DeferredHolder<Block, AdvancedTimeAcceleratorBlock> ADVANCED_TIME_ACCELERATOR = BLOCKS.register("advanced_time_accelerator", AdvancedTimeAcceleratorBlock::new);
    public static final DeferredHolder<Block, ExtendedClickerBlock> EXTENDED_CLICKER = BLOCKS.register("extended_clicker", ExtendedClickerBlock::new);
    public static final DeferredHolder<Block, ExtendedBlockBreakerBlock> EXTENDED_BLOCK_BREAKER = BLOCKS.register("extended_block_breaker", ExtendedBlockBreakerBlock::new);
    public static final DeferredHolder<Block, ExtendedBlockPlacerBlock> EXTENDED_BLOCK_PLACER = BLOCKS.register("extended_block_placer", ExtendedBlockPlacerBlock::new);
    public static final DeferredHolder<Block, ExtendedBlockSwapperBlock> EXTENDED_BLOCK_SWAPPER = BLOCKS.register("extended_block_swapper", ExtendedBlockSwapperBlock::new);
    public static final DeferredHolder<Block, ExtendedDropperBlock> EXTENDED_DROPPER = BLOCKS.register("extended_dropper", ExtendedDropperBlock::new);
    public static final DeferredHolder<Block, ExtendedSensorBlock> EXTENDED_SENSOR = BLOCKS.register("extended_sensor", ExtendedSensorBlock::new);
    public static final DeferredHolder<Block, ExtendedFluidCollectorBlock> EXTENDED_FLUID_COLLECTOR = BLOCKS.register("extended_fluid_collector", ExtendedFluidCollectorBlock::new);
    public static final DeferredHolder<Block, ExtendedFluidPlacerBlock> EXTENDED_FLUID_PLACER = BLOCKS.register("extended_fluid_placer", ExtendedFluidPlacerBlock::new);
}
