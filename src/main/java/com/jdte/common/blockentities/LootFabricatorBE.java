package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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

import java.util.List;

public class LootFabricatorBE extends BaseMachineBE implements PoweredMachineBE, RedstoneControlledBE, CustomUpgradeMachine {
    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_SLOTS = 64;
    public static final int BASE_OUTPUT_SLOTS = 16;
    public static final int OUTPUT_SLOTS_PER_CAPACITY = 16;
    public static final int UPGRADE_SLOTS = 18;
    public static final int BASE_ENERGY_CAPACITY = 500_000;
    public static final int BASE_FLUID_CAPACITY = 64_000;
    public static final int LIFE_FLUID_COST = 1_000;
    public static final int BASE_TIME_FLUID_COST = 10;
    public static final int ENERGY_COST = 5_000;
    public static final int PROCESS_TIME = 20;

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    public final JDTEFluidTank lifeFluidTank;
    public final JDTEFluidTank timeFluidTank;
    public final RedstoneControlData redstoneControlData = new RedstoneControlData();
    public final ContainerData machineData;
    private final ItemStackHandler itemHandler;
    private final ItemStackHandler upgradeHandler;
    private final IFluidHandler fluidHandler;
    private final IItemHandler automationItemHandler;
    private int progress;
    private int nextInputSlot;
    private int syncedProcessTime = PROCESS_TIME;
    private int syncedActiveOutputSlots = BASE_OUTPUT_SLOTS;
    private int syncedLifeFluid;
    private int syncedTimeFluid;

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
                return slot < INPUT_SLOTS && stack.getItem() instanceof SpawnEggItem;
            }
            @Override protected void onContentsChanged(int slot) { setChanged(); }
        };
        upgradeHandler = new ItemStackHandler(UPGRADE_SLOTS) {
            @Override public int getSlotLimit(int slot) { return 1; }
            @Override public boolean isItemValid(int slot, ItemStack stack) {
                if (stack.getItem() instanceof LootingUpgradeItem) return countLooting(slot) < 3;
                return stack.getItem() instanceof UpgradeCardItem card && card.getType() == UpgradeType.CAPACITY
                        && countCapacity(slot) < UpgradeType.CAPACITY.getMaxPerMachine();
            }
            @Override protected void onContentsChanged(int slot) { setChanged(); }
        };
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
                    default -> { }
                }
            }
            @Override public int getCount() { return 5; }
        };
    }

    private boolean isClientSide() {
        return level != null && level.isClientSide;
    }

    @Override public void tickServer() {
        super.tickServer();
        if (!isActiveRedstone() || !canRun()) return;
        processLoot();
    }

    private void processLoot() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int inputSlot = findInputSlot();
        if (inputSlot < 0 || lifeFluidTank.getFluidAmount() < LIFE_FLUID_COST
                || timeFluidTank.getFluidAmount() < getTimeFluidCost()
                || !hasEnoughPower(ENERGY_COST)) {
            progress = 0;
            return;
        }
        progress++;
        if (progress < getProcessTime()) return;

        List<ItemStack> drops = rollLoot(serverLevel, itemHandler.getStackInSlot(inputSlot));
        if (!canFitAll(drops)) {
            progress = 0;
            return;
        }
        drops.forEach(this::insertOutput);
        lifeFluidTank.drain(LIFE_FLUID_COST, IFluidHandler.FluidAction.EXECUTE);
        timeFluidTank.drain(getTimeFluidCost(), IFluidHandler.FluidAction.EXECUTE);
        extractEnergy(ENERGY_COST, false);
        nextInputSlot = (inputSlot + 1) % INPUT_SLOTS;
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
            return level.getServer().reloadableRegistries().getLootTable(living.getLootTable()).getRandomItems(params);
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, previous);
        }
    }

    @Override public boolean canRun() { return true; }

    private ItemStack createLootingWeapon(ServerLevel level) {
        ItemStack weapon = new ItemStack(Items.DIAMOND_SWORD);
        if (getLootingLevel() > 0) {
            ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            enchantments.set(level.registryAccess().holderOrThrow(Enchantments.LOOTING), getLootingLevel());
            EnchantmentHelper.setEnchantments(weapon, enchantments.toImmutable());
        }
        return weapon;
    }

    private int findInputSlot() {
        for (int i = 0; i < INPUT_SLOTS; i++) {
            int slot = (nextInputSlot + i) % INPUT_SLOTS;
            if (itemHandler.getStackInSlot(slot).getItem() instanceof SpawnEggItem) return slot;
        }
        return -1;
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
    public int getLootingLevel() { return Math.min(3, countLooting(-1)); }
    public int getProcessTime() { return Math.clamp(tickSpeed, 1, PROCESS_TIME); }
    public int getTimeFluidCost() { return BASE_TIME_FLUID_COST * Math.max(1, PROCESS_TIME / getProcessTime()); }
    private int countLooting(int ignored) { return countUpgrade(ignored, true); }
    private int countCapacity(int ignored) { return countUpgrade(ignored, false); }
    private int countUpgrade(int ignored, boolean looting) {
        int count = 0;
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            if (i == ignored) continue;
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (looting ? stack.getItem() instanceof LootingUpgradeItem
                    : stack.getItem() instanceof UpgradeCardItem card && card.getType() == UpgradeType.CAPACITY) count++;
        }
        return count;
    }

    @Override public ItemStackHandler getMachineHandler() { return itemHandler; }
    public ItemStackHandler getUpgradeHandler() { return upgradeHandler; }
    public JDTEFluidTank getLifeFluidTank() { return lifeFluidTank; }
    public JDTEFluidTank getTimeFluidTank() { return timeFluidTank; }
    public IFluidHandler getFluidHandler() { return fluidHandler; }
    public IItemHandler getAutomationItemHandler() { return automationItemHandler; }
    public ContainerData getMachineData() { return machineData; }
    @Override public int getMaxEnergy() { return BASE_ENERGY_CAPACITY; }
    @Override public ContainerData getContainerData() { return poweredMachineData; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public int getStandardEnergyCost() { return ENERGY_COST; }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneControlData; }
    @Override public BlockEntity getBlockEntity() { return this; }

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
    }
}
