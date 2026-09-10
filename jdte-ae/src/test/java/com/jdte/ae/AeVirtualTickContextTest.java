package com.jdte.ae;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AeVirtualTickContextTest {
    @AfterEach
    void resetClock() {
        AeVirtualTickContext.reset();
    }

    @Test
    void virtualScopeStartsTheMonotonicLogicalClock() {
        long nativeStamp = AeVirtualTickContext.override(100L);
        assertEquals(100L, nativeStamp);

        long virtualStamp;
        try (AeVirtualTickContext.Scope ignored = AeVirtualTickContext.enter(100L, 5)) {
            virtualStamp = AeVirtualTickContext.override(100L);
            assertTrue(virtualStamp > nativeStamp);
            assertThrows(IllegalStateException.class,
                    () -> AeVirtualTickContext.enter(100L, 6));
        }

        assertEquals(virtualStamp, AeVirtualTickContext.override(100L));
        assertFalse(AeVirtualTickContext.isActive());
    }

    @Test
    void scopeClearsAfterExceptionAndSaturatesOverflow() {
        assertThrows(TestFailure.class, () -> {
            try (AeVirtualTickContext.Scope ignored =
                         AeVirtualTickContext.enter(Long.MAX_VALUE - 1, 10)) {
                assertEquals(Long.MAX_VALUE,
                        AeVirtualTickContext.override(Long.MAX_VALUE - 1));
                throw new TestFailure();
            }
        });

        assertFalse(AeVirtualTickContext.isActive());
    }

    @Test
    void virtualRoundMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> AeVirtualTickContext.enter(100L, 0));
        assertThrows(IllegalArgumentException.class,
                () -> AeVirtualTickContext.enter(100L, -1));
        assertFalse(AeVirtualTickContext.isActive());
    }

    @Test
    void adjacentFramesCannotReuseAVirtualTickStamp() {
        long firstFrame = AeVirtualTickContext.override(100L);
        try (AeVirtualTickContext.Scope ignored = AeVirtualTickContext.enter(firstFrame, 1)) {
            assertTrue(AeVirtualTickContext.override(100L) > firstFrame);
        }

        long previousFrameStamp;
        try (AeVirtualTickContext.Scope ignored = AeVirtualTickContext.enter(firstFrame, 2)) {
            previousFrameStamp = AeVirtualTickContext.override(100L);
        }

        long nextNativeStamp = AeVirtualTickContext.override(101L);
        long nextFrameStamp;
        try (AeVirtualTickContext.Scope ignored = AeVirtualTickContext.enter(nextNativeStamp, 1)) {
            nextFrameStamp = AeVirtualTickContext.override(101L);
        }

        assertTrue(nextNativeStamp > previousFrameStamp);
        assertTrue(nextFrameStamp > previousFrameStamp);
        assertTrue(nextFrameStamp > nextNativeStamp);
        assertFalse(AeVirtualTickContext.isActive());
    }

    private static final class TestFailure extends RuntimeException {
    }
}
