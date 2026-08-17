package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BioFactoryEnergyCapacityTest {

    @Test
    void expandsCapacityToCoverAConfiguredCycleCost() {
        BioFactoryEnergyCapacity capacity = new BioFactoryEnergyCapacity();

        assertEquals(1_000_000, capacity.resolve(400_000, 1_000_000, cost -> cost));
    }

    @Test
    void preservesCapacityWhenItAlreadyCoversTheCycleCost() {
        BioFactoryEnergyCapacity capacity = new BioFactoryEnergyCapacity();

        assertEquals(10_000_000, capacity.resolve(10_000_000, 1_000_000, cost -> cost));
    }

    @Test
    void rememberedRecipeCostDoesNotDisappearWithTheRecipeCache() {
        BioFactoryEnergyCapacity capacity = new BioFactoryEnergyCapacity();
        capacity.rememberRecipeEnergy(1_000_000);

        assertEquals(1_000_000, capacity.resolve(400_000, 1_000, cost -> cost));
    }

    @Test
    void appliesTheCurrentEnergyUpgradeAdjustmentToTheCapacityFloor() {
        BioFactoryEnergyCapacity capacity = new BioFactoryEnergyCapacity();

        assertEquals(3_000_000, capacity.resolve(400_000, 1_000_000, cost -> cost * 3));
        assertEquals(400_000, capacity.resolve(400_000, 1_000_000, cost -> 0));
    }
}
