package com.jdte.common.integrations.ae2;

import net.minecraftforge.fluids.FluidStack;

public final class FluidCopies {
    private FluidCopies() {}
    public static FluidStack withAmount(FluidStack source, int amount) {
        FluidStack copy = source.copy();
        copy.setAmount(amount);
        return copy;
    }
}
