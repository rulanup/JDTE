package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.capabilities.TransmitterEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

/** Retained capabilities must not modify a detached or unloaded copy of a transmitter. */
final class TransmitterPorts {
    private TransmitterPorts() {}
    static boolean live(BaseMachineBE owner) {
        var level=owner.getLevel();
        if (owner instanceof AdvancedEnergyTransmitterBE advanced && advanced.isChunkUnloaded()
                || owner instanceof ExtendedEnergyTransmitterBE extended && extended.isChunkUnloaded()) return false;
        return level!=null&&!owner.isRemoved()&&level.isLoaded(owner.getBlockPos())&&level.getBlockEntity(owner.getBlockPos())==owner;
    }
    static boolean facing(BaseMachineBE owner,Direction side) {
        return live(owner)&&side!=null&&owner.getBlockState().getValue(BlockStateProperties.FACING)==side;
    }
    static IEnergyStorage energy(AdvancedEnergyTransmitterBE owner,Direction side) {
        return new IEnergyStorage(){
            private boolean valid(){return facing(owner,side);}
            @Override public int receiveEnergy(int n,boolean simulate){
                if(!valid()||n<=0)return 0;int moved=owner.getEnergyStorage().receiveEnergy(n,simulate);
                if(!simulate&&moved>0)owner.setChanged();return moved;
            }
            @Override public int extractEnergy(int n,boolean simulate){
                if(!valid()||n<=0)return 0;int moved=owner.getEnergyStorage().extractEnergy(n,simulate);
                if(!simulate&&moved>0)owner.setChanged();return moved;
            }
            @Override public int getEnergyStored(){return valid()?owner.getEnergyStorage().getEnergyStored():0;}
            @Override public int getMaxEnergyStored(){return valid()?owner.getEnergyStorage().getMaxEnergyStored():0;}
            @Override public boolean canReceive(){return valid()&&owner.getEnergyStorage().canReceive();}
            @Override public boolean canExtract(){return valid()&&owner.getEnergyStorage().canExtract();}
        };
    }
    static IItemHandler items(BaseMachineBE owner) {
        return new IItemHandler(){
            @Override public int getSlots(){return owner.getMachineHandler().getSlots();}
            @Override public ItemStack getStackInSlot(int slot){return live(owner)?owner.getMachineHandler().getStackInSlot(slot):ItemStack.EMPTY;}
            @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return live(owner)?owner.getMachineHandler().insertItem(slot,stack,simulate):stack;}
            @Override public ItemStack extractItem(int slot,int n,boolean simulate){return live(owner)?owner.getMachineHandler().extractItem(slot,n,simulate):ItemStack.EMPTY;}
            @Override public int getSlotLimit(int slot){return live(owner)?owner.getMachineHandler().getSlotLimit(slot):0;}
            @Override public boolean isItemValid(int slot,ItemStack stack){return live(owner)&&owner.getMachineHandler().isItemValid(slot,stack);}
        };
    }
    /** Keep JDT's instanceof check and local-buffer methods while guarding the public sided port. */
    static final class NativeEnergyPort extends TransmitterEnergyStorage {
        private final EnergyTransmitterBE owner;
        private final Direction side;
        NativeEnergyPort(EnergyTransmitterBE owner,Direction side){super(0,owner);this.owner=owner;this.side=side;}
        private boolean valid(){return facing(owner,side);}
        private TransmitterEnergyStorage raw(){return owner.getEnergyStorage();}
        @Override public void setEnergy(int n){if(valid())raw().setEnergy(n);}
        @Override public int receiveEnergy(int n,boolean simulate){return valid()?raw().receiveEnergy(n,simulate):0;}
        @Override public int extractEnergy(int n,boolean simulate){return valid()?raw().extractEnergy(n,simulate):0;}
        @Override public int realReceiveEnergy(int n,boolean simulate){return valid()?raw().realReceiveEnergy(n,simulate):0;}
        @Override public int realExtractEnergy(int n,boolean simulate){return valid()?raw().realExtractEnergy(n,simulate):0;}
        @Override public int getEnergyStored(){return valid()?raw().getEnergyStored():0;}
        @Override public int getMaxEnergyStored(){return valid()?raw().getMaxEnergyStored():0;}
        @Override public int getRealEnergyStored(){return valid()?raw().getRealEnergyStored():0;}
        @Override public int getRealMaxEnergyStored(){return valid()?raw().getRealMaxEnergyStored():0;}
        @Override public boolean canReceive(){return valid()&&raw().canReceive();}
        @Override public boolean canExtract(){return valid()&&raw().canExtract();}
        @Override public Tag serializeNBT(){return valid()?raw().serializeNBT():net.minecraft.nbt.IntTag.valueOf(0);}
        @Override public void deserializeNBT(Tag tag){if(valid())raw().deserializeNBT(tag);}
    }
}
