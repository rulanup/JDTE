package com.jdte.mixin;

import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FluidTank.class)
public interface FluidTankAccessor {
    @Accessor("capacity")
    void jdte$setCapacity(int capacity);
}
