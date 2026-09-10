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

class CrystalIncubatorAccelerationTest {

    @Test
    void standardEnergyCostUsesCrystalIncubatorBatchWorkTicks() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            RecordingCrystalIncubator incubator =
                    (RecordingCrystalIncubator) unsafe().allocateInstance(RecordingCrystalIncubator.class);

            assertEquals(401, incubator.getStandardEnergyCost());
            assertEquals(4, incubator.recordedMultiplier);
            assertEquals(400, incubator.recordedEnergyWorkTicks);
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

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static final class RecordingCrystalIncubator extends CrystalIncubatorBE {
        private int recordedMultiplier;
        private int recordedEnergyWorkTicks;

        private RecordingCrystalIncubator() {
            super(BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }

        @Override
        public int getEffectiveMultiplier() {
            return 4;
        }

        @Override
        protected int getAccelerationWorkTicks(int effectiveMultiplier) {
            recordedMultiplier = effectiveMultiplier;
            return super.getAccelerationWorkTicks(effectiveMultiplier);
        }

        @Override
        protected int getEnergyCost(int workTicks) {
            recordedEnergyWorkTicks = workTicks;
            return workTicks + 1;
        }
    }
}
