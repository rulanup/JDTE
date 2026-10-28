package com.jdte.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

/** Forge replacement for NeoForge's block capability cache; never retains a detached receiver. */
record ForgeEnergyTarget(ServerLevel level, BlockPos pos, Direction side) {
    IEnergyStorage getCapability() { return find(level, pos, side); }
    static IEnergyStorage find(ServerLevel level, BlockPos pos, Direction side) {
        if (!level.isLoaded(pos)) return null;
        var be = level.getBlockEntity(pos);
        return be == null || be.isRemoved() ? null : be.getCapability(ForgeCapabilities.ENERGY, side).orElse(null);
    }
}
