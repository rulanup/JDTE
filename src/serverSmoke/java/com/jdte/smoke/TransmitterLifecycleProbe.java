package com.jdte.smoke;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.*;
import java.util.*;

/** Opt-in two-JVM production-JAR lifecycle test, excluded from the released artifact. */
public final class TransmitterLifecycleProbe {
    private static final BlockPos[] POS = {new BlockPos(3000,90,3000),new BlockPos(3032,90,3000)};
    private MinecraftServer server;
    private String mode="";
    private int stage,ticks;
    private CompoundTag expected;
    private final IEnergyStorage[] retained=new IEnergyStorage[2];
    private final IItemHandler[] inventories=new IItemHandler[2];
    private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException("Transmitter lifecycle: "+message);}
    private BaseMachineBE machine(int i){return (BaseMachineBE)server.overworld().getBlockEntity(POS[i]);}
    private IEnergyStorage owned(int i){var be=machine(i);return be instanceof AdvancedEnergyTransmitterBE a?a.getEnergyStorage():((EnergyTransmitterBE)be).getEnergyStorage();}
    private CompoundTag state(int i){
        var data=machine(i).saveWithFullMetadata();
        for(String key:List.of("id","x","y","z","placedBy"))data.remove(key);
        return data;
    }
    private CompoundTag snapshot(){var data=new CompoundTag();for(int i=0;i<2;i++)data.put(Integer.toString(i),state(i));return data;}
    private void force(boolean value){var level=server.overworld();for(var p:POS)level.setChunkForced(p.getX()>>4,p.getZ()>>4,value);}
    @SubscribeEvent public void started(ServerStartedEvent event) throws Exception {
        mode=System.getProperty("jdte.transmitterLifecycle","");if(mode.isEmpty())return;
        check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked disposable server required");
        server=event.getServer();var level=server.overworld();force(true);for(var p:POS)level.getChunkAt(p);
        if(mode.equals("seed")) {
            for(int i=0;i<2;i++) {
                level.removeBlockEntity(POS[i]);level.setBlockAndUpdate(POS[i],Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(POS[i],(i==0?JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get():JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get()).defaultBlockState());
                var be=machine(i);((RedstoneControlledBE)be).getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.HIGH;
                var upgrades=UpgradeHelper.getUpgradeHandler(be);
                check(upgrades.getSlots()==8,"eight upgrade slots");
                check(upgrades.insertItem(0,new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get()),false).isEmpty(),"overload insertion");
                check(upgrades.insertItem(7,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false).isEmpty(),"eighth-slot capacity insertion");
                var battery=new ItemStack(Registration.EclipseAlloyPickaxe.get());battery.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).receiveEnergy(12345,false);
                be.getMachineHandler().setStackInSlot(0,battery);
                JDTEAttachments.filter(be).setStackInSlot(0,new ItemStack(JDTEItems.ADVANCED_FLUID_MIXER.get()));
                if(be instanceof AdvancedEnergyTransmitterBE advanced){
                    advanced.getEnergyStorage().setEnergy(1_900_000_000);advanced.setShowParticles(true);
                    advanced.applyCopiedPlayerBinding(Optional.of(UUID.fromString("ebdf6a41-7546-47d9-9cff-60abc13b92bd")),"OfflineProbe");
                    var tag=advanced.saveWithFullMetadata();tag.putLong("networkEnergyReserve",5_000_000_000L);advanced.load(tag);
                } else ((EnergyTransmitterBE)be).getEnergyStorage().setEnergy(30000);
                be.setChanged();
            }
            expected=snapshot();
            NbtIo.writeCompressed(expected,Path.of("transmitters-expected.nbt").toFile());stage=1;
        } else if(mode.equals("verify")) {
            expected=NbtIo.readCompressed(Path.of("transmitters-expected.nbt").toFile());
            check(expected.equals(snapshot()),"fresh JVM restored owned FE, long reserve, battery, upgrades, filters and binding");stage=4;
        } else throw new IllegalArgumentException(mode);
    }
    private void transport() {
        var level=server.overworld();
            // Exercise the actual wrench path, including the full-player-inventory drop branch.
            for(int i=0;i<2;i++) {
                var actor=new net.minecraft.server.level.ServerPlayer(server,level,new com.mojang.authlib.GameProfile(UUID.fromString("5b537ea3-e447-48ac-919d-aa6a527ac568"),"TransmitterProbe"));actor.getInventory().clearContent();
                for(int slot=0;slot<36;slot++)actor.getInventory().setItem(slot,new ItemStack(net.minecraft.world.item.Items.COBBLESTONE,64));
                actor.setPos(POS[i].getX()+.5,POS[i].getY()+.5,POS[i].getZ()+.5);
                // Failed earlier fixture runs may have left drops in chunks whose entity tracking was not ready.
                // Remove only pre-existing transmitter item entities in this marked, disposable fixture area.
                final int tier=i;
                for(var existing:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(POS[i]).inflate(3),
                        e->e.getItem().is(tier==0?JDTEItems.ADVANCED_ENERGY_TRANSMITTER.get():JDTEItems.EXTENDED_ENERGY_TRANSMITTER.get())))existing.discard();
                var old=machine(i);var face=old.getBlockState().getValue(BlockStateProperties.FACING);
                var stale=old.getCapability(ForgeCapabilities.ENERGY,face).orElseThrow(IllegalStateException::new);
                check(EclipseAlloyWrenchItem.pickupMachine(level,POS[i],actor),"wrench pickup");
                var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(POS[i]).inflate(3),e->e.getItem().is(tier==0?JDTEItems.ADVANCED_ENERGY_TRANSMITTER.get():JDTEItems.EXTENDED_ENERGY_TRANSMITTER.get()));
                check(drops.size()==1,"full inventory drops exactly one packed machine, got "+drops.size());
                var packed=drops.get(0).getItem().copy();drops.get(0).discard();
                level.setBlockAndUpdate(POS[i],(i==0?JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get():JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get()).defaultBlockState());
                var state=level.getBlockState(POS[i]);state.getBlock().setPlacedBy(level,POS[i],state,actor,packed);
                check(expected.getCompound(Integer.toString(i)).equals(state(i)),"packed state restored exactly for tier "+i+", expected="+expected.getCompound(Integer.toString(i))+", actual="+state(i));
                check(stale.receiveEnergy(100,false)==0&&stale.extractEnergy(100,false)==0,"old carried endpoint rejected");
                var be=machine(i);retained[i]=be.getCapability(ForgeCapabilities.ENERGY,face).orElseThrow(IllegalStateException::new);inventories[i]=be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
            }
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) throws Exception {
        if(mode.isEmpty()||event.phase!=TickEvent.Phase.END||event.getServer()!=server)return;
        check(++ticks<900,"timeout stage "+stage);var level=server.overworld();
        if(stage==1&&ticks>=20){check(expected.equals(snapshot()),"paused state stable");transport();server.saveEverything(false,true,true);force(false);stage=2;}
        else if(stage==2){
            for(var p:POS)if(level.getChunkSource().hasChunk(p.getX()>>4,p.getZ()>>4))return;
            for(int i=0;i<2;i++)check(retained[i].receiveEnergy(100,false)==0&&retained[i].extractEnergy(100,false)==0&&inventories[i].extractItem(0,1,false).isEmpty(),"retained endpoint rejects unloaded state");
            force(true);for(var p:POS)level.getChunkAt(p);stage=3;
        } else if(stage==3){check(expected.equals(snapshot()),"real unload/reload conserved every saved field");finish("transmitters-seed-success.txt","PASS: both production transmitters: full-inventory wrench transport, stale ports, real chunk unload/reload, exact FE/reserve/battery/upgrades/filter/binding conservation; optional AE2/Curios/JEI/Jade absent");}
        else if(stage==4&&ticks>=20){
            check(expected.equals(snapshot()),"paused fresh JVM stable");
            for(int i=0;i<2;i++){
                var port=machine(i).getCapability(ForgeCapabilities.ENERGY,machine(i).getBlockState().getValue(BlockStateProperties.FACING)).orElseThrow(IllegalStateException::new);
                if(machine(i) instanceof EnergyTransmitterBE nativeTransmitter)nativeTransmitter.getBlocksToCharge();
                int before=port.getEnergyStored();check(port.extractEnergy(123,false)==123&&port.getEnergyStored()==before-123,"restored real FE capability");
            }
            check(((AdvancedEnergyTransmitterBE)machine(0)).saveWithFullMetadata().getLong("networkEnergyReserve")==5_000_000_000L,"long reserve not silently refunded or truncated");
            finish("transmitters-restart-success.txt","PASS: second production JVM restored both tiers exactly; real FE extraction spends exactly 123 per tier; long reserve remains owned; no optional mod linkage failure");
        }
    }
    private void finish(String file,String text)throws Exception{Files.writeString(Path.of(file),text+"\n");mode="";server.halt(false);}
}
