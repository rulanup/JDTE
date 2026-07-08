package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class AdvancedPotionBrewerBE extends BaseMachineBE implements PoweredMachineBE, RedstoneControlledBE {
    public static final int BOTTLE_SLOT_0 = 0;
    public static final int BOTTLE_SLOT_1 = 1;
    public static final int BOTTLE_SLOT_2 = 2;
    public static final int INGREDIENT_SLOT = 3;
    public static final int FUEL_SLOT = 4;
    public static final int TOTAL_SLOTS = 5;
    public static final int FUEL_PER_BLAZE = 20;
    public static final int BREW_TIME = 400;

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    protected final ItemStackHandler itemHandler;
    protected int fuel = 0;
    protected int brewProgress = 0;
    protected boolean hasValidIngredients = false;
    public final ContainerData brewerData;

    public AdvancedPotionBrewerBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_POTION_BREWER.get(), pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        tickSpeed = 1;
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
        brewerData = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> brewProgress;
                    case 1 -> BREW_TIME;
                    case 2 -> fuel;
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {
                if (index == 0) brewProgress = value;
                if (index == 2) fuel = value;
            }
            @Override public int getCount() { return 3; }
        };
        itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (slot == INGREDIENT_SLOT || slot == FUEL_SLOT) {
                    hasValidIngredients = checkIngredients();
                }
            }
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot >= BOTTLE_SLOT_0 && slot <= BOTTLE_SLOT_2) {
                    return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)
                            || stack.is(Items.GLASS_BOTTLE);
                }
                if (slot == INGREDIENT_SLOT) {
                    return !stack.isEmpty();
                }
                if (slot == FUEL_SLOT) {
                    return stack.is(Items.BLAZE_POWDER);
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
        if (isActiveRedstone() && canRun()) {
            brew();
        }
    }

    protected void brew() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (fuel <= 0) {
            ItemStack fuelStack = itemHandler.getStackInSlot(FUEL_SLOT);
            if (!fuelStack.isEmpty() && fuelStack.is(Items.BLAZE_POWDER)) {
                fuelStack.shrink(1);
                itemHandler.setStackInSlot(FUEL_SLOT, fuelStack);
                fuel = FUEL_PER_BLAZE;
                setChanged();
            }
        }

        if (fuel <= 0) {
            brewProgress = 0;
            return;
        }

        if (!hasValidIngredients) {
            brewProgress = 0;
            return;
        }

        int energyCost = getEffectiveEnergyCost();
        if (energyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this) && !hasEnoughPower(energyCost)) {
            brewProgress = 0;
            return;
        }

        brewProgress++;
        if (brewProgress < BREW_TIME) {
            setChanged();
            return;
        }

        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            if (energyCost > 0) {
                extractEnergy(energyCost, false);
            }
        }

        ItemStack ingredient = itemHandler.getStackInSlot(INGREDIENT_SLOT);
        ingredient.shrink(1);
        itemHandler.setStackInSlot(INGREDIENT_SLOT, ingredient);

        for (int i = BOTTLE_SLOT_0; i <= BOTTLE_SLOT_2; i++) {
            ItemStack bottleStack = itemHandler.getStackInSlot(i);
            if (bottleStack.isEmpty()) continue;
            ItemStack result = getBrewingResult(bottleStack, ingredient);
            if (!result.isEmpty()) {
                itemHandler.setStackInSlot(i, result);
            }
        }

        fuel--;
        brewProgress = 0;
        hasValidIngredients = checkIngredients();
        setChanged();
    }

    private ItemStack getBrewingResult(ItemStack input, ItemStack ingredient) {
        PotionBrewing brewing = level.potionBrewing();
        if (brewing.isIngredient(ingredient) && brewing.hasMix(input, ingredient)) {
            return brewing.mix(ingredient, input.copy());
        }
        return ItemStack.EMPTY;
    }

    private boolean checkIngredients() {
        ItemStack ingredient = itemHandler.getStackInSlot(INGREDIENT_SLOT);
        if (ingredient.isEmpty()) return false;

        if (!(level instanceof ServerLevel serverLevel)) return false;
        PotionBrewing brewing = serverLevel.potionBrewing();
        for (int i = BOTTLE_SLOT_0; i <= BOTTLE_SLOT_2; i++) {
            ItemStack bottle = itemHandler.getStackInSlot(i);
            if (!bottle.isEmpty() && brewing.isIngredient(ingredient) && brewing.hasMix(bottle, ingredient)) {
                return true;
            }
        }
        return false;
    }

    public int getFuel() { return fuel; }
    public int getBrewProgress() { return brewProgress; }

    @Override
    public RedstoneControlData getRedstoneControlData() {
        return redstoneControlData;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public int getMaxEnergy() {
        return UpgradeHelper.adjustEnergyCapacity(this, 50000);
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
        return getEffectiveEnergyCost();
    }

    public int getEffectiveEnergyCost() {
        if (UpgradeHelper.hasCreativeUpgrade(this)) return 0;
        return UpgradeHelper.adjustEnergyCost(this, 300);
    }

    @Override
    public boolean hasEnoughPower(int energyCost) {
        return PoweredMachineBE.super.hasEnoughPower(energyCost);
    }

    @Override
    public int extractEnergy(int energy, boolean simulate) {
        return PoweredMachineBE.super.extractEnergy(energy, simulate);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", itemHandler.serializeNBT(provider));
        tag.putInt("fuel", fuel);
        tag.putInt("brewProgress", brewProgress);
        tag.putInt("energy", energyStorage.getEnergyStored());
        saveRedstoneSettings(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        }
        fuel = tag.getInt("fuel");
        brewProgress = tag.getInt("brewProgress");
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
        loadRedstoneSettings(tag);
        hasValidIngredients = checkIngredients();
    }
}
