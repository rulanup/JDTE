package com.jdte.common.blockentities;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.jdte.setup.JDTEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.config.IConfigSpec;
import sun.misc.Unsafe;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeAcceleratorTimingTest {

    @Test
    void durationSecondsBecomeMinecraftTicks() {
        assertEquals(20, TimeAcceleratorTiming.durationTicks(1));
        assertEquals(1200, TimeAcceleratorTiming.durationTicks(60));
    }

    @Test
    void nominalMultiplierIncludesTheNativeTick() {
        assertEquals(0, TimeAcceleratorTiming.additionalCycles(1));
        assertEquals(15, TimeAcceleratorTiming.additionalCycles(16));
        assertEquals(1023, TimeAcceleratorTiming.additionalCycles(1024));
    }

    @Test
    void durationOnlyBoundsOrdinaryPendingWork() {
        assertEquals(20_460L,
                TimeAcceleratorTiming.pendingWindowCycles(1024, 1, Long.MAX_VALUE));
        assertEquals(1_000L,
                TimeAcceleratorTiming.pendingWindowCycles(1024, 60, 1_000L));
    }

    @Test
    void legacyBatchWorkRemainsAvailableForProductionMachines() {
        assertEquals(400, TimeAcceleratorTiming.batchWorkTicks(4, 5));
    }

    @Test
    void configuredDurationDoesNotAmplifyBlockEntityWorkSubmission() throws Exception {
        JDTEConfig.SERVER_SPEC.acceptConfig(loadedServerConfig(5));
        try {
            assertEquals(3, newStubAccelerator().getAccelerationWorkTicksForTest(4));
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

    private static TestTimeAcceleratorBE newStubAccelerator() throws Exception {
        return (TestTimeAcceleratorBE) unsafe().allocateInstance(TestTimeAcceleratorBE.class);
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static final class TestTimeAcceleratorBE extends TimeAcceleratorBE {
        private TestTimeAcceleratorBE() {
            super(null, BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }

        @Override
        public int getEffectiveMultiplier() {
            return 1;
        }

        int getAccelerationWorkTicksForTest(int effectiveMultiplier) {
            return getAccelerationWorkTicks(effectiveMultiplier);
        }
    }
}
