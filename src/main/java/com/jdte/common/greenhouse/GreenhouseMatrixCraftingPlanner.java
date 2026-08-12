package com.jdte.common.greenhouse;

import net.minecraft.world.item.ItemStack;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Plans arbitrarily large pattern batches in O(pattern count), never O(craft count). */
public final class GreenhouseMatrixCraftingPlanner {
    private GreenhouseMatrixCraftingPlanner() {
    }

    public static Plan plan(long availableInput, List<GreenhouseMatrixCraftingRecipe> recipes, int cursor) {
        return plan(availableInput, Long.MAX_VALUE, recipes, cursor);
    }

    public static Plan plan(long availableInput, long growthCapacity,
                            List<GreenhouseMatrixCraftingRecipe> recipes, int cursor) {
        if (availableInput < 0L || recipes == null || recipes.isEmpty()) return Plan.invalid(cursor);
        if (growthCapacity < 0L) return Plan.invalid(cursor);
        ItemStack expectedInput = recipes.getFirst().input();
        BigInteger roundInput = BigInteger.ZERO;
        BigInteger roundOutput = BigInteger.ZERO;
        for (GreenhouseMatrixCraftingRecipe recipe : recipes) {
            if (!ItemStack.isSameItemSameComponents(expectedInput, recipe.input())) return Plan.invalid(cursor);
            roundInput = roundInput.add(BigInteger.valueOf(recipe.inputAmount()));
            roundOutput = roundOutput.add(BigInteger.valueOf(recipe.outputAmount()));
        }

        int size = recipes.size();
        int normalizedCursor = Math.floorMod(cursor, size);
        BigInteger available = BigInteger.valueOf(availableInput);
        BigInteger growthBudget = BigInteger.valueOf(growthCapacity);
        BigInteger roundGrowth = roundOutput.subtract(roundInput);
        BigInteger completeRoundsValue = available.divide(roundInput);
        if (roundGrowth.signum() > 0) {
            completeRoundsValue = completeRoundsValue.min(growthBudget.divide(roundGrowth));
        }
        long completeRounds;
        try {
            completeRounds = completeRoundsValue.longValueExact();
        } catch (ArithmeticException exception) {
            return Plan.invalid(normalizedCursor);
        }
        BigInteger remainingValue = available.subtract(roundInput.multiply(completeRoundsValue));
        growthBudget = growthBudget.subtract(roundGrowth.multiply(completeRoundsValue));
        long[] craftCounts = new long[size];
        java.util.Arrays.fill(craftCounts, completeRounds);
        int nextCursor = normalizedCursor;
        boolean awardedRemainder = false;

        for (int offset = 0; offset < size; offset++) {
            int index = (normalizedCursor + offset) % size;
            GreenhouseMatrixCraftingRecipe recipe = recipes.get(index);
            BigInteger cost = BigInteger.valueOf(recipe.inputAmount());
            BigInteger growth = BigInteger.valueOf(recipe.outputAmount()).subtract(cost);
            if (remainingValue.compareTo(cost) < 0
                    || growth.signum() > 0 && growthBudget.compareTo(growth) < 0) continue;
            remainingValue = remainingValue.subtract(cost);
            growthBudget = growthBudget.subtract(growth);
            if (craftCounts[index] == Long.MAX_VALUE) return Plan.invalid(normalizedCursor);
            craftCounts[index]++;
            nextCursor = (index + 1) % size;
            awardedRemainder = true;
        }
        if (!awardedRemainder) nextCursor = normalizedCursor;

        List<Allocation> allocations = new ArrayList<>(size);
        try {
            for (int index = 0; index < size; index++) {
                GreenhouseMatrixCraftingRecipe recipe = recipes.get(index);
                long crafts = craftCounts[index];
                allocations.add(new Allocation(recipe, crafts,
                        Math.multiplyExact(crafts, recipe.inputAmount()),
                        Math.multiplyExact(crafts, recipe.outputAmount())));
            }
        } catch (ArithmeticException exception) {
            return Plan.invalid(normalizedCursor);
        }
        return new Plan(true, List.copyOf(allocations), remainingValue.longValueExact(), nextCursor);
    }

    public record Allocation(GreenhouseMatrixCraftingRecipe recipe, long crafts,
                             long consumedInput, long producedOutput) {
    }

    public record Plan(boolean valid, List<Allocation> allocations, long remainingInput, int nextCursor) {
        private static Plan invalid(int cursor) {
            return new Plan(false, List.of(), 0L, Math.max(0, cursor));
        }
    }
}
