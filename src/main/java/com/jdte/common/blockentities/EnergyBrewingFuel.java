package com.jdte.common.blockentities;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

/** A whole blaze-powder-equivalent purchase against the brewer's owned FE buffer. */
public final class EnergyBrewingFuel {
    private EnergyBrewingFuel() {}

    public static boolean tryCharge(int cost, boolean creative, IntSupplier stored,
                                    IntUnaryOperator simulate, IntUnaryOperator commit,
                                    IntConsumer restore) {
        if (creative || cost <= 0) return true;
        if (simulate.applyAsInt(cost) != cost) return false;
        int before = stored.getAsInt();
        if (commit.applyAsInt(cost) == cost) return true;
        // Only the machine's owned buffer is passed here, allowing exact rollback.
        restore.accept(before);
        return false;
    }
}
