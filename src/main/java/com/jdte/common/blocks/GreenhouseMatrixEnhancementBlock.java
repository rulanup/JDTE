package com.jdte.common.blocks;

import com.jdte.common.greenhouse.GreenhouseMatrixEnhancement;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class GreenhouseMatrixEnhancementBlock extends Block {
    private final GreenhouseMatrixEnhancement enhancement;

    public GreenhouseMatrixEnhancementBlock(GreenhouseMatrixEnhancement enhancement) {
        super(Properties.of().strength(4.0F).sound(SoundType.METAL).lightLevel(state -> 5));
        this.enhancement = enhancement;
    }

    public GreenhouseMatrixEnhancement enhancement() { return enhancement; }
}
