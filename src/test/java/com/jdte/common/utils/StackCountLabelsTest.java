package com.jdte.common.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StackCountLabelsTest {
    @Test void keepsSmallCountsExact() { assertEquals("64", StackCountLabels.compact(64)); assertEquals("999", StackCountLabels.compact(999)); }
    @Test void abbreviatesCapacityCounts() { assertEquals("2K", StackCountLabels.compact(2048)); assertEquals("8.1K", StackCountLabels.compact(8192)); }
    @Test void abbreviatesUltimateCounts() { assertEquals("6.4M", StackCountLabels.compact(6_400_000)); assertEquals("6.3M", StackCountLabels.compact(6_399_936)); }
    @Test void avoidsIntegerOverflow() { assertEquals("2147.4M", StackCountLabels.compact(Integer.MAX_VALUE)); }
}
