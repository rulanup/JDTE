package com.jdte.common.upgrades;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeneratorFuelBatchTest {
    @Test void zeroFuelCannotCommit() { assertFalse(GeneratorFuelBatch.canCommit(0, 300, 300)); }
    @Test void lastSingleMbCannotCommit() { assertFalse(GeneratorFuelBatch.canCommit(1, 300, 300)); }
    @Test void wholeBatchProducesThreeTimesNativeEnergy() {
        int output = GeneratorFuelBatch.energyOutput(100);
        assertEquals(300, output);
        assertTrue(GeneratorFuelBatch.canCommit(2, output, 300));
    }
    @Test void largerFuelBufferStillPurchasesOnlyOneBatch() {
        assertEquals(2, GeneratorFuelBatch.FLUID_COST);
        assertTrue(GeneratorFuelBatch.canCommit(999, 300, 300));
    }
    @Test void partialEnergyRoomCannotConsumeFuel() {
        assertFalse(GeneratorFuelBatch.canCommit(2, 300, 299));
        assertFalse(GeneratorFuelBatch.canCommit(2, 300, 0));
    }
    @Test void invalidFuelOutputCannotCommit() {
        assertEquals(0, GeneratorFuelBatch.energyOutput(0));
        assertEquals(0, GeneratorFuelBatch.energyOutput(-1));
        assertFalse(GeneratorFuelBatch.canCommit(2, 0, 0));
    }
    @Test void overflowingOutputDoesNotBecomeNegativeOrWrap() {
        assertEquals(0, GeneratorFuelBatch.energyOutput(Integer.MAX_VALUE));
        assertEquals(2_147_483_646, GeneratorFuelBatch.energyOutput(715_827_882));
    }
}
