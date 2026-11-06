package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseContainer;
import com.jdte.common.containers.handlers.LargePocketGeneratorHandler;
import com.jdte.common.items.LargePocketGeneratorItem;
import com.jdte.setup.JDTEMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class LargePocketGeneratorContainer extends BaseContainer {
    public final LargePocketGeneratorHandler handler;
    private final ItemStack bound;
    private final Player owner;
    private final boolean curios;
    public LargePocketGeneratorContainer(int id,Inventory inv,FriendlyByteBuf data){this(id,inv,data.readBoolean(),data);}
    private LargePocketGeneratorContainer(int id,Inventory inv,boolean curios,FriendlyByteBuf data){this(id,inv,data.readItem(),curios);}
    public LargePocketGeneratorContainer(int id,Inventory inv,ItemStack stack,boolean curios){
        super(JDTEMenus.LARGE_POCKET_GENERATOR.get(),id);bound=stack;owner=inv.player;this.curios=curios;
        handler=owner.level().isClientSide?new LargePocketGeneratorHandler(stack,false):(LargePocketGeneratorHandler)stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        var layout=com.jdte.common.utils.GuiUpgradeLayoutConfig.getInstance();
        addSlot(new SlotItemHandler(handler,0,layout.getPocketWidget("input_x",80),layout.getPocketWidget("input_y",35)){
            @Override public void setChanged(){handler.commitSlotChange();super.setChanged();}
        });addPlayerSlots(inv,8,84);
    }
    public ItemStack getBoundStack(){return bound;}
    public ItemStack getCurrentStack(){var current=LargePocketGeneratorMenuAccess.resolve(owner,curios);return current.getItem() instanceof LargePocketGeneratorItem?current:bound;}
    @Override public boolean stillValid(Player player){return player==owner&&player.isAlive()&&!player.isRemoved()&&bound.getItem() instanceof LargePocketGeneratorItem&&bound.getCount()==1&&(player.level().isClientSide||player.containerMenu==this&&LargePocketGeneratorMenuAccess.resolve(player,curios)==bound);}
    @Override public void clicked(int index,int button,ClickType type,Player player){
        if(!stillValid(player)||!curios&&index==slots.size()-9+player.getInventory().selected||index>=0&&index<slots.size()&&slots.get(index).getItem()==bound||!curios&&type==ClickType.SWAP&&button==player.getInventory().selected)return;
        super.clicked(index,button,type,player);
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player)||index<0||index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index);var stack=slot.getItem();if(stack.isEmpty()||stack==bound)return ItemStack.EMPTY;var copy=stack.copy();
        if(index==0){
            var offered=handler.extractItem(0,stack.getCount(),true);if(offered.isEmpty())return ItemStack.EMPTY;int count=offered.getCount();
            if(!moveItemStackTo(offered,1,slots.size(),true))return ItemStack.EMPTY;
            handler.extractItem(0,count-offered.getCount(),false);
        }else{
            var remainder=handler.insertItem(0,stack,false);if(remainder.getCount()==stack.getCount())return ItemStack.EMPTY;slot.set(remainder);
        }
        slot.onTake(player,copy);return copy;
    }
}
