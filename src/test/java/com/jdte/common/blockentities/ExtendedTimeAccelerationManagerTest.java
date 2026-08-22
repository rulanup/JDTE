package com.jdte.common.blockentities;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.config.IConfigSpec;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedTimeAccelerationManagerTest {

    @Test
    void managerPreparesResourcesFromConfiguredWorkTicks() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();

            ExtendedTimeAccelerationManager.PreparedAcceleration prepared =
                    ExtendedTimeAccelerationManager.prepareAcceleration(accelerator);

            assertEquals(4, prepared.displayMultiplier());
            assertEquals(400, prepared.workTicks());
            assertEquals(400, accelerator.fluidWorkTicks);
            assertEquals(400, accelerator.energyWorkTicks);
            assertEquals(7, prepared.fluidCost());
            assertEquals(11, prepared.energyCost());
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void managerUsesUpdatedDurationForEachSubmission() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(2));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();

            ExtendedTimeAccelerationManager.PreparedAcceleration prepared =
                    ExtendedTimeAccelerationManager.prepareAcceleration(accelerator);

            assertEquals(160, prepared.workTicks());
            assertEquals(160, accelerator.fluidWorkTicks);
            assertEquals(160, accelerator.energyWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void managerPaymentSeamChecksCostsAndConsumesConfiguredWorkTicks() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();
            ExtendedTimeAccelerationManager.PreparedAcceleration prepared =
                    ExtendedTimeAccelerationManager.prepareAcceleration(accelerator);

            assertTrue(ExtendedTimeAccelerationManager.payForSubmission(accelerator, prepared));
            assertEquals(7, accelerator.checkedFluidCost);
            assertEquals(11, accelerator.checkedEnergyCost);
            assertEquals(400, accelerator.consumedWorkTicks);
            assertEquals(11, accelerator.consumedEnergyCost);
            assertNotEquals(prepared.fluidCost(), accelerator.consumedWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void requestLargerThanMaxPendingChargesAndEnqueuesOnlyAcceptedWork() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();
            ExtendedTimeAccelerationManager.AccelerationRequest request =
                    ExtendedTimeAccelerationManager.requestAcceleration(accelerator);

            ExtendedTimeAccelerationManager.PreparedAcceleration accepted =
                    ExtendedTimeAccelerationManager.prepareAcceptedAcceleration(
                            accelerator, request, 100L, 0L).orElseThrow();

            assertEquals(400, request.workTicks());
            assertEquals(100, accepted.workTicks());
            assertEquals(100, accelerator.fluidWorkTicks);
            assertEquals(100, accelerator.energyWorkTicks);
            assertTrue(ExtendedTimeAccelerationManager.payForSubmission(accelerator, accepted));
            assertEquals(100, accelerator.consumedWorkTicks);
            assertEquals(accepted.workTicks(), accelerator.consumedWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void nearlyFullTargetChargesAndEnqueuesOnlyItsRemainingCapacity() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();
            ExtendedTimeAccelerationManager.AccelerationRequest request =
                    ExtendedTimeAccelerationManager.requestAcceleration(accelerator);

            ExtendedTimeAccelerationManager.PreparedAcceleration accepted =
                    ExtendedTimeAccelerationManager.prepareAcceptedAcceleration(
                            accelerator, request, 100L, 95L).orElseThrow();

            assertEquals(5, accepted.workTicks());
            assertEquals(5, accelerator.fluidWorkTicks);
            assertEquals(5, accelerator.energyWorkTicks);
            assertTrue(ExtendedTimeAccelerationManager.payForSubmission(accelerator, accepted));
            assertEquals(5, accelerator.consumedWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void fullTargetRejectsSubmissionBeforeAnyCostIsCalculatedOrPaid() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingAccelerator accelerator = newRecordingAccelerator();
            ExtendedTimeAccelerationManager.AccelerationRequest request =
                    ExtendedTimeAccelerationManager.requestAcceleration(accelerator);

            assertFalse(ExtendedTimeAccelerationManager.prepareAcceptedAcceleration(
                    accelerator, request, 100L, 100L).isPresent());
            assertEquals(0, accelerator.fluidWorkTicks);
            assertEquals(0, accelerator.energyWorkTicks);
            assertEquals(0, accelerator.consumedWorkTicks);
        } finally {
            JDTEConfig.SERVER_SPEC.acceptConfig(null);
        }
    }

    @Test
    void zeroRoundedFluidCostStillAccumulatesFractionalCostFromWorkTicks() throws Exception {
        FractionalSettlementAccelerator accelerator = newFractionalSettlementAccelerator();
        ExtendedTimeAccelerationManager.PreparedAcceleration subMillibucketWork =
                new ExtendedTimeAccelerationManager.PreparedAcceleration(1, 1, 0, 0);

        for (int i = 0; i < 5; i++) {
            ExtendedTimeAccelerationManager.consumePreparedResources(accelerator, subMillibucketWork);
        }

        assertEquals(1, accelerator.drainedMb);
        assertEquals(0.0D, accelerator.pendingCost, 1.0E-9D);
    }

    private static IConfigSpec.ILoadedConfig loadedServerConfig(int durationSeconds) {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set(List.of("jdte", "timeAccelerator", "timeAcceleratorAccelerationDurationSeconds"), durationSeconds);
        JDTEConfig.SERVER_SPEC.correct(config);
        try {
            Class<?> loadedConfigClass = Class.forName("net.neoforged.fml.config.LoadedConfig");
            Constructor<?> constructor = loadedConfigClass.getDeclaredConstructor(
                    CommentedConfig.class,
                    java.nio.file.Path.class,
                    Class.forName("net.neoforged.fml.config.ModConfig"));
            constructor.setAccessible(true);
            return (IConfigSpec.ILoadedConfig) constructor.newInstance(config, null, null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to create LoadedConfig test fixture", e);
        }
    }

    private static RecordingAccelerator newRecordingAccelerator() throws Exception {
        return (RecordingAccelerator) unsafe().allocateInstance(RecordingAccelerator.class);
    }

    private static FractionalSettlementAccelerator newFractionalSettlementAccelerator() throws Exception {
        return (FractionalSettlementAccelerator) unsafe().allocateInstance(FractionalSettlementAccelerator.class);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static final class RecordingAccelerator extends TimeAcceleratorBE {
        private int fluidWorkTicks;
        private int energyWorkTicks;
        private int checkedFluidCost;
        private int checkedEnergyCost;
        private int consumedWorkTicks;
        private int consumedEnergyCost;

        private RecordingAccelerator() {
            super(null, BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }

        @Override
        public int getEffectiveMultiplier() {
            return 4;
        }

        @Override
        protected int getFluidDrainAmount(int workTicks) {
            fluidWorkTicks = workTicks;
            return 7;
        }

        @Override
        protected int getEnergyCost(int workTicks) {
            energyWorkTicks = workTicks;
            return 11;
        }

        @Override
        protected boolean hasResources(int fluidCost, int energyCost) {
            checkedFluidCost = fluidCost;
            checkedEnergyCost = energyCost;
            return true;
        }

        @Override
        protected void consumeResources(int workTicks, int energyCost) {
            consumedWorkTicks = workTicks;
            consumedEnergyCost = energyCost;
        }
    }

    private static final class FractionalSettlementAccelerator extends TimeAcceleratorBE {
        private double pendingCost;
        private int drainedMb;

        private FractionalSettlementAccelerator() {
            super(null, BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }

        @Override
        public int getEffectiveMultiplier() {
            return 1;
        }

        @Override
        protected void consumeResources(int workTicks, int energyCost) {
            TimeAcceleratorCostMath.Settlement settlement = TimeAcceleratorCostMath.settleFluid(
                    pendingCost, TimeAcceleratorCostMath.fluidCost(workTicks, 120.0D, 1.0D, 1.0D));
            drainedMb += settlement.drainMb();
            pendingCost = settlement.remainingCost();
        }
    }
}
