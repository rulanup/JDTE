package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.recipes.InfusionRecipe;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.utils.InfusionFluidHelper;
import com.jdte.common.utils.MobLootSpawnEggHelper;
import com.jdte.setup.JDTERecipes;
import com.jdte.setup.JDTEFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class InfusionMachineBE extends BaseMachineBE implements FluidMachineBE, RedstoneControlledBE {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int TOTAL_SLOTS = 2;
    public static final int BASE_FLUID_CAPACITY = 8000;
    public static final int PROCESS_TIME = 20;

    public final JDTEFluidTank fluidTank;
    public final FluidContainerData fluidContainerData;
    public final ContainerData infusionData;
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    protected final ItemStackHandler itemHandler;
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
        itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot == OUTPUT_SLOT) return false;
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
            processInfusion();
        }
    }

    public abstract int getEffectiveEnergyCost();

    public abstract int getEffectiveEnergyCost(int baseEnergyCost);

    public abstract boolean hasEnoughPower(int energyCost);

    public abstract int extractEnergy(int energy, boolean simulate);

    protected void processInfusion() {
        if (!(level instanceof ServerLevel)) return;

        ItemStack inputStack = itemHandler.getStackInSlot(INPUT_SLOT);
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
            itemHandler.setStackInSlot(INPUT_SLOT, inputStack);
            fluidTank.drain(process.fluidAmount(), IFluidHandler.FluidAction.EXECUTE);
            if (energyCost > 0) {
                extractEnergy(energyCost, false);
            }
        }

        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            outputStack.grow(result.getCount());
            itemHandler.setStackInSlot(OUTPUT_SLOT, outputStack);
        }

        progress = 0;
        setChanged();
    }

    protected InfusionProcess findProcess(ItemStack input, FluidStack fluid) {
        InfusionRecipe recipe = findRecipe(input, fluid);
        if (recipe != null) {
            return new InfusionProcess(
                    recipe.getOutput(),
                    recipe.getInput().getCount(),
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
        for (RecipeHolder<InfusionRecipe> holder : level.getRecipeManager().getAllRecipesFor(JDTERecipes.INFUSION_RECIPE_TYPE.get())) {
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

        ItemStack container = input.copy();
        container.setCount(1);
        ItemStack originalContainer = container.copy();
        IFluidHandlerItem itemHandler = container.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null || itemHandler.getTanks() <= 0) {
            return null;
        }

        FluidStack available = fluid.copy();
        int fillAmount = itemHandler.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (fillAmount <= 0 || fillAmount > fluid.getAmount()) {
            return null;
        }

        FluidStack toFill = fluid.copy();
        toFill.setAmount(fillAmount);
        int filled = itemHandler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            return null;
        }

        ItemStack result = itemHandler.getContainer();
        if (result.isEmpty() || ItemStack.isSameItemSameComponents(originalContainer, result)) {
            return null;
        }
        result.setCount(1);
        return new InfusionProcess(result.copy(), 1, filled, AdvancedInfusionMachineBE.BASE_ENERGY_COST);
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
        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
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
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", itemHandler.serializeNBT(provider));
        tag.put("fluidTank", fluidTank.serializeNBT(provider));
        tag.putInt("progress", progress);
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
        if (tag.contains("progress")) {
            progress = tag.getInt("progress");
        }
    }
}
