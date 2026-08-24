package com.jdte.common.items;

import com.jdte.common.entities.UltimateTimeWandEntity.WandState;
import com.jdte.setup.JDTEConfig;
import com.direwolf20.justdirethings.common.items.interfaces.FluidContainingItem;
import com.direwolf20.justdirethings.common.items.interfaces.PoweredItem;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        assertEquals(0, port.fluidDrains);
        assertEquals(0, port.energyDrains);
        assertEquals(BEFORE, port.entityState);
    }

    @Test
    void failedEnergyCommitRestoresFluidAndEntityState() {
        RecordingCommitPort port = new RecordingCommitPort(true, true, false, BEFORE);

        assertEquals(UltimateTimeWandItem.CommitOutcome.ROLLED_BACK,
                UltimateTimeWandItem.commitIfTargetValid(true, false, OPERATION, port));

        assertEquals(1, port.fluidDrains);
        assertEquals(1, port.fluidRefunds);
        assertEquals(1, port.energyDrains);
        assertEquals(1, port.energyRefunds);
        assertEquals(BEFORE, port.entityState);
    }

    @Test
    void failedFluidCommitRestoresAnExistingEntityState() {
        RecordingCommitPort port = new RecordingCommitPort(true, false, true, BEFORE);

        assertEquals(UltimateTimeWandItem.CommitOutcome.ROLLED_BACK,
                UltimateTimeWandItem.commitIfTargetValid(true, false, OPERATION, port));

        assertEquals(1, port.entityRollbacks);
        assertEquals(1, port.fluidRefunds);
        assertEquals(BEFORE, port.entityState);
        assertEquals(0, port.energyDrains);
    }

    @Test
    void creativeModeStillRejectsAnInvalidTargetBeforeEntityCommit() {
        RecordingCommitPort port = new RecordingCommitPort(true, true, true, BEFORE);

        assertEquals(UltimateTimeWandItem.CommitOutcome.TARGET_REJECTED,
                UltimateTimeWandItem.commitIfTargetValid(false, true, OPERATION, port));

        assertEquals(0, port.entityApplies);
        assertEquals(0, port.fluidDrains);
        assertEquals(0, port.energyDrains);
    }

    @Test
    void incompleteResourceRefundIsAnExplicitCompensationFailure() {
        RecordingCommitPort port = new RecordingCommitPort(true, true, false, false, true, BEFORE);

        assertEquals(UltimateTimeWandItem.CommitOutcome.COMPENSATION_FAILED,
                UltimateTimeWandItem.commitIfTargetValid(true, false, OPERATION, port));

        assertEquals(1, port.fluidRefunds);
        assertEquals(1, port.energyRefunds);
        assertEquals(BEFORE, port.entityState);
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

    private static final class RecordingCommitPort implements UltimateTimeWandItem.CommitPort {
        private final boolean entitySucceeds;
        private final boolean fluidSucceeds;
        private final boolean energySucceeds;
        private final boolean fluidRefundSucceeds;
        private final boolean energyRefundSucceeds;
        private final WandState before;
        private WandState entityState;
        private int entityApplies;
        private int entityRollbacks;
        private int fluidDrains;
        private int fluidRefunds;
        private int energyDrains;
        private int energyRefunds;

        private RecordingCommitPort(boolean entitySucceeds, boolean fluidSucceeds, boolean energySucceeds,
                                    WandState before) {
            this(entitySucceeds, fluidSucceeds, energySucceeds, true, true, before);
        }

        private RecordingCommitPort(boolean entitySucceeds, boolean fluidSucceeds, boolean energySucceeds,
                                    boolean fluidRefundSucceeds, boolean energyRefundSucceeds, WandState before) {
            this.entitySucceeds = entitySucceeds;
            this.fluidSucceeds = fluidSucceeds;
            this.energySucceeds = energySucceeds;
            this.fluidRefundSucceeds = fluidRefundSucceeds;
            this.energyRefundSucceeds = energyRefundSucceeds;
            this.before = before;
            this.entityState = before;
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
        public boolean drainFluid(int amount) {
            fluidDrains++;
            return fluidSucceeds;
        }

        @Override
        public boolean refundFluid(int amount) {
            fluidRefunds++;
            return fluidRefundSucceeds;
        }

        @Override
        public boolean drainEnergy(int amount) {
            energyDrains++;
            return energySucceeds;
        }

        @Override
        public boolean refundEnergy(int amount) {
            energyRefunds++;
            return energyRefundSucceeds;
        }
    }
}
