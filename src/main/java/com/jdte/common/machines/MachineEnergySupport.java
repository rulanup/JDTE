package com.jdte.common.machines;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.nbt.CompoundTag;

public class MachineEnergySupport {
    private final BaseMachineBE machine;
    private final int baseCapacity;
    private final int baseCost;
    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;

    public MachineEnergySupport(BaseMachineBE machine, int baseCapacity, int baseCost) {
        this.machine = machine;
        this.baseCapacity = baseCapacity;
        this.baseCost = baseCost;
        this.energyStorage = new MachineEnergyStorage(UpgradeHelper.adjustEnergyCapacity(machine, baseCapacity));
        this.poweredMachineData = new PoweredMachineContainerData((PoweredMachineBE) machine);
    }

    public int maxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(machine, baseCapacity);
    }

    public int standardCost() {
        if (UpgradeHelper.hasCreativeUpgrade(machine)) return 0;
        // Original 0.6.5.2: Ultimate Overclock costs 50x. This branch only fires
        // when an Ultimate card is actually installed; MachineEnergySupport is
        // only used by the powered sender/receiver/stabilizer/glue families, and
        // of those only the item senders can install Ultimate cards in Forge,
        // so no other family's current cost changes. Standard Overclock still costs 3x.
        if (UpgradeHelper.hasUltimateOverclock(machine)) return (int) Math.min(Integer.MAX_VALUE, (long) baseCost * 50L);
        if (UpgradeHelper.countUpgrades(machine, UpgradeType.OVERCLOCK) > 0) return baseCost * 3;
        return baseCost;
    }

    public void save(CompoundTag tag) {
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    public void load(CompoundTag tag) {
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
    }
}
