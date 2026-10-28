package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.capabilities.TransmitterEnergyStorage;

/** Preserve native pooled FE semantics while debiting the actual buffers, never a negative local balance. */
final class ExtendedTransmitterEnergyStorage extends TransmitterEnergyStorage {
    private final ExtendedEnergyTransmitterBE owner;
    ExtendedTransmitterEnergyStorage(int capacity,ExtendedEnergyTransmitterBE owner){super(capacity,owner);this.owner=owner;}
    @Override public int receiveEnergy(int requested,boolean simulate){return transfer(requested,simulate,true);}
    @Override public int extractEnergy(int requested,boolean simulate){return transfer(requested,simulate,false);}
    private int transfer(int requested,boolean simulate,boolean insert){
        return TransmitterNetworkEnergy.transfer(owner,requested,simulate,insert);
    }
    @Override public int realReceiveEnergy(int n,boolean simulate){
        if(n<=0)return 0;int moved=super.realReceiveEnergy(n,simulate);if(!simulate&&moved>0)owner.setChanged();return moved;
    }
    @Override public int realExtractEnergy(int n,boolean simulate){
        if(n<=0)return 0;int moved=super.realExtractEnergy(n,simulate);if(!simulate&&moved>0)owner.setChanged();return moved;
    }
}
