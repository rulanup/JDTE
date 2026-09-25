package com.jdte.common.upgrades;
import com.jdte.common.manager.ExtendedTimeAccelerationManager;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AECraftingReadMachineBehaviorTest {
    @Test
    void executableProductionDecisionPreservesPendingUntilAllowed() {
        int pending = 37;
        AECraftingReadMachinePolicy.MachineWorkDecision denied =
                AECraftingReadMachinePolicy.production(false, true, true);
        if (denied.runWork()) pending++;
        assertEquals(37, pending, "deny must preserve pending work");

        AECraftingReadMachinePolicy.MachineWorkDecision allowed =
                AECraftingReadMachinePolicy.production(true, true, true);
        if (allowed.runWork()) pending++;
        assertEquals(38, pending, "allow must resume progress");
        assertFalse(allowed.deactivate());
    }

    @Test
    void executableFreezerPolicyChecksResourcesOnlyAfterPermissionAndIntent() {
        AtomicInteger resourceChecks = new AtomicInteger();
        AECraftingReadMachinePolicy.MachineWorkDecision denied = AECraftingReadMachinePolicy.freezer(
                false, true, true, () -> {
                    resourceChecks.incrementAndGet();
                    return true;
                });
        assertTrue(denied.deactivate());
        assertFalse(denied.consumeResources());
        assertEquals(0, resourceChecks.get());

        AECraftingReadMachinePolicy.MachineWorkDecision allowed = AECraftingReadMachinePolicy.freezer(
                true, true, true, () -> {
                    resourceChecks.incrementAndGet();
                    return true;
                });
        assertFalse(allowed.deactivate());
        assertTrue(allowed.consumeResources());
        assertEquals(1, resourceChecks.get());
    }

    @Test
    void aePausePreservesProgressWhileRedstoneOffRequestsOriginalReset() {
        AECraftingReadMachinePolicy.ProductionDecision aeDenied =
                AECraftingReadMachinePolicy.productionState(false, true, true);
        assertFalse(aeDenied.runWork());
        assertFalse(aeDenied.resetInactiveState());

        AECraftingReadMachinePolicy.ProductionDecision redstoneOff =
                AECraftingReadMachinePolicy.productionState(true, false, true);
        assertFalse(redstoneOff.runWork());
        assertTrue(redstoneOff.resetInactiveState());
    }

    @Test
    void rollbackPhasesContinueWhileForwardFactoryWorkPauses() {
        assertFalse(AECraftingReadMachinePolicy.mayAdvanceFactoryPhase(false, false));
        assertTrue(AECraftingReadMachinePolicy.mayAdvanceFactoryPhase(false, true));
        assertTrue(AECraftingReadMachinePolicy.mayAdvanceFactoryPhase(true, false));
    }

    @Test
    void excludedMachinesKeepExplicitProductionGateWiring() throws Exception {
        for (String source : List.of(
                "TimeAcceleratorBE.java", "EntitySuppressorBE.java", "RangeBlockerBE.java",
                "AdvancedEnergyTransmitterBE.java", "AdvancedItemCollectorBE.java", "FactoryPackerBE.java")) {
            assertTrue(blockEntitySource(source).contains("UpgradeHelper.mayRunWithUpgrades(this)"), source);
        }
        assertTrue(blockEntitySource("TimeAcceleratorBE.java")
                .contains("ExtendedTimeAccelerationManager.deactivate(this)"));
        assertTrue(blockEntitySource("TimeFreezerBE.java")
                .contains("AECraftingReadMachinePolicy.freezer("));
    }

    private static String blockEntitySource(String fileName) throws Exception {
        Path current = Path.of(System.getProperty("user.dir", "")).toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("gradle.properties"))) current = current.getParent();
        if (current == null) throw new IllegalStateException("project root not found");
        return Files.readString(current.resolve("src/main/java/com/jdte/common/blockentities").resolve(fileName));
    }
}
