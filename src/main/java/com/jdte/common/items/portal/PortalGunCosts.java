package com.jdte.common.items.portal;

import java.util.function.IntSupplier;

/** Source 0.6.5.2 manual coordinates differ from JDT's recorded/previous destinations. */
public final class PortalGunCosts {
    public static final int CROSS_DIMENSION = 500;
    public static final int PER_BLOCK = 25;
    public static final int DISTANCE_LIMIT = 25_000;

    private PortalGunCosts() {}

    public static int manual(boolean sameDimension, double distance) {
        return sameDimension ? Math.min((int)Math.ceil(distance * PER_BLOCK), DISTANCE_LIMIT) : CROSS_DIMENSION;
    }

    public static int destination(boolean previous, boolean manualSlot, boolean sameDimension,
                                  double distance, IntSupplier jdtCost) {
        return previous || !manualSlot ? jdtCost.getAsInt() : manual(sameDimension, distance);
    }
}
