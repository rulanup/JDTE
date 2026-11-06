package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.common.capabilities.TransmitterEnergyStorage;
import com.direwolf20.justdirethings.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import java.util.*;

/** Shared Forge-native/JDTE pooled FE accounting. The FE API is int; owned totals and balance plans are long. */
public final class TransmitterNetworkEnergy {
    private TransmitterNetworkEnergy() {}
    public static net.minecraftforge.energy.IEnergyStorage handler(EnergyTransmitterBE owner,BlockPos pos) {
        if(!TransmitterPorts.live(owner)||!owner.getLevel().isLoaded(pos))return null;
        if(pos.equals(owner.getBlockPos()))return owner.getEnergyStorage();
        if(!owner.getLevel().getBlockState(pos).is(Registration.EnergyTransmitter.get()))return null;
        var peer=owner.getLevel().getBlockEntity(pos);if(peer==null||peer.isRemoved())return null;
        return peer.getCapability(ForgeCapabilities.ENERGY,peer.getBlockState().getValue(BlockStateProperties.FACING)).orElse(null);
    }
    private static Map<BlockPos,TransmitterEnergyStorage> buffers(EnergyTransmitterBE owner) {
        if(!TransmitterPorts.live(owner))return Map.of();
        var result=new LinkedHashMap<BlockPos,TransmitterEnergyStorage>();
        var seen=new IdentityHashMap<TransmitterEnergyStorage,Boolean>();
        result.put(owner.getBlockPos(),owner.getEnergyStorage());seen.put(owner.getEnergyStorage(),Boolean.TRUE);
        for(var pos:owner.getTransmitterEnergyStorages().keySet()){
            if(pos.equals(owner.getBlockPos())||!owner.getLevel().isLoaded(pos)||!owner.getLevel().getBlockState(pos).is(Registration.EnergyTransmitter.get()))continue;
            var peer=owner.getLevel().getBlockEntity(pos);if(peer==null||peer.isRemoved())continue;
            var port=peer.getCapability(ForgeCapabilities.ENERGY,peer.getBlockState().getValue(BlockStateProperties.FACING)).orElse(null);
            if(port instanceof TransmitterEnergyStorage storage&&seen.put(storage,Boolean.TRUE)==null)result.put(pos,storage);
        }
        return result;
    }
    public static int total(EnergyTransmitterBE owner,boolean capacity){
        long sum=0;for(var storage:buffers(owner).values())sum+=Math.max(0,capacity?storage.getRealMaxEnergyStored():storage.getRealEnergyStored());
        return AdvancedEnergyTransmitterScheduler.clampToInt(sum);
    }
    public static int transfer(EnergyTransmitterBE owner,int requested,boolean simulate,boolean insert){
        var own=owner.getEnergyStorage();
        if(requested<=0||!TransmitterPorts.live(owner)||(insert?!own.canReceive():!own.canExtract()))return 0;
        int remaining=Math.min(requested,own.getRealMaxEnergyStored()),total=0;
        for(var entry:buffers(owner).entrySet()){
            var storage=entry.getValue();int stored=storage.getRealEnergyStored();if(stored<0)continue;
            long room=insert?(long)storage.getRealMaxEnergyStored()-stored:stored;
            int offer=(int)Math.min(remaining,Math.max(0L,room));if(offer<=0)continue;
            int moved=insert?storage.realReceiveEnergy(offer,simulate):storage.realExtractEnergy(offer,simulate);
            moved=Math.max(0,Math.min(offer,moved));remaining-=moved;total+=moved;
            if(!simulate&&moved>0)dirty(owner,entry.getKey());if(remaining==0)break;
        }
        return total;
    }
    public static void balance(EnergyTransmitterBE owner){
        var entries=new ArrayList<>(buffers(owner).entrySet());if(entries.size()<2)return;
        long remaining=0,capacity=0;
        for(var entry:entries){int stored=entry.getValue().getRealEnergyStored();if(stored<0)return;remaining+=stored;capacity+=Math.max(0,entry.getValue().getRealMaxEnergyStored());}
        if(remaining>capacity)return; // Do not silently delete pre-existing excess in a foreign buffer.
        entries.sort(Comparator.comparingInt(e->e.getValue().getRealMaxEnergyStored()));int count=entries.size();
        for(var entry:entries){
            int target=(int)Math.min(Math.max(0,entry.getValue().getRealMaxEnergyStored()),remaining/count+(remaining%count==0?0:1));
            if(entry.getValue().getRealEnergyStored()!=target){entry.getValue().setEnergy(target);dirty(owner,entry.getKey());owner.doParticles(owner.getBlockPos(),entry.getKey());}
            remaining-=target;count--;
        }
    }
    private static void dirty(EnergyTransmitterBE owner,BlockPos pos){var be=owner.getLevel().getBlockEntity(pos);if(be!=null)be.setChanged();}
}
