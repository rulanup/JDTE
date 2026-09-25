package com.jdte.ae;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AEVirtualTickContextTest {
    @AfterEach
    void resetClock() {
        AEVirtualTickContext.reset();
    }

    @Test
    void virtualScopeStartsTheMonotonicLogicalClock() {
        long nativeStamp = AEVirtualTickContext.override(100L);
        assertEquals(100L, nativeStamp);

        long virtualStamp;
        try (AEVirtualTickContext.Scope ignored = AEVirtualTickContext.enter(100L, 5)) {
            virtualStamp = AEVirtualTickContext.override(100L);
            assertTrue(virtualStamp > nativeStamp);
            assertThrows(IllegalStateException.class,
                    () -> AEVirtualTickContext.enter(100L, 6));
        }

        assertEquals(virtualStamp, AEVirtualTickContext.override(100L));
        assertFalse(AEVirtualTickContext.isActive());
    }

    @Test
    void scopeClearsAfterExceptionAndSaturatesOverflow() {
        assertThrows(TestFailure.class, () -> {
            try (AEVirtualTickContext.Scope ignored =
                         AEVirtualTickContext.enter(Long.MAX_VALUE - 1, 10)) {
                assertEquals(Long.MAX_VALUE,
                        AEVirtualTickContext.override(Long.MAX_VALUE - 1));
                throw new TestFailure();
            }
        });

        assertFalse(AEVirtualTickContext.isActive());
    }

    @Test
    void virtualRoundMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> AEVirtualTickContext.enter(100L, 0));
        assertThrows(IllegalArgumentException.class,
                () -> AEVirtualTickContext.enter(100L, -1));
        assertFalse(AEVirtualTickContext.isActive());
    }

    @Test
    void adjacentFramesCannotReuseAVirtualTickStamp() {
        long firstFrame = AEVirtualTickContext.override(100L);
        try (AEVirtualTickContext.Scope ignored = AEVirtualTickContext.enter(firstFrame, 1)) {
            assertTrue(AEVirtualTickContext.override(100L) > firstFrame);
        }

        long previousFrameStamp;
        try (AEVirtualTickContext.Scope ignored = AEVirtualTickContext.enter(firstFrame, 2)) {
            previousFrameStamp = AEVirtualTickContext.override(100L);
        }

        long nextNativeStamp = AEVirtualTickContext.override(101L);
        long nextFrameStamp;
        try (AEVirtualTickContext.Scope ignored = AEVirtualTickContext.enter(nextNativeStamp, 1)) {
            nextFrameStamp = AEVirtualTickContext.override(101L);
        }

        assertTrue(nextNativeStamp > previousFrameStamp);
        assertTrue(nextFrameStamp > previousFrameStamp);
        assertTrue(nextFrameStamp > nextNativeStamp);
        assertFalse(AEVirtualTickContext.isActive());
    }

    private static final class TestFailure extends RuntimeException {
    }
}
