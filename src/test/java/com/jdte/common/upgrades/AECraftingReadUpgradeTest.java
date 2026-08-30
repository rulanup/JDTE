package com.jdte.common.upgrades;

import com.jdte.common.integrations.ae2.AE2CraftingReadNetwork;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AECraftingReadUpgradeTest {
    @Test
    void noCpuOrIncompleteCpuStatusIsInactive() {
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(List.of()));
        assertFalse(AE2CraftingReadNetwork.hasActiveCraftingTask(List.of(
                new AE2CraftingReadNetwork.CraftingCpuSnapshot(false, true),
                new AE2CraftingReadNetwork.CraftingCpuSnapshot(true, false))));
    }

    @Test
    void busyCpuWithJobIsActive() {
        assertTrue(AE2CraftingReadNetwork.hasActiveCraftingTask(List.of(
                new AE2CraftingReadNetwork.CraftingCpuSnapshot(false, false),
                new AE2CraftingReadNetwork.CraftingCpuSnapshot(true, true))));
    }
}
