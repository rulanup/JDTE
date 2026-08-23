package com.jdte.common.items;

public final class LargePortableContainerLogic {
    public static final int POTION_BATCH_MB = 1_000;
    private static final int POTION_CAPACITY = 4_000;
    private static final int CAPACITY_MULTIPLIER = 4;
    private static final int FUEL_MULTIPLIER = 10;

    private LargePortableContainerLogic() {
    }

    public static int pocketGeneratorCapacity(int basePocketCapacity) {
        return saturatedMultiply(basePocketCapacity, CAPACITY_MULTIPLIER);
    }

    public static int potionCapacity() {
        return POTION_CAPACITY;
    }

    public static boolean canFillPotionBatch(int potionCount, int currentPotionMb, int capacityMb) {
        return potionCount >= 4
                && currentPotionMb <= capacityMb - POTION_BATCH_MB;
    }

    public static int fuelCapacity(int baseFuelCapacity) {
        return saturatedMultiply(baseFuelCapacity, CAPACITY_MULTIPLIER);
    }

    public static int fuelMinimumConsumption(int baseMinimum) {
        return saturatedMultiply(baseMinimum, FUEL_MULTIPLIER);
    }

    public static int fuelBurnMultiplier(int baseBurnMultiplier) {
        return saturatedMultiply(baseBurnMultiplier, FUEL_MULTIPLIER);
    }

    private static int saturatedMultiply(int value, int multiplier) {
        long product = (long) value * multiplier;
        return product > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) product;
    }
}
