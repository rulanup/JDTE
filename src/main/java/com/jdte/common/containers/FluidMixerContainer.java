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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;

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
        return fluidMixerData == null ? 0 : fluidMixerData.get(0);
    }

    public int getProgressMax() {
        return fluidMixerData == null ? 1 : Math.max(1, fluidMixerData.get(1));
    }

    // 输入罐 B
    public Fluid getInputBFluidType() {
        return fluidMixerData == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(fluidMixerData.get(2));
    }

    public int getInputBFluidAmount() {
        return fluidMixerData == null ? 0 : ((fluidMixerData.get(4) << 16) | fluidMixerData.get(3));
    }

    public FluidStack getInputBFluidStack() {
        return new FluidStack(getInputBFluidType(), getInputBFluidAmount());
    }

    // 输出罐
    public Fluid getOutputFluidType() {
        return fluidMixerData == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(fluidMixerData.get(5));
    }

    public int getOutputFluidAmount() {
        return fluidMixerData == null ? 0 : ((fluidMixerData.get(7) << 16) | fluidMixerData.get(6));
    }

    public FluidStack getOutputFluidStack() {
        return new FluidStack(getOutputFluidType(), getOutputFluidAmount());
    }

    public int getFluidCapacity() {
        return baseMachineBE instanceof FluidMixerBE mixer ? mixer.getMaxMB() : FluidMixerBE.BASE_FLUID_CAPACITY;
    }
}
