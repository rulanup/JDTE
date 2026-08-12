package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreenhouseProductionEngineTest {
    @Test
    void groupSettlementIsEquivalentWhenElapsedTicksAreChunked() {
        long oneShotWork = 0L;
        long oneShotHarvests = 0L;
        GreenhouseProductionEngine.GroupWorkWindow oneShot = GreenhouseProductionEngine.accumulateGroup(
                oneShotWork, 20L, 512L, 3_000, 4_096, Long.MAX_VALUE, Long.MAX_VALUE);
        oneShotHarvests += oneShot.requestedHarvests();
        oneShotWork = oneShot.remainingAfter(oneShot.requestedHarvests());

        long chunkedWork = 0L;
        long chunkedHarvests = 0L;
        for (int tick = 0; tick < 20; tick++) {
            GreenhouseProductionEngine.GroupWorkWindow chunk = GreenhouseProductionEngine.accumulateGroup(
                    chunkedWork, 1L, 512L, 3_000, 4_096, Long.MAX_VALUE, Long.MAX_VALUE);
            chunkedHarvests += chunk.requestedHarvests();
            chunkedWork = chunk.remainingAfter(chunk.requestedHarvests());
        }

        assertEquals(oneShotHarvests, chunkedHarvests);
        assertEquals(oneShotWork, chunkedWork);
    }

    @Test
    void groupSettlementSaturatesInsteadOfOverflowingNegative() {
        GreenhouseProductionEngine.GroupWorkWindow window = GreenhouseProductionEngine.accumulateGroup(
                Long.MAX_VALUE - 10L, Long.MAX_VALUE, Long.MAX_VALUE, Integer.MAX_VALUE,
                1, Long.MAX_VALUE, Long.MAX_VALUE);

        assertEquals(Long.MAX_VALUE, window.availableWork());
        assertEquals(Long.MAX_VALUE, window.requestedHarvests());
        assertEquals(0L, window.remainingAfter(Long.MAX_VALUE));
    }
}
