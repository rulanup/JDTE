package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class ClickerFluidContainerData implements ContainerData {
    private final BaseMachineBE machine;
    private int syncedFluidId;
    private int syncedAmount;

    public ClickerFluidContainerData(BaseMachineBE machine) {
        this.machine = machine;
    }

    @Override
    public int get(int index) {
        JDTEFluidTank tank = UpgradeHelper.getClickerFluidTank(machine);
        return switch (index) {
            case 0 -> BuiltInRegistries.FLUID.getId(tank.getFluid().getFluid());
            case 1 -> tank.getFluidAmount() & 0xFFFF;
            case 2 -> tank.getFluidAmount() >> 16;
            default -> throw new IllegalArgumentException("Invalid index: " + index);
        };
    }

    @Override
    public void set(int index, int value) {
        JDTEFluidTank tank = UpgradeHelper.getClickerFluidTank(machine);
        switch (index) {
            case 0 -> syncedFluidId = value;
            case 1 -> syncedAmount = (syncedAmount & 0xFFFF0000) | (value & 0xFFFF);
            case 2 -> syncedAmount = (syncedAmount & 0xFFFF) | (value << 16);
            default -> throw new IllegalArgumentException("Invalid index: " + index);
        }
        tank.setFluid(syncedAmount <= 0 || BuiltInRegistries.FLUID.byId(syncedFluidId) == Fluids.EMPTY
                ? FluidStack.EMPTY
                : new FluidStack(BuiltInRegistries.FLUID.byId(syncedFluidId), syncedAmount));
    }

    @Override
    public int getCount() {
        return 3;
    }
}
