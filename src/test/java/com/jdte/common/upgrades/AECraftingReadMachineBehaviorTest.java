package com.jdte.common.upgrades;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AECraftingReadMachineBehaviorTest {
    @Test
    void productionMachinesGateEveryProductionEntryWithoutDiscardingPendingWork() throws Exception {
        String greenhouse = source("src/main/java/com/jdte/common/blockentities/GreenhouseBE.java");
        String largeGreenhouse = source("src/main/java/com/jdte/common/blockentities/LargeGreenhouseBE.java");
        String vat = source("src/main/java/com/jdte/common/blockentities/LifeSynthesisVatBE.java");
        String mineral = source("src/main/java/com/jdte/common/blockentities/MineralExtractorBE.java");
        String breeder = source("src/main/java/com/jdte/common/blockentities/LifeBreederBE.java");

        assertTrue(greenhouse.contains("if (!UpgradeHelper.mayRunWithUpgrades(this)) return;"));
        assertOrdered(greenhouse, "if (!UpgradeHelper.mayRunWithUpgrades(this)) {", "GreenhouseCropDefinition[] definitions");
        assertTrue(largeGreenhouse.contains("if (!UpgradeHelper.mayRunWithUpgrades(this)) return;"));
        assertOrdered(largeGreenhouse, "if (!UpgradeHelper.mayRunWithUpgrades(this)) {", "settlementTicker = saturatingAdd");
        assertTrue(vat.contains("if (!UpgradeHelper.mayRunWithUpgrades(this)) return;"));
        assertTrue(mineral.contains("if (UpgradeHelper.mayRunWithUpgrades(this)) advanceTransientTick();"));
        assertTrue(mineral.contains("if (hasTransientWork() && UpgradeHelper.mayRunWithUpgrades(this)) settle();"));
        assertTrue(mineral.contains("if (UpgradeHelper.mayRunWithUpgrades(this)) discardExpiredTransientWork();"));
        assertOrdered(breeder, "if (!UpgradeHelper.mayRunWithUpgrades(this)) return;", "if (++cycleTicker");
    }

    @Test
    void freezerAlwaysDeactivatesWhenCraftingTaskIsDenied() throws Exception {
        String source = source("src/main/java/com/jdte/common/blockentities/TimeFreezerBE.java");
        assertTrue(source.contains("boolean allowed = UpgradeHelper.mayRunWithUpgrades(this);"));
        assertTrue(source.contains("boolean wantFreeze = allowed && wantsAnything && isActiveRedstone()"));
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
