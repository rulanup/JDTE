package com.jdte.common.blockentities;

import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.jdte.common.recipes.MachineRecipeHelper;
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
import com.jdte.common.utils.ContainerDataEncoding;
import com.jdte.mixin.FluidTankAccessor;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEFluids;
import com.jdte.setup.JDTERecipes;
import com.jdte.common.capabilities.MachineAutomationHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.bee.Bee;
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

import java.util.ArrayList;
import java.util.Arrays;
import com.jdte.common.manager.MachineOutputManager;
import java.util.List;

public class BioFactoryBE extends BaseMachineBE implements PoweredMachineBE, RedstoneControlledBE, ExtendedUpgradeMachine, CoalescedAcceleratedMachine {
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
    public static final int BASE_OUTPUT_STACK_LIMIT = 64;
    public static final int ULTIMATE_CAPACITY_STACK_LIMIT = 6_400_000;
    private static final int GLOBAL_WORK_RATE_MULTIPLIER = 5;
    private static final String REMEMBERED_RECIPE_ENERGY_TAG = "rememberedRecipeEnergy";
    private static final String PENDING_OUTPUT_TAG = "pendingOutput";

    private final BioFactoryEnergyCapacity energyCapacity = new BioFactoryEnergyCapacity();
    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(getMaxEnergy());
    private final PoweredMachineContainerData poweredData = new PoweredMachineContainerData(this);
    private final RedstoneControlData redstoneData = new RedstoneControlData();
    private final BioFactoryUpgradeItemStackHandler upgradeHandler = new BioFactoryUpgradeItemStackHandler(this);
    private final JDTEFluidTank lifeFluidTank = createTank(stack -> stack.is(JDTEFluids.LIFE_FLUID_SOURCE.get()), false);
    private final JDTEFluidTank timeFluidTank = createTank(stack -> stack.getFluid() instanceof TimeFluid, false);
    private final JDTEFluidTank processFluidTank = createTank(stack -> !stack.is(JDTEFluids.LIFE_FLUID_SOURCE.get())
            && !(stack.getFluid() instanceof TimeFluid), true);
    private final JDTEFluidTank productFluidTank = createTank(stack -> true, false);
    private final MachineInventoryHandler itemHandler = new MachineInventoryHandler(TOTAL_SLOTS) {
        @Override protected int getCapacity(int slot, ItemResource resource) {
            if (slot == SPECIMEN_SLOT) return 1;
            if (slot >= OUTPUT_START_SLOT && slot < OUTPUT_START_SLOT + OUTPUT_SLOTS) return getOutputSlotLimit();
            return super.getCapacity(slot, resource);
        }

        @Override public boolean isValid(int slot, ItemResource resource) {
            return resource.isEmpty() || slot != SPECIMEN_SLOT || isValidSpecimen(resource.toStack(1));
        }

        @Override protected void onContentsChanged(int slot, ItemStack previous) {
            if (slot == SPECIMEN_SLOT || isInputStorageSlot(slot)) {
                clearRecipeCache();
                progress = 0;
            }
            if (slot >= OUTPUT_START_SLOT && slot < OUTPUT_START_SLOT + OUTPUT_SLOTS) {
                MachineOutputManager.submit(BioFactoryBE.this, slot);
            }
            setChanged();
            // Coalesced: batched output inserts would otherwise send one block
            // update per stack; the flag flushes once at the next server tick.
            clientSyncPending = true;
        }
    };
    private final ResourceHandler<ItemResource> automationItemHandler = new MachineAutomationHandler<>(
            itemHandler, slot -> slot <= INPUT_SLOTS, slot -> slot > INPUT_SLOTS) {
        @Override public int size() { return 1 + INPUT_SLOTS + getActiveOutputSlots(); }
        @Override protected int storageSlot(int slot) {
            java.util.Objects.checkIndex(slot, size());
            if (slot == 0) return SPECIMEN_SLOT;
            if (slot <= INPUT_SLOTS) return inputStorageSlot(slot - 1);
            return OUTPUT_START_SLOT + slot - 1 - INPUT_SLOTS;
        }
    };
    private final ResourceHandler<FluidResource> inputFluidHandler = new MachineAutomationHandler<>(
            new CombinedResourceHandler<>(lifeFluidTank, timeFluidTank, processFluidTank), slot -> true, slot -> false);
    private final ResourceHandler<FluidResource> outputFluidHandler = new MachineAutomationHandler<>(
            productFluidTank, slot -> false, slot -> true);
    private final ResourceHandler<FluidResource> combinedFluidHandler = new CombinedResourceHandler<>(inputFluidHandler, outputFluidHandler);
    private final ContainerData machineData = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> isClientSide() ? syncedProcessTicks : getCurrentProcessTicks();
                case 2 -> isClientSide() ? syncedOutputSlots : getActiveOutputSlots();
                case 3 -> ContainerDataEncoding.low16(isClientSide() ? syncedLifeFluid : lifeFluidTank.getFluidAmount());
                case 4 -> ContainerDataEncoding.low16(isClientSide() ? syncedTimeFluid : timeFluidTank.getFluidAmount());
                case 5 -> ContainerDataEncoding.low16(isClientSide() ? syncedProcessFluid : processFluidTank.getFluidAmount());
                case 6 -> ContainerDataEncoding.low16(isClientSide() ? syncedProductFluid : productFluidTank.getFluidAmount());
                case 7 -> ContainerDataEncoding.low16(isClientSide() ? syncedFluidCapacity : getMaxFluidCapacity());
                case 8 -> isClientSide() ? syncedProductivity
                        : (int) Math.min(Integer.MAX_VALUE, Math.round(getProductivityMultiplier() * 100.0D));
                case 9 -> isClientSide() ? syncedMultiplier : getMultiplier();
                case 10 -> isClientSide() ? syncedMaxMultiplier : getMaxSelectableMultiplier();
                case 11 -> ContainerDataEncoding.high16(isClientSide() ? syncedLifeFluid : lifeFluidTank.getFluidAmount());
                case 12 -> ContainerDataEncoding.high16(isClientSide() ? syncedTimeFluid : timeFluidTank.getFluidAmount());
                case 13 -> ContainerDataEncoding.high16(isClientSide() ? syncedProcessFluid : processFluidTank.getFluidAmount());
                case 14 -> ContainerDataEncoding.high16(isClientSide() ? syncedProductFluid : productFluidTank.getFluidAmount());
                case 15 -> ContainerDataEncoding.high16(isClientSide() ? syncedFluidCapacity : getMaxFluidCapacity());
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> syncedProcessTicks = value;
                case 2 -> syncedOutputSlots = value;
                case 3 -> syncedLifeFluid = ContainerDataEncoding.withLow16(syncedLifeFluid, value);
                case 4 -> syncedTimeFluid = ContainerDataEncoding.withLow16(syncedTimeFluid, value);
                case 5 -> syncedProcessFluid = ContainerDataEncoding.withLow16(syncedProcessFluid, value);
                case 6 -> syncedProductFluid = ContainerDataEncoding.withLow16(syncedProductFluid, value);
                case 7 -> syncedFluidCapacity = ContainerDataEncoding.withLow16(syncedFluidCapacity, value);
                case 8 -> syncedProductivity = value;
                case 9 -> syncedMultiplier = value;
                case 10 -> syncedMaxMultiplier = value;
                case 11 -> syncedLifeFluid = ContainerDataEncoding.withHigh16(syncedLifeFluid, value);
                case 12 -> syncedTimeFluid = ContainerDataEncoding.withHigh16(syncedTimeFluid, value);
                case 13 -> syncedProcessFluid = ContainerDataEncoding.withHigh16(syncedProcessFluid, value);
                case 14 -> syncedProductFluid = ContainerDataEncoding.withHigh16(syncedProductFluid, value);
                case 15 -> syncedFluidCapacity = ContainerDataEncoding.withHigh16(syncedFluidCapacity, value);
                default -> { }
            }
        }
        @Override public int getCount() { return 16; }
    };

    private int progress;
    private int settlementTicker;
    private boolean clientSyncPending;
    private int syncedProcessTicks = 600;
    private int syncedOutputSlots = BASE_OUTPUT_SLOTS;
    private int syncedLifeFluid;
    private int syncedTimeFluid;
    private int syncedProcessFluid;
    private int syncedProductFluid;
    private int syncedFluidCapacity;
    private int syncedProductivity = 100;
    private int multiplier;
    private BioFactoryPendingOutput pendingOutput;
    private int syncedMultiplier = 1;
    private int syncedMaxMultiplier = 32;
    private BioFactoryRecipe cachedRecipe;
    private Bee cachedBee;
    private ItemStack cachedSpecimen = ItemStack.EMPTY;
    private final ItemStack[] cachedInputs = new ItemStack[INPUT_SLOTS];
    private int[] cachedRecipeInputSlots;
    private Fluid cachedProcessFluid;
    private int cachedProcessFluidAmount;
    private ItemStack cachedBeeFood = ItemStack.EMPTY;
    private boolean cachedBeeUsesItemFood;
    private boolean cacheResolved;
    private long environmentCacheWindow = Long.MIN_VALUE;
    private boolean cachedBeeCanOperate = true;
    private long activeOutputSlotsTick = Long.MIN_VALUE;
    private int cachedActiveOutputSlots = BASE_OUTPUT_SLOTS;
    private long outputSlotLimitTick = Long.MIN_VALUE;
    private int cachedOutputSlotLimit = BASE_OUTPUT_STACK_LIMIT;

    public BioFactoryBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.BIO_FACTORY.get(), pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        tickSpeed = 1;
        multiplier = JDTEConfig.COMMON.bioFactoryDefaultSpeedMultiplier.get();
        Arrays.fill(cachedInputs, ItemStack.EMPTY);
    }

    @Override public void tickServer() {
        super.tickServer();
        if (clientSyncPending) {
            clientSyncPending = false;
            markDirtyClient();
        }
        if (level != null && level.getGameTime() % 20L == 0L) syncCapacities();
        if (!isActiveRedstone() || !canRun() || !resolveRecipe()
                || cachedBee != null && !canCachedBeeOperate()) {
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
        if (ticks <= 0 || !isActiveRedstone() || !canRun() || !resolveRecipe()
                || cachedBee != null && !canCachedBeeOperate()) {
            return;
        }
        processAcceleratedTicks(ticks);
    }

    protected void processAcceleratedTicks(int ticks) {
        int speed = getSpeedMultiplier();
        long addedProgress = (long) ticks * speed;
        long totalProgress = (long) progress + addedProgress;
        int processTicks = Math.max(1, getCurrentProcessTicks());
        long cycles = totalProgress / processTicks;
        progress = (int) (totalProgress % processTicks);
        if (cycles <= 0) {
            setChanged();
            return;
        }

        int maxCycles = (int) Math.min(cycles, 128);
        for (int i = 0; i < maxCycles; i++) {
            completeCycle();
            if (pendingOutput != null) {
                break;
            }
        }
        setChanged();
    }

    private boolean resolveRecipe() {
        ItemStack specimen = itemHandler.copyStack(SPECIMEN_SLOT);
        Fluid fluid = processFluidTank.getFluid().getFluid();
        if (cacheResolved) {
            return cachedRecipe != null || cachedBee != null;
        }
        clearRecipeCache();
        cachedSpecimen = specimen.copy();
        for (int i = 0; i < INPUT_SLOTS; i++) cachedInputs[i] = itemHandler.copyStack(inputStorageSlot(i));
        cachedProcessFluid = fluid;
        cachedProcessFluidAmount = processFluidTank.getFluidAmount();
        cacheResolved = true;
        if (level == null || specimen.isEmpty()) return false;
        List<ItemStack> inputs = getInputStacks();
        cachedRecipe = MachineRecipeHelper.recipes(level).byType(JDTERecipes.BIO_FACTORY_RECIPE_TYPE.get()).stream()
                .map(holder -> holder.value())
                .filter(recipe -> {
                    int[] assignment = recipe.findMatchingSlots(specimen, inputs);
                    if (assignment == null) return false;
                    cachedRecipeInputSlots = assignment;
                    return true;
                })
                .filter(this::hasRecipeProcessFluid)
                .findFirst().orElse(null);
        if (cachedRecipe != null) {
            rememberRecipeEnergy(cachedRecipe.energy());
            return true;
        }
        if (!ModList.get().isLoaded("productivebees")) return false;
        cachedBee = ProductiveBeesBioFactoryIntegration.createBee(specimen, level);
        if (cachedBee == null) return false;
        ItemStack bucket = processFluidTank.getFluid().isEmpty() ? ItemStack.EMPTY
                : new ItemStack(processFluidTank.getFluid().getFluid().getBucket());
        cachedBeeFood = inputs.stream()
                .filter(stack -> ProductiveBeesBioFactoryIntegration.isValidFood(cachedBee, stack, ItemStack.EMPTY))
                .findFirst().map(ItemStack::copy).orElse(ItemStack.EMPTY);
        cachedBeeUsesItemFood = !cachedBeeFood.isEmpty();
        if (!cachedBeeUsesItemFood && (!ProductiveBeesBioFactoryIntegration.isValidFood(
                cachedBee, ItemStack.EMPTY, bucket)
                || processFluidTank.getFluidAmount() < getRequiredBeeFlowerFluidAmount())) {
            cachedBee = null;
        }
        return cachedBee != null;
    }

    private boolean hasRecipeProcessFluid(BioFactoryRecipe recipe) {
        if (recipe.processFluid().isEmpty() || recipe.processFluidAmount() <= 0) return true;
        Fluid expected = BuiltInRegistries.FLUID.getValue(recipe.processFluid().get());
        return !processFluidTank.getFluid().isEmpty() && processFluidTank.getFluid().is(expected)
                && processFluidTank.getFluidAmount() >= recipe.processFluidAmount();
    }

    private void completeCycle() {
        BioFactoryRecipe completedRecipe = cachedRecipe;
        int[] completedInputSlots = cachedRecipeInputSlots == null ? null : cachedRecipeInputSlots.clone();
        boolean creative = UpgradeHelper.hasCreativeUpgrade(this);
        int energyCost = getEffectiveCycleEnergyCost(completedRecipe);
        int timeCost = creative ? 0 : getEffectiveTimeFluidCost();
        boolean timeBoost = creative || timeFluidTank.getFluidAmount() >= timeCost;
        int lifeCost = creative ? 0 : getEffectiveLifeFluidCost();
        boolean lifeBoost = creative || lifeFluidTank.getFluidAmount() >= lifeCost;
        int processCost = getProcessFluidCost();
        if (pendingOutput != null) {
            energyCost = pendingOutput.energyCost();
            timeCost = pendingOutput.timeCost();
            lifeCost = pendingOutput.lifeCost();
            processCost = pendingOutput.processCost();
            creative = pendingOutput.creative();
            completedInputSlots = pendingOutput.inputSlots();
        }
        if (pendingOutput == null && !creative && (!hasEnoughPower(energyCost)
                || timeBoost && timeFluidTank.getFluidAmount() < timeCost
                || lifeBoost && lifeFluidTank.getFluidAmount() < lifeCost
                || processFluidTank.getFluidAmount() < processCost)) return;

        int[] completedInputCounts = pendingOutput == null ? null : pendingOutput.inputCounts();
        if (pendingOutput == null) {
            double outputMultiplier = getProductivityMultiplier()
                    * (lifeBoost ? JDTEConfig.COMMON.bioFactoryLifeYieldMultiplier.get() : 1.0D);
            int[] inputCounts = completedRecipe == null ? null : completedRecipe.inputs().stream()
                    .mapToInt(input -> Math.max(0, input.count())).toArray();
            pendingOutput = BioFactoryPendingOutput.of(createOutputs(outputMultiplier), createFluidOutput(outputMultiplier),
                    energyCost, timeBoost ? timeCost : 0, lifeBoost ? lifeCost : 0, processCost, creative,
                    completedInputSlots, inputCounts);
            completedInputCounts = inputCounts;
            setChanged();
            if (pendingOutput.items().isEmpty() && pendingOutput.fluid().isEmpty()) {
                pendingOutput = null;
                progress = 0;
                return;
            }
        }
        try (Transaction tx = Transaction.openRoot()) {
            FluidStack fluid = pendingOutput.fluid();
            if (!fluid.isEmpty() && productFluidTank.insert(FluidResource.of(fluid), fluid.getAmount(), tx) != fluid.getAmount()) return;
            if (!itemHandler.insertBatch(OUTPUT_START_SLOT, OUTPUT_START_SLOT + getActiveOutputSlots(), pendingOutput.items(), tx)) return;
            tx.commit();
        }
        pendingOutput = null;
        if (!creative) {
            extractEnergy(energyCost, false);
            if (timeCost > 0) timeFluidTank.extractFluid(timeCost);
            if (lifeCost > 0) lifeFluidTank.extractFluid(lifeCost);
            if (processCost > 0) processFluidTank.extractFluid(processCost);
            if (completedInputSlots != null) {
                int[] inputCounts = completedInputCounts;
                if (inputCounts != null) {
                    for (int input = 0; input < Math.min(completedInputSlots.length, inputCounts.length); input++) {
                        if (inputCounts[input] > 0) {
                            consumeInput(completedInputSlots[input], inputCounts[input]);
                        }
                    }
                } else if (completedRecipe != null) {
                    for (int input = 0; input < completedRecipe.inputs().size(); input++) {
                        int count = completedRecipe.inputs().get(input).count();
                        if (count > 0) consumeInput(completedInputSlots[input], count);
                    }
                }
            }
        }
        progress = 0;
        setChanged();
        clientSyncPending = true;
    }

    private List<ItemStack> createOutputs(double multiplier) {
        if (cachedRecipe != null) {
            List<ItemStack> base = cachedRecipe.outputs().stream().map(output -> output.roll(level.getRandom(), multiplier))
                    .filter(stack -> !stack.isEmpty()).map(ItemStack::copy)
                    .map(this::applyRecipeOutputComponents).toList();
            if (base.isEmpty() || upgradeHandler.getLootingCount() <= 0) return base;
            List<ItemStack> result = new ArrayList<>(base);
            double chance = JDTEConfig.COMMON.lootingExtraDropChance.get();
            for (int level = 0; level < upgradeHandler.getLootingCount(); level++) {
                if (this.level.getRandom().nextDouble() < chance) base.forEach(stack -> result.add(stack.copy()));
            }
            return result;
        }
        boolean blockOutput = upgradeHandler.countProductivityTier(4) > 0;
        return ProductiveBeesBioFactoryIntegration.produce(
                        level, cachedBee, cachedBeeFood, worldPosition, blockOutput, multiplier).stream()
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
            level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.INSTRUMENT)
                    .get(InstrumentTags.GOAT_HORNS).flatMap(options -> options.getRandomElement(level.getRandom()))
                    .ifPresent(instrument -> stack.set(DataComponents.INSTRUMENT,
                            new net.minecraft.world.item.component.InstrumentComponent(instrument)));
        } else if (stack.is(Items.WHITE_WOOL)) {
            for (ItemStack input : getInputStacks()) {
                var dye = input.get(DataComponents.DYE);
                if (dye != null) {
                    var item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(
                            "minecraft", dye.getName() + "_wool"));
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
        Fluid fluid = BuiltInRegistries.FLUID.getValue(cachedRecipe.outputFluid().get());
        int amount = (int) Math.min(Integer.MAX_VALUE, Math.round(cachedRecipe.outputFluidAmount() * multiplier));
        return amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    private int getProcessFluidCost() {
        return cachedRecipe == null ? 0 : Math.max(0, cachedRecipe.processFluidAmount());
    }

    private int getRequiredBeeFlowerFluidAmount() {
        return JDTEConfig.COMMON.bioFactoryProcessFluidPerCycle.get();
    }

    private void consumeInput(int logicalSlot, int amount) {
        int slot = inputStorageSlot(logicalSlot);
        int remaining = Math.max(0, itemHandler.getAmountAsInt(slot) - amount);
        itemHandler.set(slot, itemHandler.getResource(slot), remaining);
    }

    private int getSpeedMultiplier() {
        boolean creative = UpgradeHelper.countUpgrades(this, UpgradeType.CREATIVE) > 0;
        int selectedMultiplier = getMultiplier();
        int acceleratedMultiplier = creative || timeFluidTank.getFluidAmount() >= getEffectiveTimeFluidCost()
                ? selectedMultiplier
                : 1;
        return Math.multiplyExact(acceleratedMultiplier, GLOBAL_WORK_RATE_MULTIPLIER);
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
        if (isClientSide()) {
            return Math.max(1, syncedMultiplier);
        }
        if (UpgradeHelper.hasUltimateOverclock(this)) {
            return JDTEConfig.COMMON.bioFactoryOverclockMaxSpeedMultiplier.get() * 10;
        }
        if (UpgradeHelper.hasOverclock(this)) {
            return JDTEConfig.COMMON.bioFactoryOverclockMaxSpeedMultiplier.get();
        }
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
        if (isClientSide()) {
            return Math.max(1, syncedMaxMultiplier);
        }
        if (UpgradeHelper.hasUltimateOverclock(this)) {
            return JDTEConfig.COMMON.bioFactoryOverclockMaxSpeedMultiplier.get() * 10;
        }
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
        if (level != null && !level.isClientSide() && cachedRecipe != null) return Math.max(1, cachedRecipe.processTicks());
        if (level != null && level.isClientSide()) return Math.max(1, syncedProcessTicks);
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
            private int lastSyncedAmount;
            @Override protected void onContentsChanged() {
                super.onContentsChanged();
                Fluid current = getFluid().getFluid();
                int amount = getFluidAmount();
                if (clearsCache && (current != lastSyncedFluid || amount != lastSyncedAmount)) {
                    clearRecipeCache();
                    progress = 0;
                }
                setChanged();
                if (current != lastSyncedFluid) {
                    lastSyncedFluid = current;
                    clientSyncPending = true;
                }
                lastSyncedAmount = amount;
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
        cachedProcessFluidAmount = 0;
        cachedBeeFood = ItemStack.EMPTY;
        cachedBeeUsesItemFood = false;
        environmentCacheWindow = Long.MIN_VALUE;
    }

    private List<ItemStack> getInputStacks() {
        List<ItemStack> inputs = new ArrayList<>(INPUT_SLOTS);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) inputs.add(itemHandler.copyStack(inputStorageSlot(slot)));
        return inputs;
    }

    private boolean inputsMatchCache() {
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack input = itemHandler.copyStack(inputStorageSlot(i));
            if (!ItemStack.isSameItemSameComponents(input, cachedInputs[i])
                    || input.getCount() != cachedInputs[i].getCount()) return false;
        }
        return true;
    }

    private boolean canCachedBeeOperate() {
        if (cachedBee == null || level == null) return true;
        long window = level.getGameTime() / 20L;
        if (window != environmentCacheWindow) {
            environmentCacheWindow = window;
            cachedBeeCanOperate = ProductiveBeesBioFactoryIntegration.canOperate(level, cachedBee);
        }
        return cachedBeeCanOperate;
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
        if (level != null && level.isClientSide()) return Math.max(BASE_OUTPUT_SLOTS, syncedOutputSlots);
        long gameTick = level == null ? Long.MIN_VALUE : level.getGameTime();
        if (activeOutputSlotsTick == gameTick) return cachedActiveOutputSlots;

        int configured = UpgradeHelper.hasUltimateCapacity(this)
                ? OUTPUT_SLOTS
                : BASE_OUTPUT_SLOTS + UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY) * OUTPUT_SLOTS_PER_CAPACITY;
        int occupied = BASE_OUTPUT_SLOTS;
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            if (!itemHandler.copyStack(OUTPUT_START_SLOT + i).isEmpty()) occupied = ((i / 8) + 1) * 8;
        }
        cachedActiveOutputSlots = Math.min(OUTPUT_SLOTS, Math.max(configured, occupied));
        activeOutputSlotsTick = gameTick;
        return cachedActiveOutputSlots;
    }

    public int getOutputSlotLimit() {
        long gameTick = level == null ? Long.MIN_VALUE : level.getGameTime();
        if (gameTick != Long.MIN_VALUE && outputSlotLimitTick == gameTick) return cachedOutputSlotLimit;
        if (UpgradeHelper.hasUltimateCapacity(this)) {
            cachedOutputSlotLimit = ULTIMATE_CAPACITY_STACK_LIMIT;
            outputSlotLimitTick = gameTick;
            return cachedOutputSlotLimit;
        }
        cachedOutputSlotLimit = BASE_OUTPUT_STACK_LIMIT;
        outputSlotLimitTick = gameTick;
        return cachedOutputSlotLimit;
    }

    public int getMaxFluidCapacity() {
        return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.bioFactoryFluidCapacity.get());
    }

    private void syncCapacities() {
        outputSlotLimitTick = Long.MIN_VALUE;
        activeOutputSlotsTick = Long.MIN_VALUE;
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

    @Override public boolean canRun() { return !itemHandler.copyStack(SPECIMEN_SLOT).isEmpty(); }
    @Override public int getMaxEnergy() {
        int configuredCapacity = UpgradeHelper.adjustEnergyCapacity(this,
                JDTEConfig.COMMON.bioFactoryEnergyCapacity.get());
        return energyCapacity.resolve(configuredCapacity,
                JDTEConfig.COMMON.bioFactoryEnergyPerCycle.get(),
                baseCost -> UpgradeHelper.adjustEnergyCost(this, baseCost));
    }
    private int getEffectiveCycleEnergyCost(BioFactoryRecipe recipe) {
        int baseCost = recipe != null ? Math.max(0, recipe.energy())
                : JDTEConfig.COMMON.bioFactoryEnergyPerCycle.get();
        return UpgradeHelper.adjustEnergyCost(this, baseCost);
    }
    private void rememberRecipeEnergy(int recipeEnergy) {
        if (!energyCapacity.rememberRecipeEnergy(recipeEnergy)) return;
        syncCapacities();
        setChanged();
        markDirtyClient();
    }
    @Override public int getStandardEnergyCost() { return JDTEConfig.COMMON.bioFactoryEnergyPerCycle.get(); }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public ContainerData getContainerData() { return poweredData; }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneData; }
    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public ItemStacksResourceHandler getMachineHandler() { return itemHandler; }
    public BioFactoryUpgradeItemStackHandler getUpgradeHandler() { return upgradeHandler; }
    public ResourceHandler<ItemResource> getAutomationItemHandler() { return automationItemHandler; }
    public ResourceHandler<FluidResource> getCombinedFluidHandler() { return combinedFluidHandler; }
    public ResourceHandler<FluidResource> getInputFluidHandler() { return inputFluidHandler; }
    public ResourceHandler<FluidResource> getOutputFluidHandler() { return outputFluidHandler; }
    public JDTEFluidTank getLifeFluidTank() { return lifeFluidTank; }
    public JDTEFluidTank getTimeFluidTank() { return timeFluidTank; }
    public JDTEFluidTank getProcessFluidTank() { return processFluidTank; }
    public JDTEFluidTank getProductFluidTank() { return productFluidTank; }
    public ContainerData getMachineData() { return machineData; }

    private boolean isClientSide() { return level != null && level.isClientSide(); }

    @Override public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        OversizedItemStackHandlerSerialization.serialize(itemHandler, tag.child("inventory"));
        tag.putChild("upgrades", upgradeHandler);
        tag.putChild("lifeFluid", lifeFluidTank);
        tag.putChild("timeFluid", timeFluidTank);
        tag.putChild("processFluid", processFluidTank);
        tag.putChild("productFluid", productFluidTank);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("progress", progress);
        tag.putInt("multiplier", getMultiplier());
        tag.putInt(REMEMBERED_RECIPE_ENERGY_TAG, energyCapacity.rememberedRecipeEnergy());
        if (pendingOutput != null) pendingOutput.save(tag.child(PENDING_OUTPUT_TAG));
    }

    @Override public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        tag.child("inventory").ifPresent(itemHandler::deserialize);
        tag.readChild("upgrades", upgradeHandler);
        tag.readChild("lifeFluid", lifeFluidTank);
        tag.readChild("timeFluid", timeFluidTank);
        tag.readChild("processFluid", processFluidTank);
        tag.readChild("productFluid", productFluidTank);
        energyCapacity.restoreRememberedRecipeEnergy(tag.getIntOr(REMEMBERED_RECIPE_ENERGY_TAG, 0));
        syncCapacities();
        energyStorage.setEnergy(tag.getIntOr("energy", 0));
        progress = tag.getIntOr("progress", 0);
        multiplier = tag.getIntOr("multiplier", JDTEConfig.COMMON.bioFactoryDefaultSpeedMultiplier.get());
        pendingOutput = tag.child(PENDING_OUTPUT_TAG).map(BioFactoryPendingOutput::load).orElse(null);
        clearRecipeCache();
    }

}
