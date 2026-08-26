package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.FilterableBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.direwolf20.justdirethings.util.interfacehelpers.FilterData;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ExtendedExperienceHolderBE extends BaseMachineBE
        implements AreaAffectingBE, RedstoneControlledBE, FilterableBE {
    public int exp;
    public int targetExp;
    public boolean collectExp;
    public boolean ownerOnly;
    public boolean showParticles = true;

    public FilterData filterData = new FilterData();
    public AreaAffectingData areaAffectingData;
    public RedstoneControlData redstoneControlData;
    private final IFluidHandler xpFluidHandler = new XpFluidHandler();

    public ExtendedExperienceHolderBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.EXTENDED_EXPERIENCE_HOLDER.get(), pos, state);
        areaAffectingData = new AreaAffectingData(state.getValue(BlockStateProperties.FACING).getOpposite());
        redstoneControlData = getDefaultRedstoneData();
        filterData.blockItemFilter = 0;
    }

    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public AreaAffectingData getAreaAffectingData() { return areaAffectingData; }
    @Override public FilterData getFilterData() { return filterData; }
    @Override public FilterBasicHandler getFilterHandler() { return getData(Registration.HANDLER_BASIC_FILTER); }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneControlData; }

    @Override
    public AreaAffectingData getDefaultAreaData(AreaAffectingBE area) {
        return area.getDefaultAreaData(getBlockState().getValue(BlockStateProperties.FACING).getOpposite());
    }

    @Override
    public RedstoneControlData getDefaultRedstoneData() {
        return new RedstoneControlData();
    }

    public IFluidHandler getXpFluidHandler() {
        return xpFluidHandler;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("exp", exp);
        tag.putInt("targetExp", targetExp);
        tag.putBoolean("collectExp", collectExp);
        tag.putBoolean("ownerOnly", ownerOnly);
        tag.putBoolean("showParticles", showParticles);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        exp = tag.getInt("exp");
        targetExp = tag.getInt("targetExp");
        collectExp = tag.getBoolean("collectExp");
        ownerOnly = tag.getBoolean("ownerOnly");
        showParticles = tag.getBoolean("showParticles");
    }

    private final class XpFluidHandler implements IFluidHandler {
        private static final int MB_PER_XP = 20;

        @Override public int getTanks() { return 1; }

        @Override
        public FluidStack getFluidInTank(int tank) {
            if (tank != 0 || exp <= 0) return FluidStack.EMPTY;
            return new FluidStack(Registration.XP_FLUID_SOURCE.get(), fluidAmount());
        }

        @Override public int getTankCapacity(int tank) { return Integer.MAX_VALUE; }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && stack.getFluid() == Registration.XP_FLUID_SOURCE.get();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!isFluidValid(0, resource)) return 0;
            int accepted = (int) Math.min((long) (resource.getAmount() / MB_PER_XP) * MB_PER_XP,
                    (long) Integer.MAX_VALUE - (long) exp * MB_PER_XP);
            accepted -= accepted % MB_PER_XP;
            if (action.execute() && accepted > 0) {
                exp = Math.min(Integer.MAX_VALUE, exp + accepted / MB_PER_XP);
                setChanged();
            }
            return accepted;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!isFluidValid(0, resource)) return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int amount, FluidAction action) {
            int drained = Math.min(exp, Math.max(0, amount / MB_PER_XP)) * MB_PER_XP;
            if (drained <= 0) return FluidStack.EMPTY;
            if (action.execute()) {
                exp -= drained / MB_PER_XP;
                setChanged();
            }
            return new FluidStack(Registration.XP_FLUID_SOURCE.get(), drained);
        }

        private int fluidAmount() {
            return (int) Math.min(Integer.MAX_VALUE, (long) exp * MB_PER_XP);
        }
    }
}
