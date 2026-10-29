package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE;
import com.direwolf20.justdirethings.common.blockentities.GeneratorFluidT1BE;
import com.jdte.common.items.FuelCanisterMath;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

/** Live views of the native owned buffers. Retained interfaces cannot mutate an unloaded/replaced machine. */
final class EntityAcceleratorPorts {
    private final AdvancedEntityAcceleratorBE owner;
    private boolean unloaded;
    private LazyOptional<IEnergyStorage> energy;
    private LazyOptional<IFluidHandler> fluid;

    EntityAcceleratorPorts(AdvancedEntityAcceleratorBE owner) { this.owner=owner; revive(); }
    boolean live() { return !unloaded && !owner.isRemoved() && owner.getLevel()!=null
            && owner.getLevel().isLoaded(owner.getBlockPos()) && owner.getLevel().getBlockEntity(owner.getBlockPos())==owner; }
    void loaded() { unloaded=false; }
    void unloaded() { unloaded=true; }
    static boolean owns(Capability<?> cap) { return cap==ForgeCapabilities.ENERGY || cap==ForgeCapabilities.FLUID_HANDLER; }
    <T> LazyOptional<T> get(Capability<T> cap) {
        if(!live())return LazyOptional.empty();
        if(cap==ForgeCapabilities.ENERGY)return energy.cast();
        return fluid.cast();
    }
    void invalidate() { unloaded=true;energy.invalidate();fluid.invalidate(); }
    void revive() {
        unloaded=false;
        energy=LazyOptional.of(EnergyPort::new);fluid=LazyOptional.of(FluidPort::new);
    }
    private IEnergyStorage rawEnergy() { return ((PoweredMachineBE)owner).getEnergyStorage(); }
    private final class EnergyPort implements IEnergyStorage {
        public int receiveEnergy(int amount,boolean simulate) { if(!live()||amount<=0)return 0;int n=rawEnergy().receiveEnergy(amount,simulate);if(n>0&&!simulate)owner.setChanged();return n; }
        public int extractEnergy(int amount,boolean simulate) { if(!live()||amount<=0)return 0;int n=rawEnergy().extractEnergy(amount,simulate);if(n>0&&!simulate)owner.setChanged();return n; }
        public int getEnergyStored() { return live()?rawEnergy().getEnergyStored():0; }
        public int getMaxEnergyStored() { return live()?rawEnergy().getMaxEnergyStored():0; }
        public boolean canReceive() { return live()&&rawEnergy().canReceive(); }
        public boolean canExtract() { return live()&&rawEnergy().canExtract(); }
    }
    private final class FluidPort implements IFluidHandler {
        private IFluidHandler tank() { return owner.getFluidTank(); }
        public int getTanks() { return 1; }
        public FluidStack getFluidInTank(int index) { return live()?tank().getFluidInTank(index).copy():FluidStack.EMPTY; }
        public int getTankCapacity(int index) { return live()?tank().getTankCapacity(index):0; }
        public boolean isFluidValid(int index,FluidStack stack) { return live()&&tank().isFluidValid(index,stack); }
        public int fill(FluidStack stack,FluidAction action) { if(!live()||stack.isEmpty())return 0;int n=tank().fill(stack.copy(),action);if(n>0&&action.execute())owner.setChanged();return n; }
        public FluidStack drain(FluidStack stack,FluidAction action) { if(!live()||stack.isEmpty())return FluidStack.EMPTY;FluidStack n=tank().drain(stack,action);if(!n.isEmpty()&&action.execute())owner.setChanged();return n; }
        public FluidStack drain(int amount,FluidAction action) { if(!live()||amount<=0)return FluidStack.EMPTY;FluidStack n=tank().drain(amount,action);if(!n.isEmpty()&&action.execute())owner.setChanged();return n; }
    }
}
