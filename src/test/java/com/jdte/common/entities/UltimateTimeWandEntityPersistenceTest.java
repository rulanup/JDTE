package com.jdte.common.entities;

import com.jdte.setup.JDTEEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UltimateTimeWandEntityPersistenceTest {

    @Test
    void persistedStateKeepsTargetExponentAndTimers() {
        UltimateTimeWandEntity.WandState original = new UltimateTimeWandEntity.WandState(
                new BlockPos(4, 5, 6), 10, 600, 477);

        CompoundTag tag = UltimateTimeWandEntity.saveState(original);

        assertEquals(original, UltimateTimeWandEntity.loadState(tag));
    }

    @Test
    void mergeAddsStepAndHalfElapsedTimeWithoutPassingMaxExponent() {
        UltimateTimeWandEntity.WandState merged = UltimateTimeWandEntity.merge(
                new UltimateTimeWandEntity.WandState(BlockPos.ZERO, 4, 600, 400), 10, 10);

        assertEquals(10, merged.exponent());
        assertEquals(500, merged.remainingTime());
    }

    @Test
    void placesRenderEntityAtTargetBlockBaseHeight() {
        UltimateTimeWandEntity entity = new UltimateTimeWandEntity(
                JDTEEntities.ULTIMATE_TIME_WAND.get(), null);
        BlockPos target = new BlockPos(4, 5, 6);

        entity.applyWandState(new UltimateTimeWandEntity.WandState(target, 1, 600, 600));

        assertEquals(target.getX() + 0.5D, entity.getX(), 0.0D);
        assertEquals(target.getY(), entity.getY(), 0.0D);
        assertEquals(target.getZ() + 0.5D, entity.getZ(), 0.0D);
    }
}
