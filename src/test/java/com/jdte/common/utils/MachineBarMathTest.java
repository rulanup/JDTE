package com.jdte.common.utils;

import com.jdte.client.screens.util.MachineBarMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MachineBarMathTest {

    @Test
    void scalesLargeValuesWithoutIntegerOverflow() {
        assertEquals(70, MachineBarMath.scaleClamped(Integer.MAX_VALUE, Integer.MAX_VALUE, 70));
        assertEquals(35, MachineBarMath.scaleClamped(1_000_000_000, 2_000_000_000, 70));
    }

    @Test
    void clampsInvalidAndOverCapacityValues() {
        assertEquals(0, MachineBarMath.scaleClamped(-1, 1_000, 70));
        assertEquals(0, MachineBarMath.scaleClamped(500, 0, 70));
        assertEquals(70, MachineBarMath.scaleClamped(2_000, 1_000, 70));
    }

    @Test
    void truncatesSmallFractionsToZeroWithoutTheMinimumRule() {
        assertEquals(0, MachineBarMath.scaleClamped(4_000, 400_000, 70));
        assertEquals(0, MachineBarMath.scaleWithMinimum(0, 400_000, 70));
    }

    @Test
    void keepsOnePixelForAnyNonEmptyAmount() {
        assertEquals(1, MachineBarMath.scaleWithMinimum(4_000, 400_000, 70));
        assertEquals(1, MachineBarMath.scaleWithMinimum(1, Integer.MAX_VALUE, 70));
        assertEquals(70, MachineBarMath.scaleWithMinimum(400_000, 400_000, 70));
        assertEquals(35, MachineBarMath.scaleWithMinimum(200_000, 400_000, 70));
    }

    @Test
    void minimumRuleIgnoresInvalidCapacityAndPixels() {
        assertEquals(0, MachineBarMath.scaleWithMinimum(4_000, 0, 70));
        assertEquals(0, MachineBarMath.scaleWithMinimum(4_000, 400_000, 0));
        assertEquals(0, MachineBarMath.scaleWithMinimum(-5, 400_000, 70));
    }
}
