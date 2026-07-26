package com.jdte.common.blockentities;

import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedBioCrusherBE extends PoweredBioCrusherBE implements ExtendedUpgradeMachine {
    public ExtendedBioCrusherBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_BIO_CRUSHER.get(), pos, state);
    }

    @Override
    protected boolean createsOutputInventory() {
        return true;
    }

    @Override
    protected int getBaseEnergyCapacity() {
        return JDTEConfig.COMMON.extendedBioCrusherEnergyCapacity.get();
    }

    @Override
    protected int getMaxEntityBatch() {
        return JDTEConfig.COMMON.extendedBioCrusherMaxEntities.get();
    }

    @Override
    protected int getNormalEntityBatch() {
        return 2;
    }
}
