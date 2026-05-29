package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class GelGeneratorBE extends BaseMachineBE implements PoweredMachineBE, FilterableBE, RedstoneControlledBE, FluidMachineBE {
    public static final int INPUT_SLOTS = 4;
    public static final int FOOD_SLOT = 4;
    public static final int OUTPUT_SLOT = 5;
    public static final int TOTAL_SLOTS = 6;
    public static final int BASE_FLUID_CAPACITY = 4000; // 4 buckets
    public static final int BASE_ENERGY_CAPACITY = 100000;

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    public final JDTEFluidTank fluidTank;
    public final FluidContainerData fluidContainerData;
    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    protected final ItemStackHandler itemHandler;
    protected int conversionProgress = 0;

    protected GelGeneratorBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
        fluidTank = new JDTEFluidTank(getMaxMB(), f -> true);
        fluidContainerData = new FluidContainerData(this);
        itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot == FOOD_SLOT) {
                    return stack.getFoodProperties(null) != null;
                }
                if (slot == OUTPUT_SLOT) {
                    return false;
                }
                return true;
            }
        };
    }

    @Override
    public ItemStackHandler getMachineHandler() {
        return itemHandler;
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        if (isActiveRedstone() && canRun()) {
            tryConvert();
        }
    }

    protected void tryConvert() {
        // Check if we have food to consume
        ItemStack foodStack = itemHandler.getStackInSlot(FOOD_SLOT);
        if (foodStack.isEmpty()) return;

        // Check for items or fluids to convert
        boolean hasInput = false;
        for (int i = 0; i < INPUT_SLOTS; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                hasInput = true;
                break;
            }
        }
        if (!hasInput && fluidTank.getFluid().isEmpty()) return;

        // Consume food and convert
        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            int energyCost = getStandardEnergyCost();
            if (!hasEnoughPower(energyCost)) return;
            extractEnergy(energyCost, false);
        }

        // Convert items
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack inputStack = itemHandler.getStackInSlot(i);
            if (!inputStack.isEmpty()) {
                ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
                if (outputStack.isEmpty()) {
                    itemHandler.setStackInSlot(OUTPUT_SLOT, inputStack.copy());
                    inputStack.shrink(1);
                    itemHandler.setStackInSlot(i, inputStack);
                } else if (ItemStack.isSameItemSameComponents(outputStack, inputStack) && outputStack.getCount() < outputStack.getMaxStackSize()) {
                    int canAdd = Math.min(inputStack.getCount(), outputStack.getMaxStackSize() - outputStack.getCount());
                    outputStack.grow(canAdd);
                    inputStack.shrink(canAdd);
                    itemHandler.setStackInSlot(OUTPUT_SLOT, outputStack);
                    itemHandler.setStackInSlot(i, inputStack);
                }
            }
        }

        // Consume food
        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            foodStack.shrink(1);
            itemHandler.setStackInSlot(FOOD_SLOT, foodStack);
        }

        setChanged();
    }

    public int getEffectiveTickSpeed(int original) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 1;
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.OVERCLOCK) > 0) return Math.max(1, original / 2);
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.UNDERCLOCK) > 0) return original * 2;
        return original;
    }

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, BASE_ENERGY_CAPACITY);
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
        int baseCost = 1000;
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 0;
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.OVERCLOCK) > 0) return baseCost * 3;
        if (UpgradeHelper.countUpgrades(this, com.jdte.common.upgrades.UpgradeType.UNDERCLOCK) > 0) return Math.max(1, baseCost / 5);
        return baseCost;
    }

    @Override
    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, BASE_FLUID_CAPACITY);
    }

    @Override
    public JDTEFluidTank getFluidTank() {
        return fluidTank;
    }

    @Override
    public FluidContainerData getFluidContainerData() {
        return fluidContainerData;
    }

    @Override
    public FilterBasicHandler getFilterHandler() {
        return getData(Registration.HANDLER_BASIC_FILTER);
    }

    @Override
    public FilterData getFilterData() {
        return filterData;
    }

    @Override
    public RedstoneControlData getRedstoneControlData() {
        return redstoneControlData;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", itemHandler.serializeNBT(provider));
        tag.put("fluidTank", fluidTank.serializeNBT(provider));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("conversionProgress", conversionProgress);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        }
        if (tag.contains("fluidTank")) {
            fluidTank.deserializeNBT(provider, tag.getCompound("fluidTank"));
        }
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
        if (tag.contains("conversionProgress")) {
            conversionProgress = tag.getInt("conversionProgress");
        }
    }
}
