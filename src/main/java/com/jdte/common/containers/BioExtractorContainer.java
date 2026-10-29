package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.BioExtractorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public abstract class BioExtractorContainer extends BaseMachineContainer {
    private final Block machineBlock;
    private final int[] synced=new int[2];
    @Override public int getFluidAmount(){return player.level().isClientSide?synced[0]:((BioExtractorBE)baseMachineBE).getFluidTank().getFluidAmount();}
    public int getScanProgress(){return player.level().isClientSide?synced[1]:((BioExtractorBE)baseMachineBE).getScanProgress();}

    protected BioExtractorContainer(@Nullable MenuType<?> menuType, int windowId, Inventory playerInventory,
                                    BlockPos blockPos, Block machineBlock) {
        super(menuType, windowId, playerInventory, blockPos);
        this.machineBlock = machineBlock;
        addDataSlots(new SplitIntContainerData(new net.minecraft.world.inventory.ContainerData(){
            public int getCount(){return 2;}
            public int get(int k){return player.level().isClientSide?synced[k]:k==0?((BioExtractorBE)baseMachineBE).getFluidTank().getFluidAmount():((BioExtractorBE)baseMachineBE).getScanProgress();}
            public void set(int k,int value){synced[k]=value;}
        }));
        addPlayerSlots(playerInventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return !baseMachineBE.isRemoved() && player.level().getBlockEntity(pos)==baseMachineBE && stillValid(ContainerLevelAccess.create(player.level(), pos), player, machineBlock);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack currentStack = slot.getItem();
        ItemStack originalStack = currentStack.copy();
        int playerStart = Math.max(0, slots.size() - 36);
        int playerEnd = slots.size();

        if (index < playerStart && !slot.mayPickup(player)) return ItemStack.EMPTY;
        if (index < playerStart) {
            if (!moveItemStackTo(currentStack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            return super.quickMoveStack(player, index);
        }

        if (currentStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (currentStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, currentStack);
        return originalStack;
    }
}
