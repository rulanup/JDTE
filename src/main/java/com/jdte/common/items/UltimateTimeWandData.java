package com.jdte.common.items;

import com.jdte.common.entities.UltimateTimeWandEntity;
import net.minecraft.core.BlockPos;

import java.util.Locale;

/** Pure data and arithmetic contracts for the Ultimate Time Wand. */
public final class UltimateTimeWandData {
    public static final int MAX_EXPONENT = 10;

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
        Mode safeMode = mode == null ? Mode.NORMAL : mode;
        long candidate = (long) Math.max(0, exponent) + safeMode.step();
        return (int) Math.min(MAX_EXPONENT, candidate);
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
        double total = Math.max(0.0D, pendingCost) + Math.max(0.0D, newCost);
        if (!keepFractionalRemainder) {
            return new FluidSettlement((int) Math.min(Integer.MAX_VALUE, Math.ceil(total)), 0.0D);
        }
        int drainMb = (int) Math.min(Integer.MAX_VALUE, Math.floor(total));
        return new FluidSettlement(drainMb, total - drainMb);
    }

    public static UltimateTimeWandEntity.WandState initialState(BlockPos target, int exponent, int duration) {
        int safeDuration = Math.max(1, duration);
        return new UltimateTimeWandEntity.WandState(target, Math.max(0, exponent), safeDuration, safeDuration);
    }

    /**
     * Builds the complete state and resource mutation before the item changes either one.
     * A saturated exponent is a no-op and therefore never charges resources.
     */
    public static OperationResult planOperation(UltimateTimeWandEntity.WandState state, Mode mode,
                                                int fluidCost, int energyCost) {
        int nextExponent = addStep(state.exponent(), mode);
        if (nextExponent <= state.exponent()) {
            return new OperationResult(false, state, 0, 0);
        }
        UltimateTimeWandEntity.WandState merged = UltimateTimeWandEntity.merge(
                state, nextExponent - state.exponent(), MAX_EXPONENT);
        return new OperationResult(true, merged, Math.max(0, fluidCost), Math.max(0, energyCost));
    }

    public static boolean canApply(UltimateTimeWandEntity.WandState state, Mode mode,
                                   int availableFluid, int availableEnergy,
                                   int fluidCost, int energyCost) {
        OperationResult result = planOperation(state, mode, fluidCost, energyCost);
        return result.success()
                && Math.max(0, availableFluid) >= result.fluidCost()
                && Math.max(0, availableEnergy) >= result.energyCost();
    }

    public static OperationResult applyIfAffordable(UltimateTimeWandEntity.WandState state, Mode mode,
                                                     int availableFluid, int availableEnergy,
                                                     int fluidCost, int energyCost) {
        OperationResult result = planOperation(state, mode, fluidCost, energyCost);
        if (!result.success() || Math.max(0, availableFluid) < result.fluidCost()
                || Math.max(0, availableEnergy) < result.energyCost()) {
            return new OperationResult(false, state, 0, 0);
        }
        return result;
    }

    private static int clampExponent(int exponent) {
        return Math.max(0, Math.min(MAX_EXPONENT, exponent));
    }

    public record FluidSettlement(int drainMb, double remainingCost) {
    }

    public record OperationResult(boolean success, UltimateTimeWandEntity.WandState state,
                                  int fluidCost, int energyCost) {
    }
}
