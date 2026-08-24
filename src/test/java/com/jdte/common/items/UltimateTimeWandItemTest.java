package com.jdte.common.items;

import com.jdte.common.entities.UltimateTimeWandEntity.WandState;
import com.jdte.setup.JDTEConfig;
import com.direwolf20.justdirethings.common.items.interfaces.FluidContainingItem;
import com.direwolf20.justdirethings.common.items.interfaces.PoweredItem;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.MagicHelpers;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UltimateTimeWandItemTest {
    private static final WandState BEFORE = new WandState(BlockPos.ZERO, 4, 600, 400);
    private static final UltimateTimeWandData.OperationResult OPERATION =
            new UltimateTimeWandData.OperationResult(true,
                    new WandState(BlockPos.ZERO, 10, 600, 500), 7, 11);

    @Test
    void entityJoinFailureDoesNotCommitResources() {
        RecordingCommitPort port = new RecordingCommitPort(false, true, true, BEFORE);

        assertEquals(UltimateTimeWandItem.CommitOutcome.ENTITY_REJECTED,
                UltimateTimeWandItem.commitIfTargetValid(true, false, OPERATION, port));

        assertEquals(1, port.resourcePrepares);
        assertEquals(0, port.resourceCommits);
        assertEquals(BEFORE, port.entityState);
        assertEquals(100, port.liveFluid);
        assertEquals(200, port.liveEnergy);
    }

    @Test
    void rejectedFinalResourceCommitRestoresEntityWithoutChargingLiveResources() {
        RecordingCommitPort port = new RecordingCommitPort(true, true, false, BEFORE);

        assertEquals(UltimateTimeWandItem.CommitOutcome.ROLLED_BACK,
                UltimateTimeWandItem.commitIfTargetValid(true, false, OPERATION, port));

        assertEquals(1, port.resourcePrepares);
        assertEquals(1, port.resourceCommits);
        assertEquals(1, port.entityRollbacks);
        assertEquals(BEFORE, port.entityState);
        assertEquals(100, port.liveFluid);
        assertEquals(200, port.liveEnergy);
    }

    @Test
    void failedResourcePreparationNeverChangesTheEntityOrLiveResources() {
        RecordingCommitPort port = new RecordingCommitPort(true, false, true, BEFORE);

        assertEquals(UltimateTimeWandItem.CommitOutcome.ROLLED_BACK,
                UltimateTimeWandItem.commitIfTargetValid(true, false, OPERATION, port));

        assertEquals(0, port.entityApplies);
        assertEquals(0, port.entityRollbacks);
        assertEquals(1, port.resourcePrepares);
        assertEquals(0, port.resourceCommits);
        assertEquals(BEFORE, port.entityState);
        assertEquals(100, port.liveFluid);
        assertEquals(200, port.liveEnergy);
    }

    @Test
    void creativeModeStillRejectsAnInvalidTargetBeforeEntityCommit() {
        RecordingCommitPort port = new RecordingCommitPort(true, true, true, BEFORE);

        assertEquals(UltimateTimeWandItem.CommitOutcome.TARGET_REJECTED,
                UltimateTimeWandItem.commitIfTargetValid(false, true, OPERATION, port));

        assertEquals(0, port.entityApplies);
        assertEquals(0, port.resourcePrepares);
        assertEquals(0, port.resourceCommits);
    }

    @Test
    void partialDrainAndRefundFailureCannotChangeLiveResources() {
        PartialFailureCommitPort port = new PartialFailureCommitPort();

        assertEquals(UltimateTimeWandItem.CommitOutcome.ROLLED_BACK,
                UltimateTimeWandItem.commitIfTargetValid(true, false, OPERATION, port));

        assertEquals(BEFORE, port.entityState);
        assertEquals(100, port.liveFluid);
        assertEquals(200, port.liveEnergy);
    }

    @Test
    void itemExposesJdtResourceInterfacesAndConfiguredCapacities() {
        assertTrue(FluidContainingItem.class.isAssignableFrom(UltimateTimeWandItem.class));
        assertTrue(PoweredItem.class.isAssignableFrom(UltimateTimeWandItem.class));
        assertEquals(JDTEConfig.COMMON.ultimateTimeWandFluidCapacity.get(),
                UltimateTimeWandItem.configuredFluidCapacity());
        assertEquals(JDTEConfig.COMMON.ultimateTimeWandEnergyCapacity.get(),
                UltimateTimeWandItem.configuredEnergyCapacity());
    }

    @Test
    void itemReadsDurationAndFractionalSettlementConfiguration() {
        assertEquals(JDTEConfig.COMMON.ultimateTimeWandDuration.get(), UltimateTimeWandItem.configuredDuration());
        assertEquals(JDTEConfig.COMMON.ultimateTimeWandFractionalFluidSettlement.get(),
                UltimateTimeWandItem.keepsFractionalFluidSettlement());
    }

    @Test
    void blockShiftRightClickStillAcceleratesInsteadOfCyclingMode() {
        assertEquals("accelerate", dispatchMarker(UltimateTimeWandItem.InteractionTarget.BLOCK, true));
        assertEquals("accelerate", dispatchMarker(UltimateTimeWandItem.InteractionTarget.BLOCK, false));
    }

    @Test
    void airShiftRightClickCyclesMode() {
        assertEquals("cycle-mode", dispatchMarker(UltimateTimeWandItem.InteractionTarget.AIR, true));
    }

    @Test
    void airNonShiftRightClickPassesThrough() {
        assertEquals("pass", dispatchMarker(UltimateTimeWandItem.InteractionTarget.AIR, false));
    }

    @Test
    void appendHoverTextShowsCurrentAndMaximumResourcesInOrder() {
        ItemStack stack = new ItemStack(new UltimateTimeWandItem());
        fillFluid(stack, 1_234);
        fillEnergy(stack, 2_222);

        List<Component> tooltip = new ArrayList<>();
        new UltimateTimeWandItem().appendHoverText(stack, null, tooltip, TooltipFlag.Default.NORMAL);

        assertEquals(2, tooltip.size());
        String fluidText = tooltip.get(0).getString();
        String energyText = tooltip.get(1).getString();
        String currentFluid = MagicHelpers.formatted(1_234);
        String maximumFluid = MagicHelpers.formatted(UltimateTimeWandItem.configuredFluidCapacity());
        String currentEnergy = MagicHelpers.formatted(2_222);
        String maximumEnergy = MagicHelpers.formatted(UltimateTimeWandItem.configuredEnergyCapacity());
        assertTrue(fluidText.contains(currentFluid));
        assertTrue(fluidText.contains(maximumFluid));
        assertTrue(energyText.contains(currentEnergy));
        assertTrue(energyText.contains(maximumEnergy));
        assertTrue(fluidText.indexOf(currentFluid) < fluidText.indexOf(maximumFluid));
        assertTrue(energyText.indexOf(currentEnergy) < energyText.indexOf(maximumEnergy));
    }

    private static String dispatchMarker(UltimateTimeWandItem.InteractionTarget target, boolean shiftDown) {
        return UltimateTimeWandItem.dispatchInteraction(target, shiftDown,
                () -> "pass", () -> "cycle-mode", () -> "accelerate");
    }

    private static void fillFluid(ItemStack stack, int amount) {
        IFluidHandlerItem fluid = stack.getCapability(Capabilities.FluidHandler.ITEM);
        assertNotNull(fluid, "Ultimate Time Wand should expose a fluid capability");
        int accepted = fluid.fill(new FluidStack(Registration.TIME_FLUID_SOURCE.get(), amount),
                IFluidHandler.FluidAction.EXECUTE);
        assertEquals(amount, accepted);
    }

    private static void fillEnergy(ItemStack stack, int amount) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        assertNotNull(energy, "Ultimate Time Wand should expose an energy capability");
        int accepted = energy.receiveEnergy(amount, false);
        assertEquals(amount, accepted);
    }

    private static final class PartialFailureCommitPort implements UltimateTimeWandItem.CommitPort {
        private WandState entityState = BEFORE;
        private int liveFluid = 100;
        private int liveEnergy = 200;

        @Override
        public boolean prepareResources(UltimateTimeWandData.OperationResult operation) {
            int stagedFluid = liveFluid - Math.max(0, operation.fluidCost() - 1);
            return stagedFluid == liveFluid;
        }

        @Override
        public boolean applyEntity(WandState after) {
            entityState = after;
            return true;
        }

        @Override
        public void rollbackEntity() {
            entityState = BEFORE;
        }

        @Override
        public boolean commitResources() {
            return false;
        }
    }

    private static final class RecordingCommitPort implements UltimateTimeWandItem.CommitPort {
        private final boolean entitySucceeds;
        private final boolean prepareSucceeds;
        private final boolean commitSucceeds;
        private final WandState before;
        private WandState entityState;
        private UltimateTimeWandData.OperationResult preparedOperation;
        private int entityApplies;
        private int entityRollbacks;
        private int resourcePrepares;
        private int resourceCommits;
        private int liveFluid = 100;
        private int liveEnergy = 200;

        private RecordingCommitPort(boolean entitySucceeds, boolean prepareSucceeds, boolean commitSucceeds,
                                    WandState before) {
            this.entitySucceeds = entitySucceeds;
            this.prepareSucceeds = prepareSucceeds;
            this.commitSucceeds = commitSucceeds;
            this.before = before;
            this.entityState = before;
        }

        @Override
        public boolean prepareResources(UltimateTimeWandData.OperationResult operation) {
            resourcePrepares++;
            preparedOperation = operation;
            return prepareSucceeds;
        }

        @Override
        public boolean applyEntity(WandState after) {
            entityApplies++;
            if (!entitySucceeds) {
                return false;
            }
            entityState = after;
            return true;
        }

        @Override
        public void rollbackEntity() {
            entityRollbacks++;
            entityState = before;
        }

        @Override
        public boolean commitResources() {
            resourceCommits++;
            if (!commitSucceeds) {
                return false;
            }
            liveFluid -= preparedOperation.fluidCost();
            liveEnergy -= preparedOperation.energyCost();
            return true;
        }
    }
}
