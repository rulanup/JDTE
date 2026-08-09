package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LootFabricatorCostTest {

    @Test
    void fractionalTimeFluidCostSettlesExactlyAcrossOperations() {
        int creditUnits = 0;
        int drainedMb = 0;

        for (int operation = 0; operation < 5; operation++) {
            LootFabricatorFluidCost.Settlement settlement =
                    LootFabricatorFluidCost.settle(1, creditUnits);
            drainedMb += settlement.drainMb();
            creditUnits = settlement.remainingCreditUnits();
        }

        assertEquals(1, drainedMb);
        assertEquals(0, creditUnits);
    }

    @Test
    void batchedAndIncrementalSettlementHaveTheSameCost() {
        LootFabricatorFluidCost.Settlement batch =
                LootFabricatorFluidCost.settle(23, 0);

        int creditUnits = 0;
        int drainedMb = 0;
        for (int operation = 0; operation < 23; operation++) {
            LootFabricatorFluidCost.Settlement settlement =
                    LootFabricatorFluidCost.settle(1, creditUnits);
            drainedMb += settlement.drainMb();
            creditUnits = settlement.remainingCreditUnits();
        }

        assertEquals(batch.drainMb(), drainedMb);
        assertEquals(batch.remainingCreditUnits(), creditUnits);
        assertEquals(5, drainedMb);
        assertEquals(2, creditUnits);
    }

    @Test
    void existingCreditReducesTheNextRequiredDrain() {
        LootFabricatorFluidCost.Settlement settlement =
                LootFabricatorFluidCost.settle(4, 4);

        assertEquals(0, settlement.drainMb());
        assertEquals(0, settlement.remainingCreditUnits());
    }

    @Test
    void displayFormattingUsesFifthsOfAMillibucket() {
        assertEquals("0.2", LootFabricatorFluidCost.format(1));
        assertEquals("1", LootFabricatorFluidCost.format(5));
        assertEquals("4", LootFabricatorFluidCost.format(20));
    }
}