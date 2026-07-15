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
import com.jdte.common.recipes.GreenhouseCropDefinition;
import com.jdte.common.recipes.GreenhouseCropResolver;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GreenhouseBE extends BaseMachineBE implements PoweredMachineBE, FluidMachineBE,
        RedstoneControlledBE, ExtendedUpgradeMachine {
    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_START_SLOT = INPUT_SLOTS;
    public static final int OUTPUT_SLOTS = 64;
    public static final int BASE_OUTPUT_SLOTS = 16;
    public static final int OUTPUT_SLOTS_PER_CAPACITY = 16;
    public static final int UPGRADE_SLOTS = 8;
    public static final int TOTAL_SLOTS = INPUT_SLOTS + OUTPUT_SLOTS;
    private static final int LEGACY_TOTAL_SLOTS = 10;
    private static final int LOOT_SAMPLES_PER_SETTLEMENT = 4;

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(getMaxEnergy());
    private final PoweredMachineContainerData poweredData = new PoweredMachineContainerData(this);
    private final JDTEFluidTank fluidTank = new JDTEFluidTank(getMaxMB(), stack -> stack.getFluid() instanceof TimeFluid);
    private final FluidContainerData fluidData = new FluidContainerData(this);
    private final RedstoneControlData redstoneData = new RedstoneControlData();
    private final ItemStack[] cachedSeeds = new ItemStack[INPUT_SLOTS];
    private final GreenhouseCropDefinition[] cachedDefinitions = new GreenhouseCropDefinition[INPUT_SLOTS];
    private final ResourceLocation[] displayBlockIds = new ResourceLocation[INPUT_SLOTS];
    private final long[] growthWork = new long[INPUT_SLOTS];
    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return super.getSlotLimit(slot);
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
            setChanged();
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
                case 0 -> isClientSide() ? syncedProgress : settlementTicker;
                case 1 -> isClientSide() ? syncedProgressMax : JDTEConfig.COMMON.greenhouseSettlementInterval.get();
                case 2 -> isClientSide() ? syncedActiveOutputSlots : getActiveOutputSlots();
                case 3 -> isClientSide() ? syncedFluidAmount : fluidTank.getFluidAmount();
                case 4 -> isClientSide() ? syncedFluidCapacity : getMaxMB();
                case 5 -> isClientSide() ? syncedMultiplier : getMultiplier();
                case 6 -> isClientSide() ? syncedMaxMultiplier : getMaxSelectableMultiplier();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> syncedProgress = value;
                case 1 -> syncedProgressMax = value;
                case 2 -> syncedActiveOutputSlots = value;
                case 3 -> syncedFluidAmount = value;
                case 4 -> syncedFluidCapacity = value;
                case 5 -> syncedMultiplier = value;
                case 6 -> syncedMaxMultiplier = value;
                default -> { }
            }
        }

        @Override public int getCount() { return 7; }
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
    private int adjacentSideCursor;
    private Direction preferredOutputSide;

    public GreenhouseBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.GREENHOUSE.get(), pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        tickSpeed = 1;
        multiplier = JDTEConfig.COMMON.greenhouseDefaultSpeedMultiplier.get();
        Arrays.fill(cachedSeeds, ItemStack.EMPTY);
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        if (!isActiveRedstone() || !canRun()) {
            setActiveMask(0);
            settlementTicker = 0;
            return;
        }

        GreenhouseCropDefinition[] definitions = resolveDefinitions();
        int defined = countDefined(definitions);
        if (defined == 0) {
            setActiveMask(0);
            settlementTicker = 0;
            return;
        }

        int runnableMask = getRunnableMask(definitions);
        if (runnableMask == 0) {
            setActiveMask(0);
            settlementTicker = 0;
            return;
        }
        setActiveMask(runnableMask);

        if (++settlementTicker < JDTEConfig.COMMON.greenhouseSettlementInterval.get()) {
            return;
        }
        int elapsed = settlementTicker;
        settlementTicker = 0;
        settleProduction(definitions, defined, elapsed);
    }

    private void settleProduction(GreenhouseCropDefinition[] definitions, int defined, int elapsedTicks) {
        int maxHarvests = JDTEConfig.COMMON.greenhouseMaxHarvestsPerSettlementV2.get();
        int baseBudget = maxHarvests / defined;
        int extraBudget = maxHarvests % defined;
        int newActiveMask = 0;
        int visitedDefined = 0;

        for (int offset = 0; offset < INPUT_SLOTS; offset++) {
            int slot = (nextInputSlot + offset) % INPUT_SLOTS;
            GreenhouseCropDefinition definition = definitions[slot];
            if (definition == null) {
                continue;
            }
            int budget = baseBudget + (visitedDefined++ < extraBudget ? 1 : 0);
            settleSlot(slot, definition, elapsedTicks, Math.max(1, budget));
            if (hasResourcesForOne(slot, definition) && hasOutputSpace(definition)) {
                newActiveMask |= 1 << slot;
            }
        }

        nextInputSlot = (nextInputSlot + 1) % INPUT_SLOTS;
        setActiveMask(newActiveMask);
        setChanged();
        markDirtyClient();
    }

    private void settleSlot(int slot, GreenhouseCropDefinition definition, int elapsedTicks, int harvestBudget) {
        int parallelPlants = Math.max(1, itemHandler.getStackInSlot(slot).getCount());
        long addedWork = (long) JDTEConfig.COMMON.greenhouseBaseMultiplier.get()
                * getMultiplier() * parallelPlants * elapsedTicks;
        long availableWork = Math.min(growthWork[slot] + addedWork,
                (long) definition.growthWork() * (harvestBudget + 1L));
        int requested = (int) Math.min(availableWork / definition.growthWork(), harvestBudget);
        if (requested <= 0) {
            growthWork[slot] = availableWork;
            return;
        }

        if (!hasOutputSpace(definition)) {
            growthWork[slot] = Math.min(availableWork, definition.growthWork());
            return;
        }
        int candidate;
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            candidate = requested;
        } else {
            int energyCost = getEffectiveEnergyPerHarvest();
            int byFluid = fluidTank.getFluidAmount() / getEffectiveFluidPerHarvest(slot, definition);
            int byEnergy = energyCost == 0 ? requested : energyStorage.getEnergyStored() / energyCost;
            candidate = Math.min(requested, Math.min(byFluid, byEnergy));
        }
        if (candidate <= 0 || !(level instanceof ServerLevel serverLevel)) {
            growthWork[slot] = Math.min(availableWork, definition.growthWork());
            return;
        }

        int paidHarvests = generateAndStoreDrops(serverLevel, definition, candidate);
        if (paidHarvests <= 0) {
            growthWork[slot] = Math.min(availableWork, definition.growthWork());
            return;
        }
        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            fluidTank.drain(paidHarvests * getEffectiveFluidPerHarvest(slot, definition), IFluidHandler.FluidAction.EXECUTE);
            energyStorage.extractEnergy(paidHarvests * getEffectiveEnergyPerHarvest(), false);
        }
        growthWork[slot] = Math.min(availableWork - (long) paidHarvests * definition.growthWork(), definition.growthWork());
    }

    private GreenhouseCropDefinition[] resolveDefinitions() {
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            getDefinition(slot);
        }
        return cachedDefinitions;
    }

    private int countDefined(GreenhouseCropDefinition[] definitions) {
        int count = 0;
        for (GreenhouseCropDefinition definition : definitions) {
            if (definition != null) count++;
        }
        return count;
    }

    private int getRunnableMask(GreenhouseCropDefinition[] definitions) {
        int mask = 0;
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            GreenhouseCropDefinition definition = definitions[slot];
            if (definition != null && hasResourcesForOne(slot, definition) && hasOutputSpace(definition)) {
                mask |= 1 << slot;
            }
        }
        return mask;
    }

    private boolean hasResourcesForOne(int slot, GreenhouseCropDefinition definition) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) {
            return true;
        }
        return fluidTank.getFluidAmount() >= getEffectiveFluidPerHarvest(slot, definition)
                && energyStorage.getEnergyStored() >= getEffectiveEnergyPerHarvest();
    }

    private boolean hasOutputSpace(GreenhouseCropDefinition definition) {
        if (definition.outputs().isEmpty()) return false;
        ItemStack primary = definition.outputs().getFirst();
        if (getInsertableCount(primary) >= primary.getCount()) return true;
        if (!(level instanceof ServerLevel serverLevel)) return false;
        IItemHandler adjacent = findAdjacentOutputTarget(serverLevel, primary, false);
        return adjacent != null && canRouteScaledDrops(adjacent, List.of(primary), 1);
    }

    private int getInsertableCount(ItemStack output) {
        int capacity = 0;
        int end = OUTPUT_START_SLOT + getActiveOutputSlots();
        for (int slot = OUTPUT_START_SLOT; slot < end; slot++) {
            ItemStack existing = itemHandler.getStackInSlot(slot);
            if (existing.isEmpty()) {
                capacity += Math.min(output.getMaxStackSize(), itemHandler.getSlotLimit(slot));
            } else if (ItemStack.isSameItemSameComponents(existing, output)) {
                capacity += Math.max(0, Math.min(existing.getMaxStackSize(), itemHandler.getSlotLimit(slot)) - existing.getCount());
            }
        }
        return capacity;
    }

    private int generateAndStoreDrops(ServerLevel serverLevel, GreenhouseCropDefinition definition, int harvests) {
        int samples = Math.min(LOOT_SAMPLES_PER_SETTLEMENT, harvests);
        int baseGroup = harvests / samples;
        int extraGroups = harvests % samples;
        int completed = 0;
        ItemStack tool = createFortuneTool(serverLevel);
        IItemHandler adjacent = findAdjacentOutputTarget(serverLevel, definition.outputs().getFirst(), true);
        for (int sample = 0; sample < samples; sample++) {
            int groupHarvests = baseGroup + (sample < extraGroups ? 1 : 0);
            List<ItemStack> drops = generateSingleHarvest(serverLevel, definition, tool);
            int fitted = fitRepetitions(adjacent, drops, groupHarvests);
            if (fitted > 0 && insertScaledDrops(adjacent, internalOutputHandler, drops, fitted)) {
                completed += fitted;
            }
        }
        return completed;
    }

    private List<ItemStack> generateSingleHarvest(ServerLevel serverLevel, GreenhouseCropDefinition definition, ItemStack tool) {
        if (!definition.useLootTable()) return definition.outputs();
        BlockState matureState = getMatureState(definition.harvestBlock());
        List<ItemStack> drops = matureState == null ? List.of() : Block.getDrops(
                matureState, serverLevel, worldPosition, null, FakePlayerFactory.getMinecraft(serverLevel), tool);
        if (drops.isEmpty()) return definition.outputs();
        List<ItemStack> result = new ArrayList<>(drops.size());
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) result.add(drop.copy());
        }
        return result.isEmpty() ? definition.outputs() : result;
    }

    private int fitRepetitions(IItemHandler adjacent, List<ItemStack> drops, int maximum) {
        int low = 0;
        int high = maximum;
        while (low < high) {
            int middle = low + (high - low + 1) / 2;
            if (canRouteScaledDrops(adjacent, drops, middle)) low = middle;
            else high = middle - 1;
        }
        return low;
    }

    private boolean canRouteScaledDrops(IItemHandler adjacent, List<ItemStack> drops, int repetitions) {
        IItemHandler adjacentSnapshot = adjacent == null ? null : snapshotHandler(adjacent);
        IItemHandler internalSnapshot = snapshotHandler(internalOutputHandler);
        return insertScaledDrops(adjacentSnapshot, internalSnapshot, drops, repetitions);
    }

    private ItemStackHandler snapshotHandler(IItemHandler source) {
        ItemStackHandler snapshot = new ItemStackHandler(source.getSlots()) {
            @Override public int getSlotLimit(int slot) { return source.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return source.isItemValid(slot, stack); }
        };
        for (int slot = 0; slot < source.getSlots(); slot++) {
            snapshot.setStackInSlot(slot, source.getStackInSlot(slot).copy());
        }
        return snapshot;
    }

    private boolean insertScaledDrops(IItemHandler adjacent, IItemHandler internal,
                                      List<ItemStack> drops, int repetitions) {
        for (ItemStack drop : drops) {
            long remaining = (long) drop.getCount() * repetitions;
            while (remaining > 0) {
                int amount = (int) Math.min(remaining, drop.getMaxStackSize());
                ItemStack remainder = drop.copyWithCount(amount);
                if (adjacent != null) {
                    remainder = ItemHandlerHelper.insertItemStacked(adjacent, remainder, false);
                }
                if (!remainder.isEmpty()) {
                    remainder = ItemHandlerHelper.insertItemStacked(internal, remainder, false);
                }
                if (!remainder.isEmpty()) return false;
                remaining -= amount;
            }
        }
        return true;
    }

    private IItemHandler findAdjacentOutputTarget(ServerLevel serverLevel, ItemStack sample, boolean scanAllSides) {
        if (preferredOutputSide != null) {
            IItemHandler preferred = getAdjacentItemHandler(serverLevel, preferredOutputSide);
            if (preferred != null && acceptsAny(preferred, sample)) return preferred;
            preferredOutputSide = null;
        }

        Direction[] directions = Direction.values();
        int attempts = scanAllSides ? directions.length : 1;
        for (int attempt = 0; attempt < attempts; attempt++) {
            Direction side = directions[adjacentSideCursor++ % directions.length];
            IItemHandler target = getAdjacentItemHandler(serverLevel, side);
            if (target != null && acceptsAny(target, sample)) {
                preferredOutputSide = side;
                return target;
            }
        }
        return null;
    }

    private IItemHandler getAdjacentItemHandler(ServerLevel serverLevel, Direction side) {
        return serverLevel.getCapability(Capabilities.ItemHandler.BLOCK,
                worldPosition.relative(side), side.getOpposite());
    }

    private boolean acceptsAny(IItemHandler target, ItemStack sample) {
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, sample.copy(), true);
        return remainder.getCount() < sample.getCount();
    }

    private ItemStack createFortuneTool(ServerLevel serverLevel) {
        ItemStack tool = new ItemStack(Items.DIAMOND_HOE);
        int fortune = Math.min(3, UpgradeHelper.countUpgrades(this, UpgradeType.FORTUNE));
        if (fortune > 0) {
            ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            enchantments.set(serverLevel.registryAccess().holderOrThrow(Enchantments.FORTUNE), fortune);
            EnchantmentHelper.setEnchantments(tool, enchantments.toImmutable());
        }
        return tool;
    }

    private GreenhouseCropDefinition getDefinition(int slot) {
        ItemStack seed = itemHandler.getStackInSlot(slot);
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
        ResourceLocation displayBlockId = displayBlockIds[slot];
        BlockState state = getMatureState(displayBlockId);
        if (state == null) return null;
        Block block = state.getBlock();
        boolean active = (activeMask & 1 << slot) != 0;
        if (block instanceof CropBlock crop) {
            int maxAge = crop.getMaxAge();
            int age = active && level != null
                    ? (int) ((level.getGameTime() % 20L) * (maxAge + 1L) / 20L) : 0;
            return crop.getStateForAge(Math.min(maxAge, age));
        }
        for (var property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty && "age".equals(property.getName())) {
                int maxAge = integerProperty.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                int age = active && level != null
                        ? (int) ((level.getGameTime() % 20L) * (maxAge + 1L) / 20L) : 0;
                return state.setValue(integerProperty, Math.min(maxAge, age));
            }
        }
        return state;
    }

    private BlockState getMatureState(ResourceLocation blockId) {
        if (blockId == null || !BuiltInRegistries.BLOCK.containsKey(blockId)) return null;
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        if (block instanceof CropBlock crop) return crop.getStateForAge(crop.getMaxAge());
        BlockState state = block.defaultBlockState();
        for (var property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty && "age".equals(property.getName())) {
                int maxAge = integerProperty.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                return state.setValue(integerProperty, maxAge);
            }
        }
        return state;
    }

    public boolean isActive() { return activeMask != 0; }
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
        return (int) Math.min(Integer.MAX_VALUE,
                (long) reducedBase * getStackFluidMultiplier(itemHandler.getStackInSlot(slot)));
    }
    private int getStackFluidMultiplier(ItemStack stack) {
        int halfStack = Math.max(1, stack.getMaxStackSize() / 2);
        return Math.max(1, (stack.getCount() + halfStack - 1) / halfStack);
    }
    public int getActiveOutputSlots() {
        int configured = BASE_OUTPUT_SLOTS
                + UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY) * OUTPUT_SLOTS_PER_CAPACITY;
        int occupied = BASE_OUTPUT_SLOTS;
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            if (!itemHandler.getStackInSlot(OUTPUT_START_SLOT + i).isEmpty()) occupied = i + 1;
        }
        return Math.min(OUTPUT_SLOTS, Math.max(configured, occupied));
    }
    private boolean isClientSide() { return level != null && level.isClientSide; }
    public ContainerData getGreenhouseData() { return greenhouseData; }
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
        tag.put("inventory", itemHandler.serializeNBT(provider));
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
        } else if (tag.contains("growthWork", Tag.TAG_LONG)) {
            growthWork[0] = Math.max(0L, tag.getLong("growthWork"));
        }
        settlementTicker = Math.max(0, tag.getInt("settlementTicker"));
        nextInputSlot = Math.floorMod(tag.getInt("nextInputSlot"), INPUT_SLOTS);
        activeMask = tag.contains("activeMask") ? tag.getInt("activeMask") : tag.getBoolean("active") ? 1 : 0;
        multiplier = tag.contains("multiplier") ? tag.getInt("multiplier")
                : JDTEConfig.COMMON.greenhouseDefaultSpeedMultiplier.get();
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            String key = "displayBlock" + slot;
            if (tag.contains(key)) displayBlockIds[slot] = ResourceLocation.tryParse(tag.getString(key));
        }
        if (displayBlockIds[0] == null && tag.contains("displayBlock")) {
            displayBlockIds[0] = ResourceLocation.tryParse(tag.getString("displayBlock"));
        }
        Arrays.fill(cachedSeeds, ItemStack.EMPTY);
        Arrays.fill(cachedDefinitions, null);
    }

    private void loadInventory(CompoundTag inventoryTag, HolderLookup.Provider provider) {
        if (inventoryTag.getInt("Size") != LEGACY_TOTAL_SLOTS) {
            itemHandler.deserializeNBT(provider, inventoryTag);
            return;
        }
        ItemStackHandler legacy = new ItemStackHandler(LEGACY_TOTAL_SLOTS);
        legacy.deserializeNBT(provider, inventoryTag);
        itemHandler.setStackInSlot(0, legacy.getStackInSlot(0));
        for (int slot = 1; slot < LEGACY_TOTAL_SLOTS; slot++) {
            itemHandler.setStackInSlot(OUTPUT_START_SLOT + slot - 1, legacy.getStackInSlot(slot));
        }
    }
}
