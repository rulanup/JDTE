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

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static final class RecordingAccelerator extends TimeAcceleratorBE {
        private int fluidWorkTicks;
        private int energyWorkTicks;

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
    }
}
