package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.blocks.LargeGreenhouseBlock;
import com.jdte.common.blocks.LargeGreenhouseStructure;
import com.jdte.common.utils.ContainerDataEncoding;
import com.jdte.common.recipes.GreenhouseCropDefinition;
import com.jdte.common.recipes.GreenhouseCropResolver;
import com.jdte.common.greenhouse.GreenhouseMatrixMember;
import com.jdte.common.greenhouse.GreenhouseMatrixMemberState;
import com.jdte.common.greenhouse.GreenhouseMatrixProductionProfile;
import com.jdte.common.greenhouse.GreenhouseMatrixRuntime;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

public class LargeGreenhouseBE extends BaseMachineBE implements PoweredMachineBE, FluidMachineBE,
        RedstoneControlledBE, ExtendedUpgradeMachine, CoalescedAcceleratedMachine, GreenhouseMatrixMember {
    public static final int INPUT_SLOTS = 12;
    public static final int OUTPUT_START_SLOT = INPUT_SLOTS;
    public static final int OUTPUT_SLOTS = 64;
    public static final int BASE_OUTPUT_SLOTS = 16;
    public static final int OUTPUT_SLOTS_PER_CAPACITY = 16;
    public static final int BASE_OUTPUT_STACK_LIMIT = 64;
    public static final int FIRST_CAPACITY_STACK_LIMIT = 2048;
    public static final int UPGRADE_SLOTS = 8;
    public static final int TOTAL_SLOTS = INPUT_SLOTS + OUTPUT_SLOTS;
    private static final int LEGACY_INPUT_SLOTS = 9;
    private static final int LEGACY_TOTAL_SLOTS = LEGACY_INPUT_SLOTS + OUTPUT_SLOTS;
    public static final int STRUCTURE_WORK_MULTIPLIER = 9;
    public static final int FLUID_EFFICIENCY_MULTIPLIER = 9;
    private static final int LOOT_SAMPLES_PER_SETTLEMENT = 4;

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(getMaxEnergy());
    private final PoweredMachineContainerData poweredData = new PoweredMachineContainerData(this);
    private final JDTEFluidTank fluidTank = new JDTEFluidTank(getMaxMB(), stack -> stack.getFluid() instanceof TimeFluid);
    private final FluidContainerData fluidData = new FluidContainerData(this);
    private final RedstoneControlData redstoneData = new RedstoneControlData();
    private final ItemStack[] cachedSeeds = new ItemStack[INPUT_SLOTS];
    private final GreenhouseCropDefinition[] cachedDefinitions = new GreenhouseCropDefinition[INPUT_SLOTS];
    private final ResourceLocation[] displayBlockIds = new ResourceLocation[INPUT_SLOTS];
    private final ResourceLocation[] cachedDisplayIds = new ResourceLocation[INPUT_SLOTS];
    private final BlockState[][] displayAgeStates = new BlockState[INPUT_SLOTS][];
    private final long[] growthWork = new long[INPUT_SLOTS];
    private final BitSet batchedOutputChanges = new BitSet(TOTAL_SLOTS);
    private long cachedRecipeGeneration = -1L;
    private boolean batchingOutputChanges;
    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            if (slot >= OUTPUT_START_SLOT) return getOutputSlotLimit();
            return super.getSlotLimit(slot);
        }

        @Override
        public int getStackLimit(int slot, ItemStack stack) {
            if (slot >= OUTPUT_START_SLOT) return getOutputSlotLimit();
            return super.getStackLimit(slot, stack);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= OUTPUT_START_SLOT || slot >= 0 && GreenhouseCropResolver.find(level, stack) != null;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (slot >= 0 && slot < INPUT_SLOTS) {
                clearCachedDefinition(slot);
                growthWork[slot] = 0;
                markDirtyClient();
            }
            if (slot >= OUTPUT_START_SLOT) {
                if (batchingOutputChanges) batchedOutputChanges.set(slot);
                else MachineOutputManager.submit(LargeGreenhouseBE.this, slot);
            }
            if (!batchingOutputChanges || slot < OUTPUT_START_SLOT) setChanged();
        }
    };
    private final IItemHandler automationItemHandler = new IItemHandler() {
        @Override public int getSlots() { return INPUT_SLOTS + getActiveOutputSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return valid(slot) ? itemHandler.getStackInSlot(slot) : ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot >= 0 && slot < INPUT_SLOTS ? itemHandler.insertItem(slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= OUTPUT_START_SLOT && slot < getSlots()
                    ? itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return valid(slot) ? itemHandler.getSlotLimit(slot) : 0; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < INPUT_SLOTS && itemHandler.isItemValid(slot, stack);
        }
        private boolean valid(int slot) { return slot >= 0 && slot < getSlots(); }
    };
    private final IItemHandler internalOutputHandler = new IItemHandler() {
        @Override public int getSlots() { return getActiveOutputSlots(); }
        @Override public ItemStack getStackInSlot(int slot) {
            return valid(slot) ? itemHandler.getStackInSlot(OUTPUT_START_SLOT + slot) : ItemStack.EMPTY;
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return valid(slot) ? itemHandler.insertItem(OUTPUT_START_SLOT + slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return valid(slot) ? itemHandler.extractItem(OUTPUT_START_SLOT + slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) {
            return valid(slot) ? itemHandler.getSlotLimit(OUTPUT_START_SLOT + slot) : 0;
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return valid(slot) && itemHandler.isItemValid(OUTPUT_START_SLOT + slot, stack);
        }
        private boolean valid(int slot) { return slot >= 0 && slot < getSlots(); }
    };
    private final ContainerData greenhouseData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> isClientSide() ? syncedProgress : Math.min(settlementTicker,
                        JDTEConfig.COMMON.greenhouseSettlementInterval.get());
                case 1 -> isClientSide() ? syncedProgressMax : JDTEConfig.COMMON.greenhouseSettlementInterval.get();
                case 2 -> isClientSide() ? syncedActiveOutputSlots : getActiveOutputSlots();
                case 3 -> ContainerDataEncoding.low16(isClientSide() ? syncedFluidAmount : fluidTank.getFluidAmount());
                case 4 -> ContainerDataEncoding.high16(isClientSide() ? syncedFluidAmount : fluidTank.getFluidAmount());
                case 5 -> ContainerDataEncoding.low16(isClientSide() ? syncedFluidCapacity : getMaxMB());
                case 6 -> ContainerDataEncoding.high16(isClientSide() ? syncedFluidCapacity : getMaxMB());
                case 7 -> isClientSide() ? syncedMultiplier : getMultiplier();
                case 8 -> isClientSide() ? syncedMaxMultiplier : getMaxSelectableMultiplier();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> syncedProgress = value;
                case 1 -> syncedProgressMax = value;
                case 2 -> syncedActiveOutputSlots = value;
                case 3 -> syncedFluidAmount = ContainerDataEncoding.withLow16(syncedFluidAmount, value);
                case 4 -> syncedFluidAmount = ContainerDataEncoding.withHigh16(syncedFluidAmount, value);
                case 5 -> syncedFluidCapacity = ContainerDataEncoding.withLow16(syncedFluidCapacity, value);
                case 6 -> syncedFluidCapacity = ContainerDataEncoding.withHigh16(syncedFluidCapacity, value);
                case 7 -> syncedMultiplier = value;
                case 8 -> syncedMaxMultiplier = value;
                default -> { }
            }
        }

        @Override public int getCount() { return 9; }
    };

    private int settlementTicker;
    private int nextInputSlot;
    private int activeMask;
    private int syncedProgress;
    private int syncedProgressMax = 1;
    private int syncedActiveOutputSlots = BASE_OUTPUT_SLOTS;
    private int syncedFluidAmount;
    private int syncedFluidCapacity;
    private int syncedMultiplier;
    private int syncedMaxMultiplier = 32;
    private int multiplier;
    private Direction cachedBoundaryFacing;
    private List<LargeGreenhouseStructure.BoundaryNeighbor> cachedBoundaryNeighbors = List.of();
    private long activeOutputSlotsTick = Long.MIN_VALUE;
    private int cachedActiveOutputSlots = BASE_OUTPUT_SLOTS;
    private long outputSlotLimitTick = Long.MIN_VALUE;
    private int cachedOutputSlotLimit = BASE_OUTPUT_STACK_LIMIT;
    private int accumulatedAcceleratedTicks;
    private long lastSettlementGameTime = Long.MIN_VALUE;
    private final GreenhouseMatrixMemberState matrixMemberState = new GreenhouseMatrixMemberState();

    public LargeGreenhouseBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.LARGE_GREENHOUSE.get(), pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        tickSpeed = 1;
        multiplier = JDTEConfig.COMMON.greenhouseDefaultSpeedMultiplier.get();
        Arrays.fill(cachedSeeds, ItemStack.EMPTY);
    }

    @Override
    public void tickServer() {
        if (isMatrixManaged()) {
            setActiveMask(0);
            return;
        }
        super.tickServer();
        if (level != null && level.getGameTime() % 20L == 0L) UpgradeHelper.syncCapacities(this);
        if (UpgradeHelper.hasEssenceConversionUpgrade(this) && level instanceof ServerLevel serverLevel
                && level.getGameTime() % 20L == 0L) {
            GreenhouseEssenceConversionHelper.convertStored(serverLevel, internalOutputHandler);
        }
        advanceProductionTicks(1);
    }

    @Override
    public void accumulateAcceleratedTicks(int ticks) {
        if (isMatrixManaged()) return;
        accumulatedAcceleratedTicks = saturatingAdd(accumulatedAcceleratedTicks, ticks);
    }

    @Override
    public void flushAcceleratedTicks() {
        if (isMatrixManaged()) {
            accumulatedAcceleratedTicks = 0;
            return;
        }
        int ticks = accumulatedAcceleratedTicks;
        accumulatedAcceleratedTicks = 0;
        advanceProductionTicks(ticks);
    }

    @Override
    public boolean claimMatrix(BlockPos controller) {
        boolean claimed = matrixMemberState.claim(controller);
        if (claimed) {
            AEOutputManager.suspend(this);
            accumulatedAcceleratedTicks = 0;
            setActiveMask(0);
        }
        return claimed;
    }

    @Override public boolean releaseMatrix(BlockPos controller) {
        boolean released = matrixMemberState.release(controller);
        if (released && UpgradeHelper.hasAEOutputUpgrade(this)) AEOutputManager.refresh(this);
        return released;
    }
    @Override public boolean isMatrixManaged() { return matrixMemberState.managed(); }

    @Override
    public List<GreenhouseMatrixProductionProfile> captureMatrixProfiles(ServerLevel serverLevel,
                                                                         GreenhouseMatrixRuntime.Effects effects) {
        if (!isActiveRedstone() || !canRun()) return List.of();
        List<GreenhouseMatrixProductionProfile> profiles = new ArrayList<>();
        long recipeGeneration = GreenhouseCropResolver.cacheGeneration();
        boolean creative = UpgradeHelper.hasCreativeUpgrade(this);
        boolean overclocked = UpgradeHelper.hasOverclock(this);
        int fortune = Math.min(3, UpgradeHelper.countUpgrades(this, UpgradeType.FORTUNE));
        int energyCost = creative ? 0 : applyMatrixEfficiency(
                JDTEConfig.COMMON.greenhouseEnergyPerHarvestV2.get(), effects);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            ItemStack seed = getLocalSeed(slot);
            GreenhouseCropDefinition definition = GreenhouseCropResolver.find(serverLevel, seed);
            if (seed.isEmpty() || definition == null) continue;
            int fullFluidCost = getEffectiveFluidPerHarvest(slot, definition);
            int fluidCost = creative ? 0 : (fullFluidCost + FLUID_EFFICIENCY_MULTIPLIER - 1)
                    / FLUID_EFFICIENCY_MULTIPLIER;
            long work = GreenhouseMatrixProductionProfile.workPerTick(
                    JDTEConfig.COMMON.greenhouseBaseMultiplier.get(), getMultiplier(), seed.getCount(),
                    STRUCTURE_WORK_MULTIPLIER, effects == null ? 0 : effects.speedPercent());
            profiles.add(new GreenhouseMatrixProductionProfile(
                    GreenhouseMatrixProductionProfile.MachineKind.LARGE, seed, seed.getCount(),
                    GreenhouseMatrixProductionProfile.definitionKey(definition), recipeGeneration,
                    getMultiplier(), STRUCTURE_WORK_MULTIPLIER, fortune, creative, overclocked,
                    energyCost, fluidCost, effects == null ? 0 : effects.speed(),
                    effects == null ? 0 : effects.efficiency(),
                    UpgradeHelper.hasSeedConversionUpgrade(this)
                            || effects != null && effects.seedConversion() > 0,
                    UpgradeHelper.hasEssenceConversionUpgrade(this)
                            || effects != null && effects.essenceConversion() > 0,
                    definition.growthWork(), work, definition, worldPosition));
        }
        return profiles;
    }

    @Override public IFluidHandler matrixFluidStorage() { return fluidTank; }
    @Override public MachineEnergyStorage matrixEnergyStorage() { return energyStorage; }
    @Override public long matrixOutputCapacity() { return (long) getActiveOutputSlots() * getOutputSlotLimit(); }

    private static int applyMatrixEfficiency(int cost, GreenhouseMatrixRuntime.Effects effects) {
        if (effects == null || !effects.enabled()) return cost;
        return Math.max(cost > 0 ? 1 : 0, cost * (100 - effects.efficiencyPercent()) / 100);
    }

    private void advanceProductionTicks(int ticks) {
        if (ticks <= 0) return;
        if (com.jdte.common.greenhouse.GreenhouseMatrixRuntime.isDisabled(this)) {
            setActiveMask(0);
            return;
        }
        if (!isActiveRedstone() || !canRun()) {
            setActiveMask(0);
            settlementTicker = 0;
            return;
        }

        int interval = JDTEConfig.COMMON.greenhouseSettlementInterval.get();
        settlementTicker = saturatingAdd(settlementTicker, ticks);
        if (settlementTicker < interval || level == null || lastSettlementGameTime == level.getGameTime()) return;
        int completedSettlements = Math.max(1, settlementTicker / interval);
        int elapsed = completedSettlements * interval;
        settlementTicker -= elapsed;
        lastSettlementGameTime = level.getGameTime();

        GreenhouseCropDefinition[] definitions = resolveDefinitions(INPUT_SLOTS);
        int defined = countDefined(definitions, INPUT_SLOTS);
        if (defined == 0) {
            setActiveMask(0);
            return;
        }
        settleProduction(definitions, elapsed, completedSettlements);
    }

    private void settleProduction(GreenhouseCropDefinition[] definitions, int elapsedTicks,
                                  int completedSettlements) {
        // 每个输入槽都是独立生产线；结构倍率同时放大每条生产线的结算预算，
        // 避免多个有效槽位共享固定总预算而无法叠加产量。
        int maxHarvests = saturatingMultiply(
                saturatingMultiply(JDTEConfig.COMMON.greenhouseMaxHarvestsPerSettlementV2.get(),
                        STRUCTURE_WORK_MULTIPLIER), completedSettlements);
        ProductionSettings settings = currentProductionSettings();
        int newActiveMask = 0;
        int[] dynamicHarvestBudget = {JDTEConfig.COMMON.greenhouseDynamicHarvestCallsPerTick.get()};

        beginOutputChangeBatch();
        try {
            GreenhouseCapacityLedger capacityLedger = GreenhouseCapacityLedger.capture(internalOutputHandler);
            for (int offset = 0; offset < INPUT_SLOTS; offset++) {
                int slot = (nextInputSlot + offset) % INPUT_SLOTS;
                GreenhouseCropDefinition definition = definitions[slot];
                if (definition == null) continue;
                settleSlot(slot, definition, elapsedTicks, maxHarvests, STRUCTURE_WORK_MULTIPLIER,
                        settings, List.of(this), dynamicHarvestBudget, capacityLedger);
                if (hasResourcesForOne(slot, definition, settings, List.of(this)) && hasOutputSpace(definition)) {
                    newActiveMask |= 1 << slot;
                }
            }
            nextInputSlot = (nextInputSlot + 1) % INPUT_SLOTS;
            setActiveMask(newActiveMask);
            if (UpgradeHelper.hasEssenceConversionUpgrade(this) && level instanceof ServerLevel serverLevel) {
                GreenhouseEssenceConversionHelper.convertStored(serverLevel, internalOutputHandler);
            }
        } finally {
            finishOutputChangeBatch();
        }
    }

    private int settleSlot(int slot, GreenhouseCropDefinition definition, int elapsedTicks,
                           int harvestBudget, int structureMultiplier, ProductionSettings settings,
                           List<LargeGreenhouseBE> resourceMembers, int[] randomBudget,
                           GreenhouseCapacityLedger capacityLedger) {
        int parallelPlants = Math.max(1, getLocalSeed(slot).getCount());
        long addedWork = GreenhouseProductionEngine.addedWork(
                elapsedTicks,
                JDTEConfig.COMMON.greenhouseBaseMultiplier.get(),
                settings.multiplier(),
                parallelPlants,
                structureMultiplier);
        addedWork = com.jdte.common.greenhouse.GreenhouseMatrixRuntime.applySpeed(this, addedWork);
        GreenhouseProductionEngine.WorkWindow work = GreenhouseProductionEngine.accumulate(
                growthWork[slot], definition.growthWork(), harvestBudget, addedWork,
                JDTEConfig.COMMON.greenhouseMaxPendingWork.get());
        int requested = work.requestedHarvests();
        if (requested <= 0) {
            growthWork[slot] = work.availableWork();
            return 0;
        }

        if (!hasOutputSpace(definition)) {
            growthWork[slot] = work.stalledWork();
            return 0;
        }
        int candidate;
        if (settings.creative()) {
            candidate = requested;
        } else {
            int energyCost = settings.energyPerHarvest();
            int fluidCost = getEffectiveFluidPerHarvest(slot, definition);
            int byFluid = getHarvestsSupportedByFluid(totalFluid(resourceMembers), fluidCost);
            int byEnergy = energyCost == 0 ? requested : totalEnergy(resourceMembers) / energyCost;
            candidate = Math.min(requested, Math.min(byFluid, byEnergy));
        }
        if (definition.harvestGenerator() != null && randomBudget != null && randomBudget[0] <= 0) {
            growthWork[slot] = work.availableWork();
            return 0;
        }
        if (definition.harvestGenerator() != null && randomBudget != null) {
            candidate = Math.min(candidate, Math.max(0, randomBudget[0]));
        }
        if (candidate <= 0 || !(level instanceof ServerLevel serverLevel)) {
            growthWork[slot] = work.stalledWork();
            return 0;
        }

        int paidHarvests = generateAndStoreDrops(serverLevel, getLocalSeed(slot), definition, candidate,
                settings.fortuneLevel(), capacityLedger);
        if (paidHarvests <= 0) {
            growthWork[slot] = work.stalledWork();
            return 0;
        }
        if (definition.harvestGenerator() != null && randomBudget != null) {
            randomBudget[0] = Math.max(0, randomBudget[0] - paidHarvests);
        }
        if (!settings.creative()) {
            drainFluid(resourceMembers, getFluidCostForHarvests(
                    getEffectiveFluidPerHarvest(slot, definition), paidHarvests));
            drainEnergy(resourceMembers, paidHarvests * settings.energyPerHarvest());
        }
        growthWork[slot] = work.remainingAfter(paidHarvests);
        return paidHarvests;
    }

    private static int totalFluid(List<LargeGreenhouseBE> members) {
        long total = 0L;
        for (LargeGreenhouseBE member : members) total += member.fluidTank.getFluidAmount();
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    private static int totalEnergy(List<LargeGreenhouseBE> members) {
        long total = 0L;
        for (LargeGreenhouseBE member : members) total += member.energyStorage.getEnergyStored();
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    private static void drainFluid(List<LargeGreenhouseBE> members, int amount) {
        int remaining = Math.max(0, amount);
        for (LargeGreenhouseBE member : members) {
            if (remaining == 0) return;
            remaining -= member.fluidTank.drain(remaining, IFluidHandler.FluidAction.EXECUTE).getAmount();
        }
    }

    private static int getHarvestsSupportedByFluid(int availableFluid, int fluidPerHarvest) {
        if (availableFluid <= 0) return 0;
        long supported = (long) availableFluid * FLUID_EFFICIENCY_MULTIPLIER
                / Math.max(1, fluidPerHarvest);
        return (int) Math.min(Integer.MAX_VALUE, supported);
    }

    private static int getFluidCostForHarvests(int fluidPerHarvest, int harvests) {
        if (harvests <= 0) return 0;
        long baseCost = (long) Math.max(1, fluidPerHarvest) * harvests;
        long discounted = (baseCost + FLUID_EFFICIENCY_MULTIPLIER - 1L)
                / FLUID_EFFICIENCY_MULTIPLIER;
        return (int) Math.min(Integer.MAX_VALUE, discounted);
    }

    private static void drainEnergy(List<LargeGreenhouseBE> members, int amount) {
        int remaining = Math.max(0, amount);
        for (LargeGreenhouseBE member : members) {
            if (remaining == 0) return;
            remaining -= member.energyStorage.extractEnergy(remaining, false);
        }
    }

    private GreenhouseCropDefinition[] resolveDefinitions(int slots) {
        long recipeGeneration = GreenhouseCropResolver.cacheGeneration();
        if (cachedRecipeGeneration != recipeGeneration) {
            cachedRecipeGeneration = recipeGeneration;
            for (int slot = 0; slot < INPUT_SLOTS; slot++) clearCachedDefinition(slot);
        }
        for (int slot = 0; slot < slots; slot++) {
            getDefinition(slot);
        }
        return cachedDefinitions;
    }

    private int countDefined(GreenhouseCropDefinition[] definitions, int slots) {
        int count = 0;
        for (int slot = 0; slot < slots; slot++) {
            if (definitions[slot] != null) count++;
        }
        return count;
    }

    private boolean hasResourcesForOne(int slot, GreenhouseCropDefinition definition,
                                       ProductionSettings settings, List<LargeGreenhouseBE> resourceMembers) {
        if (settings.creative()) {
            return true;
        }
        return totalFluid(resourceMembers) >= getFluidCostForHarvests(
                getEffectiveFluidPerHarvest(slot, definition), 1)
                && totalEnergy(resourceMembers) >= settings.energyPerHarvest();
    }

    private boolean hasOutputSpace(GreenhouseCropDefinition definition) {
        if (definition.outputs().isEmpty()) return false;
        ItemStack primary = definition.outputs().getFirst();
        if (getInsertableCount(primary) >= primary.getCount()) return true;
        if (UpgradeHelper.hasEssenceConversionUpgrade(this) && level instanceof ServerLevel serverLevel) {
            ItemStack converted = GreenhouseEssenceConversionHelper.getConversionResult(serverLevel, primary);
            return !converted.isEmpty() && getInsertableCount(converted) >= converted.getCount();
        }
        return false;
    }

    private int getInsertableCount(ItemStack output) {
        int capacity = 0;
        int end = OUTPUT_START_SLOT + getActiveOutputSlots();
        for (int slot = OUTPUT_START_SLOT; slot < end; slot++) {
            ItemStack existing = itemHandler.getStackInSlot(slot);
            if (existing.isEmpty()) {
                capacity += itemHandler.getSlotLimit(slot);
            } else if (ItemStack.isSameItemSameComponents(existing, output)) {
                capacity += Math.max(0, itemHandler.getSlotLimit(slot) - existing.getCount());
            }
        }
        return capacity;
    }

    private int generateAndStoreDrops(ServerLevel serverLevel, ItemStack plantedSeed,
                                      GreenhouseCropDefinition definition, int harvests,
                                      int fortuneLevel, GreenhouseCapacityLedger capacityLedger) {
        boolean convertEssence = UpgradeHelper.hasEssenceConversionUpgrade(this);
        boolean convertSeeds = UpgradeHelper.hasSeedConversionUpgrade(this);
        int samples = definition.harvestGenerator() == null
                ? (convertEssence ? 1 : Math.min(LOOT_SAMPLES_PER_SETTLEMENT, harvests))
                : harvests;
        int baseGroup = harvests / samples;
        int extraGroups = harvests % samples;
        int completed = 0;
        ItemStack tool = new ItemStack(Items.DIAMOND_HOE);
        for (int sample = 0; sample < samples; sample++) {
            int groupHarvests = baseGroup + (sample < extraGroups ? 1 : 0);
            List<ItemStack> drops = generateSingleHarvest(serverLevel, definition, tool);
            if (convertSeeds && !definition.outputs().isEmpty()) {
                drops = GreenhouseEssenceConversionHelper.replaceSeeds(
                        drops, plantedSeed, definition.outputs().getFirst());
            }
            int fitted = fitRepetitions(serverLevel, capacityLedger, drops, groupHarvests, fortuneLevel,
                    convertEssence);
            List<ItemStack> scaledDrops = GreenhouseFortuneHelper.scaleBatch(
                    drops, fitted, fortuneLevel, serverLevel.random);
            if (convertEssence) {
                scaledDrops = GreenhouseEssenceConversionHelper.convert(serverLevel, scaledDrops);
            }
            if (fitted > 0 && capacityLedger.canFit(scaledDrops, 1)
                    && insertScaledDrops(null, internalOutputHandler, scaledDrops, 1)) {
                capacityLedger.reserve(scaledDrops, 1);
                completed += fitted;
            }
        }
        return completed;
    }

    private List<ItemStack> generateSingleHarvest(ServerLevel serverLevel, GreenhouseCropDefinition definition,
                                                  ItemStack tool) {
        if (definition.harvestGenerator() != null) {
            return definition.generateHarvest(serverLevel, worldPosition, tool);
        }
        if (!definition.useLootTable()) return definition.outputs();
        ResourceLocation blockId = definition.harvestBlock();
        if (blockId == null || !BuiltInRegistries.BLOCK.containsKey(blockId)) return definition.outputs();
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        BlockState matureState;
        if (block instanceof CropBlock crop) {
            matureState = crop.getStateForAge(crop.getMaxAge());
        } else {
            BlockState state = block.defaultBlockState();
            matureState = null;
            for (var property : state.getProperties()) {
                if (property instanceof IntegerProperty integerProperty && "age".equals(property.getName())) {
                    int maxAge = integerProperty.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                    matureState = state.setValue(integerProperty, maxAge);
                    break;
                }
            }
            if (matureState == null) matureState = state;
        }
        List<ItemStack> drops = Block.getDrops(
                matureState, serverLevel, worldPosition, null, FakePlayerFactory.getMinecraft(serverLevel), tool);
        if (drops.isEmpty()) return definition.outputs();
        List<ItemStack> result = new ArrayList<>(drops.size());
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) result.add(drop.copy());
        }
        return result.isEmpty() ? definition.outputs() : result;
    }

    private int fitRepetitions(ServerLevel serverLevel, GreenhouseCapacityLedger capacityLedger,
                               List<ItemStack> drops, int maximum, int fortuneLevel,
                               boolean convertEssence) {
        int low = 0;
        int high = maximum;
        while (low < high) {
            int middle = low + (high - low + 1) / 2;
            List<ItemStack> capacityBound = GreenhouseFortuneHelper.capacityBound(drops, middle, fortuneLevel);
            if (convertEssence) {
                capacityBound = GreenhouseEssenceConversionHelper.convert(serverLevel, capacityBound);
            }
            if (capacityLedger.canFit(capacityBound, 1)) low = middle;
            else high = middle - 1;
        }
        return low;
    }

    private boolean insertScaledDrops(IItemHandler adjacent, IItemHandler internal,
                                      List<ItemStack> drops, int repetitions) {
        for (ItemStack drop : drops) {
            long remaining = (long) drop.getCount() * repetitions;
            if (adjacent != null) remaining = insertAmount(adjacent, drop, remaining, false);
            remaining = insertAmount(internal, drop, remaining, false);
            if (remaining > 0) return false;
        }
        return true;
    }

    private static long insertAmount(IItemHandler handler, ItemStack template, long amount, boolean simulate) {
        long remaining = amount;
        for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            ItemStack existing = handler.getStackInSlot(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, template)) {
                remaining -= insertIntoSlot(handler, slot, template, remaining, simulate);
            }
        }
        for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            if (handler.getStackInSlot(slot).isEmpty()) {
                remaining -= insertIntoSlot(handler, slot, template, remaining, simulate);
            }
        }
        return remaining;
    }

    private static int insertIntoSlot(IItemHandler handler, int slot, ItemStack template,
                                      long amount, boolean simulate) {
        int offered = (int) Math.min(amount, handler.getSlotLimit(slot));
        ItemStack stack = template.copyWithCount(offered);
        ItemStack remainder = handler.insertItem(slot, stack, simulate);
        return offered - remainder.getCount();
    }

    public List<LargeGreenhouseStructure.BoundaryNeighbor> getBoundaryNeighbors() {
        Direction facing = LargeGreenhouseStructure.horizontalFacing(getBlockState());
        if (cachedBoundaryFacing != facing) {
            cachedBoundaryFacing = facing;
            cachedBoundaryNeighbors = LargeGreenhouseStructure.boundaryNeighbors(worldPosition, facing);
        }
        return cachedBoundaryNeighbors;
    }

    private ItemStack getLocalSeed(int slot) {
        return slot >= 0 && slot < INPUT_SLOTS ? itemHandler.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    private GreenhouseCropDefinition getDefinition(int slot) {
        ItemStack seed = getLocalSeed(slot);
        if (!ItemStack.isSameItemSameComponents(seed, cachedSeeds[slot])) {
            cachedSeeds[slot] = seed.isEmpty() ? ItemStack.EMPTY : seed.copyWithCount(1);
            cachedDefinitions[slot] = GreenhouseCropResolver.find(level, seed);
            ResourceLocation nextDisplay = cachedDefinitions[slot] == null ? null : cachedDefinitions[slot].displayBlock();
            if (!Objects.equals(displayBlockIds[slot], nextDisplay)) {
                displayBlockIds[slot] = nextDisplay;
                markDirtyClient();
            }
            growthWork[slot] = 0;
        }
        return cachedDefinitions[slot];
    }

    private void clearCachedDefinition(int slot) {
        cachedSeeds[slot] = ItemStack.EMPTY;
        cachedDefinitions[slot] = null;
    }

    private void setActiveMask(int nextMask) {
        if (activeMask != nextMask) {
            activeMask = nextMask;
            markDirtyClient();
        }
    }

    public BlockState getDisplayCropState(int slot) {
        if (slot < 0 || slot >= INPUT_SLOTS) return null;
        BlockState[] ageStates = getDisplayAgeStates(slot);
        if (ageStates == null) return null;
        boolean active = (activeMask & 1 << slot) != 0;
        if (!active || level == null || ageStates.length == 1) return ageStates[ageStates.length - 1];
        int age = (int) ((level.getGameTime() % 20L) * ageStates.length / 20L);
        return ageStates[Math.min(ageStates.length - 1, age)];
    }

    // Lazily rebuilt per slot when the display block id changes (covers client sync and NBT load).
    private BlockState[] getDisplayAgeStates(int slot) {
        ResourceLocation displayBlockId = displayBlockIds[slot];
        if (!Objects.equals(displayBlockId, cachedDisplayIds[slot])) {
            cachedDisplayIds[slot] = displayBlockId;
            displayAgeStates[slot] = buildAgeStates(displayBlockId);
        }
        return displayAgeStates[slot];
    }

    private static BlockState[] buildAgeStates(ResourceLocation blockId) {
        if (blockId == null || !BuiltInRegistries.BLOCK.containsKey(blockId)) return null;
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        if (block instanceof CropBlock crop) {
            BlockState[] states = new BlockState[crop.getMaxAge() + 1];
            for (int age = 0; age < states.length; age++) states[age] = crop.getStateForAge(age);
            return states;
        }
        BlockState state = block.defaultBlockState();
        for (var property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty && "age".equals(property.getName())) {
                int maxAge = integerProperty.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                BlockState[] states = new BlockState[maxAge + 1];
                for (int age = 0; age <= maxAge; age++) states[age] = state.setValue(integerProperty, age);
                return states;
            }
        }
        return new BlockState[] { state };
    }

    public boolean isActive() { return activeMask != 0; }

    private ProductionSettings currentProductionSettings() {
        boolean creative = UpgradeHelper.countUpgrades(this, UpgradeType.CREATIVE) > 0;
        boolean overclocked = creative || UpgradeHelper.countUpgrades(this, UpgradeType.OVERCLOCK) > 0;
        int effectiveMultiplier = overclocked
                ? JDTEConfig.COMMON.greenhouseOverclockMaxSpeedMultiplier.get()
                : Math.clamp(multiplier, 1, JDTEConfig.COMMON.greenhouseMaxSpeedMultiplier.get());
        int energyPerHarvest = creative ? 0 : com.jdte.common.greenhouse.GreenhouseMatrixRuntime.applyEfficiency(this,
                JDTEConfig.COMMON.greenhouseEnergyPerHarvestV2.get());
        int fortuneLevel = Math.min(3, UpgradeHelper.countUpgrades(this, UpgradeType.FORTUNE));
        return new ProductionSettings(effectiveMultiplier, creative, energyPerHarvest, fortuneLevel);
    }

    private record ProductionSettings(int multiplier, boolean creative, int energyPerHarvest, int fortuneLevel) {
    }

    public int getMultiplier() {
        if (UpgradeHelper.hasOverclock(this)) {
            return JDTEConfig.COMMON.greenhouseOverclockMaxSpeedMultiplier.get();
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
        return UpgradeHelper.hasOverclock(this) || UpgradeHelper.hasCreativeUpgrade(this)
                ? JDTEConfig.COMMON.greenhouseOverclockMaxSpeedMultiplier.get()
                : JDTEConfig.COMMON.greenhouseMaxSpeedMultiplier.get();
    }
    public int getEffectiveEnergyPerHarvest() {
        return UpgradeHelper.hasCreativeUpgrade(this) ? 0 : JDTEConfig.COMMON.greenhouseEnergyPerHarvestV2.get();
    }
    private int getEffectiveFluidPerHarvest(int slot, GreenhouseCropDefinition definition) {
        int reducedBase = Math.max(1, (definition.timeFluid()
                + JDTEConfig.COMMON.greenhouseFluidCostDivisor.get() - 1)
                / JDTEConfig.COMMON.greenhouseFluidCostDivisor.get());
        int cost = (int) Math.min(Integer.MAX_VALUE,
                (long) reducedBase * getStackFluidMultiplier(getLocalSeed(slot)));
        return com.jdte.common.greenhouse.GreenhouseMatrixRuntime.applyEfficiency(this, cost);
    }
    private int getStackFluidMultiplier(ItemStack stack) {
        int halfStack = Math.max(1, stack.getMaxStackSize() / 2);
        return Math.max(1, (stack.getCount() + halfStack - 1) / halfStack);
    }
    public int getActiveOutputSlots() {
        if (isClientSide()) return Math.max(BASE_OUTPUT_SLOTS, syncedActiveOutputSlots);
        long gameTick = level == null ? Long.MIN_VALUE : level.getGameTime();
        if (activeOutputSlotsTick == gameTick) return cachedActiveOutputSlots;

        int configured = BASE_OUTPUT_SLOTS
                + UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY) * OUTPUT_SLOTS_PER_CAPACITY;
        int occupied = BASE_OUTPUT_SLOTS;
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            if (!itemHandler.getStackInSlot(OUTPUT_START_SLOT + i).isEmpty()) occupied = i + 1;
        }
        cachedActiveOutputSlots = Math.min(OUTPUT_SLOTS, Math.max(configured, occupied));
        activeOutputSlotsTick = gameTick;
        return cachedActiveOutputSlots;
    }
    public int getOutputSlotLimit() {
        long gameTick = level == null ? Long.MIN_VALUE : level.getGameTime();
        if (gameTick != Long.MIN_VALUE && outputSlotLimitTick == gameTick) return cachedOutputSlotLimit;
        int upgrades = UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY);
        cachedOutputSlotLimit = upgrades <= 0
                ? BASE_OUTPUT_STACK_LIMIT
                : FIRST_CAPACITY_STACK_LIMIT << Math.min(2, upgrades - 1);
        outputSlotLimitTick = gameTick;
        return cachedOutputSlotLimit;
    }
    private void beginOutputChangeBatch() {
        batchingOutputChanges = true;
        batchedOutputChanges.clear();
    }
    private void finishOutputChangeBatch() {
        batchingOutputChanges = false;
        for (int slot = batchedOutputChanges.nextSetBit(0); slot >= 0;
             slot = batchedOutputChanges.nextSetBit(slot + 1)) {
            MachineOutputManager.submit(this, slot);
        }
        batchedOutputChanges.clear();
        setChanged();
    }
    private static int saturatingAdd(int left, int right) {
        if (right <= 0) return left;
        return left > Integer.MAX_VALUE - right ? Integer.MAX_VALUE : left + right;
    }
    private static int saturatingMultiply(int left, int right) {
        long result = (long) Math.max(0, left) * Math.max(0, right);
        return (int) Math.min(Integer.MAX_VALUE, result);
    }
    private boolean isClientSide() { return level != null && level.isClientSide; }
    public ContainerData getLargeGreenhouseData() { return greenhouseData; }
    public IItemHandler getMenuItemHandler() { return itemHandler; }
    public IItemHandler getAutomationItemHandler() { return automationItemHandler; }

    @Override public ItemStackHandler getMachineHandler() { return itemHandler; }
    @Override public int getMaxEnergy() { return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.greenhouseEnergyCapacity.get()); }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public ContainerData getContainerData() { return poweredData; }
    @Override public int getStandardEnergyCost() { return JDTEConfig.COMMON.greenhouseEnergyPerHarvestV2.get(); }
    @Override public int getMaxMB() { return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.greenhouseFluidCapacity.get()); }
    @Override public JDTEFluidTank getFluidTank() { return fluidTank; }
    @Override public FluidContainerData getFluidContainerData() { return fluidData; }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneData; }
    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public boolean canRun() { return true; }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", OversizedItemStackHandlerSerialization.serialize(itemHandler, provider));
        tag.put("fluid", fluidTank.serializeNBT(provider));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putLongArray("growthWork", growthWork);
        tag.putInt("settlementTicker", settlementTicker);
        tag.putInt("nextInputSlot", nextInputSlot);
        tag.putInt("activeMask", activeMask);
        tag.putInt("multiplier", getMultiplier());
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            if (displayBlockIds[slot] != null) tag.putString("displayBlock" + slot, displayBlockIds[slot].toString());
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) loadInventory(tag.getCompound("inventory"), provider);
        if (tag.contains("fluid")) fluidTank.deserializeNBT(provider, tag.getCompound("fluid"));
        if (tag.contains("energy")) energyStorage.setEnergy(tag.getInt("energy"));
        if (tag.contains("growthWork", Tag.TAG_LONG_ARRAY)) {
            long[] savedWork = tag.getLongArray("growthWork");
            System.arraycopy(savedWork, 0, growthWork, 0, Math.min(savedWork.length, INPUT_SLOTS));
        }
        settlementTicker = Math.max(0, tag.getInt("settlementTicker"));
        nextInputSlot = Math.floorMod(tag.getInt("nextInputSlot"), INPUT_SLOTS);
        activeMask = tag.getInt("activeMask");
        multiplier = tag.contains("multiplier") ? tag.getInt("multiplier")
                : JDTEConfig.COMMON.greenhouseDefaultSpeedMultiplier.get();
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            String key = "displayBlock" + slot;
            if (tag.contains(key)) displayBlockIds[slot] = ResourceLocation.tryParse(tag.getString(key));
        }
        Arrays.fill(cachedSeeds, ItemStack.EMPTY);
        Arrays.fill(cachedDefinitions, null);
    }

    private void loadInventory(CompoundTag inventoryTag, HolderLookup.Provider provider) {
        if (inventoryTag.getInt("Size") != LEGACY_TOTAL_SLOTS) {
            OversizedItemStackHandlerSerialization.deserialize(itemHandler, provider, inventoryTag);
            return;
        }
        ItemStackHandler legacy = new ItemStackHandler(LEGACY_TOTAL_SLOTS);
        legacy.deserializeNBT(provider, inventoryTag);
        for (int slot = 0; slot < LEGACY_INPUT_SLOTS; slot++) {
            itemHandler.setStackInSlot(slot, legacy.getStackInSlot(slot));
        }
        for (int slot = LEGACY_INPUT_SLOTS; slot < LEGACY_TOTAL_SLOTS; slot++) {
            itemHandler.setStackInSlot(OUTPUT_START_SLOT + slot - LEGACY_INPUT_SLOTS, legacy.getStackInSlot(slot));
        }
    }
}
