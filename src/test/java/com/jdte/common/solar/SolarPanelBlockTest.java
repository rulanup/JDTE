package com.jdte.common.solar;

import com.jdte.common.blocks.SolarPanelBlock;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolarPanelBlockTest {
    @Test
    void panelUsesTheSameFourPixelHighShapeAsItsModel() {
        SolarPanelBlock panel = (SolarPanelBlock) JDTEBlocks.CONCENTRATED_SOLAR_PANEL.get();

        VoxelShape shape = panel.defaultBlockState().getShape(EmptyBlockGetter.INSTANCE,
                BlockPos.ZERO, CollisionContext.empty());

        assertEquals(0.0D, shape.bounds().minY);
        assertEquals(0.25D, shape.bounds().maxY);
    }

}
