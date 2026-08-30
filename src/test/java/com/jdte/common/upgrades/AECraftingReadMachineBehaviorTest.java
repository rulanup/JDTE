package com.jdte.common.upgrades;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AECraftingReadMachineBehaviorTest {
    @Test
    void executableProductionDecisionPreservesPendingUntilAllowed() {
        int pending = 37;
        AECraftingReadMachinePolicy.MachineWorkDecision denied =
                AECraftingReadMachinePolicy.production(false, true);
        if (denied.runWork()) pending++;
        assertEquals(37, pending, "deny must preserve pending work");

        AECraftingReadMachinePolicy.MachineWorkDecision allowed =
                AECraftingReadMachinePolicy.production(true, true);
        if (allowed.runWork()) pending++;
        assertEquals(38, pending, "allow must resume progress");
        assertFalse(allowed.deactivate());
    }

    @Test
    void executableFreezerPolicyDeactivatesWithoutChargingWhenDenied() {
        AECraftingReadMachinePolicy.MachineWorkDecision denied = AECraftingReadMachinePolicy.freezer(
                false, true, true, true);
        assertTrue(denied.deactivate());
        assertFalse(denied.consumeResources());
        AECraftingReadMachinePolicy.MachineWorkDecision allowed = AECraftingReadMachinePolicy.freezer(
                true, true, true, true);
        assertFalse(allowed.deactivate());
        assertTrue(allowed.consumeResources());
    }
    @Test
    void productionMachinesGateEveryProductionEntryWithoutDiscardingPendingWork() throws Exception {
        String greenhouse = source("src/main/java/com/jdte/common/blockentities/GreenhouseBE.java");
        String largeGreenhouse = source("src/main/java/com/jdte/common/blockentities/LargeGreenhouseBE.java");
        String vat = source("src/main/java/com/jdte/common/blockentities/LifeSynthesisVatBE.java");
        String mineral = source("src/main/java/com/jdte/common/blockentities/MineralExtractorBE.java");
        String breeder = source("src/main/java/com/jdte/common/blockentities/LifeBreederBE.java");

        assertTrue(greenhouse.contains("AECraftingReadMachinePolicy.production"));
        assertTrue(largeGreenhouse.contains("AECraftingReadMachinePolicy.production"));
        assertTrue(vat.contains("AECraftingReadMachinePolicy.production"));
        assertTrue(mineral.contains("AECraftingReadMachinePolicy.MachineWorkDecision decision"));
        assertTrue(mineral.contains("if (hasTransientWork() && decision.runWork()) settle();"));
        assertTrue(mineral.contains("if (decision.runWork()) discardExpiredTransientWork();"));
        assertTrue(breeder.contains("AECraftingReadMachinePolicy.production"));
        assertOrdered(breeder, "AECraftingReadMachinePolicy.production", "if (++cycleTicker");
    }

    @Test
    void freezerAlwaysDeactivatesWhenCraftingTaskIsDenied() throws Exception {
        String source = source("src/main/java/com/jdte/common/blockentities/TimeFreezerBE.java");
        assertTrue(source.contains("boolean allowed = UpgradeHelper.mayRunWithUpgrades(this);"));
        assertTrue(source.contains("AECraftingReadMachinePolicy.MachineWorkDecision decision"));
        assertTrue(source.contains("TimeFreezerManager.deactivate(this);"));
    }

    @Test
    void maintenanceMachinesRemainOutsideTheCommonTickerGate() throws Exception {
        String helper = source("src/main/java/com/jdte/common/upgrades/UpgradeHelper.java");
        assertTrue(helper.contains("&& !(machine instanceof TimeAcceleratorMachine || machine instanceof EntitySuppressorBE)"));
        assertTrue(helper.contains("&& !(machine instanceof RangeBlockerBE || machine instanceof AdvancedEnergyTransmitterBE)"));
        assertTrue(helper.contains("&& !(machine instanceof FactoryPackerBE);"));
    }

    private static void assertOrdered(String source, String first, String second) {
        int firstIndex = source.indexOf(first);
        int secondIndex = source.indexOf(second, firstIndex + first.length());
        assertTrue(firstIndex >= 0 && secondIndex > firstIndex,
                () -> "Expected " + first + " before " + second);
    }

    private static String source(String relativePath) throws Exception {
        Path current = Path.of(System.getProperty("user.dir", "")).toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("gradle.properties"))) current = current.getParent();
        if (current == null) throw new IllegalStateException("project root not found");
        return Files.readString(current.resolve(relativePath));
    }
}
