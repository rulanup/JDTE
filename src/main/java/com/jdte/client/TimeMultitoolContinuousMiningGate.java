package com.jdte.client;

/** Tracks one physical press; repeated calls on its first block still allow normal breaking progress. */
final class TimeMultitoolContinuousMiningGate {
    private boolean pressed;
    private boolean hasTarget;
    private long pressedAtMillis;
    private long firstTarget;

    void press(long nowMillis) {
        if (pressed) return;
        pressed = true;
        pressedAtMillis = nowMillis;
        hasTarget = false;
    }

    boolean allowAttack(long target, long nowMillis, int holdDelayMillis) {
        press(nowMillis);
        if (!hasTarget) {
            hasTarget = true;
            firstTarget = target;
        }
        return target == firstTarget || Math.max(0L, nowMillis - pressedAtMillis) >= Math.max(0, holdDelayMillis);
    }

    void reset() {
        pressed = false;
        hasTarget = false;
    }
}
