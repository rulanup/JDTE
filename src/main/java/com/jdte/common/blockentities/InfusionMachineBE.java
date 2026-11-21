package com.jdte.common.blockentities;

import com.jdte.common.recipes.MachineRecipeHelper;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.recipes.InfusionRecipe;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.utils.InfusionFluidHelper;
import com.jdte.common.utils.MobLootSpawnEggHelper;
import com.jdte.setup.JDTERecipes;
import com.jdte.setup.JDTEFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;

public abstract class InfusionMachineBE extends BaseMachineBE implements FluidMachineBE, RedstoneControlledBE, CoalescedAcceleratedMachine {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int TOTAL_SLOTS = 2;
    public static final int BASE_FLUID_CAPACITY = 8000;
    public static final int PROCESS_TIME = 20;

    public final JDTEFluidTank fluidTank;
    public final FluidContainerData fluidContainerData;
    public final ContainerData infusionData;
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    private final MachineInventoryHandler itemHandler;
    protected int progress = 0;

    protected InfusionMachineBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        fluidTank = new JDTEFluidTank(getMaxMB(), f -> true);
        fluidContainerData = new FluidContainerData(this);
        infusionData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> PROCESS_TIME;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) progress = value;
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        itemHandler = new MachineInventoryHandler(TOTAL_SLOTS) {
            @Override
            protected void onContentsChanged(int slot, ItemStack previous) {
                setChanged();
            }

            @Override
            public boolean isValid(int slot, ItemResource resource) {
                return slot != OUTPUT_SLOT;
            }
        };
    }

    @Override
    public ItemStacksResourceHandler getMachineHandler() {
        return itemHandler;
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        if (isActiveRedstone() && canRun()) {
            processInfusion();
        }
    }

    public abstract int getEffectiveEnergyCost();

    public abstract int getEffectiveEnergyCost(int baseEnergyCost);

    public abstract boolean hasEnoughPower(int energyCost);

    public abstract int extractEnergy(int energy, boolean simulate);

    protected void processInfusion() {
        if (!(level instanceof ServerLevel)) return;

        ItemStack inputStack = itemHandler.copyStack(INPUT_SLOT);
        FluidStack tankFluid = fluidTank.getFluid();

        if (inputStack.isEmpty() || tankFluid.isEmpty()) {
            resetProgress();
            return;
        }

        InfusionProcess process = findProcess(inputStack, tankFluid);
        if (process == null) {
            resetProgress();
            return;
        }

        ItemStack result = process.output();
        if (!canOutput(result)) {
            resetProgress();
            return;
        }

        int energyCost = getEffectiveEnergyCost(process.energyCost());
        if (energyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this) && !hasEnoughPower(energyCost)) {
            resetProgress();
            return;
        }

        progress++;
        if (progress < PROCESS_TIME) {
            setChanged();
            return;
        }

        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            inputStack.shrink(process.inputAmount());
            itemHandler.setStack(INPUT_SLOT, inputStack);
            fluidTank.extractFluid(process.fluidAmount());
            if (energyCost > 0) {
                extractEnergy(energyCost, false);
            }
        }

        ItemStack outputStack = itemHandler.copyStack(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            itemHandler.setStack(OUTPUT_SLOT, result.copy());
        } else {
            outputStack.grow(result.getCount());
            itemHandler.setStack(OUTPUT_SLOT, outputStack);
        }

        progress = 0;
        setChanged();
    }

    public int getEnergyStored() {
        if (this instanceof PoweredMachineBE poweredMachine && poweredMachine.getEnergyStorage() != null) {
            return poweredMachine.getEnergyStorage().getEnergyStored();
        }
        return 0;
    }

    public int getEffectiveProcessTime() {
        if (UpgradeHelper.hasCreativeUpgrade(this) || UpgradeHelper.hasOverclock(this)) {
            return 1;
        }
        if (UpgradeHelper.hasUndercLock(this)) {
            return 40;
        }
        return PROCESS_TIME;
    }

    protected int accumulatedAcceleratedTicks = 0;

    @Override
    public void accumulateAcceleratedTicks(int ticks) {
        if (ticks > 0) {
            accumulatedAcceleratedTicks = (int) Math.min(Integer.MAX_VALUE, (long) accumulatedAcceleratedTicks + ticks);
        }
    }

    @Override
    public void flushAcceleratedTicks() {
        int ticks = accumulatedAcceleratedTicks;
        accumulatedAcceleratedTicks = 0;
        if (ticks <= 0 || !isActiveRedstone() || !canRun()) {
            return;
        }
        processAcceleratedInfusion(ticks);
    }

    protected void processAcceleratedInfusion(int ticks) {
        if (!(level instanceof ServerLevel)) return;

        ItemStack inputStack = itemHandler.copyStack(INPUT_SLOT);
        FluidStack tankFluid = fluidTank.getFluid();

        if (inputStack.isEmpty() || tankFluid.isEmpty()) {
            resetProgress();
            return;
        }

        InfusionProcess process = findProcess(inputStack, tankFluid);
        if (process == null) {
            resetProgress();
            return;
        }

        ItemStack result = process.output();
        if (!canOutput(result)) {
            resetProgress();
            return;
        }

        int energyCost = getEffectiveEnergyCost(process.energyCost());
        if (energyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this) && !hasEnoughPower(energyCost)) {
            resetProgress();
            return;
        }

        int processTime = getEffectiveProcessTime();
        long totalProgress = (long) progress + ticks;
        long batches = totalProgress / processTime;
        progress = (int) (totalProgress % processTime);
        if (batches <= 0) {
            setChanged();
            return;
        }

        int maxBatches;
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            ItemStack output = itemHandler.copyStack(OUTPUT_SLOT);
            int availableOutputSpace = output.isEmpty() ? result.getMaxStackSize() : (result.getMaxStackSize() - output.getCount());
            maxBatches = result.getCount() > 0 ? (availableOutputSpace / result.getCount()) : (int) Math.min(Integer.MAX_VALUE, batches);
        } else {
            int inputBatches = process.inputAmount() > 0 ? (inputStack.getCount() / process.inputAmount()) : Integer.MAX_VALUE;
            int fluidBatches = process.fluidAmount() > 0 ? (tankFluid.getAmount() / process.fluidAmount()) : Integer.MAX_VALUE;
            int energyBatches = energyCost > 0 ? (getEnergyStored() / energyCost) : Integer.MAX_VALUE;
            ItemStack output = itemHandler.copyStack(OUTPUT_SLOT);
            int availableOutputSpace = output.isEmpty() ? result.getMaxStackSize() : (result.getMaxStackSize() - output.getCount());
            int outputBatches = result.getCount() > 0 ? (availableOutputSpace / result.getCount()) : Integer.MAX_VALUE;
            maxBatches = Math.min(Math.min(inputBatches, fluidBatches), Math.min(energyBatches, outputBatches));
        }

        int parallel = (int) Math.min(batches, maxBatches);
        if (parallel <= 0) {
            setChanged();
            return;
        }

        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            inputStack.shrink(process.inputAmount() * parallel);
            itemHandler.setStack(INPUT_SLOT, inputStack);
            fluidTank.extractFluid(process.fluidAmount() * parallel);
            if (energyCost > 0) {
                extractEnergy(energyCost * parallel, false);
            }
        }

        ItemStack outputStack = itemHandler.copyStack(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            outputStack = result.copy();
            outputStack.setCount(result.getCount() * parallel);
            itemHandler.setStack(OUTPUT_SLOT, outputStack);
        } else {
            outputStack.grow(result.getCount() * parallel);
            itemHandler.setStack(OUTPUT_SLOT, outputStack);
        }

        setChanged();
    }

    protected InfusionProcess findProcess(ItemStack input, FluidStack fluid) {
        InfusionRecipe recipe = findRecipe(input, fluid);
        if (recipe != null) {
            return new InfusionProcess(
                    recipe.getOutput(),
                    recipe.getInputCount(),
                    recipe.getFluidInput().getAmount(),
                    recipe.getEnergyCost());
        }
        InfusionProcess spawnEggProcess = findSpawnEggProcess(input, fluid);
        if (spawnEggProcess != null) {
            return spawnEggProcess;
        }
        return findContainerFillProcess(input, fluid);
    }

    protected InfusionProcess findSpawnEggProcess(ItemStack input, FluidStack fluid) {
        if (!(level instanceof ServerLevel serverLevel)
                || !fluid.is(JDTEFluids.LIFE_FLUID_SOURCE.get())
                || fluid.getAmount() < MobLootSpawnEggHelper.LIFE_FLUID_COST) {
            return null;
        }

        ItemStack spawnEgg = MobLootSpawnEggHelper.findUniqueSpawnEgg(serverLevel, input);
        if (spawnEgg.isEmpty()) {
            return null;
        }
        return new InfusionProcess(
                spawnEgg,
                input.getMaxStackSize(),
                MobLootSpawnEggHelper.LIFE_FLUID_COST,
                MobLootSpawnEggHelper.ENERGY_COST);
    }

    protected InfusionRecipe findRecipe(ItemStack input, FluidStack fluid) {
        if (level == null) return null;
        for (RecipeHolder<InfusionRecipe> holder : MachineRecipeHelper.recipes(level).byType(JDTERecipes.INFUSION_RECIPE_TYPE.get())) {
            if (holder.value().matches(input, fluid)) {
                return holder.value();
            }
        }
        return null;
    }

    protected InfusionProcess findContainerFillProcess(ItemStack input, FluidStack fluid) {
        InfusionProcess capabilityProcess = findFluidHandlerFillProcess(input, fluid);
        if (capabilityProcess != null) {
            return capabilityProcess;
        }
        return findVanillaBottleFillProcess(input, fluid);
    }

    private InfusionProcess findFluidHandlerFillProcess(ItemStack input, FluidStack fluid) {
        if (input.isEmpty() || fluid.isEmpty()) {
            return null;
        }

        var filled = InfusionFluidHelper.fillContainer(input, fluid);
        return filled == null ? null
                : new InfusionProcess(filled.stack(), 1, filled.fluidAmount(), AdvancedInfusionMachineBE.BASE_ENERGY_COST);
    }

    private InfusionProcess findVanillaBottleFillProcess(ItemStack input, FluidStack fluid) {
        if (!input.is(Items.GLASS_BOTTLE) || fluid.getAmount() < InfusionFluidHelper.BOTTLE_FLUID_AMOUNT) {
            return null;
        }

        ItemStack result;
        if (fluid.is(Fluids.WATER)) {
            result = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        } else if (InfusionFluidHelper.isHoneyFluid(fluid)) {
            result = new ItemStack(Items.HONEY_BOTTLE);
        } else {
            return null;
        }
        result.setCount(1);
        return new InfusionProcess(result, 1, InfusionFluidHelper.BOTTLE_FLUID_AMOUNT, AdvancedInfusionMachineBE.BASE_ENERGY_COST);
    }

    protected boolean canOutput(ItemStack result) {
        ItemStack output = itemHandler.copyStack(OUTPUT_SLOT);
        if (output.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(output, result)) return false;
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    protected void resetProgress() {
        if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    public ContainerData getInfusionData() {
        return infusionData;
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
    public RedstoneControlData getRedstoneControlData() {
        return redstoneControlData;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    protected record InfusionProcess(ItemStack output, int inputAmount, int fluidAmount, int energyCost) {
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putChild("inventory", itemHandler);
        tag.putChild("fluidTank", fluidTank);
        tag.putInt("progress", progress);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        tag.child("inventory").ifPresent(itemHandler::deserialize);
        tag.child("fluidTank").ifPresent(fluidTank::deserialize);
        progress = tag.getIntOr("progress", progress);
    }
}
