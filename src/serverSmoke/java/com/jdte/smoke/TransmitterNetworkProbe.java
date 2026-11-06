package com.jdte.smoke;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.*;

/** New native-originated mixed-network persistence path; does not repeat prior JDTE wrench/unload probes. */
public final class TransmitterNetworkProbe {
    private static final BlockPos BASE=new BlockPos(4000,90,4000);
    private MinecraftServer server;private String mode="";private int ticks;
    private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException("Mixed transmitter network: "+message);}
    private EnergyTransmitterBE node(int i){return (EnergyTransmitterBE)server.overworld().getBlockEntity(BASE.east(i*2));}
    private long stock(){long sum=0;for(int i=0;i<3;i++)sum+=node(i).getEnergyStorage().getRealEnergyStored();return sum;}
    private CompoundTag snapshot(){var out=new CompoundTag();for(int i=0;i<3;i++){var data=node(i).saveWithFullMetadata();for(var k:new String[]{"id","x","y","z"})data.remove(k);out.put(Integer.toString(i),data);}return out;}
    @SubscribeEvent public void started(ServerStartedEvent event)throws Exception{
        mode=System.getProperty("jdte.transmitterNetworkLifecycle","");if(mode.isEmpty())return;
        check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked disposable server required");server=event.getServer();var level=server.overworld();level.setChunkForced(BASE.getX()>>4,BASE.getZ()>>4,true);level.getChunkAt(BASE);
        if(mode.equals("seed")){
            for(int i=0;i<3;i++){var pos=BASE.east(i*2);level.removeBlockEntity(pos);level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(pos,(i==0?JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get():Registration.EnergyTransmitter.get()).defaultBlockState());var node=node(i);node.getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.HIGH;node.setAreaSettings(6,1,1,0,0,0,false);node.getEnergyStorage().setEnergy(i==0?1000:i==1?8000:11000);node.setChanged();}
        }else if(mode.equals("verify"))check(NbtIo.readCompressed(Path.of("transmitter-network-expected.nbt").toFile()).equals(snapshot()),"fresh JVM restores full saved native/extended states");
        else throw new IllegalArgumentException(mode);
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event)throws Exception{
        if(mode.isEmpty()||event.phase!=TickEvent.Phase.END||event.getServer()!=server)return;if(++ticks<15)return;
        for(int i=0;i<3;i++)node(i).getBlocksToCharge();
        if(mode.equals("seed")){
            check(stock()==20000&&node(0).getTotalEnergyStored()==20000,"discovered mixed source/native network");
            for(int cycle=0;cycle<32;cycle++)for(int i=0;i<3;i++){var storage=node(i).getEnergyStorage();check(storage.extractEnergy(77,false)==77&&storage.receiveEnergy(77,false)==77,"native/JDTE traffic round trip");node(i).balanceEnergy();check(stock()==20000,"no duplicate or lost FE during overlapping traffic");}
            check(node(1).getEnergyStorage().extractEnergy(5000,false)==5000,"native peer originates actual pooled debit");node(0).balanceEnergy();
            check(stock()==15000,"exact 5000 external extraction");for(int i=0;i<3;i++)check(node(i).getEnergyStorage().getRealEnergyStored()==5000,"capacity-safe native/extended balance");
            NbtIo.writeCompressed(snapshot(),Path.of("transmitter-network-expected.nbt").toFile());finish("transmitter-network-seed-success.txt","PASS: reobfuscated native/JDTE mixed network, 96 pooled round trips, native-originated debit, exact 3x5000 owned FE saved");
        }else{
            check(stock()==15000,"native buffers survive real disk save and second JVM");check(node(2).getEnergyStorage().extractEnergy(5000,false)==5000&&stock()==10000,"restored native node debits actual buffers exactly once");
            finish("transmitter-network-restart-success.txt","PASS: second production JVM restores full three-node NBT and native owned FE; native post-restart transfer conserves 10000 remaining FE");
        }
    }
    private void finish(String file,String message)throws Exception{Files.writeString(Path.of(file),message+"\n");mode="";server.halt(false);}
}
