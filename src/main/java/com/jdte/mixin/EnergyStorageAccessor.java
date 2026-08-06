package com.jdte.mixin;

import net.neoforged.neoforge.energy.EnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnergyStorage.class)
public interface EnergyStorageAccessor {
    @Accessor("capacity")
    void jdte$setCapacity(int capacity);

    @Accessor("energy")
    int jdte$getEnergy();

    @Accessor("energy")
    void jdte$setEnergy(int energy);

    @Accessor("maxReceive")
    void jdte$setMaxReceive(int maxReceive);

    @Accessor("maxExtract")
    void jdte$setMaxExtract(int maxExtract);
}
