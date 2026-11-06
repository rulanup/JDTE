package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.AdvancedEntityAcceleratorBE;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class ExtendedEntityAcceleratorContainer extends BaseMachineContainer {
    public ExtendedEntityAcceleratorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory, extraData.readBlockPos());
    }

    public ExtendedEntityAcceleratorContainer(int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(JDTEMenus.EXTENDED_ENTITY_ACCELERATOR.get(), windowId, playerInventory, blockPos);
        addDataSlots(new SplitIntContainerData(new net.minecraft.world.inventory.ContainerData(){
            public int getCount(){return 2;}
            public int get(int i){return player.level().isClientSide?synced[i]:i==0?((com.jdte.common.blockentities.AdvancedEntityAcceleratorBE)baseMachineBE).getMultiplier():((com.jdte.common.blockentities.AdvancedEntityAcceleratorBE)baseMachineBE).getFluidTank().getFluidAmount();}
            public void set(int i,int v){synced[i]=v;}
        }));
        addPlayerSlots(player.getInventory());
    }

    public int getMultiplier() {
        return player.level().isClientSide?synced[0]:((com.jdte.common.blockentities.AdvancedEntityAcceleratorBE)baseMachineBE).getMultiplier();
    }

    private final int[] synced=new int[2];
    @Override public int getFluidAmount(){return player.level().isClientSide?synced[1]:((com.jdte.common.blockentities.AdvancedEntityAcceleratorBE)baseMachineBE).getFluidTank().getFluidAmount();}
    @Override
    public boolean stillValid(Player player) {
        return !baseMachineBE.isRemoved() && player.level().getBlockEntity(pos)==baseMachineBE && stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.EXTENDED_ENTITY_ACCELERATOR.get());
    }
    @Override public void clicked(int slot,int button,net.minecraft.world.inventory.ClickType type,Player player){if(stillValid(player))super.clicked(slot,button,type,player);}
}
