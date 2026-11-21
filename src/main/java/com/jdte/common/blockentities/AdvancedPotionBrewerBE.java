package com.jdte.common.blockentities;

import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import com.jdte.common.capabilities.MachineAutomationHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineContainerData;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.common.upgrades.AdvancedPotionBrewerUpgradeItemStackHandler;
import com.jdte.common.upgrades.JDTEFluidTank;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.integrations.ae2.AE2PatternProviderInputGuard;
import com.jdte.common.utils.InfusionFluidHelper;
import com.jdte.mixin.FluidTankAccessor;
import com.jdte.setup.JDTEBlockEntities;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.fml.ModList;

import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.Map;

public class AdvancedPotionBrewerBE extends BaseMachineBE implements PoweredMachineBE, RedstoneControlledBE, CoalescedAcceleratedMachine {
    public static final int BOTTLE_SLOT_0 = 0;
    public static final int BOTTLE_SLOT_1 = 1;
    public static final int BOTTLE_SLOT_2 = 2;
    public static final int INGREDIENT_SLOT = 3;
    public static final int FUEL_SLOT = 4;
    public static final int EXTRA_INGREDIENT_SLOT_START = 5;
    public static final int EXTRA_INGREDIENT_SLOT_COUNT = 5;
    public static final int OUTPUT_SLOT_START = EXTRA_INGREDIENT_SLOT_START + EXTRA_INGREDIENT_SLOT_COUNT;
    public static final int OUTPUT_SLOT_COUNT = 3;
    public static final int TOTAL_SLOTS = OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT;
    public static final int FUEL_PER_BLAZE = 20;
    public static final int BREW_TIME = 400;
    public static final int MIN_ACCELERATED_BREW_TIME = 1;
    public static final int BASE_FLUID_CAPACITY = 8000;
    public static final int BASE_ENERGY_COST = 300;
    private static final int TIME_WAND_256X_RATE = 256;
    private static final int TIME_WAND_EFFECT_TICKS = 600;
    private static final int TIME_WAND_256X_BREW_STEPS = (TIME_WAND_EFFECT_TICKS * TIME_WAND_256X_RATE) / BREW_TIME;

    public final MachineEnergyStorage energyStorage;
    public final PoweredMachineContainerData poweredMachineData;
    public final JDTEFluidTank waterFluidTank;
    public final JDTEFluidTank timeFluidTank;
    public final ContainerData waterFluidData;
    public final ContainerData timeFluidData;
    public RedstoneControlData redstoneControlData = new RedstoneControlData();
    protected final MachineInventoryHandler itemHandler;
    private final AdvancedPotionBrewerUpgradeItemStackHandler upgradeHandler;
    protected final ResourceHandler<ItemResource> automationItemHandler;
    protected final Map<Direction, ResourceHandler<ItemResource>> sidedAutomationItemHandlers = new EnumMap<>(Direction.class);
    protected final ResourceHandler<FluidResource> fluidHandler;
    protected int fuel = 0;
    protected int brewProgress = 0;
    protected int activeBrewTime = BREW_TIME;
    protected int activeTimeFluidCost = 0;
    protected boolean sequenceActive = false;
    protected int nextIngredientOrder = 0;
    protected int activeIngredientOrder = -1;
    protected int processedBottleMask = 0;
    protected boolean recipeLocked = false;
    protected boolean fuelInputEnabled = true;
    protected final NonNullList<ItemStack> lockedRecipeTemplates = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    protected boolean hasValidIngredients = false;
    public final ContainerData brewerData;

    public AdvancedPotionBrewerBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.ADVANCED_POTION_BREWER.get(), pos, state);
        MACHINE_SLOTS = TOTAL_SLOTS;
        tickSpeed = 1;
        upgradeHandler = new AdvancedPotionBrewerUpgradeItemStackHandler(this);
        energyStorage = new MachineEnergyStorage(getMaxEnergy());
        poweredMachineData = new PoweredMachineContainerData(this);
        waterFluidTank = createTank(f -> f.is(Fluids.WATER));
        timeFluidTank = createTank(f -> f.getFluid() instanceof TimeFluid);
        waterFluidData = createFluidData(waterFluidTank);
        timeFluidData = createFluidData(timeFluidTank);
        brewerData = new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> brewProgress;
                    case 1 -> getDisplayedBrewTime();
                    case 2 -> fuel;
                    case 3 -> recipeLocked ? 1 : 0;
                    case 4 -> fuelInputEnabled ? 1 : 0;
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {
                if (index == 0) brewProgress = value;
                if (index == 1) activeBrewTime = Math.max(MIN_ACCELERATED_BREW_TIME, value);
                if (index == 2) fuel = value;
                if (index == 3) recipeLocked = value != 0;
                if (index == 4) fuelInputEnabled = value != 0;
            }
            @Override public int getCount() { return 5; }
        };
        fluidHandler = new CombinedResourceHandler<>(waterFluidTank, timeFluidTank);
        itemHandler = new MachineInventoryHandler(TOTAL_SLOTS) {
            @Override
            protected void onContentsChanged(int slot, ItemStack previous) {
                setChanged();
                if (slot != FUEL_SLOT) {
                    hasValidIngredients = checkIngredients();
                }
            }
            @Override
            public boolean isValid(int slot, ItemResource resource) {
                return resource.isEmpty() || isValidForMachineSlot(slot, resource.toStack(1));
            }
            @Override protected int getCapacity(int slot, ItemResource resource) {
                return isBottleSlot(slot) || isOutputSlot(slot) ? 1 : super.getCapacity(slot, resource);
            }
        };
        automationItemHandler = createAutomationItemHandler(null);
        for (Direction direction : Direction.values()) {
            sidedAutomationItemHandlers.put(direction, createAutomationItemHandler(direction));
        }
    }

    private ResourceHandler<ItemResource> createAutomationItemHandler(Direction accessSide) {
        return new MachineAutomationHandler<>(itemHandler, slot -> !isOutputSlot(slot), this::isOutputSlot) {
            @Override public boolean isValid(int slot, ItemResource resource) {
                return resource.isEmpty() || isValidForAutomationSlot(slot, resource.toStack(1), accessSide);
            }
        };
    }

    private boolean isValidForAutomationSlot(int slot, ItemStack stack, Direction accessSide) {
        if (!isValidSlotIndex(slot) || isOutputSlot(slot) || !isValidForMachineSlot(slot, stack)) return false;
        if (slot == FUEL_SLOT) return canAutomateFuelFrom(accessSide);
        // Exporters usually try ingredient slot 3 before fuel slot 4. Reserve
        // powder for fuel unless a locked brewing step explicitly requests it.
        if (isIngredientSlot(slot) && stack.is(Items.BLAZE_POWDER)
                && fuelInputEnabled && !hasEnergyBrewingUpgrade()) {
            return recipeLocked && !getLockedRecipeTemplate(slot).isEmpty()
                    && ItemStack.isSameItemSameComponents(getLockedRecipeTemplate(slot), stack);
        }
        return true;
    }

    private boolean canAutomateFuelFrom(Direction accessSide) {
        if (!fuelInputEnabled) {
            return false;
        }
        // Unspecified-side access (for example SFM slot targeting) has no
        // adjacent source block to inspect, but still honors the fuel toggle.
        if (accessSide == null) return true;
        if (!JDTEConfig.COMMON.potionBrewerRejectPatternProviderFuelInput.get()
                || level == null || !ModList.get().isLoaded("ae2")) {
            return true;
        }
        return !AE2PatternProviderInputGuard.isPatternProviderAt(level, worldPosition.relative(accessSide));
    }

    private JDTEFluidTank createTank(java.util.function.Predicate<FluidStack> validator) {
        return new JDTEFluidTank(getMaxMB(), validator) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
    }

    private ContainerData createFluidData(JDTEFluidTank tank) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> BuiltInRegistries.FLUID.getId(tank.getFluid().getFluid());
                    case 1 -> tank.getFluidAmount() & 0xFFFF;
                    case 2 -> tank.getFluidAmount() >> 16;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> tank.setFluid(new FluidStack(BuiltInRegistries.FLUID.byId(value), tank.getFluidAmount()));
                    case 1 -> tank.getFluid().setAmount((tank.getFluidAmount() & 0xFFFF0000) | (value & 0xFFFF));
                    case 2 -> tank.getFluid().setAmount((tank.getFluidAmount() & 0xFFFF) | (value << 16));
                    default -> {
                    }
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    private boolean isValidForMachineSlot(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (isBottleSlot(slot)) {
            return isAllowedByRecipeLock(slot, stack) && isPotionInput(stack);
        }
        if (isIngredientSlot(slot)) {
            return isAllowedByRecipeLock(slot, stack) && isBrewingIngredient(stack);
        }
        if (slot == FUEL_SLOT) {
            // 能量酿造升级安装后燃料改由 FE 支付，烈焰粉槽停用
            return !hasEnergyBrewingUpgrade() && stack.is(Items.BLAZE_POWDER);
        }
        return false;
    }

    private boolean isBottleSlot(int slot) {
        return slot >= BOTTLE_SLOT_0 && slot <= BOTTLE_SLOT_2;
    }

    private boolean isIngredientSlot(int slot) {
        return slot == INGREDIENT_SLOT || (slot >= EXTRA_INGREDIENT_SLOT_START && slot < EXTRA_INGREDIENT_SLOT_START + EXTRA_INGREDIENT_SLOT_COUNT);
    }

    private boolean isOutputSlot(int slot) {
        return slot >= OUTPUT_SLOT_START && slot < OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT;
    }

    private boolean isValidSlotIndex(int slot) {
        return slot >= 0 && slot < itemHandler.size();
    }

    private boolean isRecipeLockedSlot(int slot) {
        return isIngredientSlot(slot);
    }

    private boolean isAllowedByRecipeLock(int slot, ItemStack stack) {
        if (!recipeLocked || !isRecipeLockedSlot(slot)) {
            return true;
        }
        ItemStack template = getLockedRecipeTemplate(slot);
        return template.isEmpty() || ItemStack.isSameItemSameComponents(template, stack);
    }

    private boolean isPotionInput(ItemStack stack) {
        if (level != null && level.potionBrewing().isInput(stack)) {
            return true;
        }
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION) || stack.is(Items.GLASS_BOTTLE);
    }

    private boolean isBrewingIngredient(ItemStack stack) {
        return level != null && level.potionBrewing().isIngredient(stack);
    }

    @Override
    public ItemStacksResourceHandler getMachineHandler() {
        return itemHandler;
    }

    public AdvancedPotionBrewerUpgradeItemStackHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    /** 是否安装了能量酿造升级（酿造燃料改由 FE 支付）。 */
    public boolean hasEnergyBrewingUpgrade() {
        return upgradeHandler.hasEnergyBrewingUpgrade();
    }

    public ResourceHandler<ItemResource> getAutomationItemHandler() {
        return automationItemHandler;
    }

    public ResourceHandler<ItemResource> getAutomationItemHandler(Direction side) {
        return side == null ? automationItemHandler : sidedAutomationItemHandlers.get(side);
    }

    @Override
    public void tickServer() {
        super.tickServer();
        UpgradeHelper.syncCapacities(this);
        syncFluidCapacities();
        if (isActiveRedstone()) {
            try {
                brew();
            } catch (ConcurrentModificationException ignored) {
                // Some mods synchronize brewing recipes from the client thread during login.
                // Preserve the current operation and retry after their registry update completes.
            }
        } else {
            resetBrewProgress();
        }
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
        if (ticks <= 0 || !isActiveRedstone()) {
            return;
        }
        processAcceleratedBrewing(ticks);
    }

    protected void processAcceleratedBrewing(int ticks) {
        if (!(level instanceof ServerLevel)) return;
        int stepTime = Math.max(1, activeBrewTime > 0 ? activeBrewTime : BREW_TIME);
        long totalProgress = (long) brewProgress + ticks;
        long cycles = totalProgress / stepTime;
        brewProgress = (int) (totalProgress % stepTime);
        if (cycles <= 0) {
            setChanged();
            return;
        }

        int maxCycles = (int) Math.min(cycles, 128);
        for (int i = 0; i < maxCycles; i++) {
            brewProgress = activeBrewTime;
            try {
                brew();
            } catch (ConcurrentModificationException ignored) {
                break;
            }
            if (!hasValidIngredients && activeIngredientOrder < 0 && !sequenceActive) {
                break;
            }
        }
        setChanged();
    }

    protected void brew() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        PotionBrewing brewing = serverLevel.potionBrewing();
        if (fillGlassBottlesFromWaterTank()) {
            hasValidIngredients = checkIngredients();
        }

        if (activeIngredientOrder < 0) {
            if (!sequenceActive && !canStartSequence(brewing)) {
                hasValidIngredients = false;
                resetBrewProgress();
                return;
            }

            int nextOrder = findNextApplicableIngredientOrder(brewing, nextIngredientOrder);
            if (nextOrder >= 0) {
                activeIngredientOrder = nextOrder;
            } else if (sequenceActive) {
                if (finishSequenceToOutputs()) {
                    hasValidIngredients = checkIngredients();
                } else {
                    hasValidIngredients = false;
                }
                setChanged();
                return;
            } else {
                hasValidIngredients = false;
                resetBrewProgress();
                return;
            }
        }

        if (!canApplyIngredientOrder(brewing, activeIngredientOrder)) {
            activeIngredientOrder = -1;
            hasValidIngredients = checkIngredients();
            resetBrewProgress();
            return;
        }

        if (fuel <= 0) {
            if (hasEnergyBrewingUpgrade()) {
                // 能量酿造升级：以 FE 替代烈焰粉购买一次充能（20 次酿造）
                int cost = JDTEConfig.COMMON.potionBrewerEnergyPerBlazePowder.get();
                boolean creative = UpgradeHelper.hasCreativeUpgrade(this);
                if (creative || cost <= 0 || energyStorage.extractEnergy(cost, true) == cost) {
                    if (!creative && cost > 0) {
                        energyStorage.extractEnergy(cost, false);
                    }
                    fuel = FUEL_PER_BLAZE;
                    setChanged();
                }
            } else {
                ItemStack fuelStack = itemHandler.copyStack(FUEL_SLOT);
                if (!fuelStack.isEmpty() && fuelStack.is(Items.BLAZE_POWDER)) {
                    fuelStack.shrink(1);
                    itemHandler.setStack(FUEL_SLOT, fuelStack);
                    fuel = FUEL_PER_BLAZE;
                    setChanged();
                }
            }
        }

        if (brewProgress == 0 && fuel <= 0) {
            resetBrewProgress();
            return;
        }

        if (brewProgress == 0) {
            prepareActiveBrewTiming();
        } else if (!canPayActiveTimeFluidCost()) {
            activeBrewTime = BREW_TIME;
            activeTimeFluidCost = 0;
        }

        int energyCost = getEffectiveEnergyCost();
        if (energyCost > 0 && !UpgradeHelper.hasCreativeUpgrade(this) && !hasEnoughPower(energyCost)) {
            resetBrewProgress();
            return;
        }

        if (brewProgress == 0) {
            fuel--;
        }
        brewProgress++;
        if (brewProgress < activeBrewTime) {
            setChanged();
            return;
        }

        if (!UpgradeHelper.hasCreativeUpgrade(this)) {
            if (energyCost > 0) {
                extractEnergy(energyCost, false);
            }
            if (activeTimeFluidCost > 0) {
                timeFluidTank.extractFluid(activeTimeFluidCost);
            }
        }

        if (!applyActiveIngredient(brewing)) {
            activeIngredientOrder = -1;
            resetBrewProgress();
            hasValidIngredients = checkIngredients();
            return;
        }

        brewProgress = 0;
        resetActiveBrewTiming();
        hasValidIngredients = checkIngredients();
        setChanged();
    }

    private boolean fillGlassBottlesFromWaterTank() {
        if (waterFluidTank.getFluidAmount() < InfusionFluidHelper.BOTTLE_FLUID_AMOUNT || !waterFluidTank.getFluid().is(Fluids.WATER)) {
            return false;
        }

        boolean filled = false;
        for (int slot = BOTTLE_SLOT_0; slot <= BOTTLE_SLOT_2; slot++) {
            if (waterFluidTank.getFluidAmount() < InfusionFluidHelper.BOTTLE_FLUID_AMOUNT) {
                break;
            }
            ItemStack stack = itemHandler.copyStack(slot);
            if (!stack.is(Items.GLASS_BOTTLE)) {
                continue;
            }
            waterFluidTank.extractFluid(InfusionFluidHelper.BOTTLE_FLUID_AMOUNT);
            ItemStack waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
            waterBottle.setCount(1);
            itemHandler.setStack(slot, waterBottle);
            filled = true;
        }

        if (filled) {
            setChanged();
        }
        return filled;
    }

    private void resetBrewProgress() {
        if (brewProgress != 0 || activeBrewTime != BREW_TIME || activeTimeFluidCost != 0) {
            brewProgress = 0;
            resetActiveBrewTiming();
            setChanged();
        }
    }

    private void prepareActiveBrewTiming() {
        int configuredBrewTime = getConfiguredBrewTime();
        int timeFluidCost = getTimeFluidCostForBrewTime(configuredBrewTime);
        if (timeFluidCost > 0 && !hasEnoughTimeFluid(timeFluidCost)) {
            activeBrewTime = BREW_TIME;
            activeTimeFluidCost = 0;
            return;
        }
        activeBrewTime = configuredBrewTime;
        activeTimeFluidCost = UpgradeHelper.hasCreativeUpgrade(this) ? 0 : timeFluidCost;
    }

    private boolean canPayActiveTimeFluidCost() {
        return activeTimeFluidCost <= 0 || hasEnoughTimeFluid(activeTimeFluidCost);
    }

    private boolean hasEnoughTimeFluid(int amount) {
        return UpgradeHelper.hasCreativeUpgrade(this)
                || amount <= 0
                || timeFluidTank.getFluidAmount() >= amount;
    }

    private int getDisplayedBrewTime() {
        return brewProgress > 0 ? activeBrewTime : getAvailableBrewTime();
    }

    private int getAvailableBrewTime() {
        int configuredBrewTime = getConfiguredBrewTime();
        int timeFluidCost = getTimeFluidCostForBrewTime(configuredBrewTime);
        return timeFluidCost <= 0 || hasEnoughTimeFluid(timeFluidCost) ? configuredBrewTime : BREW_TIME;
    }

    private int getConfiguredBrewTime() {
        return Math.max(MIN_ACCELERATED_BREW_TIME, Math.min(tickSpeed, BREW_TIME));
    }

    private int getTimeFluidCostForBrewTime(int brewTime) {
        if (UpgradeHelper.hasCreativeUpgrade(this) || brewTime >= BREW_TIME) {
            return 0;
        }
        int savedTicks = brewTime <= MIN_ACCELERATED_BREW_TIME ? BREW_TIME : BREW_TIME - brewTime;
        double timeWand256xCost = TIME_WAND_256X_RATE * Config.TIMEWAND_FLUID_COST.get() * JDTEConfig.COMMON.timeAcceleratorFluidCostMultiplier.get();
        double fullBrewStepCost = timeWand256xCost / TIME_WAND_256X_BREW_STEPS;
        return Math.max(1, (int) Math.ceil(fullBrewStepCost * savedTicks / BREW_TIME));
    }

    private void resetActiveBrewTiming() {
        activeBrewTime = BREW_TIME;
        activeTimeFluidCost = 0;
    }

    private ItemStack getBrewingResult(ItemStack input, ItemStack ingredient) {
        PotionBrewing brewing = level.potionBrewing();
        if (brewing.isIngredient(ingredient) && brewing.hasMix(input, ingredient)) {
            return brewing.mix(ingredient, input.copy());
        }
        return ItemStack.EMPTY;
    }

    private boolean checkIngredients() {
        if (!(level instanceof ServerLevel serverLevel)) return false;
        PotionBrewing brewing = serverLevel.potionBrewing();
        if (activeIngredientOrder >= 0) {
            return canApplyIngredientOrder(brewing, activeIngredientOrder);
        }
        if (sequenceActive) {
            return findNextApplicableIngredientOrder(brewing, nextIngredientOrder) >= 0 || canInsertSequenceOutputs();
        }
        return canStartSequence(brewing);
    }

    private boolean canStartSequence(PotionBrewing brewing) {
        return findNextApplicableIngredientOrder(brewing, 0) >= 0 && canInsertSimulatedSequenceOutputs(brewing);
    }

    private int findNextApplicableIngredientOrder(PotionBrewing brewing, int startOrder) {
        int[] ingredientSlots = getIngredientSlots();
        for (int order = Math.max(0, startOrder); order < ingredientSlots.length; order++) {
            if (canApplyIngredientOrder(brewing, order)) {
                return order;
            }
        }
        return -1;
    }

    private boolean canApplyIngredientOrder(PotionBrewing brewing, int order) {
        int ingredientSlot = getIngredientSlotByOrder(order);
        if (ingredientSlot < 0) {
            return false;
        }
        ItemStack ingredient = itemHandler.copyStack(ingredientSlot);
        if (ingredient.isEmpty() || !brewing.isIngredient(ingredient) || !isAllowedByRecipeLock(ingredientSlot, ingredient)) {
            return false;
        }
        for (int i = BOTTLE_SLOT_0; i <= BOTTLE_SLOT_2; i++) {
            ItemStack bottle = itemHandler.copyStack(i);
            if (!bottle.isEmpty() && brewing.hasMix(bottle, ingredient)) {
                return true;
            }
        }
        return false;
    }

    private boolean applyActiveIngredient(PotionBrewing brewing) {
        int ingredientSlot = getIngredientSlotByOrder(activeIngredientOrder);
        if (ingredientSlot < 0) {
            return false;
        }
        ItemStack ingredientStack = itemHandler.copyStack(ingredientSlot);
        if (ingredientStack.isEmpty() || !brewing.isIngredient(ingredientStack) || !isAllowedByRecipeLock(ingredientSlot, ingredientStack)) {
            return false;
        }

        ItemStack ingredient = ingredientStack.copyWithCount(1);
        boolean applied = false;
        for (int i = BOTTLE_SLOT_0; i <= BOTTLE_SLOT_2; i++) {
            ItemStack bottleStack = itemHandler.copyStack(i);
            if (bottleStack.isEmpty()) continue;
            ItemStack result = getBrewingResult(bottleStack, ingredient);
            if (!result.isEmpty()) {
                itemHandler.setStack(i, result.copyWithCount(1));
                processedBottleMask |= 1 << (i - BOTTLE_SLOT_0);
                applied = true;
            }
        }

        if (!applied) {
            return false;
        }

        ingredientStack.shrink(1);
        itemHandler.setStack(ingredientSlot, ingredientStack);
        sequenceActive = true;
        nextIngredientOrder = activeIngredientOrder + 1;
        activeIngredientOrder = -1;
        return true;
    }

    private boolean canInsertSimulatedSequenceOutputs(PotionBrewing brewing) {
        NonNullList<ItemStack> simulated = NonNullList.withSize(OUTPUT_SLOT_COUNT, ItemStack.EMPTY);
        int mask = simulateSequence(brewing, 0, simulated);
        return mask != 0 && canInsertOutputs(mask, simulated);
    }

    private int simulateSequence(PotionBrewing brewing, int startOrder, NonNullList<ItemStack> simulatedBottles) {
        for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) {
            simulatedBottles.set(i, itemHandler.copyStack(BOTTLE_SLOT_0 + i).copy());
        }

        int mask = 0;
        int[] ingredientSlots = getIngredientSlots();
        for (int order = Math.max(0, startOrder); order < ingredientSlots.length; order++) {
            int ingredientSlot = ingredientSlots[order];
            ItemStack ingredientStack = itemHandler.copyStack(ingredientSlot);
            if (ingredientStack.isEmpty() || !brewing.isIngredient(ingredientStack) || !isAllowedByRecipeLock(ingredientSlot, ingredientStack)) {
                continue;
            }
            ItemStack ingredient = ingredientStack.copyWithCount(1);
            boolean applied = false;
            for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) {
                ItemStack bottle = simulatedBottles.get(i);
                if (bottle.isEmpty() || !brewing.hasMix(bottle, ingredient)) {
                    continue;
                }
                ItemStack result = brewing.mix(ingredient, bottle.copy());
                if (result.isEmpty()) {
                    continue;
                }
                simulatedBottles.set(i, result.copyWithCount(1));
                mask |= 1 << i;
                applied = true;
            }
            if (!applied) continue;
        }
        return mask;
    }

    private boolean finishSequenceToOutputs() {
        if (!canInsertSequenceOutputs()) {
            return false;
        }
        for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) {
            if ((processedBottleMask & (1 << i)) == 0) {
                continue;
            }
            int bottleSlot = BOTTLE_SLOT_0 + i;
            ItemStack bottleStack = itemHandler.copyStack(bottleSlot);
            if (bottleStack.isEmpty()) {
                continue;
            }
            insertOutput(OUTPUT_SLOT_START + i, bottleStack.copy());
            itemHandler.setStack(bottleSlot, ItemStack.EMPTY);
        }
        clearSequenceState();
        return true;
    }

    private boolean canInsertSequenceOutputs() {
        NonNullList<ItemStack> outputs = NonNullList.withSize(OUTPUT_SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) {
            outputs.set(i, itemHandler.copyStack(BOTTLE_SLOT_0 + i).copy());
        }
        return canInsertOutputs(processedBottleMask, outputs);
    }

    private boolean canInsertOutputs(int mask, NonNullList<ItemStack> outputs) {
        for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) {
            if ((mask & (1 << i)) == 0) {
                continue;
            }
            ItemStack output = outputs.get(i);
            if (output.isEmpty()) {
                continue;
            }
            if (!canInsertOutput(OUTPUT_SLOT_START + i, output)) {
                return false;
            }
        }
        return true;
    }

    private boolean canInsertOutput(int slot, ItemStack stack) {
        if (!isAllowedByRecipeLock(slot, stack)) {
            return false;
        }
        ItemStack existing = itemHandler.copyStack(slot);
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, stack)) return false;
        int maxSize = Math.min(existing.getMaxStackSize(), itemHandler.getCapacityAsInt(slot, ItemResource.EMPTY));
        return existing.getCount() + stack.getCount() <= maxSize;
    }

    private void insertOutput(int slot, ItemStack stack) {
        ItemStack existing = itemHandler.copyStack(slot);
        if (existing.isEmpty()) {
            itemHandler.setStack(slot, stack.copy());
            return;
        }
        if (ItemStack.isSameItemSameComponents(existing, stack)) {
            ItemStack merged = existing.copy();
            merged.grow(stack.getCount());
            itemHandler.setStack(slot, merged);
        }
    }

    private int[] getIngredientSlots() {
        int[] slots = new int[1 + EXTRA_INGREDIENT_SLOT_COUNT];
        slots[0] = INGREDIENT_SLOT;
        for (int i = 0; i < EXTRA_INGREDIENT_SLOT_COUNT; i++) {
            slots[i + 1] = EXTRA_INGREDIENT_SLOT_START + i;
        }
        return slots;
    }

    private int getIngredientSlotByOrder(int order) {
        int[] slots = getIngredientSlots();
        if (order < 0 || order >= slots.length) {
            return -1;
        }
        return slots[order];
    }

    private void clearSequenceState() {
        sequenceActive = false;
        nextIngredientOrder = 0;
        activeIngredientOrder = -1;
        processedBottleMask = 0;
        brewProgress = 0;
        resetActiveBrewTiming();
    }

    public int getFuel() { return fuel; }
    public int getBrewProgress() { return brewProgress; }
    public boolean isRecipeLocked() { return recipeLocked; }

    public boolean isFuelInputEnabled() {
        return fuelInputEnabled;
    }

    public void setFuelInputEnabled(boolean enabled) {
        fuelInputEnabled = enabled;
        markDirtyClient();
    }

    public int getMaxMB() {
        return UpgradeHelper.adjustFluidCapacity(this, BASE_FLUID_CAPACITY);
    }

    public JDTEFluidTank getWaterFluidTank() {
        return waterFluidTank;
    }

    public JDTEFluidTank getTimeFluidTank() {
        return timeFluidTank;
    }

    public ResourceHandler<FluidResource> getFluidHandler() {
        return fluidHandler;
    }

    public ContainerData getWaterFluidData() {
        return waterFluidData;
    }

    public ContainerData getTimeFluidData() {
        return timeFluidData;
    }

    private void syncFluidCapacities() {
        int capacity = getMaxMB();
        syncFluidCapacity(waterFluidTank, capacity);
        syncFluidCapacity(timeFluidTank, capacity);
    }

    private void syncFluidCapacity(JDTEFluidTank tank, int capacity) {
        if (tank instanceof FluidTankAccessor accessor) {
            accessor.jdte$setCapacity(capacity);
        }
    }

    public ItemStack getLockedRecipeTemplate(int slot) {
        if (slot < 0 || slot >= lockedRecipeTemplates.size()) {
            return ItemStack.EMPTY;
        }
        return lockedRecipeTemplates.get(slot);
    }

    public NonNullList<ItemStack> copyLockedRecipeTemplates() {
        NonNullList<ItemStack> copy = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            copy.set(i, lockedRecipeTemplates.get(i).copy());
        }
        return copy;
    }

    /**
     * Applies a validated copied recipe-lock configuration without resetting an in-progress brew.
     */
    public void applyCopiedRecipeLock(boolean locked, NonNullList<ItemStack> templates) {
        if (templates == null || templates.size() != TOTAL_SLOTS) {
            throw new IllegalArgumentException("Expected exactly " + TOTAL_SLOTS + " recipe-lock templates");
        }

        boolean hasTemplate = false;
        for (int slot = 0; slot < TOTAL_SLOTS; slot++) {
            ItemStack template = templates.get(slot);
            if (template == null || !isRecipeLockedSlot(slot) && !template.isEmpty()) {
                throw new IllegalArgumentException("Invalid recipe-lock template slot " + slot);
            }
            hasTemplate |= !template.isEmpty();
        }
        if (locked != hasTemplate) {
            throw new IllegalArgumentException("Recipe lock and its templates must agree");
        }

        recipeLocked = locked;
        clearLockedRecipeTemplates();
        if (locked) {
            for (int slot = 0; slot < TOTAL_SLOTS; slot++) {
                ItemStack template = templates.get(slot);
                if (!template.isEmpty()) {
                    lockedRecipeTemplates.set(slot, template.copyWithCount(1));
                }
            }
        }
        hasValidIngredients = checkIngredients();
        setChanged();
        markDirtyClient();
    }

    public void setRecipeLocked(boolean locked) {
        if (locked) {
            recipeLocked = false;
            clearLockedRecipeTemplates();
            recipeLocked = captureCurrentRecipe();
        } else {
            recipeLocked = false;
            clearLockedRecipeTemplates();
        }
        brewProgress = 0;
        hasValidIngredients = checkIngredients();
        setChanged();
    }

    private boolean captureCurrentRecipe() {
        clearLockedRecipeTemplates();
        return captureLockedSlotTemplates(lockedRecipeTemplates);
    }

    private boolean captureLockedSlotTemplates(NonNullList<ItemStack> target) {
        clearRecipeTemplates(target);
        boolean hasTemplate = false;
        for (int slot = 0; slot < TOTAL_SLOTS; slot++) {
            if (!isRecipeLockedSlot(slot)) {
                continue;
            }
            ItemStack stack = itemHandler.copyStack(slot);
            if (stack.isEmpty()) {
                continue;
            }
            target.set(slot, stack.copyWithCount(1));
            hasTemplate = true;
        }
        return hasTemplate;
    }

    private void clearLockedRecipeTemplates() {
        clearRecipeTemplates(lockedRecipeTemplates);
    }

    private void clearRecipeTemplates(NonNullList<ItemStack> templates) {
        for (int i = 0; i < templates.size(); i++) {
            templates.set(i, ItemStack.EMPTY);
        }
    }

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
        return UpgradeHelper.adjustEnergyCost(this, BASE_ENERGY_COST);
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
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putChild("inventory", itemHandler);
        tag.putChild("waterFluidTank", waterFluidTank);
        tag.putChild("timeFluidTank", timeFluidTank);
        tag.putInt("fuel", fuel);
        tag.putInt("brewProgress", brewProgress);
        tag.putInt("brewActiveTime", activeBrewTime);
        tag.putInt("brewActiveTimeFluidCost", activeTimeFluidCost);
        tag.putBoolean("brewSequenceActive", sequenceActive);
        tag.putInt("brewNextIngredientOrder", nextIngredientOrder);
        tag.putInt("brewActiveIngredientOrder", activeIngredientOrder);
        tag.putInt("brewProcessedBottleMask", processedBottleMask);
        tag.putBoolean("recipeLocked", recipeLocked);
        tag.putBoolean("fuelInputEnabled", fuelInputEnabled);
        ValueOutput recipeLockTag = tag.child("recipeLockTemplates");
        for (int i = 0; i < lockedRecipeTemplates.size(); i++) {
            if (!lockedRecipeTemplates.get(i).isEmpty()) recipeLockTag.store("slot_" + i, ItemStack.CODEC, lockedRecipeTemplates.get(i));
        }
        tag.putInt("energy", energyStorage.getEnergyStored());
        saveRedstoneSettings(tag);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        tag.readChild("inventory", itemHandler);
        tag.readChild("waterFluidTank", waterFluidTank);
        tag.readChild("timeFluidTank", timeFluidTank);
        fuel = tag.getIntOr("fuel", 0);
        brewProgress = tag.getIntOr("brewProgress", 0);
        activeBrewTime = tag.getInt("brewActiveTime").isPresent() ? Math.max(MIN_ACCELERATED_BREW_TIME, tag.getIntOr("brewActiveTime", 0)) : BREW_TIME;
        activeTimeFluidCost = tag.getInt("brewActiveTimeFluidCost").isPresent() ? Math.max(0, tag.getIntOr("brewActiveTimeFluidCost", 0)) : 0;
        sequenceActive = tag.getBooleanOr("brewSequenceActive", false);
        nextIngredientOrder = tag.getIntOr("brewNextIngredientOrder", 0);
        activeIngredientOrder = tag.getIntOr("brewActiveIngredientOrder", -1);
        processedBottleMask = tag.getIntOr("brewProcessedBottleMask", 0);
        recipeLocked = tag.getBooleanOr("recipeLocked", false);
        fuelInputEnabled = tag.getBooleanOr("fuelInputEnabled", tag.getIntOr("fuelInputSide", 0) >= 0);
        clearLockedRecipeTemplates();
        if (tag.child("recipeLockTemplates").isPresent()) {
            ValueInput recipeLockTag = tag.childOrEmpty("recipeLockTemplates");
            for (int i = 0; i < lockedRecipeTemplates.size(); i++) {
                lockedRecipeTemplates.set(i, recipeLockTag.read("slot_" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
            }
        }
        if (tag.getInt("energy").isPresent()) {
            energyStorage.setEnergy(tag.getIntOr("energy", 0));
        }
        loadRedstoneSettings(tag);
        hasValidIngredients = checkIngredients();
    }
}
