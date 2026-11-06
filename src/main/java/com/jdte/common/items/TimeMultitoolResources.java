package com.jdte.common.items;

import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.server.ServerLifecycleHooks;

/** Canonical native-format item resources with a scoped removal reservation. No cached FE/fluid copies. */
public final class TimeMultitoolResources implements ICapabilityProvider {
    private final ItemStack owner;
    private boolean reserved;
    private final LazyOptional<IEnergyStorage> energy=LazyOptional.of(Energy::new);
    private final LazyOptional<IFluidHandlerItem> fluid=LazyOptional.of(Fluid::new);
    public TimeMultitoolResources(ItemStack owner){this.owner=owner;}
    public static boolean live(ItemStack stack){var s=ServerLifecycleHooks.getCurrentServer();return stack.getItem() instanceof TimeMultitoolItem&&stack.getCount()==1&&s!=null&&s.isSameThread()&&!s.isStopped();}
    public <T> LazyOptional<T> getCapability(Capability<T> cap,Direction side){if(cap==ForgeCapabilities.ENERGY)return energy.cast();if(cap==ForgeCapabilities.FLUID_HANDLER_ITEM)return fluid.cast();return LazyOptional.empty();}
    public static TimeMultitoolResources of(ItemStack stack){return ((Energy)stack.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new)).resources();}
    private int stored(){return owner.hasTag()?Math.max(0,Math.min(TimeMultitoolItem.MAX_ENERGY,owner.getTag().getInt("Energy"))):0;}
    private FluidStack contents(){if(!owner.hasTag())return FluidStack.EMPTY;var f=FluidStack.loadFluidStackFromNBT(owner.getTag().getCompound("Fluid"));if(!(f.getFluid() instanceof com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid))return FluidStack.EMPTY;f.setAmount(Math.min(TimeMultitoolItem.MAX_TIME_FLUID,Math.max(0,f.getAmount())));return f;}
    private void energy(int amount){owner.getOrCreateTag().putInt("Energy",amount);}
    private void fluid(FluidStack f){if(f.isEmpty())owner.getOrCreateTag().remove("Fluid");else owner.getOrCreateTag().put("Fluid",f.writeToNBT(new CompoundTag()));}
    public Lease reserve(int fe,int mb){
        if(!live(owner)||reserved||fe<0||mb<0||stored()<fe||contents().getAmount()<mb)return null;
        var oldEnergy=owner.hasTag()&&owner.getTag().contains("Energy")?owner.getTag().get("Energy").copy():null;
        var oldFluid=owner.hasTag()&&owner.getTag().contains("Fluid")?owner.getTag().get("Fluid").copy():null;
        reserved=true;if(fe>0)energy(stored()-fe);if(mb>0){var f=contents();f.shrink(mb);fluid(f);}return new Lease(oldEnergy,oldFluid,fe>0,mb>0);
    }
    public final class Lease implements AutoCloseable {
        private final Tag oldEnergy,oldFluid;private final boolean chargedEnergy,chargedFluid;private boolean finished;
        private Lease(Tag energy,Tag fluid,boolean chargedEnergy,boolean chargedFluid){oldEnergy=energy;oldFluid=fluid;this.chargedEnergy=chargedEnergy;this.chargedFluid=chargedFluid;}
        public void commit(){if(finished)return;finished=true;reserved=false;}
        public void close(){if(finished)return;var tag=owner.getOrCreateTag();if(chargedEnergy){if(oldEnergy==null)tag.remove("Energy");else tag.put("Energy",oldEnergy);}if(chargedFluid){if(oldFluid==null)tag.remove("Fluid");else tag.put("Fluid",oldFluid);}finished=true;reserved=false;}
    }
    private final class Energy implements IEnergyStorage {
        private TimeMultitoolResources resources(){return TimeMultitoolResources.this;}
        public int getEnergyStored(){return stored();}public int getMaxEnergyStored(){return TimeMultitoolItem.MAX_ENERGY;}public boolean canExtract(){return true;}public boolean canReceive(){return true;}
        public int receiveEnergy(int n,boolean simulate){if(!live(owner)||reserved)return 0;int result=Math.max(0,Math.min(n,getMaxEnergyStored()-stored()));if(!simulate&&result>0)energy(stored()+result);return result;}
        public int extractEnergy(int n,boolean simulate){if(!live(owner)||reserved)return 0;int result=Math.max(0,Math.min(n,stored()));if(!simulate&&result>0)energy(stored()-result);return result;}
    }
    private final class Fluid implements IFluidHandlerItem {
        public ItemStack getContainer(){return owner;}public int getTanks(){return 1;}public FluidStack getFluidInTank(int tank){return contents();}public int getTankCapacity(int tank){return TimeMultitoolItem.MAX_TIME_FLUID;}
        public boolean isFluidValid(int tank,FluidStack f){return tank==0&&f.getFluid() instanceof com.direwolf20.justdirethings.common.fluids.timefluid.TimeFluid;}
        public int fill(FluidStack resource,FluidAction action){if(!live(owner)||reserved||resource.isEmpty()||!isFluidValid(0,resource))return 0;var current=contents();if(!current.isEmpty()&&!current.isFluidEqual(resource))return 0;int accepted=Math.max(0,Math.min(resource.getAmount(),getTankCapacity(0)-current.getAmount()));if(action.execute()&&accepted>0){var next=resource.copy();next.setAmount(current.getAmount()+accepted);fluid(next);}return accepted;}
        public FluidStack drain(FluidStack request,FluidAction action){return !request.isEmpty()&&request.isFluidEqual(contents())?drain(request.getAmount(),action):FluidStack.EMPTY;}
        public FluidStack drain(int amount,FluidAction action){if(!live(owner)||reserved||amount<=0)return FluidStack.EMPTY;var current=contents();int taken=Math.min(amount,current.getAmount());if(taken<=0)return FluidStack.EMPTY;var result=current.copy();result.setAmount(taken);if(action.execute()){current.shrink(taken);fluid(current);}return result;}
    }
}
