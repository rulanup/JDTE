package com.jdte.common.containers.handlers;

import com.jdte.common.items.LargePotionCanisterData;
import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import net.minecraftforge.items.ItemStackHandler;
import java.util.Objects;

/** One item-backed slot shared by automation and menus, with no simulation side effects. */
public final class LargePotionCanisterHandler extends ItemStackHandler {
    public static final String KEY="PotionSlot";
    private final ItemStack owner;
    private final boolean authoritative;
    private CompoundTag observed;
    private boolean syncing;
    public LargePotionCanisterHandler(ItemStack owner,boolean authoritative){super(1);this.owner=owner;this.authoritative=authoritative;refresh();}
    private boolean live(){var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();return LargePotionCanisterData.isLarge(owner)&&owner.getCount()==1&&(!authoritative||server==null||server.isSameThread()&&!server.isStopped());}
    private void refresh(){
        if(syncing)return;var data=owner.hasTag()?owner.getTag().getCompound(KEY):new CompoundTag();if(Objects.equals(observed,data))return;
        var bounded=data.copy();bounded.putInt("Size",1);syncing=true;
        try{super.deserializeNBT(bounded);}finally{syncing=false;}
        observed=data.copy();
    }
    public void commitSlotChange(){refresh();onContentsChanged(0);}
    @Override protected void onContentsChanged(int slot){
        if(syncing||!live())return;
        var pending=super.getStackInSlot(0);
        if(authoritative&&pending.getCount()==4&&LargePotionCanisterData.tryFillBatch(owner,pending))stacks.set(0,new ItemStack(Items.GLASS_BOTTLE,4));
        var data=super.serializeNBT();owner.getOrCreateTag().put(KEY,data);observed=data.copy();
    }
    @Override public int getSlotLimit(int slot){return slot==0?4:0;}
    @Override protected int getStackLimit(int slot,ItemStack stack){return 4;}
    @Override public boolean isItemValid(int slot,ItemStack stack){return slot==0&&(stack.getItem() instanceof PotionItem||stack.is(Items.GLASS_BOTTLE));}
    @Override public ItemStack getStackInSlot(int slot){refresh();return super.getStackInSlot(slot);}
    @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){refresh();return live()?super.insertItem(slot,stack.copy(),simulate):stack;}
    @Override public ItemStack extractItem(int slot,int amount,boolean simulate){refresh();return live()?super.extractItem(slot,amount,simulate):ItemStack.EMPTY;}
    @Override public void setStackInSlot(int slot,ItemStack stack){
        refresh();if(!live()||!stack.isEmpty()&&(!isItemValid(slot,stack)||stack.getCount()>4))return;super.setStackInSlot(slot,stack.copy());
    }
    @Override public void deserializeNBT(CompoundTag nbt){
        if(owner==null){super.deserializeNBT(nbt);return;}
        refresh();if(!live())return;var bounded=nbt.copy();bounded.putInt("Size",1);syncing=true;try{super.deserializeNBT(bounded);}finally{syncing=false;}onContentsChanged(0);
    }
    @Override public CompoundTag serializeNBT(){refresh();return super.serializeNBT();}
}
