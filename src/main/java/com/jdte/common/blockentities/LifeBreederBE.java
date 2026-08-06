package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LifeBreederBE extends BaseMachineBE implements AreaAffectingBE, PoweredMachineBE,
        FluidMachineBE, FilterableBE, RedstoneControlledBE, ExtendedUpgradeMachine {
    public static final int FEED_SLOTS = 4;
    public static final int OUTPUT_START_SLOT = FEED_SLOTS;
    public static final int OUTPUT_SLOTS = 8;
    public static final int TOTAL_SLOTS = FEED_SLOTS + OUTPUT_SLOTS;
    public static final int UPGRADE_SLOTS = 8;

    public enum Mode {
        BREED_AND_GROW,
        BREED_ONLY,
        GROW_ONLY
    }

    private final AreaAffectingData areaData;
    private final FilterData filterData = new FilterData();
    private final RedstoneControlData redstoneData = new RedstoneControlData();
    private final PoweredMachineContainerData poweredData = new PoweredMachineContainerData(this);
    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(getMaxEnergy());
    private final JDTEFluidTank fluidTank = new JDTEFluidTank(getMaxMB(), stack ->
            stack.getFluid() == JDTEFluids.LIFE_FLUID_SOURCE.get()
                    || stack.getFluid() == JDTEFluids.LIFE_FLUID_FLOWING.get());
    private final FluidContainerData fluidData = new FluidContainerData(this);
    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < TOTAL_SLOTS && !stack.isEmpty();
        }
        @Override protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final IItemHandler automationItemHandler = new IItemHandler() {
        @Override public int getSlots() { return TOTAL_SLOTS; }
        @Override public ItemStack getStackInSlot(int slot) { return valid(slot) ? itemHandler.getStackInSlot(slot) : ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot >= 0 && slot < FEED_SLOTS ? itemHandler.insertItem(slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= OUTPUT_START_SLOT && slot < TOTAL_SLOTS
                    ? itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return valid(slot) ? itemHandler.getSlotLimit(slot) : 0; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < FEED_SLOTS && itemHandler.isItemValid(slot, stack);
        }
        private boolean valid(int slot) { return slot >= 0 && slot < TOTAL_SLOTS; }
    };
    private final ContainerData breederData = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> isClientSide() ? syncedFluidAmount : fluidTank.getFluidAmount();
                case 1 -> isClientSide() ? syncedFluidCapacity : getMaxMB();
                case 2 -> isClientSide() ? syncedMultiplier : getMultiplier();
                case 3 -> isClientSide() ? syncedMaxMultiplier : getMaxSelectableMultiplier();
                case 4 -> isClientSide() ? syncedMode : mode.ordinal();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) syncedFluidAmount = value;
            else if (index == 1) syncedFluidCapacity = value;
            else if (index == 2) syncedMultiplier = value;
            else if (index == 3) syncedMaxMultiplier = value;
            else if (index == 4) syncedMode = value;
        }
        @Override public int getCount() { return 5; }
    };

    private Mode mode = Mode.BREED_AND_GROW;
    private int multiplier;
    private int cycleTicker;
    private final Set<EntityType<?>> cachedFilterTypes = Collections.newSetFromMap(new IdentityHashMap<>());
    private int filterFingerprint;
    private boolean cachedFilterAllowlist;
    private int syncedFluidAmount;
    private int syncedFluidCapacity = 1;
    private int syncedMultiplier = 1;
    private int syncedMaxMultiplier = 32;
    private int syncedMode;

    public LifeBreederBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.LIFE_BREEDER.get(), pos, state);
        areaData = new AreaAffectingData(state.getValue(BlockStateProperties.FACING).getOpposite());
        MACHINE_SLOTS = TOTAL_SLOTS;
        tickSpeed = 1;
        multiplier = JDTEConfig.COMMON.lifeBreederDefaultSpeedMultiplier.get();
    }

    @Override public void tickServer() {
        super.tickServer();
        if (!isActiveRedstone() || ++cycleTicker < JDTEConfig.COMMON.lifeBreederProcessingInterval.get()) return;
        cycleTicker = 0;
        if (!(level instanceof ServerLevel serverLevel)) return;

        UpgradeHelper.syncCapacities(this);
        boolean changed = collectDrops(serverLevel);
        boolean creative = UpgradeHelper.hasCreativeUpgrade(this);
        boolean hasResources = creative || energyStorage.getEnergyStored() > 0 && fluidTank.getFluidAmount() > 0;
        boolean needsGrowth = mode != Mode.BREED_ONLY;
        boolean needsBreeding = mode != Mode.GROW_ONLY && hasAnyFeed();
        if (!hasResources || !needsGrowth && !needsBreeding) {
            if (changed) setChanged();
            return;
        }

        refreshEntityFilterCache();
        int inspectLimit = JDTEConfig.COMMON.lifeBreederMaxEntitiesInspected.get();
        List<AgeableMob> animals = new ArrayList<>(inspectLimit);
        serverLevel.getEntities(EntityTypeTest.forClass(AgeableMob.class), getAABB(worldPosition),
                animal -> animal.isAlive() && isAllowedByEntityFilter(animal), animals, inspectLimit);
        if (needsGrowth) changed |= accelerateAges(animals);
        if (needsBreeding) changed |= breedAnimals(serverLevel, animals);
        if (changed) setChanged();
    }

    private boolean accelerateAges(List<AgeableMob> animals) {
        int remaining = JDTEConfig.COMMON.lifeBreederMaxAnimalsGrownPerCycle.get();
        int interval = JDTEConfig.COMMON.lifeBreederProcessingInterval.get();
        boolean instant = UpgradeHelper.hasOverclock(this) || UpgradeHelper.hasCreativeUpgrade(this);
        int normalWork = Math.max(1, interval * getMultiplier());
        boolean changed = false;
        for (AgeableMob animal : animals) {
            if (remaining <= 0) break;
            int age = animal.getAge();
            if (age == 0) continue;
            int work = instant ? Math.abs(age) : Math.min(Math.abs(age), normalWork);
            if (!payGrowth(work)) break;
            animal.setAge(age < 0 ? Math.min(0, age + work) : Math.max(0, age - work));
            changed = true;
            remaining--;
        }
        return changed;
    }

    private boolean payGrowth(int biologicalTicks) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return true;
        long energyCost = (long) biologicalTicks * JDTEConfig.COMMON.lifeBreederEnergyPerGrowthTick.get();
        int divisor = JDTEConfig.COMMON.lifeBreederGrowthTicksPerMb.get();
        long baseFluidCost = (biologicalTicks + (long) divisor - 1L) / divisor;
        long fluidCost = baseFluidCost * JDTEConfig.COMMON.lifeBreederFluidCostMultiplierV3.get();
        if (energyCost > energyStorage.getEnergyStored() || fluidCost > fluidTank.getFluidAmount()) return false;
        energyStorage.extractEnergy((int) Math.min(Integer.MAX_VALUE, energyCost), false);
        fluidTank.drain((int) Math.min(Integer.MAX_VALUE, fluidCost), IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    private boolean breedAnimals(ServerLevel serverLevel, List<AgeableMob> animals) {
        Map<EntityType<?>, List<Animal>> byType = new HashMap<>();
        List<Villager> villagers = new ArrayList<>();
        for (AgeableMob animal : animals) {
            if (animal instanceof Animal breedable) {
                byType.computeIfAbsent(breedable.getType(), ignored -> new ArrayList<>()).add(breedable);
            } else if (animal instanceof Villager villager) {
                villagers.add(villager);
            }
        }
        int pairBudget = JDTEConfig.COMMON.lifeBreederMaxPairsPerCycle.get();
        boolean changed = false;
        int densityLimit = JDTEConfig.COMMON.lifeBreederMaxAnimalsPerType.get();
        for (List<Animal> sameType : byType.values()) {
            if (pairBudget <= 0) break;
            if (densityLimit > 0 && sameType.size() >= densityLimit) continue;
            for (int first = 0; first < sameType.size() && pairBudget > 0; first++) {
                Animal parentA = sameType.get(first);
                if (!parentA.canFallInLove()) continue;
                for (int second = first + 1; second < sameType.size(); second++) {
                    Animal parentB = sameType.get(second);
                    if (!parentB.canFallInLove() || !hasFoodFor(parentA, parentB)) continue;
                    parentA.setInLoveTime(1);
                    parentB.setInLoveTime(1);
                    boolean compatible = parentA.canMate(parentB);
                    parentA.setInLoveTime(0);
                    parentB.setInLoveTime(0);
                    if (!compatible || !payBreeding(JDTEConfig.COMMON.lifeBreederBreedingCooldownTicks.get())) continue;
                    consumeFood(parentA, parentB);
                    parentA.setInLove(null);
                    parentB.setInLove(null);
                    parentA.spawnChildFromBreeding(serverLevel, parentB);
                    changed = true;
                    pairBudget--;
                    break;
                }
            }
        }
        if (pairBudget > 0) changed |= breedVillagers(serverLevel, villagers, pairBudget, densityLimit);
        return changed;
    }

    private boolean payBreeding(int cooldownTicks) {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return true;
        int energyCost = JDTEConfig.COMMON.lifeBreederBreedEnergyCost.get();
        long fluidCost = getBreedingFluidCost(cooldownTicks);
        if (!canPayBreeding(cooldownTicks)) return false;
        energyStorage.extractEnergy(energyCost, false);
        fluidTank.drain((int) Math.min(Integer.MAX_VALUE, fluidCost), IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    private boolean canPayBreeding(int cooldownTicks) {
        return UpgradeHelper.hasCreativeUpgrade(this)
                || energyStorage.getEnergyStored() >= JDTEConfig.COMMON.lifeBreederBreedEnergyCost.get()
                && fluidTank.getFluidAmount() >= getBreedingFluidCost(cooldownTicks);
    }

    private long getBreedingFluidCost(int cooldownTicks) {
        int divisor = JDTEConfig.COMMON.lifeBreederGrowthTicksPerMb.get();
        long timeEquivalent = (Math.max(0, cooldownTicks) + (long) divisor - 1L) / divisor;
        long baseFluidCost = Math.max(JDTEConfig.COMMON.lifeBreederBreedFluidCost.get(), timeEquivalent);
        return baseFluidCost * JDTEConfig.COMMON.lifeBreederFluidCostMultiplierV3.get();
    }

    private boolean breedVillagers(ServerLevel level, List<Villager> villagers, int pairBudget, int densityLimit) {
        if (densityLimit > 0 && villagers.size() >= densityLimit) return false;
        boolean changed = false;
        for (int first = 0; first < villagers.size() && pairBudget > 0; first++) {
            Villager parentA = villagers.get(first);
            if (parentA.getAge() != 0 || parentA.isSleeping()) continue;
            for (int second = first + 1; second < villagers.size(); second++) {
                Villager parentB = villagers.get(second);
                if (parentB.getAge() != 0 || parentB.isSleeping() || !hasVillagerFood()) continue;
                int cooldown = JDTEConfig.COMMON.lifeBreederBreedingCooldownTicks.get();
                if (!canPayBreeding(cooldown)) return changed;
                Villager child = parentA.getBreedOffspring(level, parentB);
                BabyEntitySpawnEvent event = new BabyEntitySpawnEvent(parentA, parentB, child);
                if (NeoForge.EVENT_BUS.post(event).isCanceled() || !(event.getChild() instanceof Villager baby)) continue;
                if (!payBreeding(cooldown)) return changed;
                consumeVillagerFood();
                parentA.setAge(cooldown);
                parentB.setAge(cooldown);
                baby.setAge(-24000);
                baby.moveTo(parentA.getX(), parentA.getY(), parentA.getZ(), 0.0F, 0.0F);
                level.addFreshEntityWithPassengers(baby);
                level.broadcastEntityEvent(parentA, (byte) 18);
                level.broadcastEntityEvent(parentB, (byte) 18);
                changed = true;
                pairBudget--;
                break;
            }
        }
        return changed;
    }

    private boolean hasVillagerFood() {
        int points = 0;
        for (int slot = 0; slot < FEED_SLOTS; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            points += stack.getCount() * villagerFoodPoints(stack);
            if (points >= 24) return true;
        }
        return false;
    }

    private void consumeVillagerFood() {
        int remainingPoints = 24;
        for (int slot = 0; slot < FEED_SLOTS && remainingPoints > 0; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            int points = villagerFoodPoints(stack);
            if (points <= 0) continue;
            int count = Math.min(stack.getCount(), (remainingPoints + points - 1) / points);
            itemHandler.extractItem(slot, count, false);
            remainingPoints -= count * points;
        }
    }

    private static int villagerFoodPoints(ItemStack stack) {
        if (stack.is(Items.BREAD)) return 4;
        if (stack.is(Items.CARROT) || stack.is(Items.POTATO) || stack.is(Items.BEETROOT)) return 1;
        return 0;
    }

    private void refreshEntityFilterCache() {
        FilterBasicHandler handler = getFilterHandler();
        int fingerprint = filterData.allowlist ? 1 : 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            fingerprint = 31 * fingerprint + ItemStack.hashItemAndComponents(stack);
        }
        if (fingerprint == filterFingerprint && cachedFilterAllowlist == filterData.allowlist) return;
        filterFingerprint = fingerprint;
        cachedFilterAllowlist = filterData.allowlist;
        cachedFilterTypes.clear();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.getItem() instanceof SpawnEggItem egg) cachedFilterTypes.add(egg.getType(stack));
        }
    }

    private boolean isAllowedByEntityFilter(AgeableMob entity) {
        if (cachedFilterTypes.isEmpty()) return true;
        boolean listed = cachedFilterTypes.contains(entity.getType());
        return cachedFilterAllowlist ? listed : !listed;
    }

    private boolean hasFoodFor(Animal first, Animal second) {
        int firstSlot = findFoodSlot(first, -1);
        if (firstSlot < 0) return false;
        return findFoodSlot(second, itemHandler.getStackInSlot(firstSlot).getCount() >= 2 ? -1 : firstSlot) >= 0;
    }

    private void consumeFood(Animal first, Animal second) {
        int firstSlot = findFoodSlot(first, -1);
        if (firstSlot < 0) return;
        itemHandler.extractItem(firstSlot, 1, false);
        int secondSlot = findFoodSlot(second, -1);
        if (secondSlot >= 0) itemHandler.extractItem(secondSlot, 1, false);
    }

    private int findFoodSlot(Animal animal, int excludedSlot) {
        for (int slot = 0; slot < FEED_SLOTS; slot++) {
            if (slot == excludedSlot) continue;
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && animal.isFood(stack)) return slot;
        }
        return -1;
    }

    private boolean collectDrops(ServerLevel serverLevel) {
        if (!hasOutputSpace()) return false;
        int limit = JDTEConfig.COMMON.lifeBreederMaxDropsCollectedPerCycle.get();
        if (limit <= 0) return false;
        List<ItemEntity> drops = new ArrayList<>(limit);
        serverLevel.getEntities(EntityTypeTest.forClass(ItemEntity.class), getAABB(worldPosition),
                ItemEntity::isAlive, drops, limit);
        if (drops.isEmpty()) return false;
        boolean changed = false;
        for (ItemEntity drop : drops) {
            ItemStack remainder = insertOutput(drop.getItem());
            if (remainder.isEmpty()) {
                drop.discard();
                changed = true;
            } else if (remainder.getCount() != drop.getItem().getCount()) {
                drop.setItem(remainder);
                changed = true;
            }
        }
        return changed;
    }

    private boolean hasOutputSpace() {
        for (int slot = OUTPUT_START_SLOT; slot < TOTAL_SLOTS; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty() || stack.getCount() < Math.min(stack.getMaxStackSize(), itemHandler.getSlotLimit(slot))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyFeed() {
        for (int slot = 0; slot < FEED_SLOTS; slot++) {
            if (!itemHandler.getStackInSlot(slot).isEmpty()) return true;
        }
        return false;
    }

    private ItemStack insertOutput(ItemStack stack) {
        ItemStack remainder = stack;
        for (int slot = OUTPUT_START_SLOT; slot < TOTAL_SLOTS && !remainder.isEmpty(); slot++) {
            remainder = itemHandler.insertItem(slot, remainder, false);
        }
        return remainder;
    }

    public int getMultiplier() { return isClientSide() ? syncedMultiplier : Math.clamp(multiplier, 1, getMaxSelectableMultiplier()); }
    public void setMultiplier(int value) {
        multiplier = Math.clamp(value, 1, getMaxSelectableMultiplier());
        setChanged();
        markDirtyClient();
    }
    public int getMaxSelectableMultiplier() {
        if (isClientSide()) return Math.max(1, syncedMaxMultiplier);
        return JDTEConfig.COMMON.lifeBreederMaxSpeedMultiplier.get();
    }
    public Mode getMode() {
        return isClientSide() ? Mode.values()[Math.floorMod(syncedMode, Mode.values().length)] : mode;
    }
    public void setMode(int value) {
        mode = Mode.values()[Math.floorMod(value, Mode.values().length)];
        setChanged();
        markDirtyClient();
    }
    private boolean isClientSide() { return level != null && level.isClientSide; }

    public ContainerData getBreederData() { return breederData; }
    public IItemHandler getAutomationItemHandler() { return automationItemHandler; }
    @Override public ItemStackHandler getMachineHandler() { return itemHandler; }
    @Override public AreaAffectingData getAreaAffectingData() { return areaData; }
    @Override public FilterData getFilterData() { return filterData; }
    @Override public FilterBasicHandler getFilterHandler() { return getData(Registration.HANDLER_BASIC_FILTER); }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneData; }
    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public ContainerData getContainerData() { return poweredData; }
    @Override public int getMaxEnergy() { return UpgradeHelper.adjustEnergyCapacity(this, JDTEConfig.COMMON.lifeBreederEnergyCapacity.get()); }
    @Override public int getStandardEnergyCost() { return JDTEConfig.COMMON.lifeBreederBreedEnergyCost.get(); }
    @Override public JDTEFluidTank getFluidTank() { return fluidTank; }
    @Override public FluidContainerData getFluidContainerData() { return fluidData; }
    @Override public int getMaxMB() { return UpgradeHelper.adjustFluidCapacity(this, JDTEConfig.COMMON.lifeBreederFluidCapacity.get()); }
    @Override public boolean canRun() { return true; }

    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", itemHandler.serializeNBT(provider));
        tag.put("fluid", fluidTank.serializeNBT(provider));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("mode", mode.ordinal());
        tag.putInt("multiplier", multiplier);
        tag.putInt("cycleTicker", cycleTicker);
    }

    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        if (tag.contains("fluid")) fluidTank.deserializeNBT(provider, tag.getCompound("fluid"));
        if (tag.contains("energy")) energyStorage.setEnergy(tag.getInt("energy"));
        mode = Mode.values()[Math.floorMod(tag.getInt("mode"), Mode.values().length)];
        multiplier = tag.contains("multiplier") ? tag.getInt("multiplier")
                : JDTEConfig.COMMON.lifeBreederDefaultSpeedMultiplier.get();
        cycleTicker = Math.max(0, tag.getInt("cycleTicker"));
    }
}
