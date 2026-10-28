package com.jdte.common.containers;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseContainer;
import com.jdte.common.containers.handlers.LargeFuelCanisterHandler;
import com.jdte.common.items.LargeFuelCanisterItem;
import com.jdte.setup.JDTEMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.*;

public class LargeFuelCanisterContainer extends BaseContainer {
    public final LargeFuelCanisterHandler handler;
    public final ItemStack fuelCanisterItemstack;
    public final Player playerEntity;
    private final boolean curios;
    public LargeFuelCanisterContainer(int id,Inventory inv,FriendlyByteBuf buf){this(id,inv,buf.readBoolean(),buf);}
    private LargeFuelCanisterContainer(int id,Inventory inv,boolean curios,FriendlyByteBuf buf){this(id,inv,buf.readItem(),curios);}
    public LargeFuelCanisterContainer(int id,Inventory inv,ItemStack stack,boolean curios){
        super(JDTEMenus.LARGE_FUEL_CANISTER.get(),id);playerEntity=inv.player;fuelCanisterItemstack=stack;this.curios=curios;
        handler=new LargeFuelCanisterHandler(1,stack);addSlot(new SlotItemHandler(handler,0,80,35));addPlayerSlots(inv,8,84);
    }
    public ItemStack getCurrentStack(){var current=LargeFuelCanisterMenuAccess.resolve(playerEntity,curios);return current.getItem() instanceof LargeFuelCanisterItem?current:fuelCanisterItemstack;}
    public ItemStack getBoundStack(){return fuelCanisterItemstack;}
    @Override public boolean stillValid(Player p){var current=LargeFuelCanisterMenuAccess.resolve(p,curios);return current.getItem() instanceof LargeFuelCanisterItem&&(p.level().isClientSide||current==fuelCanisterItemstack);}
    @Override public void clicked(int index,int button,ClickType type,Player p){
        if(!stillValid(p)||index>=0&&index<slots.size()&&slots.get(index).getItem()==fuelCanisterItemstack||!curios&&type==ClickType.SWAP&&button==p.getInventory().selected)return;
        super.clicked(index,button,type,p);
    }
    @Override public ItemStack quickMoveStack(Player p,int index){
        if(!stillValid(p)||index<0||index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index);var stack=slot.getItem();if(stack.isEmpty()||stack==fuelCanisterItemstack)return ItemStack.EMPTY;var copy=stack.copy();
        if(!moveItemStackTo(stack,index==0?1:0,index==0?slots.size():1,index==0))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;
    }
    @Override public void removed(Player p){
        if(!p.level().isClientSide){var left=handler.getStackInSlot(0);handler.setStackInSlot(0,ItemStack.EMPTY);if(!left.isEmpty())ItemHandlerHelper.giveItemToPlayer(p,left);}
        super.removed(p);
    }
}
