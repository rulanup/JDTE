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
    void roundTripsValuesAcrossSixteenBitDataSlots() {
        int value = 8_000_000;
        assertEquals(value, ContainerDataEncoding.combine16(
                ContainerDataEncoding.low16(value),
                ContainerDataEncoding.high16(value)));
    }

    @Test
    void updatesEachSixteenBitHalfWithoutDiscardingTheOther() {
        int value = 0x12345678;
        assertEquals(0x1234ABCD, ContainerDataEncoding.withLow16(value, 0xABCD));
        assertEquals(0xABCD5678, ContainerDataEncoding.withHigh16(value, 0xABCD));
    }
}