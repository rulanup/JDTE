package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.integrations.ProductiveBeesBioFactoryIntegration;
import com.jdte.common.recipes.BioFactoryRecipe;
import com.jdte.common.upgrades.BioFactoryUpgradeItemStackHandler;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.mixin.FluidTankAccessor;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEFluids;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BioFactoryBE extends BaseMachineBE implements PoweredMachineBE, RedstoneControlledBE, ExtendedUpgradeMachine {
    public static final int SPECIMEN_SLOT = 0;
    public static final int FOOD_SLOT = 1;
    public static final int OUTPUT_START_SLOT = 2;
    public static final int BASE_OUTPUT_SLOTS = 8;
    public static final int OUTPUT_SLOTS_PER_CAPACITY = 8;
    public static final int OUTPUT_SLOTS = 32;
    public static final int SECONDARY_INPUT_SLOT = OUTPUT_START_SLOT + OUTPUT_SLOTS;
    public static final int TERTIARY_INPUT_SLOT = SECONDARY_INPUT_SLOT + 1;
    public static final int INPUT_SLOTS = 3;
    public static final int TOTAL_SLOTS = TERTIARY_INPUT_SLOT + 1;
    public static final int UPGRADE_SLOTS = 8;

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(getMaxEnergy());
    private final PoweredMachineContainerData poweredData = new PoweredMachineContainerData(this);
    private final RedstoneControlData redstoneData = new RedstoneControlData();
    private final BioFactoryUpgradeItemStackHandler upgradeHandler = new BioFactoryUpgradeItemStackHandler(this);
    private final JDTEFluidTank lifeFluidTank = createTank(stack -> stack.is(JDTEFluids.LIFE_FLUID_SOURCE.get()), false);
    private final JDTEFluidTank timeFluidTank = createTank(stack -> stack.getFluid() instanceof TimeFluid, false);
    private final JDTEFluidTank processFluidTank = createTank(stack -> !stack.is(JDTEFluids.LIFE_FLUID_SOURCE.get())
            && !(stack.getFluid() instanceof TimeFluid), false);
    private final JDTEFluidTank productFluidTank = createTank(stack -> true, false);
    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override public int getSlotLimit(int slot) { return slot == SPECIMEN_SLOT ? 1 : 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == SPECIMEN_SLOT) return isValidSpecimen(stack);
            if (isInputStorageSlot(slot)) return true;
            return slot >= OUTPUT_START_SLOT && slot < OUTPUT_START_SLOT + OUTPUT_SLOTS;
        }
        @Override protected void onContentsChanged(int slot) {
            if (slot == SPECIMEN_SLOT || isInputStorageSlot(slot)) {
                clearRecipeCache();
                progress = 0;
            }
            setChanged();
            markDirtyClient();
        }
    };
    private final IItemHandler automationItemHandler = new IItemHandler() {
        @Override public int getSlots() { return 1 + INPUT_SLOTS + getActiveOutputSlots(); }
        @Override public ItemStack getStackInSlot(int slot) {
            int storageSlot = toStorageSlot(slot);
            return storageSlot >= 0 ? itemHandler.getStackInSlot(storageSlot) : ItemStack.EMPTY;
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int storageSlot = toStorageSlot(slot);
            return slot >= 0 && slot <= INPUT_SLOTS && storageSlot >= 0
                    ? itemHandler.insertItem(storageSlot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int storageSlot = toStorageSlot(slot);
            return slot > INPUT_SLOTS && storageSlot >= 0
                    ? itemHandler.extractItem(storageSlot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) {
            int storageSlot = toStorageSlot(slot);
            return storageSlot >= 0 ? itemHandler.getSlotLimit(storageSlot) : 0;
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            int storageSlot = toStorageSlot(slot);
            return slot >= 0 && slot <= INPUT_SLOTS && storageSlot >= 0
                    && itemHandler.isItemValid(storageSlot, stack);
        }
        private int toStorageSlot(int slot) {
            if (slot == 0) return SPECIMEN_SLOT;
            if (slot >= 1 && slot <= INPUT_SLOTS) return inputStorageSlot(slot - 1);
            int output = slot - 1 - INPUT_SLOTS;
            return output >= 0 && output < getActiveOutputSlots() ? OUTPUT_START_SLOT + output : -1;
        }
    };
    private final IFluidHandler inputFluidHandler = new InputFluidHandler();
    private final IFluidHandler outputFluidHandler = new OutputFluidHandler();
    private final IFluidHandler combinedFluidHandler = new CombinedFluidHandler();
    private final ContainerData machineData = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> getCurrentProcessTicks();
                case 2 -> getActiveOutputSlots();
                case 3 -> lifeFluidTank.getFluidAmount();
                case 4 -> timeFluidTank.getFluidAmount();
                case 5 -> processFluidTank.getFluidAmount();
                case 6 -> productFluidTank.getFluidAmount();
                case 7 -> getMaxFluidCapacity();
                case 8 -> (int) Math.min(Integer.MAX_VALUE, Math.round(getProductivityMultiplier() * 100.0D));
                case 9 -> isClientSide() ? syncedMultiplier : getMultiplier();
                case 10 -> isClientSide() ? syncedMaxMultiplier : getMaxSelectableMultiplier();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> syncedProcessTicks = value;
                case 2 -> syncedOutputSlots = value;
                case 3 -> syncedLifeFluid = value;
                case 4 -> syncedTimeFluid = value;
                case 5 -> syncedProcessFluid = value;
                case 6 -> syncedProductFluid = value;
                case 7 -> syncedFluidCapacity = value;
                case 8 -> syncedProductivity = value;
                case 9 -> syncedMultiplier = value;
                case 10 -> syncedMaxMultiplier = value;
                default -> { }
            }
        }
        @Override public int getCount() { return 11; }
    };

    private int progress;
    private int settlementTicker;
    private int syncedProcessTicks = 600;
    private int syncedOutputSlots = BASE_OUTPUT_SLOTS;
    private int syncedLifeFluid;
    private int syncedTimeFluid;
    private int syncedProcessFluid;
    private int syncedProductFluid;
    private int syncedFluidCapacity;
    private int syncedProductivity = 100;
    private int multiplier;
    private int syncedMultiplier = 1;
    private int syncedMaxMultiplier = 32;
    private BioFactoryRecipe cachedRecipe;
    private Bee cachedBee;
    private ItemStack cachedSpecimen = ItemStack.EMPTY;
    private final ItemStack[] cachedInputs = new ItemStack[INPUT_SLOTS];
    private int[] cachedRecipeInputSlots;
    private Fluid cachedProcessFluid;
    private boolean cachedBeeUsesItemFood;
    private boolean cacheResolved;

    public BioFactoryBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.BIO_FACTORY.get(), pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        tickSpeed = 1;
        multiplier = JDTEConfig.COMMON.bioFactoryDefaultSpeedMultiplier.get();
        Arrays.fill(cachedInputs, ItemStack.EMPTY);
    }

    @Override public void tickServer() {
        super.tickServer();
        syncCapacities();
        if (!isActiveRedstone() || !canRun() || !resolveRecipe()
                || cachedBee != null && !ProductiveBeesBioFactoryIntegration.canOperate(level, cachedBee)) {
            resetProgress();
            return;
        }
        int speed = getSpeedMultiplier();
        int remainingWork = Math.max(1, getCurrentProcessTicks() - progress);
        int ticksToCompletion = Math.max(1, (remainingWork + speed - 1) / speed);
        int settlementTarget = Math.min(JDTEConfig.COMMON.bioFactorySettlementInterval.get(), ticksToCompletion);
        if (++settlementTicker < settlementTarget) return;
        int elapsed = settlementTicker;
        settlementTicker = 0;
        progress = Math.min(getCurrentProcessTicks(), progress + elapsed * speed);
        if (progress < getCurrentProcessTicks()) {
            setChanged();
            return;
        }
        completeCycle();
    }

    private boolean resolveRecipe() {
        ItemStack specimen = itemHandler.getStackInSlot(SPECIMEN_SLOT);
        List<ItemStack> inputs = getInputStacks();
        Fluid fluid = processFluidTank.getFluid().getFluid();
        if (cacheResolved && ItemStack.isSameItemSameComponents(specimen, cachedSpecimen)
                && inputsMatchCache(inputs) && fluid == cachedProcessFluid) {
            return cachedRecipe != null || cachedBee != null;
        }
        clearRecipeCache();
        cachedSpecimen = specimen.copy();
        for (int i = 0; i < INPUT_SLOTS; i++) cachedInputs[i] = inputs.get(i).copy();
        cachedProcessFluid = fluid;
        cacheResolved = true;
        if (level == null || specimen.isEmpty()) return false;
        cachedRecipe = level.getRecipeManager().getAllRecipesFor(JDTERecipes.BIO_FACTORY_RECIPE_TYPE.get()).stream()
                .map(holder -> holder.value())
                .filter(recipe -> {
                    int[] assignment = recipe.findMatchingSlots(specimen, inputs);
                    if (assignment == null) return false;
                    cachedRecipeInputSlots = assignment;
                    return true;
                })
                .filter(this::hasRecipeProcessFluid)
                .findFirst().orElse(null);
        if (cachedRecipe != null) return true;
        if (!ModList.get().isLoaded("productivebees")) return false;
        cachedBee = ProductiveBeesBioFactoryIntegration.createBee(specimen, level);
        if (cachedBee == null) return false;
        ItemStack bucket = processFluidTank.isEmpty() ? ItemStack.EMPTY
                : new ItemStack(processFluidTank.getFluid().getFluid().getBucket());
        cachedBeeUsesItemFood = inputs.stream().anyMatch(stack ->
                ProductiveBeesBioFactoryIntegration.isValidFood(cachedBee, stack, ItemStack.EMPTY));
        if (!cachedBeeUsesItemFood && !ProductiveBeesBioFactoryIntegration.isValidFood(cachedBee, ItemStack.EMPTY, bucket)) {
            cachedBee = null;
        }
        return cachedBee != null;
    }

    private boolean hasRecipeProcessFluid(BioFactoryRecipe recipe) {
        if (recipe.processFluid().isEmpty() || recipe.processFluidAmount() <= 0) return true;
        Fluid expected = BuiltInRegistries.FLUID.get(recipe.processFluid().get());
        return !processFluidTank.isEmpty() && processFluidTank.getFluid().is(expected)
                && processFluidTank.getFluidAmount() >= recipe.processFluidAmount();
    }

    private void completeCycle() {
        BioFactoryRecipe completedRecipe = cachedRecipe;
        int[] completedInputSlots = cachedRecipeInputSlots == null ? null : cachedRecipeInputSlots.clone();
        boolean creative = UpgradeHelper.hasCreativeUpgrade(this);
        int energyCost = UpgradeHelper.adjustEnergyCost(this, completedRecipe != null
                ? Math.max(0, completedRecipe.energy()) : JDTEConfig.COMMON.bioFactoryEnergyPerCycle.get());
        int timeCost = creative ? 0 : getEffectiveTimeFluidCost();
        boolean timeBoost = creative || timeFluidTank.getFluidAmount() >= timeCost;
        int lifeCost = creative ? 0 : getEffectiveLifeFluidCost();
        boolean lifeBoost = creative || lifeFluidTank.getFluidAmount() >= lifeCost;
        int processCost = getProcessFluidCost();
        if (!creative && (!hasEnoughPower(energyCost)
                || timeBoost && timeFluidTank.getFluidAmount() < timeCost
                || lifeBoost && lifeFluidTank.getFluidAmount() < lifeCost
                || processFluidTank.getFluidAmount() < processCost)) return;

        double multiplier = getProductivityMultiplier()
                * (lifeBoost ? JDTEConfig.COMMON.bioFactoryLifeYieldMultiplier.get() : 1.0D);
        List<ItemStack> outputs = createOutputs(multiplier);
        FluidStack fluidOutput = createFluidOutput(multiplier);
        if (outputs.isEmpty() && fluidOutput.isEmpty()) {
            progress = 0;
            return;
        }
        if (!canFit(outputs) || !canFitFluid(fluidOutput)) return;

        outputs.forEach(this::insertOutput);
        if (!fluidOutput.isEmpty()) productFluidTank.fill(fluidOutput, IFluidHandler.FluidAction.EXECUTE);
        if (!creative) {
            extractEnergy(energyCost, false);
            if (timeBoost) timeFluidTank.drain(timeCost, IFluidHandler.FluidAction.EXECUTE);
            if (lifeBoost) lifeFluidTank.drain(lifeCost, IFluidHandler.FluidAction.EXECUTE);
            if (processCost > 0) processFluidTank.drain(processCost, IFluidHandler.FluidAction.EXECUTE);
            if (completedRecipe != null && completedInputSlots != null) {
                for (int input = 0; input < completedRecipe.inputs().size(); input++) {
                    int count = completedRecipe.inputs().get(input).count();
                    if (count > 0) itemHandler.extractItem(inputStorageSlot(completedInputSlots[input]), count, false);
                }
            }
        }
        progress = 0;
        setChanged();
        markDirtyClient();
    }

    private List<ItemStack> createOutputs(double multiplier) {
        if (cachedRecipe != null) {
            List<ItemStack> base = cachedRecipe.outputs().stream().map(output -> output.roll(level.random, multiplier))
                    .filter(stack -> !stack.isEmpty()).map(ItemStack::copy)
                    .map(this::applyRecipeOutputComponents).toList();
            if (base.isEmpty() || upgradeHandler.getLootingCount() <= 0) return base;
            List<ItemStack> result = new ArrayList<>(base);
            double chance = JDTEConfig.COMMON.lootingExtraDropChance.get();
            for (int level = 0; level < upgradeHandler.getLootingCount(); level++) {
                if (this.level.random.nextDouble() < chance) base.forEach(stack -> result.add(stack.copy()));
            }
            return result;
        }
        boolean blockOutput = upgradeHandler.countProductivityTier(4) > 0;
        return ProductiveBeesBioFactoryIntegration.produce(level, cachedBee, blockOutput, multiplier).stream()
                .filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
    }

    private ItemStack applyRecipeOutputComponents(ItemStack stack) {
        if (stack.is(Items.SUSPICIOUS_STEW)) {
            for (ItemStack input : getInputStacks()) {
                SuspiciousEffectHolder holder = SuspiciousEffectHolder.tryGet(input.getItem());
                if (holder != null) {
                    stack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, holder.getSuspiciousEffects());
                    break;
                }
            }
        } else if (stack.is(Items.GOAT_HORN)) {
            InstrumentItem.setRandom(stack, InstrumentTags.GOAT_HORNS, level.random);
        } else if (stack.is(Items.WHITE_WOOL)) {
            for (ItemStack input : getInputStacks()) {
                if (input.getItem() instanceof DyeItem dye) {
                    var item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(
                            "minecraft", dye.getDyeColor().getName() + "_wool"));
                    return new ItemStack(item, stack.getCount());
                }
            }
        }
        return stack;
    }

    private FluidStack createFluidOutput(double multiplier) {
        if (cachedRecipe == null || cachedRecipe.outputFluid().isEmpty() || cachedRecipe.outputFluidAmount() <= 0) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(cachedRecipe.outputFluid().get());
        int amount = (int) Math.min(Integer.MAX_VALUE, Math.round(cachedRecipe.outputFluidAmount() * multiplier));
        return amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    private int getProcessFluidCost() {
        if (cachedRecipe != null) return Math.max(0, cachedRecipe.processFluidAmount());
        return cachedBee != null && !cachedBeeUsesItemFood
                ? JDTEConfig.COMMON.bioFactoryProcessFluidPerCycle.get() : 0;
    }

    private boolean canFit(List<ItemStack> outputs) {
        ItemStackHandler copy = new ItemStackHandler(getActiveOutputSlots());
        for (int i = 0; i < copy.getSlots(); i++) copy.setStackInSlot(i, itemHandler.getStackInSlot(OUTPUT_START_SLOT + i).copy());
        for (ItemStack output : outputs) {
            ItemStack remaining = output.copy();
            while (!remaining.isEmpty()) {
                int count = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                ItemStack part = remaining.copyWithCount(count);
                if (!ItemHandlerHelper.insertItemStacked(copy, part, false).isEmpty()) return false;
                remaining.shrink(count);
            }
        }
        return true;
    }

    private boolean canFitFluid(FluidStack output) {
        return output.isEmpty() || productFluidTank.fill(output, IFluidHandler.FluidAction.SIMULATE) == output.getAmount();
    }

    private void insertOutput(ItemStack output) {
        ItemStack remaining = output.copy();
        while (!remaining.isEmpty()) {
            int count = Math.min(remaining.getCount(), remaining.getMaxStackSize());
            ItemStack part = remaining.copyWithCount(count);
            for (int slot = 0; slot < getActiveOutputSlots() && !part.isEmpty(); slot++) {
                part = itemHandler.insertItem(OUTPUT_START_SLOT + slot, part, false);
            }
            remaining.shrink(count - part.getCount());
            if (!part.isEmpty()) break;
        }
    }

    private int getSpeedMultiplier() {
        int timeCost = getEffectiveTimeFluidCost();
        if (UpgradeHelper.hasCreativeUpgrade(this) || timeFluidTank.getFluidAmount() >= timeCost) {
            return getMultiplier();
        }
        return 1;
    }

    private int getEffectiveTimeFluidCost() {
        int multiplier = cachedBee == null ? 1 : JDTEConfig.COMMON.bioFactoryExternalTimeFluidCostMultiplier.get();
        return safeMultiplyCost(JDTEConfig.COMMON.bioFactoryTimeFluidPerCycle.get(), multiplier);
    }

    private int getEffectiveLifeFluidCost() {
        int multiplier = cachedBee == null ? 1 : JDTEConfig.COMMON.bioFactoryExternalLifeFluidCostMultiplier.get();
        return safeMultiplyCost(JDTEConfig.COMMON.bioFactoryLifeFluidPerCycle.get(), multiplier);
    }

    private static int safeMultiplyCost(int value, int multiplier) {
        if (value <= 0 || multiplier <= 0) return 0;
        long result = (long) value * multiplier;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }

    public int getMultiplier() {
        return Math.clamp(multiplier, 1, getMaxSelectableMultiplier());
    }

    public void setMultiplier(int multiplier) {
        int clamped = Math.clamp(multiplier, 1, getMaxSelectableMultiplier());
        if (this.multiplier != clamped) {
            this.multiplier = clamped;
            setChanged();
            markDirtyClient();
        }
    }

    public int getMaxSelectableMultiplier() {
        return UpgradeHelper.hasOverclock(this) || UpgradeHelper.hasCreativeUpgrade(this)
                ? JDTEConfig.COMMON.bioFactoryOverclockMaxSpeedMultiplier.get()
                : JDTEConfig.COMMON.bioFactoryMaxSpeedMultiplier.get();
    }

    public double getProductivityMultiplier() {
        if (cachedRecipe != null || !ModList.get().isLoaded("productivebees")) return 1.0D;
        return ProductiveBeesBioFactoryIntegration.getProductivityMultiplier(
                upgradeHandler.countProductivityTier(1), upgradeHandler.countProductivityTier(2),
                upgradeHandler.countProductivityTier(3), upgradeHandler.countProductivityTier(4));
    }

    private int getCurrentProcessTicks() {
        if (level != null && !level.isClientSide && cachedRecipe != null) return Math.max(1, cachedRecipe.processTicks());
        if (level != null && level.isClientSide) return Math.max(1, syncedProcessTicks);
        return JDTEConfig.COMMON.bioFactoryBaseProcessTicks.get();
    }

    private boolean isValidSpecimen(ItemStack stack) {
        if (stack.getItem() instanceof SpawnEggItem) return true;
        return ModList.get().isLoaded("productivebees")
                && ProductiveBeesBioFactoryIntegration.isBeeSpecimen(stack);
    }

    private JDTEFluidTank createTank(java.util.function.Predicate<FluidStack> validator, boolean clearsCache) {
        return new JDTEFluidTank(getMaxFluidCapacity(), validator) {
            private Fluid lastSyncedFluid = Fluids.EMPTY;
            @Override protected void onContentsChanged() {
                super.onContentsChanged();
                if (clearsCache) {
                    clearRecipeCache();
                    progress = 0;
                }
                setChanged();
                Fluid current = getFluid().getFluid();
                if (current != lastSyncedFluid) {
                    lastSyncedFluid = current;
                    markDirtyClient();
                }
            }
        };
    }

    private void clearRecipeCache() {
        cacheResolved = false;
        cachedRecipe = null;
        cachedBee = null;
        cachedSpecimen = ItemStack.EMPTY;
        Arrays.fill(cachedInputs, ItemStack.EMPTY);
        cachedRecipeInputSlots = null;
        cachedProcessFluid = null;
        cachedBeeUsesItemFood = false;
    }

    private List<ItemStack> getInputStacks() {
        List<ItemStack> inputs = new ArrayList<>(INPUT_SLOTS);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) inputs.add(itemHandler.getStackInSlot(inputStorageSlot(slot)));
        return inputs;
    }

    private boolean inputsMatchCache(List<ItemStack> inputs) {
        for (int i = 0; i < INPUT_SLOTS; i++) {
            if (!ItemStack.isSameItemSameComponents(inputs.get(i), cachedInputs[i])
                    || inputs.get(i).getCount() != cachedInputs[i].getCount()) return false;
        }
        return true;
    }

    public static int inputStorageSlot(int logicalSlot) {
        return switch (logicalSlot) {
            case 0 -> FOOD_SLOT;
            case 1 -> SECONDARY_INPUT_SLOT;
            case 2 -> TERTIARY_INPUT_SLOT;
            default -> -1;
        };
    }

    private static boolean isInputStorageSlot(int slot) {
        return slot == FOOD_SLOT || slot == SECONDARY_INPUT_SLOT || slot == TERTIARY_INPUT_SLOT;
    }

    private void resetProgress() {
        settlementTicker = 0;
        if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    public int getActiveOutputSlots() {
        if (level != null && level.isClientSide) return Math.max(BASE_OUTPUT_SLOTS, syncedOutputSlots);
        int configured = BASE_OUTPUT_SLOTS + UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY) * OUTPUT_SLOTS_PER_CAPACITY;
        int occupied = BASE_OUTPUT_SLOTS;
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            if (!itemHandler.getStackInSlot(OUTPUT_START_SLOT + i).isEmpty()) occupied = ((i / 8) + 1) * 8;
        }
        return Math.min(OUTPUT_SLOTS, Math.max(configured, occupied));
    }

    public int getMaxFluidCapacity() {
        return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.bioFactoryFluidCapacity.get());
    }

    private void syncCapacities() {
        int fluidCapacity = getMaxFluidCapacity();
        syncTank(lifeFluidTank, fluidCapacity);
        syncTank(timeFluidTank, fluidCapacity);
        syncTank(processFluidTank, fluidCapacity);
        syncTank(productFluidTank, fluidCapacity);
        UpgradeHelper.syncCapacities(this);
    }

    private static void syncTank(JDTEFluidTank tank, int capacity) {
        if (tank instanceof FluidTankAccessor accessor) accessor.jdte$setCapacity(capacity);
    }

    @Override public boolean canRun() { return !itemHandler.getStackInSlot(SPECIMEN_SLOT).isEmpty(); }
    @Override public int getMaxEnergy() { return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.bioFactoryEnergyCapacity.get()); }
    @Override public int getStandardEnergyCost() { return JDTEConfig.COMMON.bioFactoryEnergyPerCycle.get(); }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public ContainerData getContainerData() { return poweredData; }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneData; }
    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public ItemStackHandler getMachineHandler() { return itemHandler; }
    public BioFactoryUpgradeItemStackHandler getUpgradeHandler() { return upgradeHandler; }
    public IItemHandler getAutomationItemHandler() { return automationItemHandler; }
    public IFluidHandler getCombinedFluidHandler() { return combinedFluidHandler; }
    public IFluidHandler getInputFluidHandler() { return inputFluidHandler; }
    public IFluidHandler getOutputFluidHandler() { return outputFluidHandler; }
    public JDTEFluidTank getLifeFluidTank() { return lifeFluidTank; }
    public JDTEFluidTank getTimeFluidTank() { return timeFluidTank; }
    public JDTEFluidTank getProcessFluidTank() { return processFluidTank; }
    public JDTEFluidTank getProductFluidTank() { return productFluidTank; }
    public ContainerData getMachineData() { return machineData; }

    private boolean isClientSide() { return level != null && level.isClientSide; }

    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", itemHandler.serializeNBT(provider));
        tag.put("upgrades", upgradeHandler.serializeNBT(provider));
        tag.put("lifeFluid", lifeFluidTank.serializeNBT(provider));
        tag.put("timeFluid", timeFluidTank.serializeNBT(provider));
        tag.put("processFluid", processFluidTank.serializeNBT(provider));
        tag.put("productFluid", productFluidTank.serializeNBT(provider));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("progress", progress);
        tag.putInt("multiplier", getMultiplier());
    }

    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
            if (itemHandler.getSlots() < TOTAL_SLOTS) {
                List<ItemStack> existing = new ArrayList<>(itemHandler.getSlots());
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    existing.add(itemHandler.getStackInSlot(slot).copy());
                }
                itemHandler.setSize(TOTAL_SLOTS);
                for (int slot = 0; slot < existing.size(); slot++) itemHandler.setStackInSlot(slot, existing.get(slot));
            }
        }
        if (tag.contains("upgrades")) upgradeHandler.deserializeNBT(provider, tag.getCompound("upgrades"));
        if (tag.contains("lifeFluid")) lifeFluidTank.deserializeNBT(provider, tag.getCompound("lifeFluid"));
        if (tag.contains("timeFluid")) timeFluidTank.deserializeNBT(provider, tag.getCompound("timeFluid"));
        if (tag.contains("processFluid")) processFluidTank.deserializeNBT(provider, tag.getCompound("processFluid"));
        if (tag.contains("productFluid")) productFluidTank.deserializeNBT(provider, tag.getCompound("productFluid"));
        energyStorage.setEnergy(tag.getInt("energy"));
        progress = tag.getInt("progress");
        multiplier = tag.contains("multiplier") ? tag.getInt("multiplier")
                : JDTEConfig.COMMON.bioFactoryDefaultSpeedMultiplier.get();
        clearRecipeCache();
    }

    private abstract class TankView implements IFluidHandler {
        protected abstract JDTEFluidTank[] tanks();
        @Override public int getTanks() { return tanks().length; }
        @Override public FluidStack getFluidInTank(int tank) { return valid(tank) ? tanks()[tank].getFluid() : FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return valid(tank) ? tanks()[tank].getCapacity() : 0; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return valid(tank) && tanks()[tank].isFluidValid(stack); }
        protected boolean valid(int tank) { return tank >= 0 && tank < tanks().length; }
    }

    private final class InputFluidHandler extends TankView {
        @Override protected JDTEFluidTank[] tanks() { return new JDTEFluidTank[]{lifeFluidTank, timeFluidTank, processFluidTank}; }
        @Override public int fill(FluidStack stack, FluidAction action) {
            for (JDTEFluidTank tank : tanks()) if (tank.isFluidValid(stack)) return tank.fill(stack, action);
            return 0;
        }
        @Override public FluidStack drain(FluidStack stack, FluidAction action) { return FluidStack.EMPTY; }
        @Override public FluidStack drain(int amount, FluidAction action) { return FluidStack.EMPTY; }
    }

    private final class OutputFluidHandler extends TankView {
        @Override protected JDTEFluidTank[] tanks() { return new JDTEFluidTank[]{productFluidTank}; }
        @Override public int fill(FluidStack stack, FluidAction action) { return 0; }
        @Override public FluidStack drain(FluidStack stack, FluidAction action) { return productFluidTank.drain(stack, action); }
        @Override public FluidStack drain(int amount, FluidAction action) { return productFluidTank.drain(amount, action); }
    }

    private final class CombinedFluidHandler extends TankView {
        @Override protected JDTEFluidTank[] tanks() { return new JDTEFluidTank[]{lifeFluidTank, timeFluidTank, processFluidTank, productFluidTank}; }
        @Override public int fill(FluidStack stack, FluidAction action) { return inputFluidHandler.fill(stack, action); }
        @Override public FluidStack drain(FluidStack stack, FluidAction action) { return outputFluidHandler.drain(stack, action); }
        @Override public FluidStack drain(int amount, FluidAction action) { return outputFluidHandler.drain(amount, action); }
    }
}
