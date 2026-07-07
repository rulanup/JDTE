package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.GelGeneratorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public abstract class GelGeneratorContainer extends BaseMachineContainer {
    private ContainerData gelGeneratorData;
    private ContainerData outputFluidData;

    protected GelGeneratorContainer(@Nullable MenuType<?> menuType, int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(menuType, windowId, playerInventory, blockPos);
        if (baseMachineBE instanceof GelGeneratorBE gelGenerator) {
            gelGeneratorData = gelGenerator.getGelGeneratorData();
            addDataSlots(gelGeneratorData);
            outputFluidData = gelGenerator.getOutputFluidContainerData();
            addDataSlots(outputFluidData);
        }
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        // 坐标对应 gui_layout.json 中的 gel_generator_slots 默认值
        addSlot(new SlotItemHandler(machineHandler, GelGeneratorBE.GEL_SLOT, 8, -12));
        addSlot(new SlotItemHandler(machineHandler, GelGeneratorBE.FOOD_SLOT, 8, 24));
        for (int i = 0; i < GelGeneratorBE.INPUT_SLOTS; i++) {
            addSlot(new SlotItemHandler(machineHandler, GelGeneratorBE.INPUT_START_SLOT + i, 44, -21 + i * 18));
            addSlot(new SlotItemHandler(machineHandler, GelGeneratorBE.OUTPUT_START_SLOT + i, 116, -21 + i * 18));
        }
    }

    public int getGelProgress() {
        return gelGeneratorData == null ? 0 : gelGeneratorData.get(0);
    }

    public int getGelProgressMax() {
        return gelGeneratorData == null ? 1 : Math.max(1, gelGeneratorData.get(1));
    }

    public boolean isAutoBalanceInputs() {
        return gelGeneratorData != null && gelGeneratorData.get(2) != 0;
    }

    public int getOutputFluidAmount() {
        return outputFluidData == null ? 0 : ((outputFluidData.get(2) << 16) | outputFluidData.get(1));
    }

    public Fluid getOutputFluidType() {
        return outputFluidData == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(outputFluidData.get(0));
    }

    public FluidStack getOutputFluidStack() {
        return new FluidStack(getOutputFluidType(), getOutputFluidAmount());
    }

    public int getOutputFluidCapacity() {
        return baseMachineBE instanceof GelGeneratorBE gelGenerator ? gelGenerator.getMaxMB() : GelGeneratorBE.BASE_FLUID_CAPACITY;
    }
}
