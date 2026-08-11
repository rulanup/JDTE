package com.jdte.common.blockentities;

/**
 * Converts fifth-millibucket cost units into whole tank transactions while carrying prepaid fractions.
 */
public final class LootFabricatorFluidCost {
    public static final int UNITS_PER_MB = 5;

    private LootFabricatorFluidCost() {
    }

    public static Settlement settle(int costUnits, int creditUnits) {
        int safeCostUnits = Math.max(0, costUnits);
        int safeCreditUnits = Math.clamp(creditUnits, 0, UNITS_PER_MB - 1);
        long payableUnits = Math.max(0L, (long) safeCostUnits - safeCreditUnits);
        int drainMb = (int) Math.min(Integer.MAX_VALUE,
                (payableUnits + UNITS_PER_MB - 1L) / UNITS_PER_MB);
        int remainingCreditUnits = (int) Math.clamp(
                (long) safeCreditUnits + (long) drainMb * UNITS_PER_MB - safeCostUnits,
                0L, UNITS_PER_MB - 1L);
        return new Settlement(drainMb, remainingCreditUnits);
    }

    public static int displayAmount(int costUnits) {
        return Math.max(1, settle(costUnits, 0).drainMb());
    }

    public static String format(int costUnits) {
        int safeCostUnits = Math.max(0, costUnits);
        int whole = safeCostUnits / UNITS_PER_MB;
        int remainder = safeCostUnits % UNITS_PER_MB;
        return remainder == 0 ? Integer.toString(whole) : whole + "." + remainder * 2;
    }

    public record Settlement(int drainMb, int remainingCreditUnits) {
    }
}