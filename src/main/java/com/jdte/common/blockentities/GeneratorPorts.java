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
final class GeneratorPorts {
    private final BaseMachineBE owner;
    private boolean unloaded;
    private LazyOptional<IEnergyStorage> energy;
    private LazyOptional<IItemHandler> items;
    private LazyOptional<IFluidHandler> fluid;

    GeneratorPorts(BaseMachineBE owner) { this.owner=owner; revive(); }
    boolean live() { return !unloaded && !owner.isRemoved() && owner.getLevel()!=null
            && owner.getLevel().isLoaded(owner.getBlockPos()) && owner.getLevel().getBlockEntity(owner.getBlockPos())==owner; }
    void loaded() { unloaded=false; }
    void unloaded() { unloaded=true; }
    static boolean owns(Capability<?> cap) { return cap==ForgeCapabilities.ENERGY || cap==ForgeCapabilities.ITEM_HANDLER || cap==ForgeCapabilities.FLUID_HANDLER; }
    <T> LazyOptional<T> get(Capability<T> cap) {
        if(!live())return LazyOptional.empty();
        if(cap==ForgeCapabilities.ENERGY)return energy.cast();
        if(cap==ForgeCapabilities.ITEM_HANDLER)return items.cast();
        return owner instanceof GeneratorFluidT1BE?fluid.cast():LazyOptional.empty();
    }
    void invalidate() { unloaded=true;energy.invalidate();items.invalidate();fluid.invalidate(); }
    void revive() {
        unloaded=false;
        energy=LazyOptional.of(EnergyPort::new);items=LazyOptional.of(ItemPort::new);fluid=LazyOptional.of(FluidPort::new);
    }
    private IEnergyStorage rawEnergy() { return ((PoweredMachineBE)owner).getEnergyStorage(); }
    /** Native output limit is per neighboring face. Resolve fresh ports; debit actual acceptance only. */
    void sendAdjacent(int perFace) {
        if(!live() || perFace<=0)return;
        for(Direction face:Direction.values()) {
            var pos=owner.getBlockPos().relative(face);
            if(!owner.getLevel().isLoaded(pos))continue;
            var peer=owner.getLevel().getBlockEntity(pos);
            if(peer==null || peer.isRemoved())continue;
            var target=peer.getCapability(ForgeCapabilities.ENERGY,face.getOpposite()).orElse(null);
            if(target==null || target==rawEnergy() || !target.canReceive())continue;
            int offered=rawEnergy().extractEnergy(perFace,true);
            if(offered<=0)return;
            int accepted=target.receiveEnergy(offered,false);
            if(accepted<0 || accepted>offered)throw new IllegalStateException("Neighbor violated FE acceptance contract at "+pos);
            if(accepted>0) {
                int removed=rawEnergy().extractEnergy(accepted,false);
                if(removed!=accepted)throw new IllegalStateException("Generator output debit changed during transfer at "+owner.getBlockPos());
                owner.setChanged();
            }
        }
    }
    private final class EnergyPort implements IEnergyStorage {
        public int receiveEnergy(int amount,boolean simulate) { if(!live()||amount<=0)return 0;int n=rawEnergy().receiveEnergy(amount,simulate);if(n>0&&!simulate)owner.setChanged();return n; }
        public int extractEnergy(int amount,boolean simulate) { if(!live()||amount<=0)return 0;int n=rawEnergy().extractEnergy(amount,simulate);if(n>0&&!simulate)owner.setChanged();return n; }
        public int getEnergyStored() { return live()?rawEnergy().getEnergyStored():0; }
        public int getMaxEnergyStored() { return live()?rawEnergy().getMaxEnergyStored():0; }
        public boolean canReceive() { return live()&&rawEnergy().canReceive(); }
        public boolean canExtract() { return live()&&rawEnergy().canExtract(); }
    }
    private final class ItemPort implements IItemHandler {
        public int getSlots() { return owner.getMachineHandler().getSlots(); }
        public ItemStack getStackInSlot(int slot) { return live()?owner.getMachineHandler().getStackInSlot(slot).copy():ItemStack.EMPTY; }
        public int getSlotLimit(int slot) { return owner.getMachineHandler().getSlotLimit(slot); }
        public boolean isItemValid(int slot,ItemStack stack) {
            if(!live()||stack.isEmpty())return false;
            if(owner instanceof GeneratorFluidT1BE generator)return generator.isStackValid(stack);
            int time=ForgeHooks.getBurnTime(stack,RecipeType.SMELTING);
            return FuelCanisterMath.energyBudget(time,((com.direwolf20.justdirethings.common.blockentities.GeneratorT1BE)owner).getFePerFuelTick(),1)>0;
        }
        public ItemStack insertItem(int slot,ItemStack stack,boolean simulate) {
            if(!isItemValid(slot,stack))return stack;
            ItemStack remaining=owner.getMachineHandler().insertItem(slot,stack.copy(),simulate);
            if(!simulate&&remaining.getCount()<stack.getCount())owner.setChanged();return remaining;
        }
        public ItemStack extractItem(int slot,int amount,boolean simulate) {
            if(!live()||amount<=0)return ItemStack.EMPTY;
            ItemStack result=owner.getMachineHandler().extractItem(slot,amount,simulate);
            if(!simulate&&!result.isEmpty())owner.setChanged();return result;
        }
    }
    private final class FluidPort implements IFluidHandler {
        private IFluidHandler tank() { return ((GeneratorFluidT1BE)owner).getFluidTank(); }
        public int getTanks() { return 1; }
        public FluidStack getFluidInTank(int index) { return live()?tank().getFluidInTank(index).copy():FluidStack.EMPTY; }
        public int getTankCapacity(int index) { return live()?tank().getTankCapacity(index):0; }
        public boolean isFluidValid(int index,FluidStack stack) { return live()&&tank().isFluidValid(index,stack); }
        public int fill(FluidStack stack,FluidAction action) { if(!live()||stack.isEmpty())return 0;int n=tank().fill(stack.copy(),action);if(n>0&&action.execute())owner.setChanged();return n; }
        public FluidStack drain(FluidStack stack,FluidAction action) { if(!live()||stack.isEmpty())return FluidStack.EMPTY;FluidStack n=tank().drain(stack,action);if(!n.isEmpty()&&action.execute())owner.setChanged();return n; }
        public FluidStack drain(int amount,FluidAction action) { if(!live()||amount<=0)return FluidStack.EMPTY;FluidStack n=tank().drain(amount,action);if(!n.isEmpty()&&action.execute())owner.setChanged();return n; }
    }
}
