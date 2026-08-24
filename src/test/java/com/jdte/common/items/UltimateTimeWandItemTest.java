package com.jdte.common.items;

import com.jdte.common.entities.UltimateTimeWandEntity.WandState;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UltimateTimeWandItemTest {
    private static final WandState BEFORE = new WandState(BlockPos.ZERO, 4, 600, 400);
    private static final UltimateTimeWandData.OperationResult OPERATION =
            new UltimateTimeWandData.OperationResult(true,
                    new WandState(BlockPos.ZERO, 10, 600, 500), 7, 11);

    @Test
    void entityJoinFailureDoesNotCommitResources() {
        RecordingCommitPort port = new RecordingCommitPort(false, true, true, BEFORE);

        assertFalse(UltimateTimeWandItem.commitIfTargetValid(true, false, OPERATION, port));

        assertEquals(0, port.fluidDrains);
        assertEquals(0, port.energyDrains);
        assertEquals(BEFORE, port.entityState);
    }

    @Test
    void failedEnergyCommitRestoresFluidAndEntityState() {
        RecordingCommitPort port = new RecordingCommitPort(true, true, false, BEFORE);

        assertFalse(UltimateTimeWandItem.commitIfTargetValid(true, false, OPERATION, port));

        assertEquals(1, port.fluidDrains);
        assertEquals(1, port.fluidRefunds);
        assertEquals(1, port.energyDrains);
        assertEquals(BEFORE, port.entityState);
    }

    @Test
    void failedFluidCommitRestoresAnExistingEntityState() {
        RecordingCommitPort port = new RecordingCommitPort(true, false, true, BEFORE);

        assertFalse(UltimateTimeWandItem.commitIfTargetValid(true, false, OPERATION, port));

        assertEquals(1, port.entityRollbacks);
        assertEquals(BEFORE, port.entityState);
        assertEquals(0, port.energyDrains);
    }

    @Test
    void creativeModeStillRejectsAnInvalidTargetBeforeEntityCommit() {
        RecordingCommitPort port = new RecordingCommitPort(true, true, true, BEFORE);

        assertFalse(UltimateTimeWandItem.commitIfTargetValid(false, true, OPERATION, port));

        assertEquals(0, port.entityApplies);
        assertEquals(0, port.fluidDrains);
        assertEquals(0, port.energyDrains);
    }

    private static final class RecordingCommitPort implements UltimateTimeWandItem.CommitPort {
        private final boolean entitySucceeds;
        private final boolean fluidSucceeds;
        private final boolean energySucceeds;
        private final WandState before;
        private WandState entityState;
        private int entityApplies;
        private int entityRollbacks;
        private int fluidDrains;
        private int fluidRefunds;
        private int energyDrains;

        private RecordingCommitPort(boolean entitySucceeds, boolean fluidSucceeds, boolean energySucceeds,
                                    WandState before) {
            this.entitySucceeds = entitySucceeds;
            this.fluidSucceeds = fluidSucceeds;
            this.energySucceeds = energySucceeds;
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
        public void refundFluid(int amount) {
            fluidRefunds++;
        }

        @Override
        public boolean drainEnergy(int amount) {
            energyDrains++;
            return energySucceeds;
        }

        @Override
        public void refundEnergy(int amount) {
        }
    }
}
