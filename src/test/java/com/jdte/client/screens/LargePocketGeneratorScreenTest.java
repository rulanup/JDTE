package com.jdte.client.screens;

import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.jdte.setup.JDTEItems;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LargePocketGeneratorScreenTest {
    @Test
    void fuelBarHeightMatchesTheOriginalJdtScreenCalculation() {
        assertEquals(6, LargePocketGeneratorScreen.fuelBarHeight(20, 40));
        assertEquals(0, LargePocketGeneratorScreen.fuelBarHeight(0, 40));
    }

    @Test
    void energyBarHeightMatchesTheOriginalJdtScreenCalculation() {
        assertEquals(35, LargePocketGeneratorScreen.energyBarHeight(500, 1_000));
        assertEquals(0, LargePocketGeneratorScreen.energyBarHeight(0, 1_000));
    }

    @Test
    void burnSpeedTooltipUsesTheOriginalFuelCanisterMultiplierLogic() {
        ItemStack largeFuelCanister = new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());
        FuelCanister.setBurnSpeed(largeFuelCanister, 2.0D);

        assertEquals(2, LargePocketGeneratorScreen.burnSpeedMultiplierTooltipValue(largeFuelCanister));
    }
}
