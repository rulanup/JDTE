package com.jdte.common.items;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LargeCanisterTooltipTest {
    @Test
    void largePotionTooltipUsesTheFourThousandMillibucketCapacity() {
        assertEquals("1,000/4,000", LargePotionCanisterItem.potionAmountTooltip(1_000));
    }

    @Test
    void largeFuelTooltipUsesTheTwoThousandTickMinimumBurnValue() {
        int largeMinimum = LargeFuelCanisterItem.getMinimumFuelConsumed(200);

        assertEquals(2_000, largeMinimum);
        assertEquals("1", LargeFuelCanisterItem.fuelItemEquivalentTooltip(2_000, largeMinimum));
        assertEquals("0.5", LargeFuelCanisterItem.fuelItemEquivalentTooltip(1_000, largeMinimum));
    }

    @Test
    void largeItemsReplaceTheInheritedFixedSizeHoverImplementations() throws Exception {
        Path projectRoot = findProjectRoot();
        String potionSource = Files.readString(projectRoot.resolve(
                "src/main/java/com/jdte/common/items/LargePotionCanisterItem.java"));
        String fuelSource = Files.readString(projectRoot.resolve(
                "src/main/java/com/jdte/common/items/LargeFuelCanisterItem.java"));

        assertTrue(potionSource.contains("appendHoverText"));
        assertTrue(potionSource.contains("getPotionCapacityMb()"));
        assertFalse(potionSource.contains("super.appendHoverText"));
        assertTrue(fuelSource.contains("appendHoverText"));
        assertTrue(fuelSource.contains("getMinimumFuelConsumed()"));
        assertFalse(fuelSource.contains("super.appendHoverText"));
    }

    private static Path findProjectRoot() {
        Path current = Path.of(System.getProperty("user.dir", "")).toAbsolutePath();
        while (current != null && !Files.exists(current.resolve("gradle.properties"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new AssertionError("Could not locate project root from test runtime path");
        }
        return current;
    }
}
