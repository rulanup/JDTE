package com.jdte.common.greenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

/** Runtime ownership contract for a greenhouse whose production is managed by a matrix controller. */
public interface GreenhouseMatrixMember {
    boolean claimMatrix(BlockPos controller);
    boolean releaseMatrix(BlockPos controller);
    boolean isMatrixManaged();
    List<GreenhouseMatrixProductionProfile> captureMatrixProfiles(ServerLevel level,
                                                                  GreenhouseMatrixRuntime.Effects effects);
    IFluidHandler matrixFluidStorage();
    IEnergyStorage matrixEnergyStorage();
    long matrixOutputCapacity();
}
