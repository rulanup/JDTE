package com.jdte.common.upgrades;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

public class JDTEFluidTank extends FluidTank implements INBTSerializable<CompoundTag> {
    public JDTEFluidTank(int capacity) {
        super(capacity);
    }

    public JDTEFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return writeToNBT(provider, new CompoundTag());
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        fluid = readFromNBT(provider, nbt).getFluid();
    }
}
