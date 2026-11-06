package com.jdte.common.items;

/** Source whole burn ticks and bounded Forge int FE arithmetic. */
public final class PocketGeneratorMath {
    private PocketGeneratorMath() {}
    public static int multiply(int a, int b) {
        return (int)Math.min(Integer.MAX_VALUE, (long)Math.max(0, a) * Math.max(0, b));
    }
    public static int burnTicks(int fuelTime, int speed) {
        return fuelTime <= 0 || speed <= 0 ? 0 : fuelTime / speed;
    }
    public static int bounded(int amount, int capacity) {
        return Math.max(0, Math.min(Math.max(0, capacity), amount));
    }
    public static int pixels(int amount, int capacity, int height) {
        return capacity <= 0 ? 0 : (int)((long)bounded(amount, capacity) * Math.max(0, height) / capacity);
    }
}
