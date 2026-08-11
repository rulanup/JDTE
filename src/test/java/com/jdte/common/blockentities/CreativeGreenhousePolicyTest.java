package com.jdte.common.blockentities;

import com.jdte.common.upgrades.UpgradeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreativeGreenhousePolicyTest {
    @Test
    void acceptsOnlyCreativeGreenhouseUpgrades() {
        assertTrue(CreativeGreenhouseBE.isSupportedUpgrade(UpgradeType.CAPACITY));
        assertTrue(CreativeGreenhouseBE.isSupportedUpgrade(UpgradeType.OVERCLOCK));
        assertTrue(CreativeGreenhouseBE.isSupportedUpgrade(UpgradeType.FORTUNE));
        assertTrue(CreativeGreenhouseBE.isSupportedUpgrade(UpgradeType.ESSENCE_CONVERSION));
        assertTrue(CreativeGreenhouseBE.isSupportedUpgrade(UpgradeType.SEED_CONVERSION));
        assertTrue(CreativeGreenhouseBE.isSupportedUpgrade(UpgradeType.AE_OUTPUT));

        assertFalse(CreativeGreenhouseBE.isSupportedUpgrade(UpgradeType.CREATIVE));
        assertFalse(CreativeGreenhouseBE.isSupportedUpgrade(UpgradeType.FLUID));
        assertFalse(CreativeGreenhouseBE.isSupportedUpgrade(UpgradeType.UNDERCLOCK));
        assertFalse(CreativeGreenhouseBE.isSupportedUpgrade(UpgradeType.RANGE));
    }

    @Test
    void growsActiveCatalogTypeLimitBySixteenPerCapacityUpgradeAndCapsAtSixtyFour() {
        assertEquals(16, CreativeGreenhouseBE.activeOutputTypeLimitForCapacityUpgrades(0));
        assertEquals(32, CreativeGreenhouseBE.activeOutputTypeLimitForCapacityUpgrades(1));
        assertEquals(48, CreativeGreenhouseBE.activeOutputTypeLimitForCapacityUpgrades(2));
        assertEquals(64, CreativeGreenhouseBE.activeOutputTypeLimitForCapacityUpgrades(3));
        assertEquals(64, CreativeGreenhouseBE.activeOutputTypeLimitForCapacityUpgrades(Integer.MAX_VALUE));
    }

    @Test
    void automationRoutesSeedTemplatesOnlyIntoTheInputRangeAndNeverBackOut() {
        assertTrue(CreativeGreenhouseBE.canAutomationInsert(0));
        assertTrue(CreativeGreenhouseBE.canAutomationInsert(CreativeGreenhouseBE.INPUT_SLOTS - 1));
        assertFalse(CreativeGreenhouseBE.canAutomationInsert(CreativeGreenhouseBE.OUTPUT_START_SLOT));
        assertFalse(CreativeGreenhouseBE.canAutomationExtract(0, 4));
        assertFalse(CreativeGreenhouseBE.canAutomationExtract(CreativeGreenhouseBE.INPUT_SLOTS - 1, 4));
    }

    @Test
    void automationExtractsOnlyTheCurrentlyActiveInfiniteOutputRange() {
        int distinctTypes = 5;
        assertTrue(CreativeGreenhouseBE.canAutomationExtract(CreativeGreenhouseBE.OUTPUT_START_SLOT, distinctTypes));
        assertTrue(CreativeGreenhouseBE.canAutomationExtract(CreativeGreenhouseBE.OUTPUT_START_SLOT + distinctTypes - 1,
                distinctTypes));
        assertFalse(CreativeGreenhouseBE.canAutomationExtract(CreativeGreenhouseBE.OUTPUT_START_SLOT + distinctTypes,
                distinctTypes));
        assertFalse(CreativeGreenhouseBE.canAutomationExtract(CreativeGreenhouseBE.TOTAL_SLOTS, distinctTypes));
    }
}
