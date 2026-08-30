package com.jdte.common.upgrades;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AECraftingReadMachineBehaviorTest {
    @Test
    void executablePolicyPreservesStateWhileDeniedAndResumesAfterTaskAppears() {
        int pending = 37;
        assertFalse(AECraftingReadMachinePolicy.mayAdvance(false, true));
        assertEquals(37, pending, "deny must preserve pending work");
        assertTrue(AECraftingReadMachinePolicy.mayAdvance(true, true));
        assertEquals(38, pending + 1, "allow must resume progress");
    }

    @Test
    void executableFreezerPolicyDeactivatesWithoutChargingWhenDenied() {
        AECraftingReadMachinePolicy.WorkDecision denied = AECraftingReadMachinePolicy.decideWork(
                false, true, true, true);
        assertTrue(denied.deactivate());
        assertFalse(denied.consumeResources());
        AECraftingReadMachinePolicy.WorkDecision allowed = AECraftingReadMachinePolicy.decideWork(
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

        assertTrue(greenhouse.contains("boolean allowed = UpgradeHelper.mayRunWithUpgrades(this);"));
        assertTrue(greenhouse.contains("captureMatrixProfiles"));
        assertTrue(greenhouse.contains("!canRun() || !UpgradeHelper.mayRunWithUpgrades(this)"));
        assertTrue(largeGreenhouse.contains("!canRun() || !UpgradeHelper.mayRunWithUpgrades(this)"));
        assertTrue(vat.contains("if (!UpgradeHelper.mayRunWithUpgrades(this)) return;"));
        assertTrue(mineral.contains("boolean allowed = UpgradeHelper.mayRunWithUpgrades(this);"));
        assertTrue(mineral.contains("if (hasTransientWork() && allowed) settle();"));
        assertTrue(mineral.contains("if (allowed) discardExpiredTransientWork();"));
        assertOrdered(breeder, "if (!UpgradeHelper.mayRunWithUpgrades(this)) return;", "if (++cycleTicker");
    }

    @Test
    void freezerAlwaysDeactivatesWhenCraftingTaskIsDenied() throws Exception {
        String source = source("src/main/java/com/jdte/common/blockentities/TimeFreezerBE.java");
        assertTrue(source.contains("boolean allowed = UpgradeHelper.mayRunWithUpgrades(this);"));
        assertTrue(source.contains("AECraftingReadMachinePolicy.WorkDecision decision"));
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
