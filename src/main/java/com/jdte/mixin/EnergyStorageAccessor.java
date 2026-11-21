package com.jdte.mixin;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleEnergyHandler.class)
public interface EnergyStorageAccessor {
    @Accessor("capacity")
    void jdte$setCapacity(int capacity);

    @Accessor("maxInsert")
    void jdte$setMaxInsert(int maxInsert);

    @Accessor("maxExtract")
    void jdte$setMaxExtract(int maxExtract);
}
