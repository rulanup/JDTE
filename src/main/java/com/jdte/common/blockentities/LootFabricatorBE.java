package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.integrations.DraconicEvolutionIntegration;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.LootFabricatorUpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.fml.ModList;
import com.jdte.mixin.FluidTankAccessor;

import java.util.ArrayList;
import java.util.List;

public class LootFabricatorBE extends BaseMachineBE implements PoweredMachineBE, RedstoneControlledBE {
    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_SLOTS = 64;
    public static final int BASE_OUTPUT_SLOTS = 16;
    public static final int OUTPUT_SLOTS_PER_CAPACITY = 16;
    public static final int UPGRADE_SLOTS = 8;
    public static final int BASE_ENERGY_CAPACITY = 500_000;
    public static final int BASE_FLUID_CAPACITY = 64_000;
    public static final int DEFAULT_LIFE_FLUID_COST = 100;
    public static final int DEFAULT_BASE_TIME_FLUID_COST = 1;
    public static final int ENERGY_COST = 5_000;
    public static final int PROCESS_TIME = 20;
    public static final int MAX_TICK_SPEED = 1200;
    public static final int BOSS_COST_MULTIPLIER = 10;
    public static final int ENDER_DRAGON_COST_MULTIPLIER = 100;

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    public final JDTEFluidTank lifeFluidTank;
    public final JDTEFluidTank timeFluidTank;
    public final RedstoneControlData redstoneControlData = new RedstoneControlData();
    public final ContainerData machineData;
    private final ItemStackHandler itemHandler;
    private final LootFabricatorUpgradeItemStackHandler upgradeHandler;
    private final IFluidHandler fluidHandler;
    private final IItemHandler automationItemHandler;
    private int progress;
    private int nextInputSlot;
    private int syncedProcessTime = PROCESS_TIME;
    private int syncedActiveOutputSlots = BASE_OUTPUT_SLOTS;
    private int syncedLifeFluid;
    private int syncedTimeFluid;
    private int syncedFluidCapacity = BASE_FLUID_CAPACITY;

    public LootFabricatorBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.LOOT_FABRICATOR.get(), pos, state);
        MACHINE_SLOTS = INPUT_SLOTS + OUTPUT_SLOTS;
        tickSpeed = PROCESS_TIME;
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
        lifeFluidTank = new JDTEFluidTank(BASE_FLUID_CAPACITY, fluid -> fluid.is(JDTEFluids.LIFE_FLUID_SOURCE.get()));
        timeFluidTank = new JDTEFluidTank(BASE_FLUID_CAPACITY, fluid -> fluid.getFluid() instanceof TimeFluid);
        itemHandler = new ItemStackHandler(MACHINE_SLOTS) {
            @Override public int getSlotLimit(int slot) { return slot < INPUT_SLOTS ? 1 : 64; }
            @Override public boolean isItemValid(int slot, ItemStack stack) {
                return slot < INPUT_SLOTS ? stack.getItem() instanceof SpawnEggItem : slot < MACHINE_SLOTS;
            }
            @Override protected void onContentsChanged(int slot) { setChanged(); }
        };
        upgradeHandler = new LootFabricatorUpgradeItemStackHandler(this);
        fluidHandler = new IFluidHandler() {
            @Override public int getTanks() { return 2; }
            @Override public FluidStack getFluidInTank(int tank) { return tank == 0 ? lifeFluidTank.getFluid() : tank == 1 ? timeFluidTank.getFluid() : FluidStack.EMPTY; }
            @Override public int getTankCapacity(int tank) { return tank == 0 ? lifeFluidTank.getCapacity() : tank == 1 ? timeFluidTank.getCapacity() : 0; }
            @Override public boolean isFluidValid(int tank, FluidStack stack) { return tank == 0 ? lifeFluidTank.isFluidValid(stack) : tank == 1 && timeFluidTank.isFluidValid(stack); }
            @Override public int fill(FluidStack stack, FluidAction action) {
                if (lifeFluidTank.isFluidValid(stack)) return lifeFluidTank.fill(stack, action);
                if (timeFluidTank.isFluidValid(stack)) return timeFluidTank.fill(stack, action);
                return 0;
            }
            @Override public FluidStack drain(FluidStack stack, FluidAction action) {
                if (lifeFluidTank.getFluid().is(stack.getFluid())) return lifeFluidTank.drain(stack, action);
                if (timeFluidTank.getFluid().is(stack.getFluid())) return timeFluidTank.drain(stack, action);
                return FluidStack.EMPTY;
            }
            @Override public FluidStack drain(int amount, FluidAction action) {
                FluidStack result = lifeFluidTank.drain(amount, action);
                return result.isEmpty() ? timeFluidTank.drain(amount, action) : result;
            }
        };
        automationItemHandler = new IItemHandler() {
            @Override public int getSlots() { return INPUT_SLOTS + getActiveOutputSlots(); }
            @Override public ItemStack getStackInSlot(int slot) { return valid(slot) ? itemHandler.getStackInSlot(slot) : ItemStack.EMPTY; }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return slot >= 0 && slot < INPUT_SLOTS ? itemHandler.insertItem(slot, stack, simulate) : stack;
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return slot >= INPUT_SLOTS && slot < getSlots() ? itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
            }
            @Override public int getSlotLimit(int slot) { return valid(slot) ? itemHandler.getSlotLimit(slot) : 0; }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return slot >= 0 && slot < INPUT_SLOTS && itemHandler.isItemValid(slot, stack); }
            private boolean valid(int slot) { return slot >= 0 && slot < getSlots(); }
        };
        machineData = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> isClientSide() ? syncedProcessTime : getProcessTime();
                    case 2 -> isClientSide() ? syncedActiveOutputSlots : getActiveOutputSlots();
                    case 3 -> isClientSide() ? syncedLifeFluid : lifeFluidTank.getFluidAmount();
                    case 4 -> isClientSide() ? syncedTimeFluid : timeFluidTank.getFluidAmount();
                    case 5 -> isClientSide() ? syncedFluidCapacity : getMaxFluidCapacity();
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> syncedProcessTime = value;
                    case 2 -> syncedActiveOutputSlots = value;
                    case 3 -> syncedLifeFluid = value;
                    case 4 -> syncedTimeFluid = value;
                    case 5 -> syncedFluidCapacity = value;
                    default -> { }
                }
            }
            @Override public int getCount() { return 6; }
        };
    }

    private boolean isClientSide() {
        return level != null && level.isClientSide;
    }

    @Override public void tickServer() {
        super.tickServer();
        syncFluidCapacities();
        if (!isActiveRedstone() || !canRun()) {
            resetProgress();
            return;
        }
        processLoot();
    }

    private void processLoot() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        List<Integer> inputSlots = findInputSlots();
        int processCount = inputSlots.size();
        int energyCost = getEffectiveEnergyCost();
        int lifeFluidCost = inputSlots.stream()
                .map(slot -> itemHandler.getStackInSlot(slot))
                .mapToInt(this::getEffectiveLifeFluidCost)
                .reduce(0, LootFabricatorBE::safeAddCost);
        int timeFluidCost = inputSlots.stream()
                .map(slot -> itemHandler.getStackInSlot(slot))
                .mapToInt(this::getEffectiveTimeFluidCost)
                .reduce(0, LootFabricatorBE::safeAddCost);
        if (processCount == 0
                || lifeFluidTank.getFluidAmount() < lifeFluidCost
                || timeFluidTank.getFluidAmount() < timeFluidCost
                || !hasEnoughPower(energyCost * processCount)) {
            resetProgress();
            return;
        }
        progress++;
        if (progress < getProcessTime()) {
            setChanged();
            return;
        }

        List<ItemStack> allDrops = new ArrayList<>();
        int successfulProcesses = 0;
        int successfulLifeFluidCost = 0;
        int successfulTimeFluidCost = 0;
        for (int inputSlot : inputSlots) {
            ItemStack spawnEgg = itemHandler.getStackInSlot(inputSlot);
            List<ItemStack> drops = rollLoot(serverLevel, spawnEgg).stream()
                    .filter(drop -> !drop.isEmpty())
                    .toList();
            if (drops.isEmpty()) continue;
            allDrops.addAll(drops);
            successfulProcesses++;
            successfulLifeFluidCost = safeAddCost(successfulLifeFluidCost, getEffectiveLifeFluidCost(spawnEgg));
            successfulTimeFluidCost = safeAddCost(successfulTimeFluidCost, getEffectiveTimeFluidCost(spawnEgg));
        }
        if (successfulProcesses == 0 || allDrops.isEmpty()) {
            resetProgress();
            return;
        }
        if (!canFitAll(allDrops)) {
            resetProgress();
            return;
        }
        allDrops.forEach(this::insertOutput);
        lifeFluidTank.drain(successfulLifeFluidCost, IFluidHandler.FluidAction.EXECUTE);
        timeFluidTank.drain(successfulTimeFluidCost, IFluidHandler.FluidAction.EXECUTE);
        extractEnergy(energyCost * successfulProcesses, false);
        nextInputSlot = (inputSlots.get(inputSlots.size() - 1) + 1) % INPUT_SLOTS;
        progress = 0;
        setChanged();
    }

    private List<ItemStack> rollLoot(ServerLevel level, ItemStack eggStack) {
        SpawnEggItem egg = (SpawnEggItem) eggStack.getItem();
        Entity entity = egg.getType(eggStack).create(level);
        if (!(entity instanceof LivingEntity living)) return List.of();
        living.moveTo(getBlockPos().getCenter());
        FakePlayer player = getFakePlayer(level);
        ItemStack previous = player.getMainHandItem().copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, createLootingWeapon(level));
        try {
            var damage = level.damageSources().playerAttack(player);
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, living)
                    .withParameter(LootContextParams.ORIGIN, living.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, damage)
                    .withParameter(LootContextParams.ATTACKING_ENTITY, player)
                    .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                    .create(LootContextParamSets.ENTITY);
            List<ItemStack> drops = new ArrayList<>(level.getServer().reloadableRegistries()
                    .getLootTable(living.getLootTable()).getRandomItems(params));
            if (ModList.get().isLoaded("draconicevolution")) {
                DraconicEvolutionIntegration.addLootFabricatorDrops(living, level.random, drops);
            }
            addVanillaBossDrops(living, drops);
            return applyLootingBonus(level, drops);
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, previous);
        }
    }

    @Override public boolean canRun() { return true; }

    private static void addVanillaBossDrops(LivingEntity living, List<ItemStack> drops) {
        if (living.getType() == EntityType.WITHER && drops.stream().noneMatch(stack -> stack.is(Items.NETHER_STAR))) {
            drops.add(new ItemStack(Items.NETHER_STAR));
        }
    }

    private List<ItemStack> applyLootingBonus(ServerLevel level, List<ItemStack> baseDrops) {
        if (getLootingLevel() <= 0 || baseDrops.isEmpty()) return baseDrops;
        List<ItemStack> originalDrops = baseDrops.stream().map(ItemStack::copy).toList();
        List<ItemStack> result = new ArrayList<>(originalDrops);
        double extraDropChance = JDTEConfig.COMMON.lootingExtraDropChance.get();
        for (int levelIndex = 0; levelIndex < getLootingLevel(); levelIndex++) {
            if (level.random.nextDouble() < extraDropChance) {
                originalDrops.forEach(stack -> result.add(stack.copy()));
            }
        }
        return result;
    }

    private ItemStack createLootingWeapon(ServerLevel level) {
        ItemStack weapon = new ItemStack(Items.DIAMOND_SWORD);
        if (getLootingLevel() > 0) {
            ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            enchantments.set(level.registryAccess().holderOrThrow(Enchantments.LOOTING), getLootingLevel());
            EnchantmentHelper.setEnchantments(weapon, enchantments.toImmutable());
        }
        return weapon;
    }

    private List<Integer> findInputSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < INPUT_SLOTS; i++) {
            int slot = (nextInputSlot + i) % INPUT_SLOTS;
            if (itemHandler.getStackInSlot(slot).getItem() instanceof SpawnEggItem) slots.add(slot);
        }
        return slots;
    }

    private void resetProgress() {
        if (progress == 0) return;
        progress = 0;
        setChanged();
    }

    private boolean canFitAll(List<ItemStack> drops) {
        ItemStackHandler copy = new ItemStackHandler(getActiveOutputSlots());
        for (int i = 0; i < copy.getSlots(); i++) copy.setStackInSlot(i, itemHandler.getStackInSlot(INPUT_SLOTS + i).copy());
        for (ItemStack drop : drops) if (!ItemHandlerHelper.insertItemStacked(copy, drop.copy(), false).isEmpty()) return false;
        return true;
    }

    private void insertOutput(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int i = 0; i < getActiveOutputSlots() && !remaining.isEmpty(); i++) {
            remaining = itemHandler.insertItem(INPUT_SLOTS + i, remaining, false);
        }
    }

    public int getActiveOutputSlots() {
        int configured = BASE_OUTPUT_SLOTS + countCapacity(-1) * OUTPUT_SLOTS_PER_CAPACITY;
        int occupied = BASE_OUTPUT_SLOTS;
        for (int i = 0; i < OUTPUT_SLOTS; i++) {
            if (!itemHandler.getStackInSlot(INPUT_SLOTS + i).isEmpty()) occupied = ((i / 16) + 1) * 16;
        }
        return Math.min(OUTPUT_SLOTS, Math.max(configured, occupied));
    }
    public int getLootingLevel() { return upgradeHandler.getLootingCount(); }
    public int getProcessTime() { return Math.clamp(UpgradeHelper.getEffectiveTickSpeed(this, tickSpeed), 1, MAX_TICK_SPEED); }
    public int getTimeFluidCost() { return getConfiguredBaseTimeFluidCost() * Math.max(1, PROCESS_TIME / getProcessTime()); }
    private int getEffectiveLifeFluidCost(ItemStack spawnEgg) { return applyLootingFluidCostIncrease(getLifeFluidCost(spawnEgg), getLootingLevel()); }
    private int getEffectiveTimeFluidCost(ItemStack spawnEgg) { return applyLootingFluidCostIncrease(safeMultiplyCost(getTimeFluidCost(), getBossCostMultiplier(spawnEgg)), getLootingLevel()); }
    public static int getConfiguredLifeFluidCost() { return Math.max(1, JDTEConfig.COMMON.lootFabricatorLifeFluidCost.get()); }
    public static int getConfiguredBaseTimeFluidCost() { return Math.max(1, JDTEConfig.COMMON.lootFabricatorBaseTimeFluidCost.get()); }
    public static int getConfiguredLootingFluidCostIncreasePercent() { return Math.max(0, JDTEConfig.COMMON.lootFabricatorLootingFluidCostIncreasePercent.get()); }
    public static int getLifeFluidCost(ItemStack spawnEgg) { return safeMultiplyCost(getConfiguredLifeFluidCost(), getBossCostMultiplier(spawnEgg)); }
    public static int getBaseTimeFluidCost(ItemStack spawnEgg) { return safeMultiplyCost(getConfiguredBaseTimeFluidCost(), getBossCostMultiplier(spawnEgg)); }
    public static int getMaxTimeFluidCost(ItemStack spawnEgg) { return safeMultiplyCost(getBaseTimeFluidCost(spawnEgg), PROCESS_TIME); }
    public static int getBossCostMultiplier(ItemStack spawnEgg) {
        if (!(spawnEgg.getItem() instanceof SpawnEggItem egg)) return 1;
        EntityType<?> type = egg.getType(spawnEgg);
        if (type == EntityType.ENDER_DRAGON) return ENDER_DRAGON_COST_MULTIPLIER;
        if (type == EntityType.WITHER || type == EntityType.ELDER_GUARDIAN) return BOSS_COST_MULTIPLIER;
        return 1;
    }
    private static int applyLootingFluidCostIncrease(int cost, int lootingLevel) {
        int percent = safeAddCost(100, safeMultiplyCost(Math.max(0, lootingLevel), getConfiguredLootingFluidCostIncreasePercent()));
        return applyPercentCost(cost, percent);
    }
    private static int applyPercentCost(int cost, int percent) {
        if (cost <= 0 || percent <= 0) return 0;
        long result = ((long) cost * percent + 99L) / 100L;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }
    private static int safeAddCost(int left, int right) {
        if (left >= Integer.MAX_VALUE - right) return Integer.MAX_VALUE;
        return left + right;
    }
    private static int safeMultiplyCost(int value, int multiplier) {
        if (value <= 0 || multiplier <= 0) return 0;
        if (value > Integer.MAX_VALUE / multiplier) return Integer.MAX_VALUE;
        return value * multiplier;
    }
    private static int clampRawTickSpeed(int value) { return Math.clamp(value, 1, MAX_TICK_SPEED); }
    private int countCapacity(int ignored) { return UpgradeHelper.countUpgrades(this, UpgradeType.CAPACITY); }

    @Override public ItemStackHandler getMachineHandler() { return itemHandler; }
    public LootFabricatorUpgradeItemStackHandler getUpgradeHandler() { return upgradeHandler; }
    public JDTEFluidTank getLifeFluidTank() { return lifeFluidTank; }
    public JDTEFluidTank getTimeFluidTank() { return timeFluidTank; }
    public IFluidHandler getFluidHandler() { return fluidHandler; }
    public IItemHandler getAutomationItemHandler() { return automationItemHandler; }
    public ContainerData getMachineData() { return machineData; }
    public int getMaxFluidCapacity() { return UpgradeHelper.adjustFluidCapacity(this, BASE_FLUID_CAPACITY); }
    public int getEffectiveEnergyCost() { return UpgradeHelper.adjustEnergyCost(this, ENERGY_COST); }
    @Override public int getMaxEnergy() { return UpgradeHelper.adjustEnergyCapacity(this, BASE_ENERGY_CAPACITY); }
    @Override public ContainerData getContainerData() { return poweredMachineData; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public int getStandardEnergyCost() { return ENERGY_COST; }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneControlData; }
    @Override public BlockEntity getBlockEntity() { return this; }

    private void syncFluidCapacities() {
        int capacity = getMaxFluidCapacity();
        syncTankCapacity(lifeFluidTank, capacity);
        syncTankCapacity(timeFluidTank, capacity);
    }

    private void syncTankCapacity(JDTEFluidTank tank, int capacity) {
        if (tank instanceof FluidTankAccessor accessor) {
            accessor.jdte$setCapacity(capacity);
            if (tank.getFluidAmount() > capacity) tank.getFluid().setAmount(capacity);
        }
    }

    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", itemHandler.serializeNBT(provider));
        tag.put("upgrades", upgradeHandler.serializeNBT(provider));
        tag.put("lifeFluid", lifeFluidTank.serializeNBT(provider));
        tag.put("timeFluid", timeFluidTank.serializeNBT(provider));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("progress", progress);
        tag.putInt("nextInputSlot", nextInputSlot);
    }

    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        if (tag.contains("upgrades")) upgradeHandler.deserializeNBT(provider, tag.getCompound("upgrades"));
        if (tag.contains("lifeFluid")) lifeFluidTank.deserializeNBT(provider, tag.getCompound("lifeFluid"));
        if (tag.contains("timeFluid")) timeFluidTank.deserializeNBT(provider, tag.getCompound("timeFluid"));
        energyStorage.setEnergy(tag.getInt("energy"));
        progress = tag.getInt("progress");
        nextInputSlot = tag.getInt("nextInputSlot");
        if (!tag.contains("tickspeed") && tag.contains("tickSpeed")) tickSpeed = tag.getInt("tickSpeed");
        tickSpeed = clampRawTickSpeed(tickSpeed);
    }
}
