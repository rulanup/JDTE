package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.blocks.baseblocks.BaseRawOre;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.datagen.JustDireItemTags;
import com.direwolf20.justdirethings.datagen.recipes.GooSpreadRecipe;
import com.direwolf20.justdirethings.datagen.recipes.GooSpreadRecipeTag;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.mixin.FluidTankAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class GelGeneratorBE extends BaseMachineBE implements PoweredMachineBE, FilterableBE, RedstoneControlledBE, FluidMachineBE, BaseFilterMachine {
    public static final int GEL_SLOT = 0;
    public static final int FOOD_SLOT = 1;
    public static final int INPUT_START_SLOT = 2;
    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_START_SLOT = INPUT_START_SLOT + INPUT_SLOTS;
    public static final int OUTPUT_SLOTS = 4;
    public static final int TOTAL_SLOTS = OUTPUT_START_SLOT + OUTPUT_SLOTS;
    private static final int OLD_TOTAL_SLOTS = 6;
    private static final int OLD_FOOD_SLOT = 4;
    private static final int OLD_OUTPUT_SLOT = 5;
    public static final int BASE_FLUID_CAPACITY = 4000; // 4 buckets
    public static final int BASE_ENERGY_CAPACITY = 100000;
    public static final int FLUID_CONVERSION_AMOUNT = 1000;
    public static final int STANDARD_ENERGY_COST = 1000;
    private static final int FORTUNE_ENERGY_PERCENT_PER_LEVEL = 5;
    private static final int FUEL_USES_PER_ITEM = 2;

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    public final JDTEFluidTank fluidTank;
    public final JDTEFluidTank outputFluidTank;
    public final FluidContainerData fluidContainerData;
    public final ContainerData outputFluidContainerData;
    public final ContainerData gelGeneratorData;
    public FilterData filterData = new FilterData();
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    protected final ItemStackHandler itemHandler;
    protected final IFluidHandler fluidHandler;
    protected int conversionProgress = 0;
    protected int fuelUsesRemaining = 0;
    protected boolean autoBalanceInputs = false;

    protected GelGeneratorBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
        fluidTank = new JDTEFluidTank(getMaxMB(), f -> true);
        outputFluidTank = new JDTEFluidTank(getMaxMB(), f -> true);
        fluidContainerData = new FluidContainerData(this);
        outputFluidContainerData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> BuiltInRegistries.FLUID.getId(outputFluidTank.getFluid().getFluid());
                    case 1 -> outputFluidTank.getFluidAmount() & 0xFFFF;
                    case 2 -> outputFluidTank.getFluidAmount() >> 16;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
        gelGeneratorData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> conversionProgress;
                    case 1 -> getConversionProgressMax();
                    case 2 -> autoBalanceInputs ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    conversionProgress = value;
                } else if (index == 2) {
                    autoBalanceInputs = value != 0;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
        fluidHandler = new IFluidHandler() {
            @Override
            public int getTanks() {
                return 2;
            }

            @Override
            public FluidStack getFluidInTank(int tank) {
                return tank == 0 ? fluidTank.getFluid() : outputFluidTank.getFluid();
            }

            @Override
            public int getTankCapacity(int tank) {
                return tank == 0 ? fluidTank.getCapacity() : outputFluidTank.getCapacity();
            }

            @Override
            public boolean isFluidValid(int tank, FluidStack stack) {
                return tank == 0 && fluidTank.isFluidValid(stack);
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return fluidTank.fill(resource, action);
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                if (resource.isEmpty() || !outputFluidTank.getFluid().is(resource.getFluid())) {
                    return FluidStack.EMPTY;
                }
                return outputFluidTank.drain(resource, action);
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return outputFluidTank.drain(maxDrain, action);
            }
        };
        itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot == GEL_SLOT) {
                    return isValidGel(stack);
                }
                if (slot == FOOD_SLOT) {
                    return isValidGelFood(stack, itemHandler.getStackInSlot(GEL_SLOT));
                }
                if (isOutputSlot(slot)) {
                    return false;
                }
                if (isInputSlot(slot)) {
                    return isValidInputItem(stack);
                }
                return false;
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
        syncOutputFluidCapacity();
        if (autoBalanceInputs) {
            balanceInputSlots();
        }
        if (isActiveRedstone()) {
            processConversion();
        } else if (conversionProgress != 0) {
            conversionProgress = 0;
            setChanged();
        }
    }

    protected void processConversion() {
        ItemStack gelStack = itemHandler.getStackInSlot(GEL_SLOT);
        ItemStack foodStack = itemHandler.getStackInSlot(FOOD_SLOT);
        int gelTier = getGelTier(gelStack);
        if (gelTier <= 0 || !hasFuel(foodStack, gelStack)) {
            resetProgress();
            return;
        }

        if (!hasConvertibleWork(gelTier)) {
            resetProgress();
            return;
        }

        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            int energyCost = getStandardEnergyCost();
            if (!hasEnoughPower(energyCost)) {
                resetProgress();
                return;
            }
        }

        conversionProgress++;
        if (conversionProgress < getConversionProgressMax()) {
            setChanged();
            return;
        }

        int itemConversions = convertInputs(gelTier);
        boolean fluidConverted = convertFluid(gelTier);
        if (itemConversions > 0 || fluidConverted) {
            if (!UpgradeHelper.hasCreativeUpgrade(this)) {
                extractEnergy(getStandardEnergyCost(), false);
                consumeFuel();
            }
        }
        conversionProgress = 0;
        setChanged();
    }

    private boolean hasFuel(ItemStack foodStack, ItemStack gelStack) {
        return UpgradeHelper.hasCreativeUpgrade(this) || fuelUsesRemaining > 0 || (!foodStack.isEmpty() && isValidGelFood(foodStack, gelStack));
    }

    private void consumeFuel() {
        if (fuelUsesRemaining > 0) {
            fuelUsesRemaining--;
            return;
        }

        ItemStack foodStack = itemHandler.getStackInSlot(FOOD_SLOT);
        if (!foodStack.isEmpty()) {
            foodStack.shrink(1);
            itemHandler.setStackInSlot(FOOD_SLOT, foodStack);
            fuelUsesRemaining = FUEL_USES_PER_ITEM - 1;
        }
    }

    private boolean hasConvertibleWork(int gelTier) {
        return hasConvertibleInput(gelTier) || canConvertFluid(gelTier);
    }

    private boolean hasConvertibleInput(int gelTier) {
        for (int i = 0; i < INPUT_SLOTS; i++) {
            int inputSlot = INPUT_START_SLOT + i;
            ItemConversion conversion = findItemConversion(itemHandler.getStackInSlot(inputSlot), gelTier);
            if (!conversion.isEmpty() && canOutputToSlot(OUTPUT_START_SLOT + i, conversion.output())) {
                return true;
            }
        }
        return false;
    }

    private int convertInputs(int gelTier) {
        int conversions = 0;
        for (int i = 0; i < INPUT_SLOTS; i++) {
            if (convertInput(INPUT_START_SLOT + i, gelTier)) {
                conversions++;
            }
        }
        return conversions;
    }

    private void balanceInputSlots() {
        ItemStack template = ItemStack.EMPTY;
        int total = 0;
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack stack = itemHandler.getStackInSlot(INPUT_START_SLOT + i);
            if (stack.isEmpty()) {
                continue;
            }
            if (template.isEmpty()) {
                template = stack.copyWithCount(1);
            } else if (!ItemStack.isSameItemSameComponents(template, stack)) {
                return;
            }
            total += stack.getCount();
        }

        if (template.isEmpty()) {
            return;
        }

        int base = total / INPUT_SLOTS;
        int remainder = total % INPUT_SLOTS;
        boolean changed = false;
        for (int i = 0; i < INPUT_SLOTS; i++) {
            int targetCount = base + (i < remainder ? 1 : 0);
            int slot = INPUT_START_SLOT + i;
            ItemStack current = itemHandler.getStackInSlot(slot);
            if (targetCount <= 0) {
                if (!current.isEmpty()) {
                    itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
                    changed = true;
                }
                continue;
            }

            if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, template) || current.getCount() != targetCount) {
                itemHandler.setStackInSlot(slot, template.copyWithCount(targetCount));
                changed = true;
            }
        }
        if (changed) {
            setChanged();
        }
    }

    private boolean convertInput(int inputSlot, int gelTier) {
        ItemStack inputStack = itemHandler.getStackInSlot(inputSlot);
        ItemConversion conversion = findItemConversion(inputStack, gelTier);
        if (conversion.isEmpty()) return false;

        ItemStack result = applyFortune(conversion);

        int outputSlot = OUTPUT_START_SLOT + (inputSlot - INPUT_START_SLOT);
        if (!canOutputToSlot(outputSlot, result)) return false;

        inputStack.shrink(1);
        itemHandler.setStackInSlot(inputSlot, inputStack);
        insertOutput(outputSlot, result);
        return true;
    }

    private boolean canOutputToSlot(int slot, ItemStack result) {
        ItemStack outputStack = itemHandler.getStackInSlot(slot);
        return outputStack.isEmpty() && result.getCount() <= result.getMaxStackSize()
                || (ItemStack.isSameItemSameComponents(outputStack, result) && outputStack.getCount() + result.getCount() <= outputStack.getMaxStackSize());
    }

    private void insertOutput(int slot, ItemStack result) {
        ItemStack outputStack = itemHandler.getStackInSlot(slot);
        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(slot, result.copy());
            return;
        }
        outputStack.grow(result.getCount());
        itemHandler.setStackInSlot(slot, outputStack);
    }

    private ItemConversion findItemConversion(ItemStack inputStack, int gelTier) {
        if (level == null || inputStack.isEmpty() || !(inputStack.getItem() instanceof BlockItem blockItem)) {
            return ItemConversion.EMPTY;
        }

        BlockState inputState = blockItem.getBlock().defaultBlockState();
        for (RecipeHolder<GooSpreadRecipe> recipe : level.getRecipeManager().getAllRecipesFor(Registration.GOO_SPREAD_RECIPE_TYPE.get())) {
            GooSpreadRecipe value = recipe.value();
            if (value.getTierRequirement() <= gelTier && value.getInput().equals(inputState)) {
                return createItemConversion(value.getOutput());
            }
        }
        for (RecipeHolder<GooSpreadRecipeTag> recipe : level.getRecipeManager().getAllRecipesFor(Registration.GOO_SPREAD_RECIPE_TYPE_TAG.get())) {
            GooSpreadRecipeTag value = recipe.value();
            if (value.getTierRequirement() <= gelTier && inputState.is(value.getInput().getTag())) {
                return createItemConversion(value.getOutput());
            }
        }

        return ItemConversion.EMPTY;
    }

    private ItemConversion createItemConversion(BlockState outputState) {
        ItemStack output = getOutputItemForState(outputState);
        return output.isEmpty()
                ? ItemConversion.EMPTY
                : new ItemConversion(output, outputState.getBlock() instanceof BaseRawOre);
    }

    private ItemStack applyFortune(ItemConversion conversion) {
        ItemStack output = conversion.output().copy();
        int fortuneLevel = UpgradeHelper.countUpgrades(this, UpgradeType.FORTUNE);
        if (!conversion.fortuneAffected() || fortuneLevel <= 0 || level == null) {
            return output;
        }

        int bonus = Math.max(0, level.getRandom().nextInt(fortuneLevel + 2) - 1);
        output.setCount(output.getCount() * (bonus + 1));
        return output;
    }

    public static ItemStack getOutputItemForState(BlockState outputState) {
        if (outputState.is(Registration.RawFerricoreOre.get())) return new ItemStack(Registration.RawFerricore.get());
        if (outputState.is(Registration.RawBlazegoldOre.get())) return new ItemStack(Registration.RawBlazegold.get());
        if (outputState.is(Registration.RawCelestigemOre.get())) return new ItemStack(Registration.Celestigem.get());
        if (outputState.is(Registration.RawEclipseAlloyOre.get())) return new ItemStack(Registration.RawEclipseAlloy.get());
        if (outputState.is(Registration.RawCoal_T1.get())) return new ItemStack(Registration.Coal_T1.get());
        if (outputState.is(Registration.RawCoal_T2.get())) return new ItemStack(Registration.Coal_T2.get());
        if (outputState.is(Registration.RawCoal_T3.get())) return new ItemStack(Registration.Coal_T3.get());
        if (outputState.is(Registration.RawCoal_T4.get())) return new ItemStack(Registration.Coal_T4.get());

        ItemStack output = new ItemStack(outputState.getBlock().asItem());
        return output.is(Items.AIR) ? ItemStack.EMPTY : output;
    }

    private boolean canConvertFluid(int gelTier) {
        FluidStack result = findFluidConversionResult(gelTier);
        return !result.isEmpty() && outputFluidTank.fill(result, IFluidHandler.FluidAction.SIMULATE) == result.getAmount();
    }

    private boolean convertFluid(int gelTier) {
        FluidStack result = findFluidConversionResult(gelTier);
        if (result.isEmpty() || outputFluidTank.fill(result, IFluidHandler.FluidAction.SIMULATE) != result.getAmount()) {
            return false;
        }

        FluidStack drained = fluidTank.drain(FLUID_CONVERSION_AMOUNT, IFluidHandler.FluidAction.EXECUTE);
        if (drained.getAmount() < FLUID_CONVERSION_AMOUNT) {
            if (!drained.isEmpty()) {
                fluidTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            }
            return false;
        }

        outputFluidTank.fill(result, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    private FluidStack findFluidConversionResult(int gelTier) {
        if (level == null || fluidTank.getFluidAmount() < FLUID_CONVERSION_AMOUNT) {
            return FluidStack.EMPTY;
        }

        FluidStack inputStack = fluidTank.getFluid();
        for (RecipeHolder<GooSpreadRecipe> recipe : level.getRecipeManager().getAllRecipesFor(Registration.GOO_SPREAD_RECIPE_TYPE.get())) {
            GooSpreadRecipe value = recipe.value();
            if (value.getTierRequirement() <= gelTier && isInputFluid(value.getInput(), inputStack)) {
                return getOutputFluid(value.getOutput());
            }
        }
        return FluidStack.EMPTY;
    }

    private boolean isInputFluid(BlockState inputState, FluidStack inputStack) {
        return inputState.getBlock() instanceof LiquidBlock && inputState.getFluidState().is(inputStack.getFluid());
    }

    private FluidStack getOutputFluid(BlockState outputState) {
        if (!(outputState.getBlock() instanceof LiquidBlock liquidBlock)) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(liquidBlock.fluid, FLUID_CONVERSION_AMOUNT);
    }

    private boolean hasConversionForAnyTier(ItemStack stack) {
        for (int tier = 1; tier <= 4; tier++) {
            if (!findItemConversion(stack, tier).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidInputItem(ItemStack stack) {
        if (stack.isEmpty() || isValidGel(stack) || !(stack.getItem() instanceof BlockItem)) {
            return false;
        }
        if (level == null) {
            return true;
        }
        int gelTier = getGelTier(itemHandler.getStackInSlot(GEL_SLOT));
        return gelTier > 0 ? !findItemConversion(stack, gelTier).isEmpty() : hasConversionForAnyTier(stack);
    }

    public static boolean isInputSlot(int slot) {
        return slot >= INPUT_START_SLOT && slot < INPUT_START_SLOT + INPUT_SLOTS;
    }

    public static boolean isOutputSlot(int slot) {
        return slot >= OUTPUT_START_SLOT && slot < OUTPUT_START_SLOT + OUTPUT_SLOTS;
    }

    public static boolean isValidGel(ItemStack stack) {
        return getGelTier(stack) > 0;
    }

    public static int getGelTier(ItemStack stack) {
        if (stack.is(Registration.GooBlock_Tier4_ITEM.get())) return 4;
        if (stack.is(Registration.GooBlock_Tier3_ITEM.get())) return 3;
        if (stack.is(Registration.GooBlock_Tier2_ITEM.get())) return 2;
        if (stack.is(Registration.GooBlock_Tier1_ITEM.get())) return 1;
        return 0;
    }

    public static boolean isValidGelFood(ItemStack foodStack, ItemStack gelStack) {
        int tier = getGelTier(gelStack);
        if (tier <= 0) {
            return isAnyGelFood(foodStack);
        }
        return isValidGelFoodForTier(foodStack, tier);
    }

    private static boolean isAnyGelFood(ItemStack foodStack) {
        return isValidGelFoodForTier(foodStack, 1)
                || isValidGelFoodForTier(foodStack, 2)
                || isValidGelFoodForTier(foodStack, 3)
                || isValidGelFoodForTier(foodStack, 4);
    }

    private static boolean isValidGelFoodForTier(ItemStack foodStack, int tier) {
        return switch (tier) {
            case 1 -> foodStack.is(JustDireItemTags.GOO_REVIVE_TIER_1);
            case 2 -> foodStack.is(JustDireItemTags.GOO_REVIVE_TIER_2);
            case 3 -> foodStack.is(JustDireItemTags.GOO_REVIVE_TIER_3);
            case 4 -> foodStack.is(JustDireItemTags.GOO_REVIVE_TIER_4);
            default -> false;
        };
    }

    private void resetProgress() {
        if (conversionProgress != 0) {
            conversionProgress = 0;
            setChanged();
        }
    }

    public int getConversionProgress() {
        return conversionProgress;
    }

    public int getConversionProgressMax() {
        return Math.max(1, getEffectiveTickSpeed(tickSpeed));
    }

    public ContainerData getGelGeneratorData() {
        return gelGeneratorData;
    }

    public boolean isAutoBalanceInputs() {
        return autoBalanceInputs;
    }

    public void setAutoBalanceInputs(boolean autoBalanceInputs) {
        this.autoBalanceInputs = autoBalanceInputs;
        markDirtyClient();
    }

    public ContainerData getOutputFluidContainerData() {
        return outputFluidContainerData;
    }

    public JDTEFluidTank getOutputFluidTank() {
        return outputFluidTank;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    private void syncOutputFluidCapacity() {
        int capacity = getMaxMB();
        if (outputFluidTank instanceof FluidTankAccessor accessor) {
            accessor.jdte$setCapacity(capacity);
            if (outputFluidTank.getFluidAmount() > capacity) {
                outputFluidTank.getFluid().setAmount(capacity);
            }
        }
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
        int baseCost = STANDARD_ENERGY_COST;
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 0;
        int adjustedCost = baseCost;
        if (UpgradeHelper.countUpgrades(this, UpgradeType.OVERCLOCK) > 0) {
            adjustedCost = baseCost * 3;
        } else if (UpgradeHelper.countUpgrades(this, UpgradeType.UNDERCLOCK) > 0) {
            adjustedCost = Math.max(1, baseCost / 5);
        }

        int fortuneLevel = UpgradeHelper.countUpgrades(this, UpgradeType.FORTUNE);
        long scaledCost = (long) adjustedCost * (100L + (long) fortuneLevel * FORTUNE_ENERGY_PERCENT_PER_LEVEL);
        return (int) Math.min(Integer.MAX_VALUE, (scaledCost + 99L) / 100L);
    }

    private record ItemConversion(ItemStack output, boolean fortuneAffected) {
        private static final ItemConversion EMPTY = new ItemConversion(ItemStack.EMPTY, false);

        private boolean isEmpty() {
            return output.isEmpty();
        }
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
        tag.put("outputFluidTank", outputFluidTank.serializeNBT(provider));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("conversionProgress", conversionProgress);
        tag.putInt("fuelUsesRemaining", fuelUsesRemaining);
        tag.putBoolean("autoBalanceInputs", autoBalanceInputs);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) {
            loadInventory(tag.getCompound("inventory"), provider);
        }
        if (tag.contains("fluidTank")) {
            fluidTank.deserializeNBT(provider, tag.getCompound("fluidTank"));
        }
        if (tag.contains("outputFluidTank")) {
            outputFluidTank.deserializeNBT(provider, tag.getCompound("outputFluidTank"));
        }
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
        if (tag.contains("conversionProgress")) {
            conversionProgress = tag.getInt("conversionProgress");
        }
        if (tag.contains("fuelUsesRemaining")) {
            fuelUsesRemaining = tag.getInt("fuelUsesRemaining");
        }
        if (tag.contains("autoBalanceInputs")) {
            autoBalanceInputs = tag.getBoolean("autoBalanceInputs");
        }
    }

    private void loadInventory(CompoundTag inventoryTag, HolderLookup.Provider provider) {
        ItemStackHandler loaded = new ItemStackHandler(TOTAL_SLOTS);
        loaded.deserializeNBT(provider, inventoryTag);

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }

        if (loaded.getSlots() == OLD_TOTAL_SLOTS) {
            itemHandler.setStackInSlot(FOOD_SLOT, loaded.getStackInSlot(OLD_FOOD_SLOT));
            itemHandler.setStackInSlot(OUTPUT_START_SLOT, loaded.getStackInSlot(OLD_OUTPUT_SLOT));
            for (int i = 0; i < INPUT_SLOTS; i++) {
                itemHandler.setStackInSlot(INPUT_START_SLOT + i, loaded.getStackInSlot(i));
            }
            return;
        }

        for (int i = 0; i < Math.min(TOTAL_SLOTS, loaded.getSlots()); i++) {
            itemHandler.setStackInSlot(i, loaded.getStackInSlot(i));
        }
    }
}
