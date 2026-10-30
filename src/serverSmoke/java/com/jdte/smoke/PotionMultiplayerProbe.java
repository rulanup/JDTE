package com.jdte.smoke;

import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.jdte.common.containers.LargePotionCanisterContainer;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.*;
import java.util.*;

/** Real remote clients own all menu clicks. Server-controlled material seeding/handoff is explicit. */
public final class PotionMultiplayerProbe {
 private final Path control=Path.of(System.getProperty("jdte.remoteControl","/tmp/jdte-potion-remote-control"));
 private MinecraftServer server;private int stage,ticks,at;private List<ServerPlayer> original=List.of();private CompoundTag expected;
 private void check(boolean v,String s){if(!v)throw new IllegalStateException("Potion multiplayer: "+s);}
 private void write(String file,String value){try{Files.writeString(control.resolve(file+".tmp"),value);Files.move(control.resolve(file+".tmp"),control.resolve(file),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(java.io.IOException x){throw new IllegalStateException(x);}}
 private void phase(int n){stage=n;at=ticks;write("phase",Integer.toString(n));}
 private boolean both(int n){return Files.exists(control.resolve("One-"+n))&&Files.exists(control.resolve("Two-"+n));}
 private List<ServerPlayer> players(){return server.getPlayerList().getPlayers().stream().filter(p->p.getGameProfile().getName().startsWith("JDTE")).sorted(Comparator.comparing(p->p.getGameProfile().getName())).toList();}
 private ServerPlayer person(String suffix){return players().stream().filter(p->p.getGameProfile().getName().endsWith(suffix)).findFirst().orElseThrow();}
 private ItemStack current(){return person("Two").getMainHandItem();}
 private void ledger(List<ServerPlayer> people,int stored,int pending,int loose,int bottles){
  int actualStored=0,actualPending=0,actualLoose=0,actualBottles=0,canisters=0;
  for(var p:people){for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.is(JDTEItems.LARGE_POTION_CANISTER.get())){canisters+=s.getCount();actualStored+=PotionCanister.getPotionAmount(s);var slot=PotionProbeItems.handler(s).getStackInSlot(0);if(slot.is(Items.POTION))actualPending+=slot.getCount();else if(slot.is(Items.GLASS_BOTTLE))actualBottles+=slot.getCount();}else if(s.is(Items.POTION))actualLoose+=s.getCount();else if(s.is(Items.GLASS_BOTTLE))actualBottles+=s.getCount();}
   var cursor=p.containerMenu.getCarried();if(cursor.is(Items.POTION))actualLoose+=cursor.getCount();if(cursor.is(Items.GLASS_BOTTLE))actualBottles+=cursor.getCount();
  }
  check(canisters==2&&actualStored==stored&&actualPending==pending&&actualLoose==loose&&actualBottles==bottles,"conservation canisters="+canisters+" stored="+actualStored+" pending="+actualPending+" loose="+actualLoose+" bottles="+actualBottles);
 }
 @SubscribeEvent public void start(ServerStartedEvent e)throws Exception{
  var mode=System.getProperty("jdte.potionMultiplayer","");if(mode.isEmpty())return;server=e.getServer();check(Files.exists(control.resolve(".fixture"))&&Files.exists(Path.of(".jdte-lifecycle-fixture")),"marked fixture");
  if(mode.equals("restart")){expected=NbtIo.readCompressed(control.resolve("expected.nbt").toFile());phase(9);return;}
  var l=server.overworld();l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING).set(false,server);
  var base=new BlockPos(32,100,32);for(var p:BlockPos.betweenClosed(base.offset(-3,-1,-3),base.offset(5,3,5)))l.setBlockAndUpdate(p,p.getY()==99?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());phase(0);
 }
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent e)throws Exception{
  if(server==null||e.phase!=TickEvent.Phase.END)return;
  try{step();}catch(Exception x){write("server-failure",x.toString());throw x;}
 }
 private void step()throws Exception{
  ticks++;if(stage==99)return;check(ticks-at<3600,"timeout stage "+stage);var people=players();
  if(stage==0&&people.size()==2){original=people;for(int n=0;n<2;n++){var p=people.get(n);p.getInventory().clearContent();p.getInventory().selected=0;var c=new ItemStack(JDTEItems.LARGE_POTION_CANISTER.get());c.getOrCreateTag().putString("fixture_owner",n==0?"One":"Two");p.setItemInHand(InteractionHand.MAIN_HAND,c);for(int i=9;i<=12;i++)p.getInventory().setItem(i,PotionProbeItems.potion(1));p.teleportTo(server.overworld(),32.5+n*2,100,33.5,0,0);p.inventoryMenu.broadcastChanges();}phase(1);}
  else if(stage==1&&both(1)){ledger(people,0,4,4,0);phase(2);}
  else if(stage==2&&both(2)){ledger(people,2000,0,0,8);var one=person("One");var two=person("Two");var moving=one.getMainHandItem();PotionProbeItems.handler(moving).insertItem(0,PotionProbeItems.potion(3),false);one.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);two.closeContainer();two.getInventory().setItem(35,two.getMainHandItem());two.setItemInHand(InteractionHand.MAIN_HAND,moving);two.getInventory().setItem(9,PotionProbeItems.potion(1));one.inventoryMenu.broadcastChanges();two.inventoryMenu.broadcastChanges();phase(3);}
  else if(stage==3&&both(3)){ledger(people,2000,3,1,8);check(!(person("One").containerMenu instanceof LargePotionCanisterContainer),"old owner menu invalidated after controlled physical item handoff");phase(4);}
  else if(stage==4&&both(4)){ledger(people,3000,0,0,12);PotionProbeItems.handler(current()).insertItem(0,PotionProbeItems.potion(2),false);person("Two").containerMenu.broadcastChanges();phase(5);}
  else if(stage==5&&both(5)){ledger(people,3000,1,1,12);check(person("Two").containerMenu.getCarried().is(Items.POTION),"real remote player holds one potion on cursor");phase(6);}
  else if(stage==6&&Files.exists(control.resolve("Two-6"))&&people.size()==1){ledger(original,3000,1,1,12);check(original.get(1).containerMenu.getCarried().isEmpty(),"logout settles cursor without duplicating potion");phase(7);}
  else if(stage==7&&people.size()==2&&both(7)){ledger(people,3000,1,1,12);for(var p:people)p.closeContainer();expected=new CompoundTag();for(var p:people)expected.put(p.getGameProfile().getName(),p.getInventory().save(new ListTag()));NbtIo.writeCompressed(expected,control.resolve("expected.nbt").toFile());phase(8);for(var p:people)p.connection.disconnect(net.minecraft.network.chat.Component.literal("Potion saved restart fixture"));}
  else if(stage==8&&people.isEmpty()){server.saveEverything(false,true,true);write("seed-success","PASS: two actual independent clients concurrently fill/recover distinct portable menus, controlled server handoff plus actual stale-C2S replay rejection, fourth potion resumes exactly once, real cursor logout/reconnect; exact two canisters/3000mB/pending1/loose1/twelve bottles saved. Handoff/material setup is instrumented, not player-driven drag/drop.\n");server.halt(false);stage=99;}
  else if(stage==9&&people.size()==2&&both(9)){for(var p:people)check(expected.getList(p.getGameProfile().getName(),Tag.TAG_COMPOUND).equals(p.getInventory().save(new ListTag())),"new JVM restores exact player inventory/effects/pending state "+p.getGameProfile().getName());ledger(people,3000,1,1,12);phase(10);}
  else if(stage==10&&both(10)){ledger(people,3000,1,1,12);write("restart-success","PASS: fresh production server and both actual reconnected clients restore exact inventory/potion effects/colors/pending slots and synchronized portable menu/empty cursor; two canisters/3000mB/pending1/loose1/twelve bottles conserved.\n");phase(11);for(var p:people)p.connection.disconnect(net.minecraft.network.chat.Component.literal("Potion multiplayer passed"));server.saveEverything(false,true,true);server.halt(false);stage=99;}
 }
}
