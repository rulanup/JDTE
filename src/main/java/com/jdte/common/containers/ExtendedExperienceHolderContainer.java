package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.jdte.setup.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;

public class ExtendedExperienceHolderContainer extends BaseMachineContainer {
    private final int[] received = new int[5];
    public ExtendedExperienceHolderContainer(int id, Inventory inventory, FriendlyByteBuf data) { this(id,inventory,data.readBlockPos()); }
    public ExtendedExperienceHolderContainer(int id, Inventory inventory, BlockPos pos) {
        super(JDTEMenus.EXTENDED_EXPERIENCE_HOLDER.get(),id,inventory,pos);
        if (baseMachineBE == null && player.level().isClientSide) {
            var view = new ExtendedExperienceHolderBE(pos,JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get().defaultBlockState());
            view.setLevel(player.level()); baseMachineBE=view;
            var cards=com.jdte.common.upgrades.UpgradeHelper.getUpgradeHandler(view);
            com.jdte.common.utils.UpgradeSlotStorage.setUpgradeSlots(this,cards.getSlots());
            for(int i=0;i<cards.getSlots();i++)addSlot(new com.jdte.common.upgrades.UpgradeSlot(cards,i,0,0));
        }
        if (!(baseMachineBE instanceof ExtendedExperienceHolderBE)) throw new IllegalStateException("Invalid XP holder menu target at "+pos);
        addPlayerSlots(inventory);
        addDataSlots(new SplitIntContainerData(new ContainerData() {
            public int getCount(){return 5;}
            public int get(int i){
                if(player.level().isClientSide)return received[i];
                var b=(ExtendedExperienceHolderBE)baseMachineBE;
                return switch(i){case 0->b.exp;case 1->b.targetExp;case 2->b.ownerOnly?1:0;case 3->b.collectExp?1:0;default->b.showParticles?1:0;};
            }
            public void set(int i,int value){received[i]=value;}
        }));
    }
    public BlockPos getBlockPos(){return pos;}
    public int storedXp(){return player.level().isClientSide?received[0]:((ExtendedExperienceHolderBE)baseMachineBE).exp;}
    public int targetXp(){return player.level().isClientSide?received[1]:((ExtendedExperienceHolderBE)baseMachineBE).targetExp;}
    public boolean ownerOnly(){return player.level().isClientSide?received[2]!=0:((ExtendedExperienceHolderBE)baseMachineBE).ownerOnly;}
    public boolean collectExp(){return player.level().isClientSide?received[3]!=0:((ExtendedExperienceHolderBE)baseMachineBE).collectExp;}
    public boolean showParticles(){return player.level().isClientSide?received[4]!=0:((ExtendedExperienceHolderBE)baseMachineBE).showParticles;}
    @Override public boolean stillValid(Player player){return !baseMachineBE.isRemoved()&&player.level()==baseMachineBE.getLevel()&&player.level().hasChunkAt(pos)&&player.level().getBlockEntity(pos)==baseMachineBE&&stillValid(ContainerLevelAccess.create(player.level(),pos),player,JDTEBlocks.EXTENDED_EXPERIENCE_HOLDER.get());}
    @Override public void clicked(int slot,int button,ClickType type,Player player){if(stillValid(player))super.clicked(slot,button,type,player);}
    @Override public net.minecraft.world.item.ItemStack quickMoveStack(Player player,int slot){return stillValid(player)?super.quickMoveStack(player,slot):net.minecraft.world.item.ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player player,int id){
        if(!stillValid(player)||id<0||id>8||player.level().isClientSide)return false;
        var b=(ExtendedExperienceHolderBE)baseMachineBE;
        if(id<3)b.storeExp(player,id==0?1:id==1?10:-1);
        else if(id<6)b.extractExp(player,id==3?1:id==4?10:-1);
        else b.changeSettings(player,b.targetExp,id==6?!b.ownerOnly:b.ownerOnly,id==7?!b.collectExp:b.collectExp,id==8?!b.showParticles:b.showParticles);
        broadcastChanges(); return true;
    }
}
