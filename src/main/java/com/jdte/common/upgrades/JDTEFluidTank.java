package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.capabilities.JustDireFluidTank;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Predicate;

public class JDTEFluidTank extends JustDireFluidTank implements INBTSerializable<CompoundTag> {
    public JDTEFluidTank(int capacity) {
        super(capacity);
    }

    public JDTEFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    @Override
    public CompoundTag serializeNBT() {
        return writeToNBT(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        fluid = readFromNBT(nbt).getFluid();
    }
}
