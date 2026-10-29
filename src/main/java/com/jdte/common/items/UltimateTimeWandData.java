package com.jdte.common.items;


import net.minecraft.core.BlockPos;

import java.util.Locale;

/** Pure data and arithmetic contracts for the Ultimate Time Wand. */
public final class UltimateTimeWandData {
    public static final int MAX_EXPONENT = 10;
    public static final int ULTIMATE_MAX_EXPONENT = 15;

    private UltimateTimeWandData() {
    }

    public enum Mode {
        NORMAL("normal", 1),
        X2("x2", 2),
        X4("x4", 4),
        MAX("max", 10);

        private final String serializedName;
        private final int step;

        Mode(String serializedName, int step) {
            this.serializedName = serializedName;
            this.step = step;
        }

        public Mode next() {
            Mode[] modes = values();
            return modes[(ordinal() + 1) % modes.length];
        }

        public int step() {
            return step;
        }

        public String serializedName() {
            return serializedName;
        }

        public static Mode fromOrdinal(int ordinal) {
            return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : NORMAL;
        }

        public static Mode fromName(String name) {
            if (name == null) {
                return NORMAL;
            }
            String normalized = name.toLowerCase(Locale.ROOT);
            for (Mode mode : values()) {
                if (mode.serializedName.equals(normalized)) {
                    return mode;
                }
            }
            return NORMAL;
        }
    }

    public static int multiplierForExponent(int exponent) {
        return 1 << clampExponent(exponent);
    }

    public static int addStep(int exponent, Mode mode) {
        return addStep(exponent, mode, MAX_EXPONENT);
    }

    public static int addStep(int exponent, Mode mode, int maxExponent) {
        Mode safeMode = mode == null ? Mode.NORMAL : mode;
        long candidate = safeMode == Mode.MAX ? maxExponent : (long) Math.max(0, exponent) + safeMode.step();
        return (int) Math.min(maxExponent, candidate);
    }

    public static int saturatingEnergyCost(int multiplier, int baseCost) {
        long product = Math.max(0L, (long) multiplier) * Math.max(0L, (long) baseCost);
        return product >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) product;
    }

    public static FluidSettlement settleFluid(double pendingCost, double newCost) {
        return settleFluid(pendingCost, newCost, true);
    }

    /**
     * Without fractional settlement each operation rounds its full cost up, so no fractional
     * debt is carried to a later use.
     */
    public static FluidSettlement settleFluid(double pendingCost, double newCost, boolean keepFractionalRemainder) {
        double total = finiteCost(pendingCost) + finiteCost(newCost);
        if (!keepFractionalRemainder) {
            return new FluidSettlement((int) Math.min(Integer.MAX_VALUE, Math.ceil(total)), 0.0D);
        }
        int drainMb = (int) Math.min(Integer.MAX_VALUE, Math.floor(total));
        return new FluidSettlement(drainMb, total - drainMb);
    }

    public static WandState initialState(BlockPos target, int exponent, int duration) {
        int safeDuration = Math.max(1, duration);
        return new WandState(target, Math.max(0, exponent), safeDuration, safeDuration);
    }

    /**
     * Builds the complete state and resource mutation before the item changes either one.
     * A saturated exponent is a no-op and therefore never charges resources.
     */
    public static OperationResult planOperation(WandState state, Mode mode,
                                                int fluidCost, int energyCost) {
        return planOperation(state, mode, fluidCost, energyCost, MAX_EXPONENT);
    }

    public static OperationResult planOperation(WandState state, Mode mode,
                                                int fluidCost, int energyCost, int maxExponent) {
        int nextExponent = addStep(state.exponent(), mode, maxExponent);
        if (nextExponent <= state.exponent()) {
            return new OperationResult(false, state, 0, 0);
        }
        WandState merged = merge(
                state, nextExponent - state.exponent(), maxExponent);
        return new OperationResult(true, merged, Math.max(0, fluidCost), Math.max(0, energyCost));
    }

    public static boolean canApply(WandState state, Mode mode,
                                   int availableFluid, int availableEnergy,
                                   int fluidCost, int energyCost) {
        return canApply(state, mode, availableFluid, availableEnergy, fluidCost, energyCost, MAX_EXPONENT);
    }

    public static boolean canApply(WandState state, Mode mode,
                                   int availableFluid, int availableEnergy,
                                   int fluidCost, int energyCost, int maxExponent) {
        OperationResult result = planOperation(state, mode, fluidCost, energyCost, maxExponent);
        return result.success()
                && Math.max(0, availableFluid) >= result.fluidCost()
                && Math.max(0, availableEnergy) >= result.energyCost();
    }

    public static OperationResult applyIfAffordable(WandState state, Mode mode,
                                                     int availableFluid, int availableEnergy,
                                                     int fluidCost, int energyCost) {
        return applyIfAffordable(state, mode, availableFluid, availableEnergy, fluidCost, energyCost, MAX_EXPONENT);
    }

    public static OperationResult applyIfAffordable(WandState state, Mode mode,
                                                     int availableFluid, int availableEnergy,
                                                     int fluidCost, int energyCost, int maxExponent) {
        OperationResult result = planOperation(state, mode, fluidCost, energyCost, maxExponent);
        if (!result.success() || Math.max(0, availableFluid) < result.fluidCost()
                || Math.max(0, availableEnergy) < result.energyCost()) {
            return new OperationResult(false, state, 0, 0);
        }
        return result;
    }

    public static WandState merge(WandState state,int step,int maxExponent){
        int total=Math.max(0,state.totalTime());int remaining=Math.max(0,Math.min(total,state.remainingTime()));
        int restored=(int)Math.min(total,(long)remaining+(total-remaining)/2L);
        int exponent=(int)Math.min(Math.max(0,maxExponent),(long)Math.max(0,state.exponent())+Math.max(0,step));
        return new WandState(state.target(),exponent,total,restored);
    }
    public record WandState(BlockPos target,int exponent,int totalTime,int remainingTime){public WandState{target=target==null?null:target.immutable();}}

    private static double finiteCost(double cost){return Double.isNaN(cost)?0:Math.max(0,cost);}

    private static int clampExponent(int exponent) {
        return Math.max(0, Math.min(ULTIMATE_MAX_EXPONENT, exponent));
    }

    public record FluidSettlement(int drainMb, double remainingCost) {
    }

    public record OperationResult(boolean success, WandState state,
                                  int fluidCost, int energyCost) {
    }
}
