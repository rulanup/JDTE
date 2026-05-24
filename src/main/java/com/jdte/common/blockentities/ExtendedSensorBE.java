package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.SensorT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ExtendedSensorBE extends SensorT1BE implements PoweredMachineBE, AreaAffectingBE, ExtendedUpgradeMachine {
    public AreaAffectingData areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));
    public final PoweredMachineContainerData poweredMachineData;

    public ExtendedSensorBE(BlockPos pPos, BlockState pBlockState) {
        super(JDTEBlockEntities.EXTENDED_SENSOR.get(), pPos, pBlockState);
        poweredMachineData = new PoweredMachineContainerData(this);
    }

    @Override public PoweredMachineContainerData getContainerData() { return poweredMachineData; }
    @Override public MachineEnergyStorage getEnergyStorage() { return getData(Registration.ENERGYSTORAGE_MACHINES); }
    @Override public int getStandardEnergyCost() { return 250; }
    @Override public AreaAffectingData getAreaAffectingData() { return areaAffectingData; }
}
