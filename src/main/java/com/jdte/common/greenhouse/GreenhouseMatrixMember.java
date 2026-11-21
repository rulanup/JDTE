package com.jdte.common.greenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import java.util.List;

/** Runtime ownership contract for a greenhouse whose production is managed by a matrix controller. */
public interface GreenhouseMatrixMember {
    boolean claimMatrix(BlockPos controller);
    boolean releaseMatrix(BlockPos controller);
    boolean isMatrixManaged();
    List<GreenhouseMatrixProductionProfile> captureMatrixProfiles(ServerLevel level,
                                                                  GreenhouseMatrixRuntime.Effects effects);
    ResourceHandler<FluidResource> matrixFluidStorage();
    EnergyHandler matrixEnergyStorage();
    long matrixOutputCapacity();
}
