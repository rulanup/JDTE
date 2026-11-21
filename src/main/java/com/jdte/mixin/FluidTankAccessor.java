package com.jdte.mixin;

import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FluidStacksResourceHandler.class)
public interface FluidTankAccessor {
    @Accessor("capacity")
    void jdte$setCapacity(int capacity);
}
