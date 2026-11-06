package com.jdte.smoke;

import com.jdte.common.blockentities.*;
import com.jdte.common.blocks.LargeMineralExtractorStructure;
import com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.*;
import java.util.*;

/** Dedicated-server assertions; every tested interaction arrives from independent remote clients. */
public final class MultiplayerProbe {
    private static final BlockPos NORMAL=new BlockPos(1023,80,1024), LARGE=new BlockPos(1027,80,1024), SOURCE=new BlockPos(1023,80,1020);
    private final Path control=Path.of(System.getProperty("jdte.remoteControl","/tmp/jdte-remote-control"));
    private MinecraftServer server;
    private int stage,ticks,at;
    private boolean restart;
    private List<ServerPlayer> originals;
    private CompoundTag expected;
    private void check(boolean value,String message){if(!value)throw new IllegalStateException("JDTE remote: "+message);}
    private void write(String name,String data){try{Files.writeString(control.resolve(name),data);}catch(java.io.IOException e){throw new IllegalStateException(e);}}
    private boolean done(String name){return Files.isRegularFile(control.resolve(name));}
    private void phase(int value){stage=value;at=ticks;write("phase",Integer.toString(value));}
    private List<ServerPlayer> players(){return server.getPlayerList().getPlayers().stream().filter(p->p.getGameProfile().getName().startsWith("JDTE")).sorted(Comparator.comparing(p->p.getGameProfile().getName())).toList();}
    private MineralExtractorBE machine(BlockPos pos){return (MineralExtractorBE)server.overworld().getBlockEntity(pos);}
    @SubscribeEvent public void started(ServerStartedEvent event){
        String mode=System.getProperty("jdte.multiplayer","");if(mode.isEmpty())return;
        check(Files.isRegularFile(control.resolve(".fixture")),"marked control fixture required");
        server=event.getServer();restart=mode.equals("restart");
        for(BlockPos pos:List.of(NORMAL,LARGE,SOURCE)){server.overworld().setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);server.overworld().getChunkAt(pos);}
        if(restart){try{expected=NbtIo.readCompressed(control.resolve("expected.nbt").toFile());}catch(java.io.IOException e){throw new IllegalStateException(e);}phase(9);return;}
        var level=server.overworld();
        for(int x=1021;x<1030;x++)for(int z=1019;z<1029;z++){
            level.setBlockAndUpdate(new BlockPos(x,79,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for(int y=80;y<84;y++){var pos=new BlockPos(x,y,z);level.removeBlockEntity(pos);level.setBlock(pos,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2|64);}
        }
        level.setBlockAndUpdate(NORMAL,JDTEBlocks.MINERAL_EXTRACTOR.get().defaultBlockState());
        level.setBlockAndUpdate(SOURCE,JDTEBlocks.MINERAL_EXTRACTOR.get().defaultBlockState());
        level.setBlockAndUpdate(LARGE,JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get().defaultBlockState());
        check(LargeMineralExtractorStructure.placeParts(level,LARGE,Direction.NORTH),"place large fixture");
        for(BlockPos pos:List.of(NORMAL,LARGE,SOURCE)){
            var be=machine(pos);be.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;be.setMultiplier(17);
            be.getEnergyStorage().setEnergy(1_000_000);
        }
        for(var drop:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(NORMAL).inflate(12)))drop.discard();
        var be=machine(NORMAL);UpgradeHelper.getUpgradeHandler(be).insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);
        be.getMachineHandler().setStackInSlot(1,new ItemStack(Items.IRON_ORE,128));
        be.getMachineHandler().setStackInSlot(17,new ItemStack(Items.IRON_ORE,800));
        be.getMachineHandler().setStackInSlot(49,new ItemStack(Items.IRON_ORE,4096));
        phase(0);
    }
    private void equip(ServerPlayer p,ItemStack stack){p.setItemInHand(InteractionHand.MAIN_HAND,stack);p.inventoryMenu.broadcastChanges();}
    private int capacityCards(Player p){int sum=p.getInventory().countItem(JDTEItems.CAPACITY_UPGRADE.get());for(var bag:com.jdte.common.items.AdvancedUpgradeStorageItem.findAllStorages(p)){var h=new AdvancedUpgradeStorageHandler(bag);for(int i=0;i<h.getSlots();i++)if(h.getStackInSlot(i).is(JDTEItems.CAPACITY_UPGRADE.get()))sum+=h.getStackInSlot(i).getCount();}return sum;}
    private int totalCards(){return players().stream().mapToInt(this::capacityCards).sum();}
    private void openNormal(){for(var p:players())JDTEBlocks.MINERAL_EXTRACTOR.get().openMenu(p,NORMAL);}
    private boolean bothDone(int value){return done("One-"+value)&&done("Two-"+value);}
    private CompoundTag snapshot(){var tag=new CompoundTag();tag.put("normal",machine(NORMAL).saveWithFullMetadata());tag.put("large",machine(LARGE).saveWithFullMetadata());for(var p:originals)tag.put(p.getGameProfile().getName(),p.getInventory().save(new ListTag()));return tag;}
    private void compareMachines(){for(var entry:Map.of("normal",NORMAL,"large",LARGE).entrySet()){var actual=machine(entry.getValue()).saveWithFullMetadata();var saved=expected.getCompound(entry.getKey());for(String key:List.of("inventory","energy","timeFluid","experienceFluid","multiplier","ForgeCaps","pendingBaseWork","pendingAcceleratedWork")){
            var a=actual.get(key);var b=saved.get(key);
            if(key.equals("ForgeCaps")&&a instanceof CompoundTag ca&&b instanceof CompoundTag cb){
                ca=ca.copy();cb=cb.copy();
                var fa=ca.getCompound("jdte:machine_data").getCompound("filter_handler");var fb=cb.getCompound("jdte:machine_data").getCompound("filter_handler");
                // Opening a menu expands empty ghost-filter rows from nine to 27; contents are unchanged.
                if(fa.getList("Items",Tag.TAG_COMPOUND).isEmpty()&&fb.getList("Items",Tag.TAG_COMPOUND).isEmpty()
                        &&fa.getInt("Size")==27&&fb.getInt("Size")==9){fa.putInt("Size",9);}
                a=ca;b=cb;
            }
            check(Objects.equals(a,b),"restart field "+entry.getKey()+"/"+key);
        }}}
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event){
        if(server==null||event.phase!=TickEvent.Phase.END)return;
        try{step();}catch(Throwable failure){write("failure",failure.toString());throw new IllegalStateException("Remote probe failed at stage "+stage,failure);}
    }
    private void step()throws java.io.IOException{
        ticks++;if(ticks-at>2400)throw new IllegalStateException("remote timeout stage "+stage);
        var people=players();
        if(stage==0&&people.size()==2){
            for(var drop:server.overworld().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(NORMAL).inflate(12)))drop.discard();
            originals=people;
            for(int i=0;i<2;i++){var p=people.get(i);p.teleportTo(server.overworld(),1024.5+i,80,1027.5,180,0);p.getInventory().clearContent();p.getInventory().selected=0;
                var bag=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());new AdvancedUpgradeStorageHandler(bag).insertItem(0,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get(),64),false);equip(p,bag);}
            phase(-2);
        }else if(stage==-2&&bothDone(-2)){
            openNormal();phase(1);
        }else if(stage==1&&bothDone(1)){
            check(machine(NORMAL).getMachineHandler().getStackInSlot(1).isEmpty(),"shared output extracted once");
            check(people.stream().allMatch(p->p.getInventory().countItem(Items.IRON_ORE)==64),"each remote player received exactly 64");phase(2);
        }else if(stage==2&&bothDone(2)){
            check(machine(NORMAL).getMachineHandler().getStackInSlot(17).getCount()==736,"first viewer page conserved");
            check(machine(NORMAL).getMachineHandler().getStackInSlot(49).getCount()==4032,"second viewer page conserved");
            check(people.stream().allMatch(p->p.getInventory().countItem(Items.IRON_ORE)==128),"independent page inventories");phase(3);
        }else if(stage==3&&bothDone(3)){
            check(UpgradeHelper.countUpgrades(machine(NORMAL),com.jdte.common.upgrades.UpgradeType.CAPACITY)==3,"concurrent inserts respect machine limit");check(totalCards()==125,"only three bag cards consumed");
            for(var p:people){p.closeContainer();p.getInventory().setItem(35,p.getMainHandItem());var copier=new ItemStack(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get());com.direwolf20.justdirethings.common.items.MachineSettingsCopier.setSettings(copier,true,true,true,true);JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get().saveSettings(machine(SOURCE),copier);equip(p,copier);}phase(4);
        }else if(stage==4&&bothDone(4)){
            check(UpgradeHelper.getUpgradeHandler(machine(NORMAL)).getStackInSlot(0).isEmpty(),"empty upgrade preset removes old cards");check(totalCards()==128,"concurrent copier refunds conserved");check(UpgradeHelper.countUpgrades(machine(NORMAL),com.jdte.common.upgrades.UpgradeType.CAPACITY)==0,"all surplus capacity cards removed");
            check(people.stream().mapToInt(p->p.getInventory().countItem(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get())).sum()==1,"ultimate card refunded exactly once");
            for(var p:people){p.getInventory().setItem(34,p.getMainHandItem());var bag=p.getInventory().getItem(35);p.getInventory().setItem(35,ItemStack.EMPTY);equip(p,bag);}phase(5);
        }else if(stage==5&&bothDone(5)){
            check(totalCards()==128,"simultaneous storage menus conserve cards");check(people.stream().mapToInt(p->p.getInventory().countItem(Items.IRON_ORE)).sum()==256,"all extracted ores remain with players");phase(6);
        }else if(stage==6&&people.size()==1&&done("One-6")){
            var old=originals.get(0);check(old.containerMenu==old.inventoryMenu,"disconnect closes storage menu");check(old.inventoryMenu.getCarried().isEmpty(),"disconnect settles carried cards");phase(7);
        }else if(stage==7&&people.size()==2&&bothDone(7)){
            var drops=server.overworld().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(NORMAL).inflate(12),e->e.getItem().is(JDTEItems.CAPACITY_UPGRADE.get()));
            long dropped=drops.stream().mapToLong(e->e.getItem().getCount()).sum();
            check(totalCards()+dropped==128,"reconnect preserves inventory plus vanilla cursor drops: players="+totalCards()+", world="+dropped);
            if(!drops.isEmpty()){
                var drop=drops.get(0);people.get(0).teleportTo(drop.getX(),drop.getY(),drop.getZ());return;
            }
            check(totalCards()==128,"reconnected player picked up all cursor drops");originals=people;expected=snapshot();NbtIo.writeCompressed(expected,control.resolve("expected.nbt").toFile());
            phase(8);for(var p:people)p.connection.disconnect(net.minecraft.network.chat.Component.literal("JDTE restart fixture"));
        }else if(stage==8&&people.isEmpty()){
            if(ticks-at<40)return;
            for(BlockPos pos:List.of(NORMAL,LARGE,SOURCE))server.overworld().setChunkForced(pos.getX()>>4,pos.getZ()>>4,false);
            if(server.overworld().getChunkSource().getChunkNow(NORMAL.getX()>>4,NORMAL.getZ()>>4)!=null)return;
            server.saveEverything(false,true,true);write("seed-success","PASS: two remote clients, shared and independent output pages, concurrent cards/copiers/bags, disconnect with cursor, reconnect, actual chunk unload and saved shutdown\n");server.halt(false);stage=99;
        }else if(stage==9&&people.size()==2&&bothDone(9)){
            compareMachines();check(totalCards()==128,"restart player bag/inventory counts");
            for(var p:people)check(p.getInventory().save(new ListTag()).equals(expected.get(p.getGameProfile().getName())),"exact player inventory restored "+p.getGameProfile().getName());
            openNormal();phase(10);
        }else if(stage==10&&bothDone(10)){
            compareMachines();write("restart-success","PASS: fresh dedicated server and independent clients restored exact player/machine inventories and reopened synchronized menus\n");phase(11);for(var p:people)p.connection.disconnect(net.minecraft.network.chat.Component.literal("JDTE remote checks passed"));server.saveEverything(false,true,true);server.halt(false);stage=99;
        }
    }
}
