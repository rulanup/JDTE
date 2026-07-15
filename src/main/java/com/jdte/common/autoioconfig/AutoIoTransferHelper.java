package com.jdte.common.autoioconfig;

import com.direwolf20.justdirethings.common.blockentities.BlockBreakerT1BE;
import com.direwolf20.justdirethings.common.blockentities.BlockPlacerT1BE;
import com.direwolf20.justdirethings.common.blockentities.BlockSwapperT1BE;
import com.direwolf20.justdirethings.common.blockentities.ClickerT1BE;
import com.direwolf20.justdirethings.common.blockentities.DropperT1BE;
import com.direwolf20.justdirethings.common.blockentities.FluidCollectorT1BE;
import com.direwolf20.justdirethings.common.blockentities.FluidPlacerT1BE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FluidMachineBE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.FluidReceiverBE;
import com.jdte.common.blockentities.FluidSenderBE;
import com.jdte.common.blockentities.FluidStabilizerBE;
import com.jdte.common.blockentities.GelGeneratorBE;
import com.jdte.common.blockentities.GlueActivatorBE;
import com.jdte.common.blockentities.InfusionMachineBE;
import com.jdte.common.blockentities.ItemReceiverBE;
import com.jdte.common.blockentities.ItemSenderBE;
import com.jdte.common.blockentities.LifeExtractorBE;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.common.blockentities.TimeAcceleratorBE;
import com.jdte.common.blockentities.CrystalIncubatorBE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.JDTEAttachments;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Arrays;

public final class AutoIoTransferHelper {
    private static final int SUCCESS_COOLDOWN_TICKS = 0;

    private static final int IO_SIDE_NORTH = 0;
    private static final int IO_SIDE_SOUTH = 1;
    private static final int IO_SIDE_WEST = 2;
    private static final int IO_SIDE_EAST = 3;
    private static final int IO_SIDE_UP = 4;
    private static final int IO_SIDE_DOWN = 5;

    private static final int[] NO_SLOTS = new int[0];

    private AutoIoTransferHelper() {
    }

    public static boolean supportsInput(BaseMachineBE machine) {
        if (machine == null) {
            return false;
        }
        IoRoutes routes = getRoutes(machine);
        return routes.hasItemInputs() || routes.fluidInput() != null;
    }

    public static boolean supportsOutput(BaseMachineBE machine) {
        if (machine == null) {
            return false;
        }
        IoRoutes routes = getRoutes(machine);
        return routes.hasItemOutputs() || routes.fluidOutput() != null;
    }

    public static void tick(BaseMachineBE machine) {
        if (machine == null || !(machine.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!AutoIoConfigHelper.hasConfigurableIo(machine)) {
            return;
        }

        AutoIoConfigData data = machine.getData(JDTEAttachments.AUTO_IO_CONFIG.get());
        int inputMask = data.getInputMask();
        int outputMask = data.getOutputMask();
        if (inputMask == 0 && outputMask == 0) {
            data.resetTransferState();
            return;
        }
        if (OverclockDirectTransferHelper.isEnabled(machine)) {
            data.resetTransferState();
            return;
        }

        int cooldown = data.getTransferCooldown();
        if (cooldown > 0) {
            data.setTransferCooldown(cooldown - 1);
            return;
        }

        IoRoutes routes = getRoutes(machine);
        if (routes.isEmpty()) {
            data.setTransferCooldown(getMaxFailureBackoff());
            return;
        }

        boolean moved = transferEnabledSides(serverLevel, machine, inputMask, outputMask, routes);
        if (moved) {
            data.setFailureBackoff(0);
            data.setTransferCooldown(SUCCESS_COOLDOWN_TICKS);
            machine.setChanged();
            return;
        }

        int nextBackoff = data.getFailureBackoff() <= 0
                ? getFailureBackoffStart()
                : Math.min(getMaxFailureBackoff(), data.getFailureBackoff() * 2);
        data.setFailureBackoff(nextBackoff);
        data.setTransferCooldown(nextBackoff);
    }

    private static boolean transferEnabledSides(ServerLevel level, BaseMachineBE machine,
                                                int inputMask, int outputMask, IoRoutes routes) {
        boolean moved = false;
        int startSide = (int) (level.getGameTime() % AutoIoConfigData.SIDE_COUNT);
        for (int i = 0; i < AutoIoConfigData.SIDE_COUNT; i++) {
            int uiSide = (startSide + i) % AutoIoConfigData.SIDE_COUNT;
            int bit = 1 << uiSide;
            boolean inputEnabled = (inputMask & bit) != 0;
            boolean outputEnabled = (outputMask & bit) != 0;
            if (!inputEnabled && !outputEnabled) {
                continue;
            }
            Direction side = directionForUiSide(machine, uiSide);
            if (transferSide(level, machine, side, routes, inputEnabled, outputEnabled)) {
                moved = true;
            }
        }
        return moved;
    }

    private static boolean transferSide(ServerLevel level, BaseMachineBE machine, Direction side, IoRoutes routes,
                                        boolean inputEnabled, boolean outputEnabled) {
        boolean moved = false;
        BlockPos neighborPos = machine.getBlockPos().relative(side);
        Direction neighborSide = side.getOpposite();

        ItemStackHandler internalItems = machine.getMachineHandler();
        if (outputEnabled && internalItems != null && routes.hasItemOutputs()) {
            IItemHandler externalItems = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, neighborSide);
            if (externalItems != null) {
                moved |= pushItems(internalItems, routes.itemOutputs(), externalItems,
                        JDTEConfig.COMMON.autoIoItemTransferRate.get());
            }
        }
        if (outputEnabled && routes.fluidOutput() != null) {
            IFluidHandler externalFluid = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, neighborSide);
            if (externalFluid != null) {
                moved |= pushFluid(routes.fluidOutput(), externalFluid,
                        JDTEConfig.COMMON.autoIoFluidTransferRate.get());
            }
        }
        if (inputEnabled && internalItems != null && routes.hasItemInputs()) {
            IItemHandler externalItems = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, neighborSide);
            if (externalItems != null) {
                moved |= pullItems(externalItems, internalItems, routes.itemInputs(),
                        JDTEConfig.COMMON.autoIoItemTransferRate.get());
            }
        }
        if (inputEnabled && routes.fluidInput() != null) {
            IFluidHandler externalFluid = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, neighborSide);
            if (externalFluid != null) {
                moved |= pullFluid(externalFluid, routes.fluidInput(),
                        JDTEConfig.COMMON.autoIoFluidTransferRate.get());
            }
        }

        return moved;
    }

    private static IoRoutes getRoutes(BaseMachineBE machine) {
        ItemStackHandler handler = machine.getMachineHandler();
        int[] itemInputs = NO_SLOTS;
        int[] itemOutputs = NO_SLOTS;
        IFluidHandler fluidInput = null;
        IFluidHandler fluidOutput = null;

        if (machine instanceof GelGeneratorBE generator) {
            itemInputs = boundedSlots(handler,
                    GelGeneratorBE.GEL_SLOT,
                    GelGeneratorBE.FOOD_SLOT,
                    GelGeneratorBE.INPUT_START_SLOT,
                    GelGeneratorBE.INPUT_START_SLOT + 1,
                    GelGeneratorBE.INPUT_START_SLOT + 2,
                    GelGeneratorBE.INPUT_START_SLOT + 3);
            itemOutputs = boundedSlots(handler, range(GelGeneratorBE.OUTPUT_START_SLOT, GelGeneratorBE.OUTPUT_SLOTS));
            fluidInput = generator.getFluidTank();
            fluidOutput = generator.getOutputFluidTank();
        } else if (machine instanceof InfusionMachineBE infusion) {
            itemInputs = boundedSlots(handler, InfusionMachineBE.INPUT_SLOT);
            itemOutputs = boundedSlots(handler, InfusionMachineBE.OUTPUT_SLOT);
            fluidInput = infusion.getFluidTank();
        } else if (machine instanceof AdvancedPotionBrewerBE brewer) {
            itemInputs = boundedSlots(handler,
                    AdvancedPotionBrewerBE.BOTTLE_SLOT_0,
                    AdvancedPotionBrewerBE.BOTTLE_SLOT_1,
                    AdvancedPotionBrewerBE.BOTTLE_SLOT_2,
                    AdvancedPotionBrewerBE.FUEL_SLOT,
                    AdvancedPotionBrewerBE.INGREDIENT_SLOT,
                    AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START,
                    AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + 1,
                    AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + 2,
                    AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + 3,
                    AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + 4);
            itemOutputs = boundedSlots(handler, range(AdvancedPotionBrewerBE.OUTPUT_SLOT_START, AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT));
            fluidInput = brewer.getFluidHandler();
        } else if (machine instanceof LootFabricatorBE fabricator) {
            itemInputs = boundedSlots(handler, range(0, LootFabricatorBE.INPUT_SLOTS));
            itemOutputs = boundedSlots(handler, range(LootFabricatorBE.INPUT_SLOTS, fabricator.getActiveOutputSlots()));
            fluidInput = fabricator.getFluidHandler();
        } else if (machine instanceof GlueActivatorBE) {
            itemInputs = boundedSlots(handler, GlueActivatorBE.REVIVE_SLOT);
        } else if (machine instanceof FluidStabilizerBE) {
            itemInputs = boundedSlots(handler, FluidStabilizerBE.CATALYST_SLOT);
        } else if (machine instanceof ItemSenderBE) {
            itemInputs = allSlots(handler);
        } else if (machine instanceof ItemReceiverBE) {
            itemOutputs = allSlots(handler);
        } else if (machine instanceof FluidSenderBE sender) {
            fluidInput = sender.getFluidTank();
        } else if (machine instanceof FluidReceiverBE receiver) {
            fluidOutput = receiver.getFluidTank();
        } else if (machine instanceof BioCrusherBE crusher) {
            itemOutputs = crusher.hasOutputInventory() ? boundedSlots(handler, range(0, crusher.getActiveOutputSlotCount())) : NO_SLOTS;
            fluidOutput = crusher.getFluidTank();
        } else if (machine instanceof LifeExtractorBE extractor) {
            fluidOutput = extractor.getFluidTank();
        } else if (machine instanceof CrystalIncubatorBE incubator) {
            itemOutputs = allSlots(handler);
            fluidInput = incubator.getFluidTank();
        } else if (machine instanceof TimeAcceleratorBE accelerator) {
            fluidInput = accelerator.getFluidTank();
        } else if (machine instanceof DropperT1BE) {
            itemInputs = allSlots(handler);
        } else if (machine instanceof ClickerT1BE
                || machine instanceof BlockBreakerT1BE
                || machine instanceof BlockPlacerT1BE
                || machine instanceof FluidCollectorT1BE
                || machine instanceof FluidPlacerT1BE) {
            itemInputs = boundedSlots(handler, 0);
        } else if (!(machine instanceof BlockSwapperT1BE) && handler != null && handler.getSlots() > 0) {
            itemInputs = allSlots(handler);
        }

        if (machine instanceof FluidCollectorT1BE collector) {
            fluidOutput = collector.getFluidTank();
        } else if (machine instanceof FluidPlacerT1BE placer) {
            fluidInput = placer.getFluidTank();
        } else if (machine instanceof ClickerT1BE && UpgradeHelper.hasFluidStorageUpgrade(machine)) {
            fluidInput = UpgradeHelper.getClickerFluidTank(machine);
        } else if (fluidInput == null && fluidOutput == null && machine instanceof FluidMachineBE fluidMachine) {
            fluidInput = fluidMachine.getFluidTank();
        }

        return new IoRoutes(itemInputs, itemOutputs, fluidInput, fluidOutput);
    }

    private static boolean pullItems(IItemHandler source, ItemStackHandler target, int[] targetSlots, int limit) {
        int moved = 0;
        for (int sourceSlot = 0; sourceSlot < source.getSlots() && moved < limit; sourceSlot++) {
            ItemStack simulatedExtract = source.extractItem(sourceSlot, limit - moved, true);
            if (simulatedExtract.isEmpty()) {
                continue;
            }

            ItemStack simulatedRemainder = insertIntoSlots(target, targetSlots, simulatedExtract, true);
            int movable = simulatedExtract.getCount() - simulatedRemainder.getCount();
            if (movable <= 0) {
                continue;
            }

            ItemStack extracted = source.extractItem(sourceSlot, movable, false);
            if (extracted.isEmpty()) {
                continue;
            }

            ItemStack remainder = insertIntoSlots(target, targetSlots, extracted, false);
            int inserted = extracted.getCount() - remainder.getCount();
            if (!remainder.isEmpty()) {
                source.insertItem(sourceSlot, remainder, false);
            }
            if (inserted > 0) {
                moved += inserted;
            }
        }
        return moved > 0;
    }

    private static boolean pushItems(ItemStackHandler source, int[] sourceSlots, IItemHandler target, int limit) {
        int moved = 0;
        for (int sourceSlot : sourceSlots) {
            if (moved >= limit) {
                break;
            }

            ItemStack simulatedExtract = source.extractItem(sourceSlot, limit - moved, true);
            if (simulatedExtract.isEmpty()) {
                continue;
            }

            ItemStack simulatedRemainder = ItemHandlerHelper.insertItemStacked(target, simulatedExtract, true);
            int movable = simulatedExtract.getCount() - simulatedRemainder.getCount();
            if (movable <= 0) {
                continue;
            }

            ItemStack extracted = source.extractItem(sourceSlot, movable, false);
            if (extracted.isEmpty()) {
                continue;
            }

            ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, extracted, false);
            int inserted = extracted.getCount() - remainder.getCount();
            if (!remainder.isEmpty()) {
                returnToInternalSlot(source, sourceSlot, remainder);
            }
            if (inserted > 0) {
                moved += inserted;
            }
        }
        return moved > 0;
    }

    private static ItemStack insertIntoSlots(ItemStackHandler target, int[] targetSlots, ItemStack stack, boolean simulate) {
        ItemStack remainder = stack.copy();
        for (int targetSlot : targetSlots) {
            if (remainder.isEmpty()) {
                break;
            }
            remainder = target.insertItem(targetSlot, remainder, simulate);
        }
        return remainder;
    }

    private static void returnToInternalSlot(ItemStackHandler handler, int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack current = handler.getStackInSlot(slot);
        if (current.isEmpty()) {
            handler.setStackInSlot(slot, stack.copy());
            return;
        }
        if (ItemStack.isSameItemSameComponents(current, stack)) {
            ItemStack merged = current.copy();
            merged.grow(stack.getCount());
            handler.setStackInSlot(slot, merged);
        }
    }

    private static boolean pullFluid(IFluidHandler source, IFluidHandler target, int limit) {
        FluidStack simulatedDrain = source.drain(limit, IFluidHandler.FluidAction.SIMULATE);
        if (simulatedDrain.isEmpty()) {
            return false;
        }

        int fillable = target.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
        if (fillable <= 0) {
            return false;
        }

        FluidStack toDrain = simulatedDrain.copy();
        toDrain.setAmount(Math.min(fillable, simulatedDrain.getAmount()));
        FluidStack drained = source.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            return false;
        }

        int filled = target.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            source.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            return false;
        }
        if (filled < drained.getAmount()) {
            FluidStack remainder = drained.copy();
            remainder.setAmount(drained.getAmount() - filled);
            source.fill(remainder, IFluidHandler.FluidAction.EXECUTE);
        }
        return true;
    }

    private static boolean pushFluid(IFluidHandler source, IFluidHandler target, int limit) {
        FluidStack simulatedDrain = source.drain(limit, IFluidHandler.FluidAction.SIMULATE);
        if (simulatedDrain.isEmpty()) {
            return false;
        }

        int fillable = target.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
        if (fillable <= 0) {
            return false;
        }

        FluidStack drained = source.drain(Math.min(fillable, simulatedDrain.getAmount()), IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            return false;
        }

        int filled = target.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) {
            source.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            return false;
        }
        if (filled < drained.getAmount()) {
            FluidStack remainder = drained.copy();
            remainder.setAmount(drained.getAmount() - filled);
            source.fill(remainder, IFluidHandler.FluidAction.EXECUTE);
        }
        return true;
    }

    private static Direction directionForUiSide(BaseMachineBE machine, int uiSide) {
        return switch (uiSide) {
            case IO_SIDE_NORTH -> Direction.NORTH;
            case IO_SIDE_SOUTH -> Direction.SOUTH;
            case IO_SIDE_WEST -> Direction.WEST;
            case IO_SIDE_EAST -> Direction.EAST;
            case IO_SIDE_UP -> Direction.UP;
            case IO_SIDE_DOWN -> Direction.DOWN;
            default -> Direction.NORTH;
        };
    }

    private static int getFailureBackoffStart() {
        return Math.min(JDTEConfig.COMMON.transferFailureBackoffStart.get(), getMaxFailureBackoff());
    }

    private static int getMaxFailureBackoff() {
        return JDTEConfig.COMMON.transferFailureBackoffMax.get();
    }

    private static int[] allSlots(ItemStackHandler handler) {
        if (handler == null || handler.getSlots() <= 0) {
            return NO_SLOTS;
        }
        return range(0, handler.getSlots());
    }

    private static int[] boundedSlots(ItemStackHandler handler, int... slots) {
        if (handler == null || handler.getSlots() <= 0 || slots.length == 0) {
            return NO_SLOTS;
        }
        int[] bounded = Arrays.stream(slots)
                .filter(slot -> slot >= 0 && slot < handler.getSlots())
                .toArray();
        return bounded.length == 0 ? NO_SLOTS : bounded;
    }

    private static int[] range(int start, int count) {
        if (count <= 0) {
            return NO_SLOTS;
        }
        int[] slots = new int[count];
        for (int i = 0; i < count; i++) {
            slots[i] = start + i;
        }
        return slots;
    }

    private record IoRoutes(int[] itemInputs, int[] itemOutputs, IFluidHandler fluidInput, IFluidHandler fluidOutput) {
        boolean hasItemInputs() {
            return itemInputs.length > 0;
        }

        boolean hasItemOutputs() {
            return itemOutputs.length > 0;
        }

        boolean isEmpty() {
            return !hasItemInputs() && !hasItemOutputs() && fluidInput == null && fluidOutput == null;
        }
    }
}
