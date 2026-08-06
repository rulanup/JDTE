package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.FluidPlacerT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ExtendedFluidPlacerBE extends FluidPlacerT1BE implements PoweredMachineBE, AreaAffectingBE, FilterableBE, ExtendedUpgradeMachine {
    public FilterData filterData = new FilterData();
    public AreaAffectingData areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
    public final PoweredMachineContainerData poweredMachineData;

    public ExtendedFluidPlacerBE(BlockPos pPos, BlockState pBlockState) {
        super(JDTEBlockEntities.EXTENDED_FLUID_PLACER.get(), pPos, pBlockState);
        poweredMachineData = new PoweredMachineContainerData(this);
    }

    @Override public PoweredMachineContainerData getContainerData() { return poweredMachineData; }
    @Override public MachineEnergyStorage getEnergyStorage() { return getData(Registration.ENERGYSTORAGE_MACHINES); }
    @Override public int getStandardEnergyCost() { return 250; }
    @Override public AreaAffectingData getAreaAffectingData() { return areaAffectingData; }
    @Override public FilterBasicHandler getFilterHandler() { return getData(Registration.HANDLER_BASIC_FILTER); }
    @Override public FilterData getFilterData() { return filterData; }

    @Override
    public List<BlockPos> findSpotsToPlace(FakePlayer fakePlayer) {
        AABB area = getAABB(getBlockPos());
        return BlockPos.betweenClosedStream(
                        (int) Math.floor(area.minX), (int) Math.floor(area.minY), (int) Math.floor(area.minZ),
                        (int) Math.ceil(area.maxX) - 1, (int) Math.ceil(area.maxY) - 1,
                        (int) Math.ceil(area.maxZ) - 1)
                .filter(pos -> isBlockPosValid(pos, fakePlayer))
                .map(BlockPos::immutable)
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(getBlockPos())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean isBlockPosValid(BlockPos pos, FakePlayer fakePlayer) {
        if (!super.isBlockPosValid(pos, fakePlayer)) return false;
        BlockState targetState = level.getBlockState(pos.relative(getDirectionValue()));
        return isStackValidFilter(targetState.getCloneItemStack(
                new BlockHitResult(Vec3.ZERO, getDirectionValue(), pos, false), level, pos, null));
    }
}
