package com.jdte.common.items;
import com.direwolf20.justdirethings.common.capabilities.EnergyStorageItemStackNoReceive;
import com.direwolf20.justdirethings.common.items.PocketGenerator;
import com.jdte.mixin.EnergyStorageAccessor;
import com.jdte.mixin.NativeItemEnergyAccessor;
import net.minecraftforge.server.ServerLifecycleHooks;
/** Forge's base receive checks virtual canReceive(), even from the native internal force path. */
public final class NativePocketGeneratorEnergy {
    private NativePocketGeneratorEnergy() {}
    /** The native path must also reject a fuel worth less than one requested burn tick. */
    public static boolean canInitialize(PocketGenerator generator, net.minecraft.world.item.ItemStack stack) {
        var inventory=new net.minecraftforge.items.ItemStackHandler(1);
        if(stack.hasTag())inventory.deserializeNBT(stack.getTag().getCompound("FuelInventory"));
        var fuel=inventory.getStackInSlot(0);
        int time=net.minecraftforge.common.ForgeHooks.getBurnTime(fuel,net.minecraft.world.item.crafting.RecipeType.SMELTING);
        int multiplier=PortableFuelBurnSpeedHelper.resolveBurnSpeedMultiplier(fuel);
        // This is a Forge native special case, separate from the pinned Large implementation.
        if(fuel.is(net.minecraft.world.item.Items.COAL_BLOCK))multiplier=9;
        long speed=(long)com.direwolf20.justdirethings.setup.Config.POCKET_GENERATOR_BURN_SPEED_MULTIPLIER.get()*multiplier;
        return time>0&&speed>0&&time/speed>0&&(!fuel.hasCraftingRemainingItem()||fuel.getCount()==1);
    }
    public static int generate(EnergyStorageItemStackNoReceive storage,int amount,boolean simulate) {
        var stack=((NativeItemEnergyAccessor)storage).jdte$owner();var server=ServerLifecycleHooks.getCurrentServer();
        if(!(stack.getItem() instanceof PocketGenerator)||stack.getCount()!=1||server==null||!server.isSameThread()||server.isStopped())return 0;
        int stored=PocketGeneratorMath.bounded(stack.hasTag()?stack.getTag().getInt("Energy"):0,storage.getMaxEnergyStored());
        int accepted=Math.max(0,Math.min(amount,storage.getMaxEnergyStored()-stored));
        if(!simulate&&accepted>0){((EnergyStorageAccessor)storage).jdte$setEnergy(stored+accepted);stack.getOrCreateTag().putInt("Energy",stored+accepted);}
        return accepted;
    }
}
