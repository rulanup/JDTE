package com.jdte.common.entities;

import com.jdte.setup.JDTEEntities;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeAcceleratorEffectEntityTest {

    @Test
    void calculateExponentHandlesZeroAndPowersOfTwoSafely() {
        assertEquals(0, TimeAcceleratorEffectEntity.calculateExponent(0));
        assertEquals(0, TimeAcceleratorEffectEntity.calculateExponent(-5));
        assertEquals(0, TimeAcceleratorEffectEntity.calculateExponent(1));
        assertEquals(1, TimeAcceleratorEffectEntity.calculateExponent(2));
        assertEquals(2, TimeAcceleratorEffectEntity.calculateExponent(4));
        assertEquals(4, TimeAcceleratorEffectEntity.calculateExponent(16));
        assertEquals(5, TimeAcceleratorEffectEntity.calculateExponent(32));
        assertEquals(10, TimeAcceleratorEffectEntity.calculateExponent(1024));
    }

    @Test
    void renewRefreshesTimersAndExponent() {
        TimeAcceleratorEffectEntity entity = new TimeAcceleratorEffectEntity(
                JDTEEntities.TIME_ACCELERATOR_EFFECT.get(), null);
        entity.setRemainingTime(3);
        entity.setTotalTime(10);
        entity.setTickSpeed(1);

        entity.renew(16, 20);

        assertEquals(4, entity.getTickSpeed());
        assertEquals(20, entity.getTotalTime());
        assertEquals(20, entity.getRemainingTime());
        assertEquals(20, entity.getServerRemainingTicks());
    }
}
