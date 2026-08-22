package com.jdte.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrystalIncubatorAccelerationTest {

    @Test
    void standardEnergyCostUsesDurationAdjustedWorkTicks() throws Exception {
        RecordingCrystalIncubator incubator =
                (RecordingCrystalIncubator) unsafe().allocateInstance(RecordingCrystalIncubator.class);

        assertEquals(401, incubator.getStandardEnergyCost());
        assertEquals(4, incubator.recordedMultiplier);
        assertEquals(400, incubator.recordedEnergyWorkTicks);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static final class RecordingCrystalIncubator extends CrystalIncubatorBE {
        private int recordedMultiplier;
        private int recordedEnergyWorkTicks;

        private RecordingCrystalIncubator() {
            super(BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }

        @Override
        public int getEffectiveMultiplier() {
            return 4;
        }

        @Override
        protected int getAccelerationWorkTicks(int effectiveMultiplier) {
            recordedMultiplier = effectiveMultiplier;
            return 400;
        }

        @Override
        protected int getEnergyCost(int workTicks) {
            recordedEnergyWorkTicks = workTicks;
            return workTicks + 1;
        }
    }
}
