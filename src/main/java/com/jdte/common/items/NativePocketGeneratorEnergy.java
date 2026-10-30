package com.jdte.common.items;
import com.direwolf20.justdirethings.common.capabilities.EnergyStorageItemStackNoReceive;
import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.jdte.mixin.EnergyStorageAccessor;
import com.jdte.mixin.NativeItemEnergyAccessor;
import net.minecraftforge.server.ServerLifecycleHooks;
/** Forge's base receive checks virtual canReceive(), even from the native internal force path. */
public final class NativePocketGeneratorEnergy {
    private NativePocketGeneratorEnergy() {}
    public static int generate(EnergyStorageItemStackNoReceive storage,int amount,boolean simulate) {
        var stack=((NativeItemEnergyAccessor)storage).jdte$owner();var server=ServerLifecycleHooks.getCurrentServer();
        if(!(stack.getItem() instanceof PocketGenerator)||stack.getCount()!=1||server==null||!server.isSameThread()||server.isStopped())return 0;
        int stored=PocketGeneratorMath.bounded(stack.hasTag()?stack.getTag().getInt("Energy"):0,storage.getMaxEnergyStored());
        int accepted=Math.max(0,Math.min(amount,storage.getMaxEnergyStored()-stored));
        if(!simulate&&accepted>0){((EnergyStorageAccessor)storage).jdte$setEnergy(stored+accepted);stack.getOrCreateTag().putInt("Energy",stored+accepted);}
        return accepted;
    }
}
