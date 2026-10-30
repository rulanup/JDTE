package com.jdte.smoke;

import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.direwolf20.justdirethings.common.items.tools.basetools.BaseBow;
import com.direwolf20.justdirethings.common.entities.JustDireArrow;
import com.jdte.common.items.LargePotionCanisterData;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.*;
import java.util.*;

/** Actual packaged JVMs and chunk events; FakePlayer native shots are not real-client input. */
public final class PotionCanisterLifecycleProbe {
 private static final BlockPos P=new BlockPos(1024,110,1024);
 private MinecraftServer server;private String mode="";private int stage,ticks;private boolean unloaded;private ChestBlockEntity old;private CompoundTag expected;
 private void check(boolean ok,String text){if(!ok)throw new IllegalStateException("Potion lifecycle: "+text);}
 private ChestBlockEntity chest(){return (ChestBlockEntity)server.overworld().getBlockEntity(P);}
 private void force(boolean v){server.overworld().setChunkForced(P.getX()>>4,P.getZ()>>4,v);}
 @SubscribeEvent public void start(ServerStartedEvent e)throws Exception{
  mode=System.getProperty("jdte.potionLifecycle","");if(mode.isEmpty())return;server=e.getServer();check(Files.exists(Path.of(".jdte-lifecycle-fixture")),"marked fixture");force(true);var l=server.overworld();l.getChunkAt(P);
  if(mode.equals("verify")){expected=NbtIo.readCompressed(Path.of("potion-expected.nbt").toFile());check(expected.equals(chest().saveWithoutMetadata()),"new JVM exact chest/item/effect/color/pending/bow data before work");stage=3;return;}
  l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING).set(false,server);
  for(var pos:BlockPos.betweenClosed(P.offset(-2,-1,-2),P.offset(3,2,3))){l.removeBlockEntity(pos);l.setBlockAndUpdate(pos,pos.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}l.setBlockAndUpdate(P,Blocks.CHEST.defaultBlockState());
  var stored=PotionProbeItems.filled();check(PotionCanister.getStoredPotion(stored)==net.minecraft.world.item.alchemy.Potions.SWIFTNESS&&PotionCanister.getPotionColor(stored)==0x12ab34,"native full-color/base getter interoperability");PotionProbeItems.handler(stored).insertItem(0,PotionProbeItems.potion(3),false);stored.getOrCreateTag().putString("fixture","pending-three-preserved");chest().setItem(0,stored);
  var bow=PotionProbeItems.bow();var canister=PotionProbeItems.filled();PotionProbeItems.handler(canister).insertItem(0,PotionProbeItems.potion(2),false);BaseBow.setPotionCanister(bow,canister,0);shoot(bow,1);
  check(PotionCanister.getPotionAmount(BaseBow.getPotionCanister(bow,0))==3900,"native bow exact100mB, no1000mB clamp");chest().setItem(1,bow);chest().setItem(2,new ItemStack(Items.GLASS_BOTTLE,32));chest().setChanged();
 }
 private void shoot(ItemStack bow,int count){
  var l=server.overworld();var actor=FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.fromString("352ba670-3440-45f6-b6e3-1067099b2001"),"PotionLifecycle"));actor.setPos(Vec3.atCenterOf(P.east()));actor.setItemInHand(InteractionHand.MAIN_HAND,bow);actor.getInventory().setItem(9,new ItemStack(Items.ARROW,count));
  for(int i=0;i<count;i++){
   var joined=new java.util.ArrayList<JustDireArrow>();java.util.function.Consumer<net.minecraftforge.event.entity.EntityJoinLevelEvent> listener=e->{if(e.getLevel()==l&&e.getEntity() instanceof JustDireArrow a&&!e.isCanceled())joined.add(a);};
   net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(listener);
   try{((BaseBow)bow.getItem()).releaseUsing(bow,l,actor,bow.getUseDuration()-20);}finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener);}
   var visible=l.getEntitiesOfClass(JustDireArrow.class,new AABB(P).inflate(5));
   check(joined.size()==1&&joined.get(0).isAddedToWorld()&&joined.get(0).getEffects().stream().anyMatch(x->x.getEffect()==net.minecraft.world.effect.MobEffects.REGENERATION&&x.getDuration()==321&&x.getAmplifier()==2),"actual admitted custom-effect arrow for native shot "+i+" joined="+joined.size()+" visible="+visible.size()+" ammo="+actor.getInventory().countItem(Items.ARROW)+" amount="+PotionCanister.getPotionAmount(BaseBow.getPotionCanister(bow,0))+" effects="+(joined.isEmpty()?"none":joined.get(0).getEffects()));
   com.mojang.logging.LogUtils.getLogger().info("Potion native shot admitted={}, visible={}, added={}",joined.size(),visible.size(),joined.get(0).isAddedToWorld());
   joined.forEach(net.minecraft.world.entity.Entity::discard);
  }
  check(actor.getInventory().countItem(Items.ARROW)==0,"each admitted native shot spends one ammo");actor.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
 }
 @SubscribeEvent public void unload(ChunkEvent.Unload e){if(server!=null&&stage==1&&e.getLevel()==server.overworld()&&e.getChunk().getPos().equals(new net.minecraft.world.level.ChunkPos(P)))unloaded=true;}
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent e)throws Exception{
  if(mode.isEmpty()||e.phase!=TickEvent.Phase.END||e.getServer()!=server)return;check(++ticks<1800,"timeout stage "+stage);var l=server.overworld();
  if(stage==0&&ticks>10){var item=chest().removeItem(0,1);check(!item.isEmpty()&&chest().getItem(0).isEmpty(),"actual inventory removal transfers one item");chest().setItem(5,item);check(PotionProbeItems.handler(item).getStackInSlot(0).getCount()==3,"pending slot survives move");expected=chest().saveWithoutMetadata();old=chest();force(false);stage=1;}
  else if(stage==1&&unloaded&&!l.getChunkSource().hasChunk(P.getX()>>4,P.getZ()>>4)){l.getChunkSource().save(true);force(true);l.getChunkAt(P);check(chest()!=old&&expected.equals(chest().saveWithoutMetadata()),"actual unload/fresh BE exact whole inventory NBT");stage=2;}
  else if(stage==2){NbtIo.writeCompressed(expected,Path.of("potion-expected.nbt").toFile());finish("potion-seed-success","PASS: minimal actual production native custom-effect arrow100mB/ammo, full4000mB/pending3 and nested3900mB/pending2 canisters,32 returned bottles, actual inventory move/unload/fresh chest/exact complete NBT and saved shutdown.");}
  else if(stage==3&&ticks>10){var item=chest().removeItem(5,1);check(PotionCanister.getPotionAmount(item)==4000&&PotionProbeItems.handler(item).getStackInSlot(0).getCount()==3,"fresh JVM full potion and partial batch restored");var bow=PotionProbeItems.bow();BaseBow.setPotionCanister(bow,item,0);shoot(bow,10);var after=BaseBow.getPotionCanister(bow,0);check(PotionCanister.getPotionAmount(after)==3000&&PotionProbeItems.handler(after).getStackInSlot(0).getCount()==3,"ten native shots spend exactly1000mB without touching pending potions");
   check(PotionProbeItems.handler(after).insertItem(0,PotionProbeItems.potion(1),false).isEmpty()&&PotionCanister.getPotionAmount(after)==4000,"fourth pending potion resumes one batch after restart and available capacity");var bottles=PotionProbeItems.handler(after).extractItem(0,4,false);check(bottles.is(Items.GLASS_BOTTLE)&&bottles.getCount()==4,"resumed batch returns exactly four bottles");chest().setItem(5,after);chest().setItem(2,new ItemStack(Items.GLASS_BOTTLE,36));chest().setChanged();finish("potion-verify-success","PASS: fresh production JVM restores exact full NBT, native ten-shot1000mB/ammo ledger and preserved pending3; one new potion resumes1000mB/four bottles, total36 bottles. FakePlayer method fixture, not real-client interaction.");}
 }
 private void finish(String marker,String text)throws Exception{Files.writeString(Path.of(marker),text+"\n");mode="";server.saveEverything(false,true,true);server.halt(false);}
}
