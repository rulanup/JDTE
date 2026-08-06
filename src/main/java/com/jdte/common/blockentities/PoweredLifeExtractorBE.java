package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class PoweredLifeExtractorBE extends LifeExtractorBE implements PoweredMachineBE {
    public static final int OVERCLOCK_SCAN_INTERVAL = 5;

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    private final int baseEnergyCapacity;
    private final int normalBatchSize;
    private final int overclockBatchSize;

    protected PoweredLifeExtractorBE(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                     int baseEnergyCapacity, int normalBatchSize, int overclockBatchSize) {
        super(type, pos, state);
        tickSpeed = 20;
        this.baseEnergyCapacity = baseEnergyCapacity;
        this.normalBatchSize = normalBatchSize;
        this.overclockBatchSize = overclockBatchSize;
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
    }

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, baseEnergyCapacity);
    }

    @Override
    public ContainerData getContainerData() {
        return poweredMachineData;
    }

    @Override
    public MachineEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public int getStandardEnergyCost() {
        return getEffectiveEnergyCost();
    }

    @Override
    public int getEffectiveEnergyCost() {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 0;
        int baseCost = BASE_ENERGY_COST;
        long scaledCost = (long) baseCost * getAreaEnergyScale();
        return UpgradeHelper.adjustEnergyCost(this, scaledCost > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) scaledCost);
    }

    @Override
    public boolean hasEnoughPower(int energyCost) {
        return PoweredMachineBE.super.hasEnoughPower(energyCost);
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        return PoweredMachineBE.super.extractEnergy(energy, simulate);
    }

    @Override
    protected int getExtractInterval() {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return OVERCLOCK_SCAN_INTERVAL;
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.OVERCLOCK) > 0) return OVERCLOCK_SCAN_INTERVAL;
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.UNDERCLOCK) > 0) return 40;
        return 20;
    }

    @Override
    protected int getMaxEntitiesPerTick() {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return overclockBatchSize;
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.OVERCLOCK) > 0) return overclockBatchSize;
        return normalBatchSize;
    }

    @Override
    protected double getFluidLossMultiplier() {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 0;
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.OVERCLOCK) > 0) return 0.10;
        return 0;
    }

    @Override
    protected double getFluidBonusMultiplier() {
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.UNDERCLOCK) > 0) return 1.5;
        return 1.0;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
    }
}
