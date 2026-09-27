package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.upgrades.JDTEFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeAcceleratorOptimizationTest {

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static final class TestBasicTimeAccelerator extends TimeAcceleratorBE {
        private TestBasicTimeAccelerator() {
            super(null, BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }

        @Override
        public int getEffectiveMultiplier() {
            return 16;
        }
    }

    private static final class TestAdvancedTimeAccelerator extends AdvancedTimeAcceleratorBE {
        private TestAdvancedTimeAccelerator() {
            super(null, BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
        }
    }

    @Test
    void basicAcceleratorCannotRunWithoutFluid() throws Exception {
        TestBasicTimeAccelerator accelerator = (TestBasicTimeAccelerator) unsafe().allocateInstance(TestBasicTimeAccelerator.class);
        JDTEFluidTank tank = new JDTEFluidTank(1000, f -> true);
        Field tankField = TimeAcceleratorBE.class.getDeclaredField("fluidTank");
        tankField.setAccessible(true);
        tankField.set(accelerator, tank);

        // Empty fluid tank -> cannot run
        assertFalse(accelerator.canRun());

        // With fluid -> can run
        tank.setFluid(new FluidStack(Registration.TIME_FLUID_SOURCE.get(), 100));
        assertTrue(accelerator.canRun());
    }

    @Test
    void advancedAcceleratorRequiresBothFluidAndEnergy() throws Exception {
        TestAdvancedTimeAccelerator accelerator = (TestAdvancedTimeAccelerator) unsafe().allocateInstance(TestAdvancedTimeAccelerator.class);
        JDTEFluidTank tank = new JDTEFluidTank(1000, f -> true);
        Field tankField = TimeAcceleratorBE.class.getDeclaredField("fluidTank");
        tankField.setAccessible(true);
        tankField.set(accelerator, tank);

        MachineEnergyStorage storage = new MachineEnergyStorage(100000);
        Field storageField = AdvancedTimeAcceleratorBE.class.getDeclaredField("energyStorage");
        storageField.setAccessible(true);
        storageField.set(accelerator, storage);

        // Empty tank and no energy -> cannot run
        assertFalse(accelerator.canRun());

        // Fluid but 0 energy -> cannot run
        tank.setFluid(new FluidStack(Registration.TIME_FLUID_SOURCE.get(), 100));
        assertFalse(accelerator.canRun());

        // Energy but no fluid -> cannot run
        tank.setFluid(FluidStack.EMPTY);
        storage.setEnergy(5000);
        assertFalse(accelerator.canRun());

        // Both fluid and energy -> can run
        tank.setFluid(new FluidStack(Registration.TIME_FLUID_SOURCE.get(), 100));
        assertTrue(accelerator.canRun());
    }
}
