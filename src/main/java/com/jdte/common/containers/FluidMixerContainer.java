package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.FluidMixerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public abstract class FluidMixerContainer extends BaseMachineContainer {
    private final Block machineBlock;
    protected ContainerData fluidMixerData;

    protected FluidMixerContainer(@Nullable MenuType<?> menuType, int windowId, Inventory playerInventory,
                                  BlockPos blockPos, Block machineBlock) {
        super(menuType, windowId, playerInventory, blockPos);
        this.machineBlock = machineBlock;
        if (baseMachineBE instanceof FluidMixerBE mixer) {
            fluidMixerData = mixer.getFluidMixerData();
            addDataSlots(fluidMixerData);
        }
        addPlayerSlots(player.getInventory());
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, machineBlock);
    }

    public static final int CATALYST_SLOT_X = 94;
    public static final int CATALYST_SLOT_Y = 14;

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        // 催化剂物品槽 — 位于中心上方，垂直对齐进度箭头
        addSlot(new SlotItemHandler(machineHandler, FluidMixerBE.CATALYST_SLOT, CATALYST_SLOT_X, CATALYST_SLOT_Y));
    }

    // --- 客户端数据读取 ---

    public int getProgress() {
        return fluidMixerData == null ? 0 : combine(0,13);
    }

    public int getProgressMax() {
        return fluidMixerData == null ? 1 : Math.max(1, combine(1,14));
    }

    // 输入罐 B
    public Fluid getInputBFluidType() {
        return fluidMixerData == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(fluidMixerData.get(2));
    }

    public int getInputBFluidAmount() {
        return fluidMixerData == null ? 0 : combine(3,4);
    }

    public FluidStack getInputBFluidStack() {
        return new FluidStack(getInputBFluidType(), getInputBFluidAmount());
    }

    // 输出罐
    public Fluid getOutputFluidType() {
        return fluidMixerData == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(fluidMixerData.get(5));
    }

    public int getOutputFluidAmount() {
        return fluidMixerData == null ? 0 : combine(6,7);
    }

    public FluidStack getOutputFluidStack() {
        return new FluidStack(getOutputFluidType(), getOutputFluidAmount());
    }

    public int getFluidCapacity() {
        return fluidMixerData == null ? FluidMixerBE.BASE_FLUID_CAPACITY : combine(11,12);
    }
    private int combine(int low,int high) { return (fluidMixerData.get(low)&0xFFFF) | (fluidMixerData.get(high)<<16); }
    public FluidStack getInputAFluidStack() { return fluidMixerData==null ? FluidStack.EMPTY : new FluidStack(BuiltInRegistries.FLUID.byId(fluidMixerData.get(8)),combine(9,10)); }
}
