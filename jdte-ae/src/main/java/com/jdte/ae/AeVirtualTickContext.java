package com.jdte.ae;

public final class AeVirtualTickContext {
    private static final ThreadLocal<ClockState> CLOCK =
            ThreadLocal.withInitial(ClockState::new);

    private AeVirtualTickContext() {
    }

    public static Scope enter(long frameTick, int virtualRound) {
        if (virtualRound <= 0) {
            throw new IllegalArgumentException("virtualRound must be positive");
        }
        ClockState state = CLOCK.get();
        if (state.scopedTick != null) {
            throw new IllegalStateException("AE virtual tick context is already active");
        }
        long scopedTick = state.enter(frameTick);
        return () -> state.exit(scopedTick);
    }

    public static long override(long nativeTick) {
        ClockState state = CLOCK.get();
        return state.scopedTick == null
                ? state.observeNative(nativeTick)
                : state.scopedTick;
    }

    public static boolean isActive() {
        return CLOCK.get().scopedTick != null;
    }

    static void reset() {
        CLOCK.remove();
    }

    private static long increment(long value) {
        if (value == Long.MAX_VALUE) {
            throw new IllegalStateException("AE logical tick clock exhausted");
        }
        return value + 1L;
    }

    private static final class ClockState {
        private boolean activated;
        private boolean nativeTickSeen;
        private long lastNativeTick;
        private long logicalTick;
        private Long scopedTick;

        private long observeNative(long nativeTick) {
            if (!activated) {
                nativeTickSeen = true;
                lastNativeTick = nativeTick;
                logicalTick = nativeTick;
                return nativeTick;
            }
            if (!nativeTickSeen) {
                nativeTickSeen = true;
                lastNativeTick = nativeTick;
                logicalTick = nativeTick;
            } else if (nativeTick != lastNativeTick) {
                lastNativeTick = nativeTick;
                logicalTick = Math.max(increment(logicalTick), nativeTick);
            }
            return logicalTick;
        }

        private long enter(long frameTick) {
            if (!activated) {
                activated = true;
                if (!nativeTickSeen) {
                    nativeTickSeen = true;
                    lastNativeTick = frameTick;
                    logicalTick = frameTick;
                }
            }
            if (frameTick > logicalTick) {
                logicalTick = frameTick;
            }
            logicalTick = increment(logicalTick);
            scopedTick = logicalTick;
            return logicalTick;
        }

        private void exit(long expectedTick) {
            if (scopedTick == null || scopedTick != expectedTick) {
                throw new IllegalStateException("AE virtual tick scope closed out of order");
            }
            scopedTick = null;
        }
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
