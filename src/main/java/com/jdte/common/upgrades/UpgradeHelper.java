package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.jdte.common.blockentities.AdvancedItemCollectorBE;
import com.jdte.common.blockentities.EntitySuppressorBE;
import com.jdte.common.blockentities.GelGeneratorBE;
import com.jdte.common.blockentities.CrystalIncubatorBE;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.blockentities.LargeGreenhouseBE;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.blockentities.LifeSynthesisVatBE;
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.blockentities.FactoryPackerBE;
import com.jdte.common.blockentities.TimeAcceleratorMachine;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.integrations.ae2.AE2CraftingReadNetwork;
import com.jdte.common.blockentities.BasicTimeAcceleratorBE;
import com.jdte.common.blockentities.AdvancedTimeAcceleratorBE;
import com.jdte.mixin.EnergyStorageAccessor;
import com.jdte.mixin.FluidTankAccessor;
import com.jdte.setup.JDTEAttachments;
import com.jdte.setup.JDTEConfig;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class UpgradeHelper {
    // Optional AE2 types remain behind the common facade.

    public static int getFilterSlotsPerUpgrade() {
        return JDTEConfig.COMMON.filterSlotsPerUpgrade.get();
    }

    public static UpgradeItemStackHandler getUpgradeHandler(BaseMachineBE machine) {
        if (machine == null) return null;
        if (machine instanceof com.jdte.common.blockentities.LootFabricatorBE fabricator) {
            return fabricator.getUpgradeHandler();
        }
        if (machine instanceof BioFactoryBE factory) {
            return factory.getUpgradeHandler();
        }
        if (machine instanceof com.jdte.common.blockentities.ExtendedUpgradeMachine) {
            return JDTEAttachments.extendedUpgrades(machine);
        }
        return JDTEAttachments.upgrades(machine);
    }

    public static boolean isMixingUpgrade(ItemStack stack) { return stack.getItem() instanceof com.jdte.common.items.MixingUpgradeItem || isUltimateMixingParallelUpgrade(stack); }
    public static boolean isUltimateMixingParallelUpgrade(ItemStack stack) { return stack.getItem() instanceof com.jdte.common.items.UltimateMixingParallelUpgradeItem; }
    public static boolean hasMixingUpgrade(BaseMachineBE machine) {
        var handler = getUpgradeHandler(machine);if(handler==null)return false;
        for(int i=0;i<handler.getSlots();i++)if(isMixingUpgrade(handler.getStackInSlot(i)))return true;return false;
    }
    public static boolean hasUltimateMixingParallelUpgrade(BaseMachineBE machine) {
        var handler = getUpgradeHandler(machine);if(handler==null)return false;
        for(int i=0;i<handler.getSlots();i++)if(isUltimateMixingParallelUpgrade(handler.getStackInSlot(i)))return true;return false;
    }
    public static boolean isUpgrade(ItemStack stack) {
        return stack.getItem() instanceof UpgradeCardItem || isMixingUpgrade(stack) || isEnergyUpgrade(stack);
    }

    public static boolean isEnergyBrewingUpgrade(ItemStack stack) { return stack.getItem() instanceof com.jdte.common.items.EnergyBrewingUpgradeItem; }
    public static boolean isEnergyOverloadUpgrade(ItemStack stack) { return stack.getItem() instanceof com.jdte.common.items.EnergyOverloadUpgradeItem; }
    public static boolean isEnergyUpgrade(ItemStack stack) { return isEnergyBrewingUpgrade(stack) || isEnergyOverloadUpgrade(stack); }
    public static boolean isEnergyTransmitter(BaseMachineBE machine) { return machine instanceof com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE || machine instanceof com.jdte.common.blockentities.AdvancedEnergyTransmitterBE; }
    public static boolean hasEnergyOverloadUpgrade(BaseMachineBE machine) { return hasSpecialUpgrade(machine,false); }
    public static boolean hasEnergyBrewingUpgrade(BaseMachineBE machine) { return hasSpecialUpgrade(machine,true); }
    private static boolean hasSpecialUpgrade(BaseMachineBE machine,boolean brewing) {
        if(machine==null || machine.isRemoved())return false;
        var handler=getUpgradeHandler(machine);if(handler==null)return false;
        for(int slot=0;slot<handler.getSlots();slot++)if(brewing?isEnergyBrewingUpgrade(handler.getStackInSlot(slot)):isEnergyOverloadUpgrade(handler.getStackInSlot(slot)))return true;
        return false;
    }

    public static boolean isUpgrade(ItemStack stack, UpgradeType type) {
        return stack.getItem() instanceof UpgradeCardItem upgradeCard && upgradeCard.getType() == type;
    }

    public static boolean hasAEAccelerationUpgrade(BaseMachineBE machine) { return countUpgrades(machine,UpgradeType.AE_ACCELERATION)>0; }

    public static boolean hasAeCraftingReadUpgrade(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.AE_CRAFTING_READ) > 0;
    }

    /** Returns whether normal machine work is allowed by the optional AE crafting binding. */
    public static boolean mayRunWithUpgrades(BaseMachineBE machine) {
        if (machine == null || !hasAeCraftingReadUpgrade(machine)) {
            return true;
        }
        if (!(machine.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) {
            return true;
        }
        UpgradeItemStackHandler handler = getUpgradeHandler(machine);
        if (handler == null) return false;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack upgrade = handler.getStackInSlot(slot);
            if (isUpgrade(upgrade, UpgradeType.AE_CRAFTING_READ)
                    && AE2CraftingReadNetwork.hasActiveCraftingTask(level, upgrade)) return true;
        }
        return false;
    }

    public static boolean mayRunWithUpgrades(UpgradeItemStackHandler handler, boolean activeCraftingTask) {
        return handler == null || countUpgrades(handler, UpgradeType.AE_CRAFTING_READ) == 0 || activeCraftingTask;
    }

    public static boolean mayRunWithUpgrades(UpgradeItemStackHandler handler, long gameTime,
                                             AE2CraftingReadNetwork.TargetResolver resolver,
                                             java.util.function.Predicate<ItemStack> isBound) {
        if (handler == null) return true;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack upgrade = handler.getStackInSlot(slot);
            if (isUpgrade(upgrade, UpgradeType.AE_CRAFTING_READ)
                    && isBound.test(upgrade)
                    && AE2CraftingReadNetwork.hasActiveCraftingTask(upgrade, gameTime, resolver)) return true;
        }
        return countUpgrades(handler, UpgradeType.AE_CRAFTING_READ) == 0;
    }

    /** Returns whether the whole ticker is a safe boundary for the common AE work gate. */
    public static boolean usesCommonAeTickerGate(BaseMachineBE machine) {
        return !(machine instanceof GreenhouseBE || machine instanceof LargeGreenhouseBE)
                && !(machine instanceof LifeSynthesisVatBE || machine instanceof com.jdte.common.blockentities.MineralExtractorBE)
                && !(machine instanceof LifeBreederBE || machine instanceof com.jdte.common.blockentities.TimeFreezerBE)
                && !(machine instanceof AdvancedItemCollectorBE)
                && !(machine instanceof TimeAcceleratorMachine || machine instanceof EntitySuppressorBE)
                && !(machine instanceof RangeBlockerBE || machine instanceof com.jdte.common.blockentities.AdvancedEnergyTransmitterBE)
                && !(machine instanceof FactoryPackerBE)
                && !(machine instanceof CrystalIncubatorBE);
    }

    /** Executes the actual machine ticker while preserving the original redstone-off reset path. */
    public static void runServerTicker(boolean redstoneActive, java.util.function.BooleanSupplier mayRun,
                                       boolean overclock, Runnable originalTicker) {
        runServerTicker(true, redstoneActive, mayRun, overclock ? 2 : 1, originalTicker);
    }

    public static void runServerTicker(boolean useCommonGate, boolean redstoneActive, java.util.function.BooleanSupplier mayRun,
                                       boolean overclock, Runnable originalTicker) {
        runServerTicker(useCommonGate, redstoneActive, mayRun, overclock ? 2 : 1, originalTicker);
    }

    public static void runServerTicker(boolean useCommonGate, boolean redstoneActive, java.util.function.BooleanSupplier mayRun,
                                       int executions, Runnable originalTicker) {
        if (!useCommonGate) {
            for (int i = 0; i < executions; i++) {
                originalTicker.run();
            }
            return;
        }
        if (!redstoneActive) {
            originalTicker.run();
            return;
        }
        if (!mayRun.getAsBoolean()) {
            return;
        }
        for (int i = 0; i < executions; i++) {
            originalTicker.run();
        }
    }

    private static int countUpgrades(UpgradeItemStackHandler handler, UpgradeType type) {
        int count = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (isUpgrade(handler.getStackInSlot(slot), type)) count++;
        }
        return count;
    }

    public static boolean hasAEOutputUpgrade(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.AE_OUTPUT) > 0
                || countUpgrades(machine, UpgradeType.ADVANCED_AE_OUTPUT) > 0;
    }

    public static boolean hasAdvancedAEOutputUpgrade(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.ADVANCED_AE_OUTPUT) > 0;
    }

    public static ItemStack getAEOutputUpgrade(BaseMachineBE machine) {
        UpgradeItemStackHandler handler = getUpgradeHandler(machine);
        if (handler == null) return ItemStack.EMPTY;
        ItemStack fallback = ItemStack.EMPTY;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (isUpgrade(stack, UpgradeType.ADVANCED_AE_OUTPUT)) return stack;
            if (fallback.isEmpty() && isUpgrade(stack, UpgradeType.AE_OUTPUT)) fallback = stack;
        }
        return fallback;
    }

    public static boolean hasSeedConversionUpgrade(BaseMachineBE machine) { return countUpgrades(machine,UpgradeType.SEED_CONVERSION)>0; }

    public static boolean hasEssenceConversionUpgrade(BaseMachineBE machine) { return countUpgrades(machine,UpgradeType.ESSENCE_CONVERSION)>0; }
    public static boolean isUpgradeCompatible(BaseMachineBE machine, UpgradeType type) {
        if(type==UpgradeType.AE_ACCELERATION)return machine instanceof BasicTimeAcceleratorBE || machine instanceof AdvancedTimeAcceleratorBE;
        if(type==UpgradeType.AE_CRAFTING_READ || type.isAEOutputUpgrade()) return true;
        if(type==UpgradeType.ESSENCE_CONVERSION || type==UpgradeType.SEED_CONVERSION)return machine instanceof GreenhouseBE||machine instanceof LargeGreenhouseBE;
        if (machine instanceof com.jdte.common.blockentities.BioExtractorBE) return type==UpgradeType.CAPACITY||type==UpgradeType.ULTIMATE_CAPACITY||type==UpgradeType.FLUID||type==UpgradeType.OVERCLOCK||type==UpgradeType.ULTIMATE_OVERCLOCK||type==UpgradeType.UNDERCLOCK||type==UpgradeType.RANGE||type==UpgradeType.FILTER||type==UpgradeType.CREATIVE;
        if (machine instanceof com.jdte.common.blockentities.AdvancedEnergyTransmitterBE)
            return type == UpgradeType.RANGE || type == UpgradeType.FILTER || type == UpgradeType.CAPACITY
                    || type == UpgradeType.ULTIMATE_CAPACITY || type == UpgradeType.OVERCLOCK
                    || type == UpgradeType.ULTIMATE_OVERCLOCK || type == UpgradeType.CREATIVE || type == UpgradeType.AE_CRAFTING_READ;
        if (machine instanceof com.jdte.common.blockentities.ExtendedEnergyTransmitterBE
                && (type == UpgradeType.ULTIMATE_CAPACITY || type == UpgradeType.ULTIMATE_OVERCLOCK)) return true;
        if (type == UpgradeType.ULTIMATE_CAPACITY || type == UpgradeType.ULTIMATE_OVERCLOCK)
            return machine instanceof com.jdte.common.blockentities.MineralExtractorBE || machine instanceof com.jdte.common.blockentities.FluidMixerBE || isExtendedGenerator(machine);
        if (machine instanceof com.jdte.common.blockentities.MineralExtractorBE)
            return type == UpgradeType.CAPACITY || type == UpgradeType.FLUID || type == UpgradeType.FILTER
                    || type == UpgradeType.OVERCLOCK || type == UpgradeType.CREATIVE;

        if (machine instanceof GreenhouseBE || machine instanceof LargeGreenhouseBE) {
            return type == UpgradeType.CAPACITY || type == UpgradeType.FLUID
                    || type == UpgradeType.OVERCLOCK || type == UpgradeType.CREATIVE
                    || type == UpgradeType.FORTUNE;
        }
        if (machine instanceof LifeSynthesisVatBE) {
            return type == UpgradeType.CAPACITY || type == UpgradeType.FLUID
                    || type == UpgradeType.OVERCLOCK || type == UpgradeType.CREATIVE;
        }
        if (machine instanceof BioFactoryBE) {
            return type == UpgradeType.CAPACITY || type == UpgradeType.FLUID
                    || type == UpgradeType.OVERCLOCK || type == UpgradeType.CREATIVE;
        }
        if (machine instanceof LifeBreederBE) {
            return type == UpgradeType.CAPACITY || type == UpgradeType.FLUID
                    || type == UpgradeType.OVERCLOCK || type == UpgradeType.RANGE || type == UpgradeType.FILTER
                    || type == UpgradeType.CREATIVE;
        }
        if (machine instanceof AdvancedItemCollectorBE) {
            return type == UpgradeType.RANGE || type == UpgradeType.FILTER;
        }
        if (machine instanceof EntitySuppressorBE || machine instanceof RangeBlockerBE) {
            return type == UpgradeType.RANGE || type == UpgradeType.FILTER
                    || type == UpgradeType.CAPACITY || type == UpgradeType.CREATIVE;
        }
        if (machine instanceof FactoryPackerBE) {
            return type == UpgradeType.RANGE || type == UpgradeType.CAPACITY
                    || type == UpgradeType.OVERCLOCK || type == UpgradeType.CREATIVE;
        }
        return switch (type) {
            case FLUID_STORAGE -> machine instanceof ClickerT1BE;
            case GENERATOR -> machine instanceof GeneratorT1BE || machine instanceof GeneratorFluidT1BE;
            case RANGE -> machine instanceof AreaAffectingBE;
            case FILTER -> machine instanceof FilterableBE;
            case FORTUNE -> machine instanceof GelGeneratorBE || machine instanceof CrystalIncubatorBE;
            case PRECISION -> machine instanceof CrystalIncubatorBE;
            default -> true;
        };
    }

    public static int countUpgrades(BaseMachineBE machine, UpgradeType type) {
        UpgradeItemStackHandler handler = getUpgradeHandler(machine);
        if (handler == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            if (isUpgrade(handler.getStackInSlot(i), type)) {
                count++;
            }
        }
        return Math.min(count, type.getMaxPerMachine());
    }

    public static int getMaxUpgrades(BaseMachineBE machine, UpgradeType type) {
        if ((machine instanceof GreenhouseBE || machine instanceof LargeGreenhouseBE)
                && type == UpgradeType.FORTUNE) {
            return 3;
        }
        return type.getMaxPerMachine();
    }

    public static int getMaxExtraFilterSlots() {
        return UpgradeType.FILTER.getMaxPerMachine() * getFilterSlotsPerUpgrade();
    }

    public static int getMaxFilterSlots(int baseSlots) {
        return baseSlots + getMaxExtraFilterSlots();
    }

    public static int getActiveFilterSlots(BaseMachineBE machine, int baseSlots) {
        if (machine == null) {
            return baseSlots;
        }
        return baseSlots + countUpgrades(machine, UpgradeType.FILTER) * getFilterSlotsPerUpgrade();
    }

    public static int getBaseFilterSlots(FilterBasicHandler handler) {
        int slots = handler.getSlots();
        int filterSlotsPerUpgrade = getFilterSlotsPerUpgrade();
        if (slots <= filterSlotsPerUpgrade) {
            return slots;
        }

        int remainder = slots % filterSlotsPerUpgrade;
        return remainder == 0 ? filterSlotsPerUpgrade : remainder;
    }

    public static boolean hasBaseFilterSlots(BaseMachineBE machine) {
        return machine instanceof com.jdte.common.blockentities.BaseFilterMachine;
    }

    public static void trimInactiveFilterSlots(BaseMachineBE machine) {
        if (!(machine instanceof FilterableBE filterable)) {
            return;
        }

        FilterBasicHandler handler = filterable.getFilterHandler();
        int baseSlots = getBaseFilterSlots(handler);
        int activeSlots = getActiveFilterSlots(machine, baseSlots);
        boolean changed = false;
        for (int i = activeSlots; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                handler.setStackInSlot(i, ItemStack.EMPTY);
                changed = true;
            }
        }
        if (changed) {
            machine.setChanged();
        }
    }

    public static int adjustEnergyCapacity(BaseMachineBE machine, int original) {
        return multiplyByPowersOfTwo(scaledUltimateCapacity(machine, original), countUpgrades(machine, UpgradeType.CAPACITY));
    }

    public static int adjustFluidCapacity(BaseMachineBE machine, int original) {
        int upgrades = countUpgrades(machine, UpgradeType.CAPACITY) + countUpgrades(machine, UpgradeType.FLUID);
        return multiplyByPowersOfTwo(scaledUltimateCapacity(machine, original), upgrades);
    }

    public static int adjustEnergyCost(BaseMachineBE machine, int original) {
        if (original <= 0) {
            return original;
        }
        if (hasCreativeUpgrade(machine)) {
            return 0;
        }
        if ((machine instanceof com.jdte.common.blockentities.FluidMixerBE || machine instanceof com.jdte.common.blockentities.BioExtractorBE) && hasUltimateOverclock(machine)) return (int)Math.min(Integer.MAX_VALUE,(long)original*50);
        if (countUpgrades(machine, UpgradeType.UNDERCLOCK) > 0) {
            return Math.max(1, (int) Math.ceil(original * JDTEConfig.COMMON.underclockEnergyMultiplier.get()));
        }
        if (countUpgrades(machine, UpgradeType.OVERCLOCK) > 0) {
            long result = (long) original * JDTEConfig.COMMON.overclockEnergyMultiplier.get();
            return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
        }
        return original;
    }

    public static int getEffectiveTickSpeed(BaseMachineBE machine, int original) {
        if (!usesLockedDelay(machine)) {
            return original;
        }
        if (hasCreativeUpgrade(machine) || (machine instanceof com.jdte.common.blockentities.AdvancedEnergyTransmitterBE || machine instanceof com.jdte.common.blockentities.ExtendedEnergyTransmitterBE || isExtendedGenerator(machine) || machine instanceof com.jdte.common.blockentities.BioExtractorBE) && hasUltimateOverclock(machine)) {
            return 1;
        }
        if (countUpgrades(machine, UpgradeType.UNDERCLOCK) > 0) {
            return JDTEConfig.COMMON.underclockTickSpeed.get();
        }
        if (countUpgrades(machine, UpgradeType.OVERCLOCK) > 0) {
            return JDTEConfig.COMMON.overclockTickSpeed.get();
        }
        return original;
    }

    public static boolean shouldRunOverclock(BaseMachineBE machine) {
        return usesLockedDelay(machine) && (hasOverclock(machine) || hasCreativeUpgrade(machine)) && countUpgrades(machine, UpgradeType.UNDERCLOCK) == 0;
    }

    public static boolean usesLockedDelay(BaseMachineBE machine) {
        return !(machine instanceof com.jdte.common.blockentities.MineralExtractorBE) && !(machine instanceof TimeAcceleratorMachine) && !(machine instanceof GreenhouseBE || machine instanceof LargeGreenhouseBE)
                && !(machine instanceof BioFactoryBE) && !(machine instanceof LifeBreederBE)
                && !(machine instanceof LifeSynthesisVatBE);
    }

    public static boolean hasOverclock(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.OVERCLOCK) > 0 || hasCreativeUpgrade(machine)
                || (machine instanceof com.jdte.common.blockentities.AdvancedEnergyTransmitterBE || machine instanceof com.jdte.common.blockentities.ExtendedEnergyTransmitterBE || isExtendedGenerator(machine) || machine instanceof com.jdte.common.blockentities.BioExtractorBE) && hasUltimateOverclock(machine);
    }

    public static boolean hasUndercLock(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.UNDERCLOCK) > 0;
    }

    public static boolean hasGeneratorUpgrade(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.GENERATOR) > 0;
    }

    public static boolean hasCreativeUpgrade(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.CREATIVE) > 0;
    }

    public static double getMaxAreaRadius(BaseMachineBE machine) {
        int rangeUpgrades = countUpgrades(machine, UpgradeType.RANGE);
        double baseRadius = machine instanceof FactoryPackerBE
                ? JDTEConfig.COMMON.factoryPackerBaseRadius.get()
                : JDTEConfig.COMMON.baseAreaRadius.get();
        return baseRadius * (1 << rangeUpgrades);
    }

    public static int getMaxAreaOffset(BaseMachineBE machine) {
        int rangeUpgrades = countUpgrades(machine, UpgradeType.RANGE);
        return JDTEConfig.COMMON.baseAreaOffset.get() * (1 << rangeUpgrades);
    }

    public static boolean hasFluidStorageUpgrade(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.FLUID_STORAGE) > 0;
    }

    public static JDTEFluidTank getClickerFluidTank(BaseMachineBE machine) {
        return JDTEAttachments.clickerFluidTank(machine);
    }

    public static int getClickerFluidCapacity(BaseMachineBE machine) {
        if (!hasFluidStorageUpgrade(machine)) {
            return 0;
        }
        int upgrades = countUpgrades(machine, UpgradeType.CAPACITY) + countUpgrades(machine, UpgradeType.FLUID);
        return multiplyByPowersOfTwo(UpgradeItemStackHandler.BASE_CLICKER_FLUID_CAPACITY, upgrades);
    }

    public static void syncCapacities(BaseMachineBE machine) {
        if (machine instanceof PoweredMachineBE poweredMachine) {
            MachineEnergyStorage storage = poweredMachine.getEnergyStorage();
            int capacity = poweredMachine.getMaxEnergy();
            if (storage instanceof EnergyStorageAccessor accessor) {
                accessor.jdte$setCapacity(capacity);
                accessor.jdte$setMaxReceive(capacity);
                accessor.jdte$setMaxExtract(capacity);
                if (accessor.jdte$getEnergy() > capacity) {
                    accessor.jdte$setEnergy(capacity);
                }
            }
        }

        if (machine instanceof FluidMachineBE fluidMachine) {
            FluidTank tank = fluidMachine.getFluidTank();
            int capacity = fluidMachine.getMaxMB();
            if (tank instanceof FluidTankAccessor accessor) {
                accessor.jdte$setCapacity(capacity);
                if (tank.getFluidAmount() > capacity) {
                    tank.getFluid().setAmount(capacity);
                }
            }
        }

        if(machine instanceof com.jdte.common.blockentities.FluidMixerBE mixer)mixer.syncTankCapacities();
        syncClickerFluidTank(machine);
        trimInactiveFilterSlots(machine);
    }

    public static void syncClickerFluidTank(BaseMachineBE machine) {
        JDTEFluidTank tank = getClickerFluidTank(machine);
        int capacity = Math.max(UpgradeItemStackHandler.BASE_CLICKER_FLUID_CAPACITY, getClickerFluidCapacity(machine));
        if (tank instanceof FluidTankAccessor accessor) {
            accessor.jdte$setCapacity(capacity);
            if (tank.getFluidAmount() > capacity) {
                tank.getFluid().setAmount(capacity);
            }
        }
    }

    public static void fillClickerItemFromTank(BaseMachineBE machine) {
        if (!hasFluidStorageUpgrade(machine)) {
            return;
        }

        JDTEFluidTank tank = getClickerFluidTank(machine);
        if (tank.getFluid().isEmpty()) {
            return;
        }

        ItemStackHandler itemHandler = machine.getMachineHandler();
        ItemStack itemStack = itemHandler.getStackInSlot(0);
        if (itemStack.isEmpty()) {
            return;
        }

        IFluidHandlerItem itemFluidHandler = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (itemFluidHandler == null) {
            return;
        }

        FluidStack available = tank.getFluid().copy();
        int insertAmount = itemFluidHandler.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (insertAmount <= 0) {
            return;
        }

        FluidStack extracted = tank.drain(insertAmount, IFluidHandler.FluidAction.EXECUTE);
        if (!extracted.isEmpty()) {
            itemFluidHandler.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
            itemHandler.setStackInSlot(0, itemFluidHandler.getContainer());
            machine.setChanged();
        }
    }

    private static int multiplyByPowersOfTwo(int original, int powers) {
        if (original <= 0 || powers <= 0) {
            return original;
        }
        long result = (long) original << powers;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }

    public static boolean isSmelterUpgrade(ItemStack stack) {
        return stack.is(com.direwolf20.justdirethings.setup.Registration.UPGRADE_SMELTER.get());
    }
    public static boolean hasSmelterUpgrade(BaseMachineBE machine) {
        var handler = getUpgradeHandler(machine);
        for (int slot = 0; slot < handler.getSlots(); slot++)
            if (isSmelterUpgrade(handler.getStackInSlot(slot))) return true;
        return false;
    }
    public static boolean hasUltimateCapacity(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.ULTIMATE_CAPACITY) > 0;
    }
    public static boolean hasUltimateOverclock(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.ULTIMATE_OVERCLOCK) > 0;
    }
    private static int scaledUltimateCapacity(BaseMachineBE machine, int original) {
        return hasUltimateCapacity(machine) ? (int) Math.min(Integer.MAX_VALUE, (long) original * 50) : original;
    }
    public static boolean isExtendedGenerator(BaseMachineBE machine) {
        return machine instanceof com.jdte.common.blockentities.ExtendedGeneratorBE || machine instanceof com.jdte.common.blockentities.ExtendedFluidGeneratorBE;
    }
}
