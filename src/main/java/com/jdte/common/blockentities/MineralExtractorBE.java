package com.jdte.common.blockentities;

import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.jdte.common.manager.MachineOutputManager;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.common.items.interfaces.Helpers;
import com.direwolf20.justdirethings.setup.JDTRegistration;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.minerals.MineralEntry;
import com.jdte.common.minerals.MineralExtractorFluidRoles;
import com.jdte.common.minerals.MineralProductionEngine;
import com.jdte.common.minerals.MineralOutputPlanner;
import com.jdte.common.minerals.MineralSurveyData;
import com.jdte.common.minerals.MineralSurveyIndex;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.AECraftingReadMachinePolicy;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.recipes.MineralExtractorResourceResolver;
import com.jdte.common.utils.ContainerDataEncoding;
import com.jdte.mixin.FluidTankAccessor;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import com.jdte.common.capabilities.MachineAutomationHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class MineralExtractorBE extends BaseMachineBE implements PoweredMachineBE, RedstoneControlledBE,
        FilterableBE, BaseFilterMachine, ExtendedUpgradeMachine, CoalescedAcceleratedMachine {
    public static final int SURVEY_SLOT = 0;
    public static final int SURVEY_SLOTS = 1;
    public static final int OUTPUT_START_SLOT = SURVEY_SLOTS;
    public static final int OUTPUT_SLOTS = 64;
    public static final int BASE_OUTPUT_SLOTS = 16;
    public static final int OUTPUT_SLOTS_PER_CAPACITY = 16;
    public static final int BASE_OUTPUT_STACK_LIMIT = 64;
    public static final int FIRST_CAPACITY_STACK_LIMIT = 2048;
    public static final int ULTIMATE_CAPACITY_STACK_LIMIT = 6_400_000;
    public static final int TOTAL_SLOTS = OUTPUT_START_SLOT + OUTPUT_SLOTS;
    public static final int UPGRADE_SLOTS = 8;
    public static final int BASE_PRODUCTION_MULTIPLIER = 64;

    public enum State {
        IDLE,
        RUNNING,
        NO_MINERALS,
        FILTERED,
        NO_ENERGY,
        NO_TIME_FLUID,
        OUTPUT_FULL,
        STALE_SURVEY
    }

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(getMaxEnergy());
    private final PoweredMachineContainerData poweredData = new PoweredMachineContainerData(this);
    private final RedstoneControlData redstoneData = new RedstoneControlData();
    private final FilterData filterData = new FilterData();
    private Function<Level, MineralExtractorFluidRoles> fluidRolesResolver = MineralExtractorResourceResolver::resolve;
    private final JDTEFluidTank experienceFluidTank = new JDTEFluidTank(getMaxFluidCapacity(),
            this::matchesFortuneFluid);
    private final JDTEFluidTank timeFluidTank = new JDTEFluidTank(getMaxFluidCapacity(),
            this::matchesAccelerationFluid);
    private final ResourceHandler<FluidResource> combinedFluidHandler = new CombinedResourceHandler<>(experienceFluidTank, timeFluidTank);
    private final MachineInventoryHandler itemHandler = new MachineInventoryHandler(totalSlots()) {
        @Override protected int getCapacity(int slot, ItemResource resource) {
            return isSurveySlot(slot) ? 1 : getOutputSlotLimit();
        }
        @Override public boolean isValid(int slot, ItemResource resource) {
            return resource.isEmpty() || isSurveySlot(slot) && resource.is(JDTEItems.MINERAL_SURVEY.get());
        }
        @Override protected void onContentsChanged(int slot, ItemStack previous) {
            if (isSurveySlot(slot)) invalidateMineralCache();
            else if (slot >= outputStartSlot()) MachineOutputManager.submit(MineralExtractorBE.this, slot);
            setChanged();
        }
    };
    private final ResourceHandler<ItemResource> automationItemHandler = new MachineAutomationHandler<>(
            itemHandler, this::isSurveySlot, slot -> slot >= outputStartSlot() && slot < outputStartSlot() + getActiveOutputSlots()) {
        @Override public int size() { return outputStartSlot() + getActiveOutputSlots(); }
    };
    private final ContainerData machineData = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> isClientSide() ? syncedProgress : progress();
                case 1 -> isClientSide() ? syncedProcessTicks : processTicks();
                case 2 -> isClientSide() ? syncedActiveOutputSlots : getActiveOutputSlots();
                case 3 -> ContainerDataEncoding.low16(isClientSide() ? syncedExperienceFluid : experienceFluidTank.getFluidAmount());
                case 4 -> ContainerDataEncoding.low16(isClientSide() ? syncedTimeFluid : timeFluidTank.getFluidAmount());
                case 5 -> ContainerDataEncoding.low16(isClientSide() ? syncedFluidCapacity : getMaxFluidCapacity());
                case 6 -> isClientSide() ? syncedMultiplier : getMultiplier();
                case 7 -> isClientSide() ? syncedMaxMultiplier : getMaxSelectableMultiplier();
                case 8 -> isClientSide() ? syncedState : state.ordinal();
                case 9 -> isClientSide() ? syncedFortunePercent : currentFortunePercent;
                case 10 -> isClientSide() ? syncedSurveySource : surveySource ? 1 : 0;
                case 11 -> isClientSide() ? syncedMineralCount : cachedEntries.size();
                case 12 -> ContainerDataEncoding.high16(isClientSide() ? syncedExperienceFluid : experienceFluidTank.getFluidAmount());
                case 13 -> ContainerDataEncoding.high16(isClientSide() ? syncedTimeFluid : timeFluidTank.getFluidAmount());
                case 14 -> ContainerDataEncoding.high16(isClientSide() ? syncedFluidCapacity : getMaxFluidCapacity());
                case 15 -> isClientSide() ? syncedExperienceFluidType : encodedFluidType(experienceFluidTank.getFluid());
                case 16 -> isClientSide() ? syncedTimeFluidType : encodedFluidType(timeFluidTank.getFluid());
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> syncedProgress = value;
                case 1 -> syncedProcessTicks = value;
                case 2 -> syncedActiveOutputSlots = value;
                case 3 -> syncedExperienceFluid = ContainerDataEncoding.withLow16(syncedExperienceFluid, value);
                case 4 -> syncedTimeFluid = ContainerDataEncoding.withLow16(syncedTimeFluid, value);
                case 5 -> syncedFluidCapacity = ContainerDataEncoding.withLow16(syncedFluidCapacity, value);
                case 6 -> syncedMultiplier = value;
                case 7 -> syncedMaxMultiplier = value;
                case 8 -> syncedState = value;
                case 9 -> syncedFortunePercent = value;
                case 10 -> syncedSurveySource = value;
                case 11 -> syncedMineralCount = value;
                case 12 -> syncedExperienceFluid = ContainerDataEncoding.withHigh16(syncedExperienceFluid, value);
                case 13 -> syncedTimeFluid = ContainerDataEncoding.withHigh16(syncedTimeFluid, value);
                case 14 -> syncedFluidCapacity = ContainerDataEncoding.withHigh16(syncedFluidCapacity, value);
                case 15 -> syncedExperienceFluidType = value & 0xFFFF;
                case 16 -> syncedTimeFluidType = value & 0xFFFF;
                default -> { }
            }
        }
        @Override public int getCount() { return 17; }
    };

    private List<MineralEntry> cachedEntries = List.of();
    private Map<Identifier, ProductionStack> cachedProductionStacks = Map.of();
    private Identifier cachedBiomeId;
    private long cachedIndexVersion = Long.MIN_VALUE;
    private int cachedFilterFingerprint;
    private boolean cachedSmelterUpgrade;
    private boolean mineralCacheValid;
    private boolean surveySource;
    private boolean staleSurvey;
    private boolean cachedSourceHadEntries;
    private long pendingBaseWork;
    private long pendingAcceleratedWork;
    private long transientBaseWork;
    private long transientAcceleratedWork;
    private int accumulatedAcceleratedTicks;
    private int settlementTicker;
    private long regularTickGameTime = Long.MIN_VALUE;
    private long settlementBudgetGameTime = Long.MIN_VALUE;
    private long settledCyclesThisGameTime;
    private int multiplier;
    private State state = State.IDLE;
    private int currentFortunePercent;
    private int syncedProgress;
    private int syncedProcessTicks = 20;
    private int syncedActiveOutputSlots = BASE_OUTPUT_SLOTS;
    private int syncedExperienceFluid;
    private int syncedTimeFluid;
    private int syncedFluidCapacity = 1;
    private int syncedExperienceFluidType;
    private int syncedTimeFluidType;
    private int syncedMultiplier = 1;
    private int syncedMaxMultiplier = 32;
    private int syncedState;
    private int syncedFortunePercent;
    private int syncedSurveySource;
    private int syncedMineralCount;
    private long outputSlotLimitTick = Long.MIN_VALUE;
    private int cachedOutputSlotLimit = BASE_OUTPUT_STACK_LIMIT;

    public MineralExtractorBE(BlockPos pos, BlockState state) {
        this(JDTEBlockEntities.MINERAL_EXTRACTOR.get(), pos, state);
    }

    protected MineralExtractorBE(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                                 BlockPos pos, BlockState state) {
        super(type, pos, state);
        MACHINE_SLOTS = totalSlots();
        tickSpeed = 1;
        multiplier = JDTEConfig.COMMON.mineralExtractor.defaultMultiplier.get();
    }

    @Override public void tickServer() {
        boolean allowed = UpgradeHelper.mayRunWithUpgrades(this);
        AECraftingReadMachinePolicy.ProductionDecision decision =
                AECraftingReadMachinePolicy.productionState(allowed, isActiveRedstone(), true);
        if (level instanceof ServerLevel serverLevel && regularTickGameTime == serverLevel.getGameTime()) {
            if (decision.runWork()) advanceTransientTick();
            return;
        }
        super.tickServer();
        syncCapacities();
        if (!(level instanceof ServerLevel serverLevel)) return;
        long gameTime = serverLevel.getGameTime();
        if (hasTransientWork() && (decision.runWork() || decision.resetInactiveState())) settle();
        if (decision.runWork() || decision.resetInactiveState()) discardExpiredTransientWork();
        regularTickGameTime = gameTime;
        if (!isActiveRedstone()) {
            setState(State.IDLE);
            return;
        }
        if (!decision.runWork()) return;
        advanceBaseTick();
    }

    private void advanceBaseTick() {
        MineralProductionEngine.WorkAllocation work = workForCurrentMultiplier();
        pendingBaseWork = MineralProductionEngine.accumulateWork(
                pendingBaseWork, baseWorkPerTick(), work.baseWork(), maxPendingWork());
        pendingAcceleratedWork = MineralProductionEngine.accumulateWork(
                pendingAcceleratedWork, baseWorkPerTick(), work.acceleratedWork(), maxPendingWork());
        settlementTicker++;
        if (MineralProductionEngine.shouldSettle(
                pendingBaseWork, pendingAcceleratedWork, processTicks(), settlementTicker,
                JDTEConfig.COMMON.mineralExtractor.settlementInterval.get())) {
            settle();
        }
    }

    private void advanceTransientTick() {
        if (!isActiveRedstone()) return;
        MineralProductionEngine.WorkAllocation work = workForCurrentMultiplier();
        transientBaseWork = MineralProductionEngine.accumulateWork(
                transientBaseWork, baseWorkPerTick(), work.baseWork(), maxPendingWork());
        transientAcceleratedWork = MineralProductionEngine.accumulateWork(
                transientAcceleratedWork, baseWorkPerTick(), work.acceleratedWork(), maxPendingWork());
    }

    private MineralProductionEngine.WorkAllocation workForCurrentMultiplier() {
        return MineralProductionEngine.workForTick(
                getMultiplier(), BASE_PRODUCTION_MULTIPLIER, canUseTimeFluid());
    }

    private boolean hasTransientWork() {
        return transientBaseWork > 0L || transientAcceleratedWork > 0L;
    }

    private void discardExpiredTransientWork() {
        transientBaseWork = 0L;
        transientAcceleratedWork = 0L;
    }

    @Override public void accumulateAcceleratedTicks(int ticks) {
        if (ticks > 0) accumulatedAcceleratedTicks = saturatingAdd(accumulatedAcceleratedTicks, ticks);
    }

    @Override public void flushAcceleratedTicks() {
        boolean allowed = UpgradeHelper.mayRunWithUpgrades(this);
        AECraftingReadMachinePolicy.ProductionDecision decision =
                AECraftingReadMachinePolicy.productionState(allowed, isActiveRedstone(), true);
        if (!decision.runWork()) {
            if (decision.resetInactiveState()) accumulatedAcceleratedTicks = 0;
            return;
        }
        int ticks = accumulatedAcceleratedTicks;
        accumulatedAcceleratedTicks = 0;
        if (ticks <= 0 || !isActiveRedstone()) return;
        transientAcceleratedWork = MineralProductionEngine.accumulateWork(
                transientAcceleratedWork, ticks,
                saturatingMultiply(BASE_PRODUCTION_MULTIPLIER, baseWorkPerTick()), maxPendingWork());
        settle(true);
    }

    private void settle() {
        settle(false);
    }

    private void settle(boolean isAccelerated) {
        settlementTicker = 0;
        if (!(level instanceof ServerLevel serverLevel)) return;
        long gameTime = serverLevel.getGameTime();
        if (settlementBudgetGameTime != gameTime) {
            settlementBudgetGameTime = gameTime;
            settledCyclesThisGameTime = 0L;
        }
        long maxCycles = isAccelerated ? Long.MAX_VALUE : JDTEConfig.COMMON.mineralExtractor.maxCyclesPerSettlement.get();
        long remainingCycleBudget = Math.max(0L, maxCycles - settledCyclesThisGameTime);
        if (remainingCycleBudget <= 0L) return;
        refreshMineralCache(serverLevel);
        if (staleSurvey) {
            setState(State.STALE_SURVEY);
            return;
        }
        if (cachedEntries.isEmpty()) {
            setState(staleSurvey ? State.STALE_SURVEY : cachedSourceHadEntries ? State.FILTERED : State.NO_MINERALS);
            return;
        }

        long processTicks = processTicks();
        long baseCycles = saturatingAdd(pendingBaseWork, transientBaseWork) / processTicks;
        long acceleratedCycles = saturatingAdd(pendingAcceleratedWork, transientAcceleratedWork) / processTicks;
        long requested = Math.min(remainingCycleBudget, saturatingAdd(baseCycles, acceleratedCycles));
        if (requested <= 0L) {
            setState(staleSurvey ? State.STALE_SURVEY : State.RUNNING);
            return;
        }

        boolean creative = UpgradeHelper.hasCreativeUpgrade(this);
        boolean smelting = UpgradeHelper.hasSmelterUpgrade(this);
        int configuredEnergy = JDTEConfig.COMMON.mineralExtractor.energyPerCycle.get();
        int energyPerCycle = creative ? 0 : smelting ? saturatingMultiply(configuredEnergy, 2) : configuredEnergy;
        long energyBudget = energyPerCycle == 0 ? requested : energyStorage.getEnergyStored() / energyPerCycle;
        if (energyBudget <= 0L) {
            setState(State.NO_ENERGY);
            return;
        }
        requested = Math.min(requested, energyBudget);
        long paidBaseCycles = Math.min(baseCycles, requested);
        long wantedAcceleratedCycles = requested - paidBaseCycles;
        int timePerCycle = creative ? 0 : JDTEConfig.COMMON.mineralExtractor.timeFluidPerAcceleratedCycle.get();
        int usableAcceleration = usableAccelerationFluid();
        long timeBudget = timePerCycle == 0 ? wantedAcceleratedCycles : usableAcceleration / timePerCycle;
        long paidAcceleratedCycles = Math.min(wantedAcceleratedCycles, timeBudget);
        long cycles = paidBaseCycles + paidAcceleratedCycles;
        if (cycles <= 0L) {
            setState(State.NO_TIME_FLUID);
            return;
        }

        int experiencePerCycle = creative ? 0 : JDTEConfig.COMMON.mineralExtractor.experienceFluidPerCycle.get();
        int usableFortune = usableFortuneFluid();
        long fortuneLimit = experiencePerCycle == 0 ? cycles
                : Math.min(cycles, usableFortune / experiencePerCycle);
        int fortunePercent = fortuneLimit > 0L ? JDTEConfig.COMMON.mineralExtractor.fortuneBonusPercent.get() : 0;
        long distributionSeed = serverLevel.getRandom().nextLong();
        Settlement settlement = findSettlement(cycles, fortuneLimit, fortunePercent, distributionSeed);
        if (settlement.cycles() <= 0L) {
            setState(State.OUTPUT_FULL);
            return;
        }

        long settledCycles = settlement.cycles();
        MineralProductionEngine.CycleAllocation allocation = MineralProductionEngine.allocateCycles(
                paidBaseCycles, paidAcceleratedCycles, settledCycles);
        long settledBaseCycles = allocation.baseCycles();
        long settledAcceleratedCycles = allocation.acceleratedCycles();
        long settledFortuneCycles = Math.min(fortuneLimit, settledCycles);
        currentFortunePercent = settledFortuneCycles > 0L ? fortunePercent : 0;
        commitOutput(settlement.outputPlan());
        if (!creative) {
            energyStorage.extractEnergy(safeCost(settledCycles, energyPerCycle), false);
            if (matchesAccelerationFluid(timeFluidTank.getFluid())) {
                timeFluidTank.extractFluid(safeCost(settledAcceleratedCycles, timePerCycle));
            }
            if (matchesFortuneFluid(experienceFluidTank.getFluid())) {
                experienceFluidTank.extractFluid(safeCost(settledFortuneCycles, experiencePerCycle));
            }
        }
        consumeBaseWork(settledBaseCycles * processTicks);
        consumeAcceleratedWork(settledAcceleratedCycles * processTicks);
        settledCyclesThisGameTime = saturatingAdd(settledCyclesThisGameTime, settledCycles);
        setState(staleSurvey ? State.STALE_SURVEY : State.RUNNING);
        setChanged();
    }

    private void consumeBaseWork(long consumedWork) {
        long transientConsumed = Math.min(transientBaseWork, consumedWork);
        transientBaseWork -= transientConsumed;
        pendingBaseWork = Math.max(0L, pendingBaseWork - (consumedWork - transientConsumed));
    }

    private void consumeAcceleratedWork(long consumedWork) {
        long transientConsumed = Math.min(transientAcceleratedWork, consumedWork);
        transientAcceleratedWork -= transientConsumed;
        pendingAcceleratedWork = Math.max(0L, pendingAcceleratedWork - (consumedWork - transientConsumed));
    }

    private void refreshMineralCache(ServerLevel serverLevel) {
        int filterFingerprint = computeFilterFingerprint();
        boolean smelterUpgrade = UpgradeHelper.hasSmelterUpgrade(this);
        long indexVersion = MineralSurveyIndex.version(serverLevel.getServer());
        if (mineralCacheValid && cachedIndexVersion == indexVersion
                && cachedFilterFingerprint == filterFingerprint && cachedSmelterUpgrade == smelterUpgrade) return;

        Identifier biomeId;
        List<MineralEntry> sourceEntries;
        List<MineralEntry> surveyedEntries = new ArrayList<>();
        Identifier firstSurveyBiome = null;
        boolean hasSurvey = false;
        boolean hasStaleSurvey = false;
        for (int slot = 0; slot < surveySlotCount(); slot++) {
            MineralSurveyData survey = itemHandler.copyStack(slot).get(JDTEDataComponents.MINERAL_SURVEY.get());
            if (survey == null) continue;
            hasSurvey = true;
            if (firstSurveyBiome == null) firstSurveyBiome = survey.biomeId();
            surveyedEntries.addAll(survey.entries());
            hasStaleSurvey |= survey.indexVersion() != indexVersion;
        }
        if (hasSurvey) {
            biomeId = firstSurveyBiome;
            sourceEntries = MineralProductionEngine.mergeWeightedEntries(surveyedEntries);
            surveySource = true;
            staleSurvey = hasStaleSurvey;
        } else {
            var biome = serverLevel.getBiome(worldPosition);
            biomeId = biome.unwrapKey().map(ResourceKey::identifier).orElse(null);
            sourceEntries = MineralSurveyIndex.profile(serverLevel, biome).entries();
            surveySource = false;
            staleSurvey = false;
        }
        cachedBiomeId = biomeId;
        cachedIndexVersion = indexVersion;
        cachedFilterFingerprint = filterFingerprint;
        cachedSmelterUpgrade = smelterUpgrade;
        cachedSourceHadEntries = !sourceEntries.isEmpty();
        cachedEntries = MineralProductionEngine.select(sourceEntries, this::allowedByFilter);
        cachedProductionStacks = buildProductionStackCache(serverLevel, cachedEntries, smelterUpgrade);
        mineralCacheValid = true;
        if (cachedEntries.isEmpty() && !sourceEntries.isEmpty()) setState(State.FILTERED);
        markDirtyClient();
    }

    private int computeFilterFingerprint() {
        FilterBasicHandler handler = getFilterHandler();
        int hash = filterData.allowlist ? 1 : 0;
        int baseFilterSlots = UpgradeHelper.getBaseFilterSlots(handler);
        int activeSlots = UpgradeHelper.getActiveFilterSlots(this, baseFilterSlots);
        for (int slot = 0; slot < Math.min(activeSlots, handler.size()); slot++) {
            hash = 31 * hash + ItemStack.hashItemAndComponents(handler.getResource(slot).toStack(handler.getAmountAsInt(slot)));
        }
        return hash;
    }

    private boolean allowedByFilter(MineralEntry entry) {
        FilterBasicHandler handler = getFilterHandler();
        int baseFilterSlots = UpgradeHelper.getBaseFilterSlots(handler);
        int activeSlots = UpgradeHelper.getActiveFilterSlots(this, baseFilterSlots);
        boolean hasFilter = false;
        boolean listed = false;
        ItemStack candidate = oreStack(entry.oreId(), 1);
        for (int slot = 0; slot < Math.min(activeSlots, handler.size()); slot++) {
            ItemStack filter = handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
            if (filter.isEmpty()) continue;
            hasFilter = true;
            if (ItemStack.isSameItemSameComponents(filter, candidate)) {
                listed = true;
                break;
            }
        }
        return MineralProductionEngine.allowsListedCandidate(filterData.allowlist, hasFilter, listed);
    }

    private Settlement findSettlement(long requestedCycles, long fortuneLimit, int fortunePercent, long seed) {
        List<MineralOutputPlanner.SlotState> outputSnapshot = outputSnapshot();
        MineralOutputPlanner.FittingPlan fitting = MineralOutputPlanner.findLargestFitting(requestedCycles,
                candidate -> planSettlement(candidate, fortuneLimit, fortunePercent, seed, outputSnapshot).outputPlan());
        return fitting.cycles() > 0L
                ? new Settlement(fitting.cycles(), fitting.plan())
                : Settlement.EMPTY;
    }

    private Settlement planSettlement(long cycles, long fortuneLimit, int fortunePercent, long seed,
                                      List<MineralOutputPlanner.SlotState> outputSnapshot) {
        long fortuneCycles = Math.min(cycles, fortuneLimit);
        var random = net.minecraft.util.RandomSource.create(seed);
        MineralProductionEngine.Batch normal = MineralProductionEngine.distribute(
                cachedEntries, cycles - fortuneCycles, cycles, 0, random);
        MineralProductionEngine.Batch fortunate = MineralProductionEngine.distribute(
                cachedEntries, fortuneCycles, cycles, fortunePercent, random);
        Map<Identifier, Long> oreAmounts = mergeAmounts(normal.amounts(), fortunate.amounts());
        Map<Identifier, Long> itemAmounts = new LinkedHashMap<>();
        Map<Identifier, Integer> stackLimits = new LinkedHashMap<>();
        oreAmounts.forEach((oreId, amount) -> {
            ProductionStack production = cachedProductionStacks.get(oreId);
            if (production != null) {
                long produced = MineralProductionEngine.scaleOutput(amount, production.count());
                itemAmounts.merge(production.itemId(), produced, MineralExtractorBE::saturatingAdd);
                stackLimits.put(production.itemId(), getOutputSlotLimit());
            }
        });
        if (itemAmounts.isEmpty()) return Settlement.EMPTY;
        MineralOutputPlanner.Plan outputPlan = MineralOutputPlanner.plan(outputSnapshot, itemAmounts, stackLimits);
        return new Settlement(cycles, outputPlan);
    }

    private List<MineralOutputPlanner.SlotState> outputSnapshot() {
        int activeOutputSlots = getActiveOutputSlots();
        List<MineralOutputPlanner.SlotState> slots = new ArrayList<>(activeOutputSlots);
        for (int slot = 0; slot < activeOutputSlots; slot++) {
            int handlerSlot = outputStartSlot() + slot;
            ItemStack stack = itemHandler.copyStack(handlerSlot);
            int configuredLimit = getOutputSlotLimit();
            int limit = stack.isEmpty() ? configuredLimit : Math.max(configuredLimit, stack.getCount());
            slots.add(stack.isEmpty()
                    ? MineralOutputPlanner.SlotState.empty(limit)
                    : new MineralOutputPlanner.SlotState(
                            BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getCount(), limit));
        }
        return slots;
    }

    private void commitOutput(MineralOutputPlanner.Plan plan) {
        for (int slot = 0; slot < plan.slots().size(); slot++) {
            MineralOutputPlanner.SlotState state = plan.slots().get(slot);
            ItemStack stack = state.isEmpty() ? ItemStack.EMPTY
                    : BuiltInRegistries.ITEM.getOptional(state.itemId())
                            .map(item -> new ItemStack(item, state.count()))
                            .orElse(ItemStack.EMPTY);
            itemHandler.setStack(outputStartSlot() + slot, stack);
        }
    }

    private record ProductionStack(Identifier itemId, int count) {
    }

    private record Settlement(long cycles, MineralOutputPlanner.Plan outputPlan) {
        private static final Settlement EMPTY = new Settlement(0L,
                new MineralOutputPlanner.Plan(false, List.of()));
    }

    private static Map<Identifier, ProductionStack> buildProductionStackCache(
            ServerLevel level, List<MineralEntry> entries, boolean smelting) {
        Map<Identifier, ProductionStack> stacks = new LinkedHashMap<>();
        for (MineralEntry entry : entries) {
            ItemStack ore = oreStack(entry.oreId(), 1);
            if (ore.isEmpty()) continue;
            ItemStack smelted = smelting ? Helpers.getSmeltedItem(level, ore) : ItemStack.EMPTY;
            ItemStack output = smelted.isEmpty() ? ore : smelted;
            stacks.put(entry.oreId(), new ProductionStack(
                    BuiltInRegistries.ITEM.getKey(output.getItem()),
                    output.getCount()));
        }
        return Map.copyOf(stacks);
    }

    private static ItemStack oreStack(Identifier id, int amount) {
        return BuiltInRegistries.BLOCK.getOptional(id)
                .map(block -> new ItemStack(block.asItem(), amount))
                .orElse(ItemStack.EMPTY);
    }

    private static Map<Identifier, Long> mergeAmounts(Map<Identifier, Long> left,
                                                             Map<Identifier, Long> right) {
        Map<Identifier, Long> merged = new LinkedHashMap<>(left);
        right.forEach((id, amount) -> merged.merge(id, amount, MineralExtractorBE::saturatingAdd));
        return merged;
    }

    private void invalidateMineralCache() {
        cachedBiomeId = null;
        cachedIndexVersion = Long.MIN_VALUE;
        cachedEntries = List.of();
        cachedProductionStacks = Map.of();
        mineralCacheValid = false;
    }

    private boolean canUseTimeFluid() {
        return UpgradeHelper.hasCreativeUpgrade(this)
                || JDTEConfig.COMMON.mineralExtractor.timeFluidPerAcceleratedCycle.get() == 0
                || usableAccelerationFluid() > 0;
    }

    void setFluidRolesResolver(Function<Level, MineralExtractorFluidRoles> resolver) {
        fluidRolesResolver = Objects.requireNonNull(resolver, "resolver");
    }

    int usableFortuneFluid() {
        return matchesFortuneFluid(experienceFluidTank.getFluid()) ? experienceFluidTank.getFluidAmount() : 0;
    }

    int usableAccelerationFluid() {
        return matchesAccelerationFluid(timeFluidTank.getFluid()) ? timeFluidTank.getFluidAmount() : 0;
    }

    private boolean matchesFortuneFluid(FluidStack stack) {
        return fluidRolesResolver.apply(level).matchesFortune(stack);
    }

    private boolean matchesAccelerationFluid(FluidStack stack) {
        return fluidRolesResolver.apply(level).matchesAcceleration(stack);
    }

    public int surveySlotCount() { return SURVEY_SLOTS; }
    public int outputStartSlot() { return surveySlotCount(); }
    public int totalSlots() { return outputStartSlot() + OUTPUT_SLOTS; }
    protected long baseWorkPerTick() { return 1L; }
    public boolean isSurveySlot(int slot) { return slot >= 0 && slot < surveySlotCount(); }
    public int getActiveOutputSlots() {
        int configured = UpgradeHelper.hasUltimateCapacity(this)
                ? OUTPUT_SLOTS
                : BASE_OUTPUT_SLOTS + UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY) * OUTPUT_SLOTS_PER_CAPACITY;
        int occupied = BASE_OUTPUT_SLOTS;
        for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
            if (!itemHandler.copyStack(outputStartSlot() + slot).isEmpty()) occupied = slot + 1;
        }
        return Math.min(OUTPUT_SLOTS, Math.max(configured, occupied));
    }
    public int getOutputSlotLimit() {
        long gameTick = level == null ? Long.MIN_VALUE : level.getGameTime();
        if (gameTick != Long.MIN_VALUE && outputSlotLimitTick == gameTick) return cachedOutputSlotLimit;
        if (UpgradeHelper.hasUltimateCapacity(this)) {
            cachedOutputSlotLimit = ULTIMATE_CAPACITY_STACK_LIMIT;
            outputSlotLimitTick = gameTick;
            return ULTIMATE_CAPACITY_STACK_LIMIT;
        }
        int upgrades = UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY);
        cachedOutputSlotLimit = upgrades <= 0
                ? BASE_OUTPUT_STACK_LIMIT
                : FIRST_CAPACITY_STACK_LIMIT << Math.min(2, upgrades - 1);
        outputSlotLimitTick = gameTick;
        return cachedOutputSlotLimit;
    }

    public int getMultiplier() {
        return Math.clamp(multiplier, 1, getMaxSelectableMultiplier());
    }
    public void setMultiplier(int value) {
        multiplier = Math.clamp(value, 1, getMaxSelectableMultiplier());
        setChanged();
        markDirtyClient();
    }
    public int getMaxSelectableMultiplier() {
        if (UpgradeHelper.hasUltimateOverclock(this)) {
            return JDTEConfig.COMMON.mineralExtractor.overclockMaxMultiplier.get() * 10;
        }
        return UpgradeHelper.hasOverclock(this) || UpgradeHelper.hasCreativeUpgrade(this)
                ? JDTEConfig.COMMON.mineralExtractor.overclockMaxMultiplier.get()
                : JDTEConfig.COMMON.mineralExtractor.maxMultiplier.get();
    }
    public State getMachineState() { return State.values()[Math.floorMod(machineData.get(8), State.values().length)]; }
    public Identifier getSourceBiomeId() { return cachedBiomeId; }
    public boolean isSurveySource() { return machineData.get(10) != 0; }
    public ContainerData getMachineData() { return machineData; }
    public JDTEFluidTank getExperienceFluidTank() { return experienceFluidTank; }
    public JDTEFluidTank getTimeFluidTank() { return timeFluidTank; }
    public ResourceHandler<FluidResource> getCombinedFluidHandler() { return combinedFluidHandler; }
    public ResourceHandler<ItemResource> getAutomationItemHandler() { return automationItemHandler; }
    public int getMaxFluidCapacity() { return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.mineralExtractor.fluidCapacity.get()); }

    private static int encodedFluidType(FluidStack stack) {
        if (stack.isEmpty()) return 0;
        int registryId = BuiltInRegistries.FLUID.getId(stack.getFluid());
        return registryId >= 0 && registryId < 0xFFFF ? registryId + 1 : 0;
    }

    private int progress() {
        long persistent = saturatingAdd(pendingBaseWork, pendingAcceleratedWork);
        long transientWork = saturatingAdd(transientBaseWork, transientAcceleratedWork);
        long total = saturatingAdd(persistent, transientWork);
        return (int) Math.min(processTicks(), total % processTicks());
    }
    private int processTicks() { return JDTEConfig.COMMON.mineralExtractor.processTicks.get(); }
    private long maxPendingWork() { return JDTEConfig.COMMON.mineralExtractor.maxPendingWork.get(); }
    private boolean isClientSide() { return level != null && level.isClientSide(); }
    private void setState(State next) {
        if (state == next) return;
        state = next;
        markDirtyClient();
    }

    private void syncCapacities() {
        UpgradeHelper.syncCapacities(this);
        int capacity = getMaxFluidCapacity();
        syncTank(experienceFluidTank, capacity);
        syncTank(timeFluidTank, capacity);
    }
    private static void syncTank(JDTEFluidTank tank, int capacity) {
        if (tank instanceof FluidTankAccessor accessor) {
            accessor.jdte$setCapacity(capacity);
        }
    }

    @Override public ItemStacksResourceHandler getMachineHandler() { return itemHandler; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public ContainerData getContainerData() { return poweredData; }
    @Override public int getMaxEnergy() { return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.mineralExtractor.energyCapacity.get()); }
    @Override public int getStandardEnergyCost() { return JDTEConfig.COMMON.mineralExtractor.energyPerCycle.get(); }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneData; }
    @Override public FilterData getFilterData() { return filterData; }
    @Override public FilterBasicHandler getFilterHandler() { return getData(JDTRegistration.HANDLER_BASIC_FILTER); }
    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public boolean canRun() { return true; }

    @Override public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        OversizedItemStackHandlerSerialization.serialize(itemHandler, tag.child("inventory"));
        tag.putChild("experienceFluid", experienceFluidTank);
        tag.putChild("timeFluid", timeFluidTank);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putLong("pendingBaseWork", pendingBaseWork);
        tag.putLong("pendingAcceleratedWork", pendingAcceleratedWork);
        tag.putInt("settlementTicker", settlementTicker);
        tag.putInt("multiplier", multiplier);
        tag.putInt("state", state.ordinal());
    }

    @Override public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        tag.child("inventory").ifPresent(itemHandler::deserialize);
        tag.readChild("experienceFluid", experienceFluidTank);
        tag.readChild("timeFluid", timeFluidTank);
        energyStorage.setEnergy(tag.getIntOr("energy", 0));
        pendingBaseWork = Math.clamp(tag.getLongOr("pendingBaseWork", 0L), 0L, maxPendingWork());
        pendingAcceleratedWork = Math.clamp(tag.getLongOr("pendingAcceleratedWork", 0L), 0L, maxPendingWork());
        settlementTicker = Math.max(0, tag.getIntOr("settlementTicker", 0));
        multiplier = tag.getIntOr("multiplier", JDTEConfig.COMMON.mineralExtractor.defaultMultiplier.get());
        state = State.values()[Math.floorMod(tag.getIntOr("state", 0), State.values().length)];
        invalidateMineralCache();
    }

    private static int safeCost(long count, int unitCost) {
        if (count <= 0L || unitCost <= 0) return 0;
        return (int) Math.min(Integer.MAX_VALUE, count * (long) unitCost);
    }
    private static int saturatingMultiply(int value, int multiplier) {
        if (value <= 0 || multiplier <= 0) return 0;
        return value > Integer.MAX_VALUE / multiplier ? Integer.MAX_VALUE : value * multiplier;
    }
    private static long saturatingMultiply(long value, long multiplier) {
        if (value <= 0L || multiplier <= 0L) return 0L;
        return value > Long.MAX_VALUE / multiplier ? Long.MAX_VALUE : value * multiplier;
    }
    private static int saturatingAdd(int left, int right) {
        return right > Integer.MAX_VALUE - left ? Integer.MAX_VALUE : left + right;
    }
    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }
}
