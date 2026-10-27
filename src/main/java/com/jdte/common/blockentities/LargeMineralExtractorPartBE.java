package com.jdte.common.blockentities;

import com.jdte.common.blocks.LargeMineralExtractorPartBlock;
import com.jdte.setup.JDTEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

/** Forge capability endpoint. All storage and production remain owned by the controller. */
public final class LargeMineralExtractorPartBE extends BlockEntity {
    // Resolve on every operation: a pipe may retain a capability while the controller's
    // neighboring chunk unloads. A retained endpoint must never mutate a detached controller.
    private final IEnergyStorage energyRelay = new IEnergyStorage() {
        public int receiveEnergy(int amount, boolean simulate) {
            var be = controller(); return be == null ? 0 : be.getEnergyStorage().receiveEnergy(amount, simulate);
        }
        public int extractEnergy(int amount, boolean simulate) {
            var be = controller(); return be == null ? 0 : be.getEnergyStorage().extractEnergy(amount, simulate);
        }
        public int getEnergyStored() { var be = controller(); return be == null ? 0 : be.getEnergyStorage().getEnergyStored(); }
        public int getMaxEnergyStored() { var be = controller(); return be == null ? 0 : be.getEnergyStorage().getMaxEnergyStored(); }
        public boolean canExtract() { var be = controller(); return be != null && be.getEnergyStorage().canExtract(); }
        public boolean canReceive() { var be = controller(); return be != null && be.getEnergyStorage().canReceive(); }
    };
    private final IFluidHandler fluidRelay = new IFluidHandler() {
        public int getTanks() { return 2; }
        public FluidStack getFluidInTank(int tank) {
            var be = controller(); return be == null ? FluidStack.EMPTY : be.getCombinedFluidHandler().getFluidInTank(tank);
        }
        public int getTankCapacity(int tank) {
            var be = controller(); return be == null ? 0 : be.getCombinedFluidHandler().getTankCapacity(tank);
        }
        public boolean isFluidValid(int tank, FluidStack fluid) {
            var be = controller(); return be != null && be.getCombinedFluidHandler().isFluidValid(tank, fluid);
        }
        public int fill(FluidStack fluid, FluidAction action) {
            var be = controller(); return be == null ? 0 : be.getCombinedFluidHandler().fill(fluid, action);
        }
        public FluidStack drain(FluidStack fluid, FluidAction action) {
            var be = controller(); return be == null ? FluidStack.EMPTY : be.getCombinedFluidHandler().drain(fluid, action);
        }
        public FluidStack drain(int amount, FluidAction action) {
            var be = controller(); return be == null ? FluidStack.EMPTY : be.getCombinedFluidHandler().drain(amount, action);
        }
    };
    private final IItemHandler itemRelay = new IItemHandler() {
        public int getSlots() { var be = controller(); return be == null ? 0 : be.getAutomationItemHandler().getSlots(); }
        public ItemStack getStackInSlot(int slot) {
            var be = controller(); return be == null ? ItemStack.EMPTY : be.getAutomationItemHandler().getStackInSlot(slot);
        }
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            var be = controller(); return be == null ? stack : be.getAutomationItemHandler().insertItem(slot, stack, simulate);
        }
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            var be = controller(); return be == null ? ItemStack.EMPTY : be.getAutomationItemHandler().extractItem(slot, amount, simulate);
        }
        public int getSlotLimit(int slot) {
            var be = controller(); return be == null ? 0 : be.getAutomationItemHandler().getSlotLimit(slot);
        }
        public boolean isItemValid(int slot, ItemStack stack) {
            var be = controller(); return be != null && be.getAutomationItemHandler().isItemValid(slot, stack);
        }
    };
    private LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyRelay);
    private LazyOptional<IFluidHandler> fluids = LazyOptional.of(() -> fluidRelay);
    private LazyOptional<IItemHandler> items = LazyOptional.of(() -> itemRelay);
    public LargeMineralExtractorPartBE(BlockPos pos, BlockState state) {
        super(JDTEBlockEntities.LARGE_MINERAL_EXTRACTOR_PART.get(), pos, state);
    }
    private LargeMineralExtractorBE controller() {
        if (isRemoved() || level == null || !level.hasChunkAt(worldPosition)
                || level.getBlockEntity(worldPosition) != this
                || !(getBlockState().getBlock() instanceof LargeMineralExtractorPartBlock part)) return null;
        BlockPos pos = part.getControllerPos(worldPosition, getBlockState());
        return level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof LargeMineralExtractorBE be && !be.isRemoved() ? be : null;
    }
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (controller() != null) {
            if (cap == ForgeCapabilities.ENERGY) return energy.cast();
            if (cap == ForgeCapabilities.FLUID_HANDLER) return fluids.cast();
            if (cap == ForgeCapabilities.ITEM_HANDLER) return items.cast();
        }
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() {
        super.invalidateCaps(); energy.invalidate(); fluids.invalidate(); items.invalidate();
    }
    @Override public void reviveCaps() {
        super.reviveCaps();
        energy = LazyOptional.of(() -> energyRelay);
        fluids = LazyOptional.of(() -> fluidRelay);
        items = LazyOptional.of(() -> itemRelay);
    }
}
