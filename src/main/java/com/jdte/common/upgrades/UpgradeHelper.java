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
import com.jdte.common.blockentities.RangeBlockerBE;
import com.jdte.common.blockentities.TimeAcceleratorMachine;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.mixin.EnergyStorageAccessor;
import com.jdte.mixin.FluidTankAccessor;
import com.jdte.setup.JDTEAttachments;
import com.jdte.setup.JDTEConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class UpgradeHelper {
    public static int getFilterSlotsPerUpgrade() {
        return JDTEConfig.COMMON.filterSlotsPerUpgrade.get();
    }

    public static UpgradeItemStackHandler getUpgradeHandler(BaseMachineBE machine) {
        if (machine instanceof com.jdte.common.blockentities.LootFabricatorBE fabricator) {
            return fabricator.getUpgradeHandler();
        }
        if (machine instanceof com.jdte.common.blockentities.ExtendedUpgradeMachine) {
            return machine.getData(JDTEAttachments.EXTENDED_UPGRADE_HANDLER);
        }
        return machine.getData(JDTEAttachments.UPGRADE_HANDLER);
    }

    public static boolean isUpgrade(ItemStack stack) {
        return stack.getItem() instanceof UpgradeCardItem;
    }

    public static boolean isUpgrade(ItemStack stack, UpgradeType type) {
        return stack.getItem() instanceof UpgradeCardItem upgradeCard && upgradeCard.getType() == type;
    }

    public static boolean isUpgradeCompatible(BaseMachineBE machine, UpgradeType type) {
        if (machine instanceof GreenhouseBE) {
            return type == UpgradeType.CAPACITY || type == UpgradeType.FLUID
                    || type == UpgradeType.OVERCLOCK || type == UpgradeType.CREATIVE
                    || type == UpgradeType.FORTUNE;
        }
        if (machine instanceof AdvancedItemCollectorBE) {
            return type == UpgradeType.RANGE || type == UpgradeType.FILTER;
        }
        if (machine instanceof EntitySuppressorBE || machine instanceof RangeBlockerBE) {
            return type == UpgradeType.RANGE || type == UpgradeType.FILTER
                    || type == UpgradeType.CAPACITY || type == UpgradeType.CREATIVE;
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
        if (machine instanceof GreenhouseBE && type == UpgradeType.FORTUNE) {
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
        return multiplyByPowersOfTwo(original, countUpgrades(machine, UpgradeType.CAPACITY));
    }

    public static int adjustFluidCapacity(BaseMachineBE machine, int original) {
        int upgrades = countUpgrades(machine, UpgradeType.CAPACITY) + countUpgrades(machine, UpgradeType.FLUID);
        return multiplyByPowersOfTwo(original, upgrades);
    }

    public static int adjustEnergyCost(BaseMachineBE machine, int original) {
        if (original <= 0) {
            return original;
        }
        if (hasCreativeUpgrade(machine)) {
            return 0;
        }
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
        if (hasCreativeUpgrade(machine)) {
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
        return !(machine instanceof TimeAcceleratorMachine) && !(machine instanceof GreenhouseBE);
    }

    public static boolean hasOverclock(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.OVERCLOCK) > 0 || hasCreativeUpgrade(machine);
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
        return JDTEConfig.COMMON.baseAreaRadius.get() * (1 << rangeUpgrades);
    }

    public static int getMaxAreaOffset(BaseMachineBE machine) {
        int rangeUpgrades = countUpgrades(machine, UpgradeType.RANGE);
        return JDTEConfig.COMMON.baseAreaOffset.get() * (1 << rangeUpgrades);
    }

    public static boolean hasFluidStorageUpgrade(BaseMachineBE machine) {
        return countUpgrades(machine, UpgradeType.FLUID_STORAGE) > 0;
    }

    public static JDTEFluidTank getClickerFluidTank(BaseMachineBE machine) {
        return machine.getData(JDTEAttachments.CLICKER_FLUID_TANK);
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

        IFluidHandlerItem itemFluidHandler = itemStack.getCapability(Capabilities.FluidHandler.ITEM);
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
}
