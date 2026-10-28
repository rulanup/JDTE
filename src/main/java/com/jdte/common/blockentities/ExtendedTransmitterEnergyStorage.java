package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.capabilities.TransmitterEnergyStorage;

/** Preserve native pooled FE semantics while debiting the actual buffers, never a negative local balance. */
final class ExtendedTransmitterEnergyStorage extends TransmitterEnergyStorage {
    private final ExtendedEnergyTransmitterBE owner;
    ExtendedTransmitterEnergyStorage(int capacity,ExtendedEnergyTransmitterBE owner){super(capacity,owner);this.owner=owner;}
    @Override public int receiveEnergy(int requested,boolean simulate){return transfer(requested,simulate,true);}
    @Override public int extractEnergy(int requested,boolean simulate){return transfer(requested,simulate,false);}
    private int transfer(int requested,boolean simulate,boolean insert){
        if(requested<=0||!TransmitterPorts.live(owner)||(insert?!canReceive():!canExtract()))return 0;
        int remaining=Math.min(requested,getRealMaxEnergyStored()),total=0;
        for(var entry:owner.getTransmitterEnergyStorages().entrySet()){
            var storage=entry.getValue();int stored=storage.getRealEnergyStored();
            if(stored<0)continue;
            long room=insert?(long)storage.getRealMaxEnergyStored()-stored:stored;
            int offer=(int)Math.min(remaining,Math.max(0L,room));if(offer<=0)continue;
            int moved=insert?storage.realReceiveEnergy(offer,simulate):storage.realExtractEnergy(offer,simulate);
            moved=Math.max(0,Math.min(offer,moved));remaining-=moved;total+=moved;
            if(!simulate&&moved>0)owner.markBufferChanged(entry.getKey());
            if(remaining==0)break;
        }
        return total;
    }
    @Override public int realReceiveEnergy(int n,boolean simulate){
        if(n<=0)return 0;int moved=super.realReceiveEnergy(n,simulate);if(!simulate&&moved>0)owner.setChanged();return moved;
    }
    @Override public int realExtractEnergy(int n,boolean simulate){
        if(n<=0)return 0;int moved=super.realExtractEnergy(n,simulate);if(!simulate&&moved>0)owner.setChanged();return moved;
    }
}
