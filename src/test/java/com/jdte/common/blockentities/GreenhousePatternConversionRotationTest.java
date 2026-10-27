package com.jdte.common.blockentities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GreenhousePatternConversionRotationTest {

    @Test
    void convertHandlesLargeRotationArraySafelyWithoutCrashing() {
        int[] largeRotation = new int[]{10, 20, 30, 40, 50, 60, 70, 80};
        var result = GreenhousePatternConversionHelper.convert(null, List.of(), List.of(), largeRotation);
        assertNotNull(result);
        assertEquals(largeRotation.length, result.rotation().length);
        assertArrayEquals(largeRotation, result.rotation());
    }

    @Test
    void convertHandlesNullAndEmptyRotationArraySafely() {
        var resultNull = GreenhousePatternConversionHelper.convert(null, List.of(), List.of(), null);
        assertNotNull(resultNull);
        assertEquals(0, resultNull.rotation().length);

        var resultEmpty = GreenhousePatternConversionHelper.convert(null, List.of(), List.of(), new int[0]);
        assertNotNull(resultEmpty);
        assertEquals(0, resultEmpty.rotation().length);
    }
}
