package com.jdte.common.items;

import com.jdte.setup.JDTEItems;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void registersLargePortableContainerItemHolders() {
        assertEquals("large_pocket_generator", JDTEItems.LARGE_POCKET_GENERATOR.getId().getPath());
        assertEquals("large_potion_canister", JDTEItems.LARGE_POTION_CANISTER.getId().getPath());
        assertEquals("large_fuel_canister", JDTEItems.LARGE_FUEL_CANISTER.getId().getPath());
    }

    @Test
    void exposesLargePortableContainerItemTypesAndSingleStackDefaults() {
        LargePocketGeneratorItem largePocketGenerator = JDTEItems.LARGE_POCKET_GENERATOR.get();
        LargePotionCanisterItem largePotionCanister = JDTEItems.LARGE_POTION_CANISTER.get();
        LargeFuelCanisterItem largeFuelCanister = JDTEItems.LARGE_FUEL_CANISTER.get();

        assertNotNull(largePocketGenerator);
        assertNotNull(largePotionCanister);
        assertNotNull(largeFuelCanister);
        assertEquals(1, new ItemStack(largePocketGenerator).getMaxStackSize());
        assertEquals(1, new ItemStack(largePotionCanister).getMaxStackSize());
        assertEquals(1, new ItemStack(largeFuelCanister).getMaxStackSize());
    }

    @Test
    void largePocketGeneratorUsesFourTimesTheConfiguredEnergyCapacity() {
        int basePocketCapacity = 536_870_911;

        assertEquals(
                LargePortableContainerLogic.pocketGeneratorCapacity(basePocketCapacity),
                LargePocketGeneratorItem.getScaledMaxEnergy(basePocketCapacity));
    }
}
