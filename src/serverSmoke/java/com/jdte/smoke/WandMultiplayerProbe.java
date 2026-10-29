package com.jdte.smoke;

import com.jdte.common.entities.*;
import com.jdte.common.items.UltimateTimeWandItem;
import com.jdte.common.manager.EntityAccelerationManager;
import com.jdte.setup.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import java.nio.file.*;
import java.util.*;

/** Opt-in test mod: real remote packets, live natural work and two production JVMs. */
public final class WandMultiplayerProbe {
 public static final BlockPos A=new BlockPos(1023,80,1024);
 private final Path control=Path.of(System.getProperty("jdte.remoteControl","/tmp/jdte-wand-remote-control"));
 private MinecraftServer server;private int stage,ticks,at,observedAge,observedTicks,observedRemaining;private boolean restart,cowLoaded,markerLoaded;
 private Cow cow;private UltimateTimeWandEntity marker;private FurnaceBlockEntity furnace;private CompoundTag expected;
 private void check(boolean value,String message){if(!value)throw new IllegalStateException("Wand multiplayer: "+message);}
 private void write(String name,String value){try{Files.writeString(control.resolve(name+".tmp"),value);Files.move(control.resolve(name+".tmp"),control.resolve(name),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(java.io.IOException x){throw new IllegalStateException(x);}}
 private void phase(int value){stage=value;at=ticks;write("phase",Integer.toString(value));}
 private boolean both(int n){return Files.exists(control.resolve("One-"+n))&&Files.exists(control.resolve("Two-"+n));}
 private List<ServerPlayer> players(){return server.getPlayerList().getPlayers().stream().filter(p->p.getGameProfile().getName().startsWith("JDTE")).sorted(Comparator.comparing(p->p.getGameProfile().getName())).toList();}
 private void force(boolean value){for(var p:List.of(A,A.east(2)))server.overworld().setChunkForced(p.getX()>>4,p.getZ()>>4,value);}
 private int rf(ItemStack stack){return stack.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).getEnergyStored();}
 private int fluid(ItemStack stack){return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new).getFluidInTank(0).getAmount();}
 private ItemStack wand(){var stack=new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());stack.getOrCreateTag().putString("fixture","retained-wand-NBT");stack.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).receiveEnergy(10000000,false);stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new).fill(new FluidStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(),1000),IFluidHandler.FluidAction.EXECUTE);return stack;}
 @SubscribeEvent public void start(ServerStartedEvent e){var mode=System.getProperty("jdte.wandMultiplayer","");if(mode.isEmpty())return;server=e.getServer();check(Files.isRegularFile(control.resolve(".fixture"))&&Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked disposable fixtures");restart=mode.equals("restart");
  // Source-supported duration only: leave enough live time for real logins/unload/restart.
  // Default 600-tick semantics are independently asserted by GameTests.
  JDTEConfig.COMMON.ultimateTimeWandDuration.set(6000);
  if(restart){try{expected=NbtIo.readCompressed(control.resolve("expected.nbt").toFile());}catch(java.io.IOException x){throw new IllegalStateException(x);}force(true);server.overworld().getChunkAt(A);furnace=(FurnaceBlockEntity)server.overworld().getBlockEntity(A);check(expected.getCompound("furnace").equals(furnace.saveWithFullMetadata()),"exact furnace disk state before natural tick");phase(9);return;}
  force(true);var l=server.overworld();for(var p:List.of(A,A.east(2)))l.getChunkAt(p);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING).set(false,server);
  for(var ent:l.getEntitiesOfClass(Entity.class,new AABB(A).inflate(10)))if(!(ent instanceof ServerPlayer))ent.discard();
  for(int x=-3;x<6;x++)for(int z=-3;z<6;z++)for(int y=-1;y<4;y++){var p=A.offset(x,y,z);l.removeBlockEntity(p);l.setBlock(p,y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2|64);}
  l.setBlockAndUpdate(A,Blocks.FURNACE.defaultBlockState());furnace=(FurnaceBlockEntity)l.getBlockEntity(A);furnace.setItem(0,new ItemStack(Items.RAW_IRON,64));furnace.setItem(1,new ItemStack(Items.COAL));
  cow=new Cow(net.minecraft.world.entity.EntityType.COW,l);cow.setPersistenceRequired();cow.setAge(-100000);cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0);cow.moveTo(A.getX()+2.5,A.getY(),A.getZ()+.5);l.addFreshEntity(cow);phase(0);
 }
 @SubscribeEvent public void join(EntityJoinLevelEvent e){if(server==null||!restart||expected==null||!e.loadedFromDisk())return;var entity=e.getEntity();if(entity.getUUID().toString().equals(expected.getString("cowUUID"))){check(entity instanceof Cow,"restored cow type");cow=(Cow)entity;check(cow.getAge()==expected.getInt("age"),"exact biological age at disk join");check(EntityAccelerationManager.get(cow)!=null&&EntityAccelerationManager.get(cow).save().equals(expected.getCompound("effect")),"exact saved entity effect at disk join");cowLoaded=true;}
  if(entity.getUUID().toString().equals(expected.getString("markerUUID"))){check(entity instanceof UltimateTimeWandEntity,"restored marker type");marker=(UltimateTimeWandEntity)entity;check(UltimateTimeWandEntity.saveState(marker.state()).equals(expected.getCompound("request")),"exact saved request at disk join");markerLoaded=true;}}
 @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST,receiveCanceled=true) public void blockClick(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock e){if(server==null||stage!=2)return;write("interaction-"+e.getEntity().getGameProfile().getName()+"-"+ticks,"pos="+e.getPos()+" shift="+e.getEntity().isShiftKeyDown()+" stack="+e.getItemStack()+" cancelled="+e.isCanceled()+" useItem="+e.getUseItem()+" useBlock="+e.getUseBlock());}
 private void ledger(int totalMultipliers){long fe=0;double spent=0;for(var p:players()){var w=p.getMainHandItem();check(w.is(JDTEItems.ULTIMATE_TIME_WAND.get())&&UltimateTimeWandItem.hasEntityAcceleration(w),"real GUI card installed");check(p.getInventory().countItem(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get())==1,"one attached card, one remains");fe+=rf(w);spent+=1000-fluid(w)+UltimateTimeWandItem.pendingFluid(w);check("retained-wand-NBT".equals(w.getOrCreateTag().getString("fixture")),"attachment preserves custom NBT");}
  int unit=(int)Math.floor(com.direwolf20.justdirethings.setup.Config.TIME_WAND_FE_COST.get()*JDTEConfig.COMMON.ultimateTimeWandEnergyCostMultiplier.get());double tf=totalMultipliers*com.direwolf20.justdirethings.setup.Config.TIME_WAND_FLUID_COST.get()*JDTEConfig.COMMON.ultimateTimeWandBaseCostMultiplier.get()/600d;
  check(fe==20000000L-(long)unit*totalMultipliers,"exact FE activation ledger "+fe);check(Math.abs(spent-tf)<1e-9,"exact fractional Time Fluid ledger "+spent+" vs "+tf);
 }
 private CompoundTag snapshot(){var t=new CompoundTag();t.put("furnace",furnace.saveWithFullMetadata());t.putString("cowUUID",cow.getUUID().toString());t.putString("markerUUID",marker.getUUID().toString());t.putInt("age",cow.getAge());t.put("effect",EntityAccelerationManager.get(cow).save());t.put("request",UltimateTimeWandEntity.saveState(marker.state()));return t;}
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){if(server==null||e.getServer()!=server||e.phase!=TickEvent.Phase.END)return;try{run();}catch(Exception x){write("server-failure",x.toString());throw new IllegalStateException(x);}}
 private void run()throws java.io.IOException{ticks++;check(ticks-at<3600,"timeout stage "+stage);var people=players();
  if(stage==0&&people.size()==2){for(var p:people){p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getInventory().clearContent();p.teleportTo(A.getX()+.5,A.getY(),A.getZ()+2.5);p.getInventory().selected=0;p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(0));p.getInventory().setItem(0,wand());p.getInventory().setItem(9,new ItemStack(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get(),2));p.inventoryMenu.broadcastChanges();}phase(-2);}
  else if(stage==-2&&both(-2))phase(1);
  else if(stage==1&&both(1)){ledger(0);phase(2);}
  else if(stage==2&&both(2)){var markers=server.overworld().getEntitiesOfClass(UltimateTimeWandEntity.class,new AABB(A).inflate(1));write("block-debug", "markers="+markers.size()+" valid="+com.jdte.common.blockentities.UltimateTimeWandTargetRuntime.resolve(server.overworld(),A)+" players="+people.stream().map(p->p.position()+" crouch="+p.isShiftKeyDown()+" build="+p.getAbilities().mayBuild+" menu="+p.containerMenu+" wand="+p.getMainHandItem().save(new CompoundTag())).toList());check(markers.size()==1,"one request for four real player clicks: "+markers.size());marker=markers.get(0);check(marker.getExponent()==4,"serialized shared block exponent four");ledger(30);phase(3);}
  else if(stage==3&&both(3)){check(EntityAccelerationManager.get(cow)!=null&&EntityAccelerationManager.get(cow).getExponent()==4,"serialized shared living exponent four");ledger(60);observedAge=cow.getAge();observedTicks=cow.tickCount;observedRemaining=EntityAccelerationManager.get(cow).getRemainingTime();phase(4);}
  else if(stage==4&&both(4)&&ticks-at>=10){int delta=observedRemaining-EntityAccelerationManager.get(cow).getRemainingTime();check(delta>=10&&cow.tickCount-observedTicks==delta&&cow.getAge()-observedAge==16*delta,"actual 16x age, natural tickCount/countdown once: age="+(cow.getAge()-observedAge)+" ticks="+(cow.tickCount-observedTicks)+" countdown="+delta);check(furnace.getItem(0).getCount()+furnace.getItem(2).getCount()==64&&furnace.getItem(2).getCount()>0,"real accelerated smelting item conservation");ledger(60);phase(6);}
  else if(stage==6&&Files.exists(control.resolve("One-6"))&&people.size()==1)phase(7);
  else if(stage==7&&people.size()==2&&both(7)){ledger(60);expected=new CompoundTag();for(var p:people){expected.put(p.getGameProfile().getName(),p.getInventory().save(new ListTag()));write("wand-"+(p.getGameProfile().getName().endsWith("One")?"One":"Two"),rf(p.getMainHandItem())+" "+fluid(p.getMainHandItem())+" "+UltimateTimeWandItem.pendingFluid(p.getMainHandItem()));}NbtIo.writeCompressed(expected,control.resolve("players.nbt").toFile());phase(8);for(var p:people)p.connection.disconnect(net.minecraft.network.chat.Component.literal("Wand disk recovery fixture"));force(false);}
  else if(stage==8&&people.isEmpty()){for(var pos:List.of(A,A.east(2)))if(server.overworld().getChunkSource().hasChunk(pos.getX()>>4,pos.getZ()>>4))return;check(EntityAccelerationManager.get(cow)!=null&&marker.getRemainingTime()>0,"effects still active after actual unload");NbtIo.writeCompressed(snapshot(),control.resolve("expected.nbt").toFile());server.saveEverything(false,true,true);write("seed-success","PASS: two independent clients attached cards through real inventory packets, raced four block/four entity activations, exact shared FE/fractional Time Fluid and card ledger, natural 16x biological ticks/real smelting, S2C timers, disconnect/reconnect and actual unload. Fixture source duration=6000; default 600 covered by GameTests.\n");server.halt(false);stage=99;}
  else if(stage==9&&people.size()==2&&both(9)&&cowLoaded&&markerLoaded){var saved=NbtIo.readCompressed(control.resolve("players.nbt").toFile());for(var p:people)check(saved.getList(p.getGameProfile().getName(),Tag.TAG_COMPOUND).equals(p.getInventory().save(new ListTag())),"exact saved held wand/card inventory after restart");phase(10);}
  else if(stage==10&&both(10)){write("restart-success","PASS: new production JVM restored exact furnace/marker/effect state at load, both original clients reconnected with exact wand FE/fluid/fraction/custom NBT/cards and synchronized active timers.\n");phase(11);server.saveEverything(false,true,true);server.halt(false);stage=99;}
 }
}
