package com.jdte.smoke;
import com.jdte.common.blockentities.*;
import com.jdte.common.entities.MachineLightningEffects;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.*;
import java.util.*;

/** Real packaged server ticks/weather, exact resource ledger and survival wrench relocation. */
public final class LightningLifecycleProbe {
 private static final BlockPos A=new BlockPos(1023,110,1024),B=A.east(3);
 private MinecraftServer server;private String mode="";private int ticks,stage,at,bolts;private CompoundTag expected;private ChargedLightningRodBE unloadingOwner;private net.minecraftforge.energy.IEnergyStorage retained;
 private static void check(boolean ok,String text){if(!ok)throw new IllegalStateException("Lightning lifecycle: "+text);}
 private ChargedLightningRodBE be(BlockPos p){return (ChargedLightningRodBE)server.overworld().getBlockEntity(p);}
 private void force(boolean value){for(var p:List.of(A,B))server.overworld().setChunkForced(p.getX()>>4,p.getZ()>>4,value);}
 private void storm(boolean enabled){var l=server.overworld();l.setWeatherParameters(enabled?0:6000,enabled?6000:0,enabled,enabled);l.setRainLevel(enabled?1:0);l.setThunderLevel(enabled?1:0);}
 @SubscribeEvent public void start(ServerStartedEvent e)throws Exception{mode=System.getProperty("jdte.lightningLifecycle","");if(mode.isEmpty())return;check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked disposable server");server=e.getServer();storm(false);force(true);for(var p:List.of(A,B))server.overworld().getChunkAt(p);if(mode.equals("verify")){expected=NbtIo.readCompressed(Path.of("lightning-expected.nbt").toFile());check(expected.equals(be(B).saveWithoutMetadata()),"exact saved machine FE/cards/cooldown/settings/anchor before natural tick");stage=4;return;}
  var l=server.overworld();l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOFIRETICK).set(false,server);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING).set(false,server);
  for(var pos:BlockPos.betweenClosed(A.offset(-2,-1,-2),B.offset(2,4,2))){l.removeBlockEntity(pos);l.setBlockAndUpdate(pos,pos.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}
  l.setBlockAndUpdate(A,JDTEBlocks.CHARGED_LIGHTNING_ROD.get().defaultBlockState());be(A).setPlacedBy(UUID.fromString("352ba670-3440-45f6-b6e3-1067099b2001"));be(A).setLightningEffects(false,false);be(A).getEnergyStorage().setEnergy(10000000);storm(true);
 }
 @SubscribeEvent public void join(EntityJoinLevelEvent e){if(server==null||e.getLevel().isClientSide||!(e.getEntity() instanceof LightningBolt bolt)||!bolt.getPersistentData().contains(ChargedLightningRodBE.MACHINE_POSITION_TAG))return;
  check(bolt.getPersistentData().getBoolean(ChargedLightningRodBE.NATURAL_LIGHTNING_TAG)&&!((MachineLightningEffects)bolt).jdte$isLightningRenderEnabled()&&bolt.isSilent()&&bolt.getCause()==null,"actual vanilla bolt source marker/hidden/silent/cause");bolts++;
 }
 private void pack()throws Exception{var l=server.overworld();var actor=net.minecraftforge.common.util.FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.fromString("352ba670-3440-45f6-b6e3-1067099b2001"),"LightningPack"));actor.setPos(Vec3.atCenterOf(A.south()));for(int i=0;i<36;i++)actor.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));var before=be(A).saveWithoutMetadata();check(com.jdte.common.items.EclipseAlloyWrenchItem.pickupMachine(l,A,actor),"real survival full-inventory wrench");check(l.getBlockState(A.above()).isAir(),"owned rod removed without item drop");var drops=l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(A).inflate(3));check(drops.stream().noneMatch(d->d.getItem().is(Items.LIGHTNING_ROD)),"no extra rod item created");var machines=drops.stream().filter(d->d.getItem().is(JDTEItems.CHARGED_LIGHTNING_ROD.get())).toList();check(machines.size()==1,"exactly one packed machine");var packed=machines.get(0).getItem().copy();machines.get(0).discard();actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,packed);actor.setPos(Vec3.atCenterOf(B.south()));var use=new net.minecraft.world.item.context.UseOnContext(actor,net.minecraft.world.InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(B.below()),Direction.UP,B.below(),false));check(packed.useOn(use).consumesAction()&&packed.isEmpty(),"survival BlockItem placement consumes package");
  var after=be(B).saveWithoutMetadata();for(String key:List.of("chargedLightningRodEnergy","chargedLightningRodCooldown","chargedLightningRodRender","chargedLightningRodSound","jdteUpgrades"))if(before.contains(key))check(before.get(key).equals(after.get(key)),"transport retains "+key);
  UpgradeHelper.getUpgradeHandler(be(B)).insertItem(0,new ItemStack(JDTEItems.THUNDERSTORM_UPGRADE.get()),false);
  retained=be(B).getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);
 }
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent e)throws Exception{if(mode.isEmpty()||e.phase!=TickEvent.Phase.END||e.getServer()!=server)return;check(++ticks<1800,"timeout stage "+stage);var l=server.overworld();
  if(stage==0&&ticks>=20&&bolts>0){check(bolts==1&&be(A).getEnergyStorage().getEnergyStored()==10000000-10000*bolts&&be(A).getStrikeCooldown()>0,"real storm natural world ticks exact one base strike/payment: bolts="+bolts+" energy="+be(A).getEnergyStorage().getEnergyStored()+" weather="+be(A).canOperateInWeather());int cooldown=be(A).getStrikeCooldown();be(A).tickServer();check(be(A).getStrikeCooldown()==cooldown&&bolts==1,"same-time manual/accelerated invocation suppressed");storm(false);pack();stage=1;at=ticks;}
  else if(stage==1&&ticks-at>=210){check(l.getBlockState(B.above()).is(Blocks.LIGHTNING_ROD)&&be(B).ownsRod(),"relocated machine recreates exactly its owned rod");check(bolts>=2&&be(B).getEnergyStorage().getEnergyStored()==10000000-10000*bolts,"weather card resumes normal paid strikes in clear weather after transport");UpgradeHelper.getUpgradeHandler(be(B)).extractItem(0,1,false);expected=be(B).saveWithoutMetadata();NbtIo.writeCompressed(expected,Path.of("lightning-expected.nbt").toFile());unloadingOwner=be(B);force(false);stage=2;}
  else if(stage==2){for(var p:List.of(A,B))if(l.getChunkSource().hasChunk(p.getX()>>4,p.getZ()>>4))return;check(retained.receiveEnergy(1,false)==0&&retained.extractEnergy(1,false)==0,"old FE endpoints inert after actual unload");expected.putInt("chargedLightningRodCooldown",unloadingOwner.getStrikeCooldown());force(true);l.getChunkAt(B);check(expected.equals(be(B).saveWithoutMetadata()),"exact cooldown and complete machine NBT at actual reload boundary");stage=3;}
  else if(stage==3){check(expected.equals(be(B).saveWithoutMetadata()),"unload/reload full state with settled cooldown");UpgradeHelper.getUpgradeHandler(be(B)).insertItem(0,new ItemStack(JDTEItems.THUNDERSTORM_UPGRADE.get()),false);at=bolts;stage=6;}
  else if(stage==6&&bolts>at){UpgradeHelper.getUpgradeHandler(be(B)).extractItem(0,1,false);check(be(B).getStrikeCooldown()>0,"real nonzero cooldown prepared for saved restart");expected=be(B).saveWithoutMetadata();NbtIo.writeCompressed(expected,Path.of("lightning-expected.nbt").toFile());finish("lightning-seed-success","PASS: packaged real server storm work/tagged complete lightning/exact FE, same-time guard, full-inventory survival wrench relocation and single owned rod, clear-weather upgrade work, actual unload/reload with exact cooldown at boundary and inert FE. Nonzero natural cooldown saved. Weather set deterministically; optional AE2LT not installed.");}
  else if(stage==4&&ticks>=3){check(!be(B).isLightningRenderEnabled()&&!be(B).isLightningSoundEnabled(),"exact saved controls");UpgradeHelper.getUpgradeHandler(be(B)).insertItem(0,new ItemStack(JDTEItems.THUNDERSTORM_UPGRADE.get()),false);stage=5;at=ticks;}
  else if(stage==5&&ticks-at>=210){check(bolts>0&&be(B).getEnergyStorage().getEnergyStored()==expected.getInt("chargedLightningRodEnergy")-10000*bolts,"new production JVM resumes exactly paid real lightning");finish("lightning-verify-success","PASS: fresh production JVM restores exact FE/upgrades/cooldown/controls/owner anchor, then resumes real source-marked lightning at exact FE cost. No randomly arriving thunderstorm or real AE2LT integration claimed.");}
 }
 private void finish(String name,String text)throws Exception{Files.writeString(Path.of(name),text+"\n");mode="";server.saveEverything(false,true,true);server.halt(false);}
}
