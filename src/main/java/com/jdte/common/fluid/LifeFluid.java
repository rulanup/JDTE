package com.jdte.common.fluid;

import com.jdte.setup.JDTEFluids;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class LifeFluid extends BaseFlowingFluid {
    public static final Properties PROPERTIES = new Properties(
            JDTEFluids.LIFE_FLUID_TYPE,
            JDTEFluids.LIFE_FLUID_FLOWING,
            JDTEFluids.LIFE_FLUID_SOURCE
    ).bucket(JDTEFluids.LIFE_FLUID_BUCKET).block(JDTEFluids.LIFE_FLUID_BLOCK);

    protected LifeFluid(Properties properties) {
        super(properties);
    }

    @Override
    public Fluid getFlowing() {
        return JDTEFluids.LIFE_FLUID_FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return JDTEFluids.LIFE_FLUID_SOURCE.get();
    }

    @Override
    public Item getBucket() {
        return JDTEFluids.LIFE_FLUID_BUCKET.get();
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    public static class Flowing extends LifeFluid {
        public Flowing() {
            super(PROPERTIES);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends LifeFluid {
        public Source() {
            super(PROPERTIES);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
