package com.jdte.common.blockentities;

import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Extended Entity Accelerator: 8-slot upgraded tier with higher capacity,
 * higher speed multipliers, and higher entity throughput.
 */
public class ExtendedEntityAcceleratorBE extends AdvancedEntityAcceleratorBE implements ExtendedUpgradeMachine {

    public ExtendedEntityAcceleratorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_ENTITY_ACCELERATOR.get(), pos, state);
    }

    @Override
    protected int getMaxAdjustableMultiplier() {
        return JDTEConfig.COMMON.entityAccelerator.extendedMaxMultiplier.get();
    }

    @Override
    protected int getOverclockMultiplier() {
        return JDTEConfig.COMMON.entityAccelerator.extendedOverclockMultiplier.get();
    }

    @Override
    protected double getTierFluidCostMultiplier() {
        return JDTEConfig.COMMON.entityAccelerator.extendedTierFluidCostMultiplier.get();
    }

    @Override
    protected int getMaxEntitiesPerTick() {
        return JDTEConfig.COMMON.entityAccelerator.extendedMaxEntitiesPerTick.get();
    }

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.entityAccelerator.extendedEnergyCapacity.get());
    }
}
