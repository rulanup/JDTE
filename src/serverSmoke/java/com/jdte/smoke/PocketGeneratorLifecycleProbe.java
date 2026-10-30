package com.jdte.smoke;

import com.jdte.common.items.*;
import com.jdte.setup.JDTEItems;
import com.direwolf20.justdirethings.common.items.PocketGenerator;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.*;
import java.util.UUID;

/** Packaged production lifecycle; explicit FakePlayer method fixture, not real-client interaction. */
public final class PocketGeneratorLifecycleProbe {
    private static final BlockPos P=new BlockPos(1024,110,1024);
    private MinecraftServer server;private String mode="";private int stage,ticks;private boolean unloaded;private ChestBlockEntity old;private CompoundTag expected;
    private void check(boolean ok,String text){if(!ok)throw new IllegalStateException("Pocket lifecycle: "+text);}
    private ChestBlockEntity chest(){return (ChestBlockEntity)server.overworld().getBlockEntity(P);}
    private void force(boolean v){server.overworld().setChunkForced(P.getX()>>4,P.getZ()>>4,v);}
    @SubscribeEvent public void start(ServerStartedEvent e)throws Exception{
        mode=System.getProperty("jdte.pocketLifecycle","");if(mode.isEmpty())return;server=e.getServer();check(Files.exists(Path.of(".jdte-lifecycle-fixture")),"marked fixture");force(true);var l=server.overworld();l.getChunkAt(P);
        if(mode.equals("verify")){expected=NbtIo.readCompressed(Path.of("pocket-expected.nbt").toFile());check(expected.equals(chest().saveWithoutMetadata()),"fresh JVM exact whole chest/FE/fuel/counter/name/enabled state before work");stage=3;return;}
        l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING).set(false,server);
        for(var pos:BlockPos.betweenClosed(P.offset(-2,-1,-2),P.offset(3,2,3))){l.removeBlockEntity(pos);l.setBlockAndUpdate(pos,pos.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}l.setBlockAndUpdate(P,Blocks.CHEST.defaultBlockState());
        var gen=new ItemStack(JDTEItems.LARGE_POCKET_GENERATOR.get());gen.setHoverName(net.minecraft.network.chat.Component.literal("Retain portable fuel and burn"));gen.getOrCreateTag().putString("unrecognized_fixture_tag","keep");LargePocketGeneratorItem.fuel(gen).insertItem(0,new ItemStack(Items.COAL,3),false);
        var item=JDTEItems.LARGE_POCKET_GENERATOR.get();for(int i=0;i<10;i++)item.tryBurn(LargePocketGeneratorItem.energy(gen),gen);
        var target=new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());charge(gen,target);for(int i=0;i<10;i++)item.tryBurn(LargePocketGeneratorItem.energy(gen),gen);
        check(LargePocketGeneratorItem.energy(gen).getEnergyStored()==600&&target.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).getEnergyStored()==660&&gen.getTag().getInt(PocketGenerator.COUNTER)==379&&LargePocketGeneratorItem.fuel(gen).getStackInSlot(0).getCount()==2,"21 real production method ticks:600 buffer+660 actual inventory charge and preserved379 credit/two coal");
        chest().setItem(0,gen);chest().setItem(1,target);
        var full=new ItemStack(JDTEItems.LARGE_POCKET_GENERATOR.get());LargePocketGeneratorItem.energy(full).forceReceiveEnergy(Integer.MAX_VALUE,false);item.setEnabled(full,false);LargePocketGeneratorItem.fuel(full).insertItem(0,new ItemStack(Items.COAL,16),false);chest().setItem(2,full);chest().setChanged();
    }
    private void charge(ItemStack gen,ItemStack target){
        var p=FakePlayerFactory.get(server.overworld(),new com.mojang.authlib.GameProfile(UUID.fromString("352ba670-3440-45f6-b6e3-1067099b2002"),"PocketLifecycle"));p.getInventory().clearContent();p.getInventory().setItem(0,gen);p.getInventory().setItem(1,target);var item=JDTEItems.LARGE_POCKET_GENERATOR.get();item.inventoryTick(gen,server.overworld(),p,0,true);item.inventoryTick(gen,server.overworld(),p,0,true);p.getInventory().clearContent();
    }
    @SubscribeEvent public void unload(ChunkEvent.Unload e){if(server!=null&&stage==1&&e.getLevel()==server.overworld()&&e.getChunk().getPos().equals(new net.minecraft.world.level.ChunkPos(P)))unloaded=true;}
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e)throws Exception{
        if(mode.isEmpty()||e.phase!=TickEvent.Phase.END||e.getServer()!=server)return;check(++ticks<1800,"timeout stage "+stage);var l=server.overworld();
        if(stage==0&&ticks>10){var gen=chest().removeItem(0,1);check(!gen.isEmpty()&&chest().getItem(0).isEmpty(),"actual transfer removes one generator");chest().setItem(5,gen);chest().setChanged();old=chest();expected=old.saveWithoutMetadata();server.saveEverything(false,true,true);force(false);stage=1;}
        else if(stage==1&&unloaded&&!l.getChunkSource().hasChunk(P.getX()>>4,P.getZ()>>4)){l.getChunkSource().save(true);force(true);l.getChunkAt(P);check(chest()!=old&&expected.equals(chest().saveWithoutMetadata()),"actual unload/fresh BE retains exact full FE/fuel/progress/inventory NBT");stage=2;}
        else if(stage==2){NbtIo.writeCompressed(expected,Path.of("pocket-expected.nbt").toFile());finish("pocket-seed-success","PASS: minimal packaged Forge actual native-compatible FE/fuel generation+inventory charge (explicit FakePlayer method),600+660FE/379ticks/two coal, full4M/disabled generator; real inventory move, ChunkEvent.Unload/fresh chest/exact complete NBT and saved shutdown.");}
        else if(stage==3&&ticks>10){var gen=chest().removeItem(5,1);var target=chest().removeItem(1,1);charge(gen,target);check(LargePocketGeneratorItem.energy(gen).getEnergyStored()==0&&target.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).getEnergyStored()==1320&&gen.getTag().getInt(PocketGenerator.COUNTER)==378&&LargePocketGeneratorItem.fuel(gen).getStackInSlot(0).getCount()==2,"fresh JVM resumes exactly one tick/660FE charge; duplicate callback no debit");var full=chest().getItem(2);check(LargePocketGeneratorItem.energy(full).getEnergyStored()==4_000_000&&!JDTEItems.LARGE_POCKET_GENERATOR.get().getEnabled(full)&&LargePocketGeneratorItem.fuel(full).getStackInSlot(0).getCount()==16,"full FE/disabled/fuel survive new process");chest().setItem(5,gen);chest().setItem(1,target);chest().setChanged();finish("pocket-verify-success","PASS: fresh production JVM exact full saved data before work; resumed one tick/660FE inventory transfer,1320FE total target,378 credit/two coal; disabled full4M/sixteen coal unchanged. FakePlayer method fixture, not graphical input.");}
    }
    private void finish(String marker,String text)throws Exception{Files.writeString(Path.of(marker),text+"\n");mode="";server.saveEverything(false,true,true);server.halt(false);}
}
