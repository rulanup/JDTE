package com.jdte.common.items;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LargePortableContainerLogicTest {
    @Test
    void scalesPocketGeneratorCapacityByFourWithSaturation() {
        int basePocketCapacity = 12_345;

        assertEquals(basePocketCapacity * 4,
                LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity));
        assertEquals(Integer.MAX_VALUE,
                LargePortableContainerLogic.pocketGeneratorCapacity(Integer.MAX_VALUE));
    }

    @Test
    void exposesTheConfiguredPotionBatchCapacityAndBatchSize() {
        assertEquals(4_000, LargePortableContainerLogic.potionCapacity());
        assertEquals(1_000, LargePortableContainerLogic.POTION_BATCH_MB);
    }

    @Test
    void onlyFourPotionsCanFillAOneThousandMillibucketBatchWithinCapacity() {
        assertTrue(LargePortableContainerLogic.canFillPotionBatch(4, 3_000, 4_000));
        assertFalse(LargePortableContainerLogic.canFillPotionBatch(3, 3_000, 4_000));
    }

    @Test
    void scalesFuelCapacityByFourWithSaturation() {
        int baseFuelCapacity = 9_876;

        assertEquals(baseFuelCapacity * 4,
                LargePortableContainerLogic.fuelCapacity(baseFuelCapacity));
        assertEquals(Integer.MAX_VALUE,
                LargePortableContainerLogic.fuelCapacity(Integer.MAX_VALUE));
    }

    @Test
    void scalesFuelMinimumConsumptionAndBurnMultiplierByTenWithSaturation() {
        int baseMinimum = 77;
        int baseBurnMultiplier = 6;

        assertEquals(baseMinimum * 10, LargePortableContainerLogic.fuelMinimumConsumption(baseMinimum));
        assertEquals(baseBurnMultiplier * 10, LargePortableContainerLogic.fuelBurnMultiplier(baseBurnMultiplier));
        assertEquals(Integer.MAX_VALUE, LargePortableContainerLogic.fuelMinimumConsumption(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, LargePortableContainerLogic.fuelBurnMultiplier(Integer.MAX_VALUE));
    }
}
