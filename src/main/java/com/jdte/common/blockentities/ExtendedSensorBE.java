package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.SensorT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ExtendedSensorBE extends SensorT1BE implements FilterableBE, PoweredMachineBE, AreaAffectingBE, ExtendedUpgradeMachine {
    public AreaAffectingData areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
    public final PoweredMachineContainerData poweredMachineData;

    public ExtendedSensorBE(BlockPos pPos, BlockState pBlockState) {
        super(JDTEBlockEntities.EXTENDED_SENSOR.get(), pPos, pBlockState);
        poweredMachineData = new PoweredMachineContainerData(this);
    }

    @Override public FilterBasicHandler getFilterHandler() { return getData(Registration.HANDLER_BASIC_FILTER); }
    @Override public PoweredMachineContainerData getContainerData() { return poweredMachineData; }
    @Override public MachineEnergyStorage getEnergyStorage() { return getData(Registration.ENERGYSTORAGE_MACHINES); }
    @Override public int getStandardEnergyCost() { return 250; }
    @Override public AreaAffectingData getAreaAffectingData() { return areaAffectingData; }

    @Override
    public AABB getAABB() {
        return AreaAffectingBE.super.getAABB(getBlockPos());
    }

    @Override
    public List<BlockPos> findPositions() {
        AABB area = getAABB();
        return BlockPos.betweenClosedStream(
                        (int) Math.floor(area.minX), (int) Math.floor(area.minY), (int) Math.floor(area.minZ),
                        (int) Math.ceil(area.maxX) - 1, (int) Math.ceil(area.maxY) - 1,
                        (int) Math.ceil(area.maxZ) - 1)
                .filter(this::isBlockPosValid)
                .map(BlockPos::immutable)
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(getBlockPos())))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
