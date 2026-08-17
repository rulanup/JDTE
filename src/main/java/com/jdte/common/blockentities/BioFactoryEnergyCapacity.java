package com.jdte.common.blockentities;

import java.util.function.IntUnaryOperator;

final class BioFactoryEnergyCapacity {
    private int rememberedRecipeEnergy;

    int resolve(int configuredCapacity, int defaultCycleEnergy, IntUnaryOperator energyCostAdjustment) {
        int requiredBaseEnergy = Math.max(Math.max(0, defaultCycleEnergy), rememberedRecipeEnergy);
        int requiredEnergy = Math.max(0, energyCostAdjustment.applyAsInt(requiredBaseEnergy));
        return Math.max(configuredCapacity, requiredEnergy);
    }

    boolean rememberRecipeEnergy(int recipeEnergy) {
        int sanitizedEnergy = Math.max(0, recipeEnergy);
        if (sanitizedEnergy <= rememberedRecipeEnergy) return false;
        rememberedRecipeEnergy = sanitizedEnergy;
        return true;
    }

    int rememberedRecipeEnergy() {
        return rememberedRecipeEnergy;
    }

    void restoreRememberedRecipeEnergy(int recipeEnergy) {
        rememberedRecipeEnergy = Math.max(0, recipeEnergy);
    }
}
