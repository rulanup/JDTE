package com.jdte.common.blockentities;

import com.jdte.common.blocks.LargeMineralExtractorStructure;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class LargeMineralExtractorBE extends MineralExtractorBE {
    public static final int SURVEY_SLOTS = 4;
    public static final int BASE_SPEED_MULTIPLIER = 4;

    private Direction cachedBoundaryFacing;
    private List<LargeMineralExtractorStructure.BoundaryNeighbor> cachedBoundaryNeighbors = List.of();

    public LargeMineralExtractorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.LARGE_MINERAL_EXTRACTOR.get(), pos, state);
    }

    public List<LargeMineralExtractorStructure.BoundaryNeighbor> getBoundaryNeighbors() {
        Direction facing = LargeMineralExtractorStructure.horizontalFacing(getBlockState());
        if (cachedBoundaryFacing != facing) {
            cachedBoundaryFacing = facing;
            cachedBoundaryNeighbors = LargeMineralExtractorStructure.boundaryNeighbors(worldPosition, facing);
        }
        return cachedBoundaryNeighbors;
    }

    @Override public int surveySlotCount() { return SURVEY_SLOTS; }
    @Override protected long baseWorkPerTick() { return BASE_SPEED_MULTIPLIER; }
}