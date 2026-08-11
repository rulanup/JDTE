package com.jdte.common.blocks;

import com.jdte.common.blockentities.GreenhouseMatrixPortBE;
import com.jdte.common.greenhouse.GreenhouseMatrixPortType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GreenhouseMatrixPortBlock extends Block implements EntityBlock {
    private final GreenhouseMatrixPortType portType;

    public GreenhouseMatrixPortBlock(GreenhouseMatrixPortType portType) {
        super(Properties.of().strength(5.0F).sound(SoundType.METAL));
        this.portType = portType;
    }

    public GreenhouseMatrixPortType portType() { return portType; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GreenhouseMatrixPortBE(pos, state);
    }
}
