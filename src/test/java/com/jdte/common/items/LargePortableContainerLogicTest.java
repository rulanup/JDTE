package com.jdte.common.items;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LargePortableContainerLogicTest {
    @Test
    void scalesPocketGeneratorCapacityByFourAtTheLastSafeValue() {
        int basePocketCapacity = 536_870_911;

        assertEquals(2_147_483_644,
                LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity));
    }

    @Test
    void saturatesPocketGeneratorCapacityWhenMultiplicationWouldOverflow() {
        int basePocketCapacity = 536_870_912;

        assertEquals(Integer.MAX_VALUE,
                LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity));
    }

    @Test
    void exposesTheConfiguredPotionBatchCapacityAndBatchSize() {
        assertEquals(4_000, LargePortableContainerLogic.potionCapacity());
        assertEquals(1_000, LargePortableContainerLogic.POTION_BATCH_MB);
    }

    @Test
    void onlyFourPotionsCanFillAOneThousandMillibucketBatchWithinCapacity() {
        assertTrue(LargePortableContainerLogic.canFillPotionBatch(4, 3_000, 4_000));
        assertFalse(LargePortableContainerLogic.canFillPotionBatch(4, 3_001, 4_000));
        assertFalse(LargePortableContainerLogic.canFillPotionBatch(3, 3_000, 4_000));
    }

    @Test
    void scalesFuelCapacityByFourAtTheLastSafeValue() {
        int baseFuelCapacity = 536_870_911;

        assertEquals(2_147_483_644, LargePortableContainerLogic.fuelCapacity(baseFuelCapacity));
    }

    @Test
    void saturatesFuelCapacityWhenMultiplicationWouldOverflow() {
        int baseFuelCapacity = 536_870_912;

        assertEquals(Integer.MAX_VALUE, LargePortableContainerLogic.fuelCapacity(baseFuelCapacity));
    }

    @Test
    void scalesFuelMinimumConsumptionByTenAtTheLastSafeValue() {
        int baseMinimum = 214_748_364;

        assertEquals(2_147_483_640, LargePortableContainerLogic.fuelMinimumConsumption(baseMinimum));
    }

    @Test
    void saturatesFuelMinimumConsumptionWhenMultiplicationWouldOverflow() {
        int baseMinimum = 214_748_365;

        assertEquals(Integer.MAX_VALUE, LargePortableContainerLogic.fuelMinimumConsumption(baseMinimum));
    }

    @Test
    void scalesFuelBurnMultiplierByTenAtTheLastSafeValue() {
        int baseBurnMultiplier = 214_748_364;

        assertEquals(2_147_483_640, LargePortableContainerLogic.fuelBurnMultiplier(baseBurnMultiplier));
    }

    @Test
    void saturatesFuelBurnMultiplierWhenMultiplicationWouldOverflow() {
        int baseBurnMultiplier = 214_748_365;

        assertEquals(Integer.MAX_VALUE, LargePortableContainerLogic.fuelBurnMultiplier(baseBurnMultiplier));
    }
}
