package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.minerals.MineralEntry;
import com.jdte.common.minerals.MineralProductionEngine;
import com.jdte.common.minerals.MineralSurveyData;
import com.jdte.common.minerals.MineralSurveyIndex;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MineralExtractorBE extends BaseMachineBE implements PoweredMachineBE, RedstoneControlledBE,
        FilterableBE, BaseFilterMachine, ExtendedUpgradeMachine, CoalescedAcceleratedMachine {
    public static final int SURVEY_SLOT = 0;
    public static final int OUTPUT_START_SLOT = 1;
    public static final int OUTPUT_SLOTS = 64;
    public static final int BASE_OUTPUT_SLOTS = 16;
    public static final int OUTPUT_SLOTS_PER_CAPACITY = 16;
    public static final int TOTAL_SLOTS = OUTPUT_START_SLOT + OUTPUT_SLOTS;
    public static final int UPGRADE_SLOTS = 8;

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
    private final JDTEFluidTank experienceFluidTank = new JDTEFluidTank(getMaxFluidCapacity(),
            stack -> stack.is(Registration.XP_FLUID_SOURCE.get()));
    private final JDTEFluidTank timeFluidTank = new JDTEFluidTank(getMaxFluidCapacity(),
            stack -> stack.getFluid() instanceof TimeFluid);
    private final IFluidHandler combinedFluidHandler = new CombinedFluidHandler();
    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override public int getSlotLimit(int slot) { return slot == SURVEY_SLOT ? 1 : super.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot == SURVEY_SLOT && stack.is(JDTEItems.MINERAL_SURVEY.get());
        }
        @Override protected void onContentsChanged(int slot) {
            if (slot == SURVEY_SLOT) invalidateMineralCache();
            else if (slot >= OUTPUT_START_SLOT) MachineOutputManager.submit(MineralExtractorBE.this, slot);
            setChanged();
        }
    };
    private final IItemHandler automationItemHandler = new IItemHandler() {
        @Override public int getSlots() { return OUTPUT_START_SLOT + getActiveOutputSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return valid(slot) ? itemHandler.getStackInSlot(slot) : ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot == SURVEY_SLOT ? itemHandler.insertItem(slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= OUTPUT_START_SLOT && slot < getSlots()
                    ? itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return valid(slot) ? itemHandler.getSlotLimit(slot) : 0; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot == SURVEY_SLOT && itemHandler.isItemValid(slot, stack);
        }
        private boolean valid(int slot) { return slot >= 0 && slot < getSlots(); }
    };
    private final ContainerData machineData = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> isClientSide() ? syncedProgress : progress();
                case 1 -> isClientSide() ? syncedProcessTicks : processTicks();
                case 2 -> isClientSide() ? syncedActiveOutputSlots : getActiveOutputSlots();
                case 3 -> isClientSide() ? syncedExperienceFluid : experienceFluidTank.getFluidAmount();
                case 4 -> isClientSide() ? syncedTimeFluid : timeFluidTank.getFluidAmount();
                case 5 -> isClientSide() ? syncedFluidCapacity : getMaxFluidCapacity();
                case 6 -> isClientSide() ? syncedMultiplier : getMultiplier();
                case 7 -> isClientSide() ? syncedMaxMultiplier : getMaxSelectableMultiplier();
                case 8 -> isClientSide() ? syncedState : state.ordinal();
                case 9 -> isClientSide() ? syncedFortunePercent : currentFortunePercent;
                case 10 -> isClientSide() ? syncedSurveySource : surveySource ? 1 : 0;
                case 11 -> isClientSide() ? syncedMineralCount : cachedEntries.size();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> syncedProgress = value;
                case 1 -> syncedProcessTicks = value;
                case 2 -> syncedActiveOutputSlots = value;
                case 3 -> syncedExperienceFluid = value;
                case 4 -> syncedTimeFluid = value;
                case 5 -> syncedFluidCapacity = value;
                case 6 -> syncedMultiplier = value;
                case 7 -> syncedMaxMultiplier = value;
                case 8 -> syncedState = value;
                case 9 -> syncedFortunePercent = value;
                case 10 -> syncedSurveySource = value;
                case 11 -> syncedMineralCount = value;
                default -> { }
            }
        }
        @Override public int getCount() { return 12; }
    };

    private List<MineralEntry> cachedEntries = List.of();
    private ResourceLocation cachedBiomeId;
    private long cachedIndexVersion = Long.MIN_VALUE;
    private int cachedFilterFingerprint;
    private boolean surveySource;
    private boolean staleSurvey;
    private boolean cachedSourceHadEntries;
    private long pendingBaseWork;
    private long pendingAcceleratedWork;
    private int accumulatedAcceleratedTicks;
    private int settlementTicker;
    private long lastSettlementGameTime = Long.MIN_VALUE;
    private int multiplier;
    private State state = State.IDLE;
    private int currentFortunePercent;
    private int syncedProgress;
    private int syncedProcessTicks = 20;
    private int syncedActiveOutputSlots = BASE_OUTPUT_SLOTS;
    private int syncedExperienceFluid;
    private int syncedTimeFluid;
    private int syncedFluidCapacity = 1;
    private int syncedMultiplier = 1;
    private int syncedMaxMultiplier = 32;
    private int syncedState;
    private int syncedFortunePercent;
    private int syncedSurveySource;
    private int syncedMineralCount;

    public MineralExtractorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.MINERAL_EXTRACTOR.get(), pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        tickSpeed = 1;
        multiplier = JDTEConfig.COMMON.mineralExtractor.defaultMultiplier.get();
    }

    @Override public void tickServer() {
        super.tickServer();
        syncCapacities();
        if (!isActiveRedstone()) {
            setState(State.IDLE);
            return;
        }
        advanceBaseTick();
    }

    private void advanceBaseTick() {
        int selectedMultiplier = getMultiplier();
        long acceleratedTicks = selectedMultiplier > 1 && canUseTimeFluid() ? selectedMultiplier - 1L : 0L;
        pendingBaseWork = MineralProductionEngine.accumulateWork(pendingBaseWork, 1L, 1L, maxPendingWork());
        pendingAcceleratedWork = MineralProductionEngine.accumulateWork(
                pendingAcceleratedWork, acceleratedTicks, 1L, maxPendingWork());
        settlementTicker++;
        if (settlementTicker >= JDTEConfig.COMMON.mineralExtractor.settlementInterval.get()) settle();
    }

    @Override public void accumulateAcceleratedTicks(int ticks) {
        if (ticks > 0) accumulatedAcceleratedTicks = saturatingAdd(accumulatedAcceleratedTicks, ticks);
    }

    @Override public void flushAcceleratedTicks() {
        int ticks = accumulatedAcceleratedTicks;
        accumulatedAcceleratedTicks = 0;
        if (ticks <= 0 || !isActiveRedstone()) return;
        pendingAcceleratedWork = MineralProductionEngine.accumulateWork(
                pendingAcceleratedWork, ticks, 1L, maxPendingWork());
        settle();
    }

    private void settle() {
        settlementTicker = 0;
        if (!(level instanceof ServerLevel serverLevel) || lastSettlementGameTime == level.getGameTime()) return;
        lastSettlementGameTime = level.getGameTime();
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
        long baseCycles = pendingBaseWork / processTicks;
        long acceleratedCycles = pendingAcceleratedWork / processTicks;
        long maxCycles = JDTEConfig.COMMON.mineralExtractor.maxCyclesPerSettlement.get();
        long requested = Math.min(maxCycles, saturatingAdd(baseCycles, acceleratedCycles));
        if (requested <= 0L) {
            setState(staleSurvey ? State.STALE_SURVEY : State.RUNNING);
            return;
        }

        boolean creative = UpgradeHelper.hasCreativeUpgrade(this);
        int energyPerCycle = creative ? 0 : JDTEConfig.COMMON.mineralExtractor.energyPerCycle.get();
        long energyBudget = energyPerCycle == 0 ? requested : energyStorage.getEnergyStored() / energyPerCycle;
        if (energyBudget <= 0L) {
            setState(State.NO_ENERGY);
            return;
        }
        requested = Math.min(requested, energyBudget);
        long paidBaseCycles = Math.min(baseCycles, requested);
        long wantedAcceleratedCycles = requested - paidBaseCycles;
        int timePerCycle = creative ? 0 : JDTEConfig.COMMON.mineralExtractor.timeFluidPerAcceleratedCycle.get();
        long timeBudget = timePerCycle == 0 ? wantedAcceleratedCycles : timeFluidTank.getFluidAmount() / timePerCycle;
        long paidAcceleratedCycles = Math.min(wantedAcceleratedCycles, timeBudget);
        long cycles = paidBaseCycles + paidAcceleratedCycles;
        if (cycles <= 0L) {
            setState(State.NO_TIME_FLUID);
            return;
        }

        int experiencePerCycle = creative ? 0 : JDTEConfig.COMMON.mineralExtractor.experienceFluidPerCycle.get();
        long fortuneCycles = experiencePerCycle == 0 ? cycles
                : Math.min(cycles, experienceFluidTank.getFluidAmount() / experiencePerCycle);
        int fortunePercent = fortuneCycles > 0L ? JDTEConfig.COMMON.mineralExtractor.fortuneBonusPercent.get() : 0;
        currentFortunePercent = fortunePercent;
        MineralProductionEngine.Batch normal = MineralProductionEngine.distribute(
                cachedEntries, cycles - fortuneCycles, cycles, 0, serverLevel.random);
        MineralProductionEngine.Batch fortunate = MineralProductionEngine.distribute(
                cachedEntries, fortuneCycles, cycles, fortunePercent, serverLevel.random);
        Map<ResourceLocation, Long> amounts = mergeAmounts(normal.amounts(), fortunate.amounts());
        if (amounts.isEmpty()) return;
        if (!canFit(amounts)) {
            setState(State.OUTPUT_FULL);
            return;
        }

        insertAll(amounts);
        if (!creative) {
            energyStorage.extractEnergy(safeCost(cycles, energyPerCycle), false);
            timeFluidTank.drain(safeCost(paidAcceleratedCycles, timePerCycle), IFluidHandler.FluidAction.EXECUTE);
            experienceFluidTank.drain(safeCost(fortuneCycles, experiencePerCycle), IFluidHandler.FluidAction.EXECUTE);
        }
        pendingBaseWork -= paidBaseCycles * processTicks;
        pendingAcceleratedWork -= paidAcceleratedCycles * processTicks;
        setState(staleSurvey ? State.STALE_SURVEY : State.RUNNING);
        setChanged();
    }

    private void refreshMineralCache(ServerLevel serverLevel) {
        ItemStack surveyStack = itemHandler.getStackInSlot(SURVEY_SLOT);
        MineralSurveyData survey = surveyStack.get(JDTEDataComponents.MINERAL_SURVEY.get());
        int filterFingerprint = computeFilterFingerprint();
        long indexVersion = MineralSurveyIndex.version(serverLevel.getServer());
        ResourceLocation biomeId;
        List<MineralEntry> sourceEntries;
        if (survey != null) {
            biomeId = survey.biomeId();
            sourceEntries = survey.entries();
            surveySource = true;
            staleSurvey = survey.indexVersion() != indexVersion;
        } else {
            var biome = serverLevel.getBiome(worldPosition);
            biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
            sourceEntries = MineralSurveyIndex.profile(serverLevel, biome).entries();
            surveySource = false;
            staleSurvey = false;
        }
        if (Objects.equals(cachedBiomeId, biomeId) && cachedIndexVersion == indexVersion
                && cachedFilterFingerprint == filterFingerprint && !cachedEntries.isEmpty()) return;
        cachedBiomeId = biomeId;
        cachedIndexVersion = indexVersion;
        cachedFilterFingerprint = filterFingerprint;
        cachedSourceHadEntries = !sourceEntries.isEmpty();
        cachedEntries = MineralProductionEngine.select(sourceEntries, this::allowedByFilter);
        if (cachedEntries.isEmpty() && !sourceEntries.isEmpty()) setState(State.FILTERED);
        markDirtyClient();
    }

    private int computeFilterFingerprint() {
        FilterBasicHandler handler = getFilterHandler();
        int hash = filterData.allowlist ? 1 : 0;
        int activeSlots = UpgradeHelper.getActiveFilterSlots(this, 1);
        for (int slot = 0; slot < Math.min(activeSlots, handler.getSlots()); slot++) {
            hash = 31 * hash + ItemStack.hashItemAndComponents(handler.getStackInSlot(slot));
        }
        return hash;
    }

    private boolean allowedByFilter(MineralEntry entry) {
        FilterBasicHandler handler = getFilterHandler();
        int activeSlots = UpgradeHelper.getActiveFilterSlots(this, 1);
        boolean hasFilter = false;
        boolean listed = false;
        ItemStack candidate = oreStack(entry.oreId(), 1);
        for (int slot = 0; slot < Math.min(activeSlots, handler.getSlots()); slot++) {
            ItemStack filter = handler.getStackInSlot(slot);
            if (filter.isEmpty()) continue;
            hasFilter = true;
            if (ItemStack.isSameItemSameComponents(filter, candidate)) {
                listed = true;
                break;
            }
        }
        return !hasFilter || (filterData.allowlist ? listed : !listed);
    }

    private boolean canFit(Map<ResourceLocation, Long> amounts) {
        ItemStackHandler simulation = new ItemStackHandler(getActiveOutputSlots());
        for (int slot = 0; slot < simulation.getSlots(); slot++) {
            simulation.setStackInSlot(slot, itemHandler.getStackInSlot(OUTPUT_START_SLOT + slot).copy());
        }
        for (Map.Entry<ResourceLocation, Long> entry : amounts.entrySet()) {
            long remaining = entry.getValue();
            while (remaining > 0L) {
                int amount = (int) Math.min(remaining, 64L);
                if (!ItemHandlerHelper.insertItemStacked(simulation, oreStack(entry.getKey(), amount), false).isEmpty()) {
                    return false;
                }
                remaining -= amount;
            }
        }
        return true;
    }

    private void insertAll(Map<ResourceLocation, Long> amounts) {
        for (Map.Entry<ResourceLocation, Long> entry : amounts.entrySet()) {
            long remaining = entry.getValue();
            while (remaining > 0L) {
                int amount = (int) Math.min(remaining, 64L);
                ItemStack remainder = oreStack(entry.getKey(), amount);
                for (int slot = 0; slot < getActiveOutputSlots() && !remainder.isEmpty(); slot++) {
                    remainder = itemHandler.insertItem(OUTPUT_START_SLOT + slot, remainder, false);
                }
                if (!remainder.isEmpty()) throw new IllegalStateException("Simulated mineral output no longer fits");
                remaining -= amount;
            }
        }
    }

    private static ItemStack oreStack(ResourceLocation id, int amount) {
        return BuiltInRegistries.BLOCK.getOptional(id)
                .map(block -> new ItemStack(block.asItem(), amount))
                .orElse(ItemStack.EMPTY);
    }

    private static Map<ResourceLocation, Long> mergeAmounts(Map<ResourceLocation, Long> left,
                                                             Map<ResourceLocation, Long> right) {
        Map<ResourceLocation, Long> merged = new LinkedHashMap<>(left);
        right.forEach((id, amount) -> merged.merge(id, amount, MineralExtractorBE::saturatingAdd));
        return merged;
    }

    private void invalidateMineralCache() {
        cachedBiomeId = null;
        cachedIndexVersion = Long.MIN_VALUE;
        cachedEntries = List.of();
    }

    private boolean canUseTimeFluid() {
        return UpgradeHelper.hasCreativeUpgrade(this)
                || JDTEConfig.COMMON.mineralExtractor.timeFluidPerAcceleratedCycle.get() == 0
                || timeFluidTank.getFluidAmount() > 0;
    }

    public int getActiveOutputSlots() {
        int configured = BASE_OUTPUT_SLOTS
                + UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY) * OUTPUT_SLOTS_PER_CAPACITY;
        int occupied = BASE_OUTPUT_SLOTS;
        for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
            if (!itemHandler.getStackInSlot(OUTPUT_START_SLOT + slot).isEmpty()) occupied = slot + 1;
        }
        return Math.min(OUTPUT_SLOTS, Math.max(configured, occupied));
    }

    public int getMultiplier() {
        if (UpgradeHelper.hasOverclock(this) || UpgradeHelper.hasCreativeUpgrade(this)) return getMaxSelectableMultiplier();
        return Math.clamp(multiplier, 1, getMaxSelectableMultiplier());
    }
    public void setMultiplier(int value) {
        multiplier = Math.clamp(value, 1, getMaxSelectableMultiplier());
        setChanged();
        markDirtyClient();
    }
    public int getMaxSelectableMultiplier() {
        return UpgradeHelper.hasOverclock(this) || UpgradeHelper.hasCreativeUpgrade(this)
                ? JDTEConfig.COMMON.mineralExtractor.overclockMaxMultiplier.get()
                : JDTEConfig.COMMON.mineralExtractor.maxMultiplier.get();
    }
    public State getMachineState() { return State.values()[Math.floorMod(machineData.get(8), State.values().length)]; }
    public ResourceLocation getSourceBiomeId() { return cachedBiomeId; }
    public boolean isSurveySource() { return machineData.get(10) != 0; }
    public ContainerData getMachineData() { return machineData; }
    public JDTEFluidTank getExperienceFluidTank() { return experienceFluidTank; }
    public JDTEFluidTank getTimeFluidTank() { return timeFluidTank; }
    public IFluidHandler getCombinedFluidHandler() { return combinedFluidHandler; }
    public IItemHandler getAutomationItemHandler() { return automationItemHandler; }
    public int getMaxFluidCapacity() { return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.mineralExtractor.fluidCapacity.get()); }

    private int progress() {
        long total = saturatingAdd(pendingBaseWork, pendingAcceleratedWork);
        return (int) Math.min(processTicks(), total % processTicks());
    }
    private int processTicks() { return JDTEConfig.COMMON.mineralExtractor.processTicks.get(); }
    private long maxPendingWork() { return JDTEConfig.COMMON.mineralExtractor.maxPendingWork.get(); }
    private boolean isClientSide() { return level != null && level.isClientSide; }
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
            if (tank.getFluidAmount() > capacity) tank.getFluid().setAmount(capacity);
        }
    }

    @Override public ItemStackHandler getMachineHandler() { return itemHandler; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public ContainerData getContainerData() { return poweredData; }
    @Override public int getMaxEnergy() { return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.mineralExtractor.energyCapacity.get()); }
    @Override public int getStandardEnergyCost() { return JDTEConfig.COMMON.mineralExtractor.energyPerCycle.get(); }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneData; }
    @Override public FilterData getFilterData() { return filterData; }
    @Override public FilterBasicHandler getFilterHandler() { return getData(Registration.HANDLER_BASIC_FILTER); }
    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public boolean canRun() { return true; }

    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", itemHandler.serializeNBT(provider));
        tag.put("experienceFluid", experienceFluidTank.serializeNBT(provider));
        tag.put("timeFluid", timeFluidTank.serializeNBT(provider));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putLong("pendingBaseWork", pendingBaseWork);
        tag.putLong("pendingAcceleratedWork", pendingAcceleratedWork);
        tag.putInt("settlementTicker", settlementTicker);
        tag.putInt("multiplier", multiplier);
        tag.putInt("state", state.ordinal());
    }

    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        if (tag.contains("experienceFluid")) experienceFluidTank.deserializeNBT(provider, tag.getCompound("experienceFluid"));
        if (tag.contains("timeFluid")) timeFluidTank.deserializeNBT(provider, tag.getCompound("timeFluid"));
        energyStorage.setEnergy(tag.getInt("energy"));
        pendingBaseWork = Math.clamp(tag.getLong("pendingBaseWork"), 0L, maxPendingWork());
        pendingAcceleratedWork = Math.clamp(tag.getLong("pendingAcceleratedWork"), 0L, maxPendingWork());
        settlementTicker = Math.max(0, tag.getInt("settlementTicker"));
        multiplier = tag.contains("multiplier") ? tag.getInt("multiplier")
                : JDTEConfig.COMMON.mineralExtractor.defaultMultiplier.get();
        state = State.values()[Math.floorMod(tag.getInt("state"), State.values().length)];
        invalidateMineralCache();
    }

    private final class CombinedFluidHandler implements IFluidHandler {
        @Override public int getTanks() { return 2; }
        @Override public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? experienceFluidTank.getFluid() : tank == 1 ? timeFluidTank.getFluid() : FluidStack.EMPTY;
        }
        @Override public int getTankCapacity(int tank) { return tank >= 0 && tank < 2 ? getMaxFluidCapacity() : 0; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 ? experienceFluidTank.isFluidValid(stack) : tank == 1 && timeFluidTank.isFluidValid(stack);
        }
        @Override public int fill(FluidStack stack, FluidAction action) {
            if (experienceFluidTank.isFluidValid(stack)) return experienceFluidTank.fill(stack, action);
            if (timeFluidTank.isFluidValid(stack)) return timeFluidTank.fill(stack, action);
            return 0;
        }
        @Override public FluidStack drain(FluidStack stack, FluidAction action) {
            if (experienceFluidTank.getFluid().is(stack.getFluid())) return experienceFluidTank.drain(stack, action);
            if (timeFluidTank.getFluid().is(stack.getFluid())) return timeFluidTank.drain(stack, action);
            return FluidStack.EMPTY;
        }
        @Override public FluidStack drain(int amount, FluidAction action) {
            FluidStack experience = experienceFluidTank.drain(amount, action);
            return experience.isEmpty() ? timeFluidTank.drain(amount, action) : experience;
        }
    }

    private static int safeCost(long count, int unitCost) {
        if (count <= 0L || unitCost <= 0) return 0;
        return (int) Math.min(Integer.MAX_VALUE, count * (long) unitCost);
    }
    private static int saturatingAdd(int left, int right) {
        return right > Integer.MAX_VALUE - left ? Integer.MAX_VALUE : left + right;
    }
    private static long saturatingAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }
}