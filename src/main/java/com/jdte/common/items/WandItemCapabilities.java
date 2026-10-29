package com.jdte.common.items;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import javax.annotation.Nullable;

/** Item-owned NBT views: stack copies, crafting and client inventory sync preserve both resources. */
final class WandItemCapabilities implements ICapabilityProvider {
    private static final String ENERGY="jdte:ultimate_time_wand_energy";
    private final ItemStack stack;
    private final LazyOptional<IEnergyStorage> energy;
    private final LazyOptional<IFluidHandlerItem> fluid;
    WandItemCapabilities(ItemStack stack){this.stack=stack;energy=LazyOptional.of(Energy::new);fluid=LazyOptional.of(()->new FluidHandlerItemStack(stack,UltimateTimeWandItem.configuredFluidCapacity()){
        @Override public boolean isFluidValid(int tank,FluidStack fluid){return fluid.getFluid() instanceof com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;}
        @Override public int getTankCapacity(int tank){return UltimateTimeWandItem.configuredFluidCapacity();}
        @Override public int fill(FluidStack resource,FluidAction action){capacity=UltimateTimeWandItem.configuredFluidCapacity();return super.fill(resource,action);}
    });}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,@Nullable Direction side){if(cap==ForgeCapabilities.ENERGY)return energy.cast();if(cap==ForgeCapabilities.FLUID_HANDLER_ITEM)return fluid.cast();return LazyOptional.empty();}
    private final class Energy implements IEnergyStorage {
        public int getEnergyStored(){CompoundTag tag=stack.getTag();return tag==null?0:Math.max(0,Math.min(getMaxEnergyStored(),tag.getInt(ENERGY)));}
        public int getMaxEnergyStored(){return UltimateTimeWandItem.configuredEnergyCapacity();}
        public boolean canExtract(){return true;} public boolean canReceive(){return true;}
        public int receiveEnergy(int requested,boolean simulate){int n=Math.max(0,Math.min(requested,getMaxEnergyStored()-getEnergyStored()));if(n>0&&!simulate)stack.getOrCreateTag().putInt(ENERGY,getEnergyStored()+n);return n;}
        public int extractEnergy(int requested,boolean simulate){int n=Math.max(0,Math.min(requested,getEnergyStored()));if(n>0&&!simulate)stack.getOrCreateTag().putInt(ENERGY,getEnergyStored()-n);return n;}
    }
}
