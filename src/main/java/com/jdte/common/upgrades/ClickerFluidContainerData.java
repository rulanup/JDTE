package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerData;

public class ClickerFluidContainerData implements ContainerData {
    private final BaseMachineBE machine;

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
            case 0 -> tank.setFluid(new net.neoforged.neoforge.fluids.FluidStack(BuiltInRegistries.FLUID.byId(value), tank.getFluidAmount()));
            case 1 -> tank.getFluid().setAmount((tank.getFluidAmount() & 0xFFFF0000) | (value & 0xFFFF));
            case 2 -> tank.getFluid().setAmount((tank.getFluidAmount() & 0xFFFF) | (value << 16));
            default -> throw new IllegalArgumentException("Invalid index: " + index);
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
