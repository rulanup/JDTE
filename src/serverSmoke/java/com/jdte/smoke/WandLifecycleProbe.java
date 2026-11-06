package com.jdte.smoke;

import com.jdte.common.entities.*;
import com.jdte.common.items.UltimateTimeWandItem;
import com.jdte.common.manager.EntityAccelerationManager;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import java.nio.file.*;
import java.util.UUID;

/** Minimal-dependency production smoke; separate from the actual remote-client fixture. */
public final class WandLifecycleProbe {
 private final BlockPos pos=new BlockPos(1023,80,1024);private MinecraftServer server;private boolean restart,cowRestored,markerRestored;private int ticks,stage,at,age,remaining;private Cow cow;private UltimateTimeWandEntity marker;private CompoundTag expected;private ItemStack wand;private net.minecraftforge.common.util.FakePlayer actor;private boolean activated;
 private void check(boolean v,String s){if(!v)throw new IllegalStateException("Wand lifecycle: "+s);}
 private void save()throws java.io.IOException{NbtIo.writeCompressed(expected,Path.of("wand-expected.nbt").toFile());}
 private void force(boolean value){server.overworld().setChunkForced(pos.getX()>>4,pos.getZ()>>4,value);}
 @SubscribeEvent public void start(ServerStartedEvent e)throws java.io.IOException{var mode=System.getProperty("jdte.wandLifecycle","");if(mode.isEmpty())return;server=e.getServer();restart=mode.equals("verify");check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked server");check(!net.minecraftforge.fml.ModList.get().isLoaded("ae2")&&!net.minecraftforge.fml.ModList.get().isLoaded("curios")&&!net.minecraftforge.fml.ModList.get().isLoaded("jei")&&!net.minecraftforge.fml.ModList.get().isLoaded("jade"),"real optional dependencies absent");
  JDTEConfig.COMMON.ultimateTimeWandDuration.set(6000);
  if(restart)expected=NbtIo.readCompressed(Path.of("wand-expected.nbt").toFile());force(true);var l=server.overworld();l.getChunkAt(pos);
  if(restart){wand=ItemStack.of(expected.getCompound("wand"));check(wand.save(new CompoundTag()).equals(expected.getCompound("wand")),"exact saved wand capabilities/NBT");return;}
  for(int x=-2;x<3;x++)for(int z=-2;z<3;z++)for(int y=-1;y<4;y++){var p=pos.offset(x,y,z);l.removeBlockEntity(p);l.setBlock(p,y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2|64);}
  l.setBlockAndUpdate(pos,Blocks.FURNACE.defaultBlockState());var f=(net.minecraft.world.level.block.entity.FurnaceBlockEntity)l.getBlockEntity(pos);f.setItem(0,new ItemStack(Items.RAW_IRON,64));f.setItem(1,new ItemStack(Items.COAL));
  cow=new Cow(net.minecraft.world.entity.EntityType.COW,l);cow.setAge(-100000);cow.setPersistenceRequired();cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0);cow.moveTo(Vec3.atCenterOf(pos.south()));l.addFreshEntity(cow);
  wand=new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());wand.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new).receiveEnergy(10000000,false);wand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new).fill(new FluidStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(),1000),IFluidHandler.FluidAction.EXECUTE);UltimateTimeWandItem.setEntityAcceleration(wand,true);
  actor=FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.fromString("52e935ee-f790-49f0-bd8a-e4f4cc9d0a12"),"WandLifecycle"));actor.setPos(Vec3.atCenterOf(pos.south(2)));actor.setItemInHand(InteractionHand.MAIN_HAND,wand);
 }
 @SubscribeEvent public void join(EntityJoinLevelEvent e){if(server!=null&&!restart&&!e.loadedFromDisk()&&e.getEntity() instanceof UltimateTimeWandEntity request&&pos.equals(request.getTarget()))marker=request;if(server==null||!restart||expected==null||!e.loadedFromDisk())return;var ent=e.getEntity();if(ent.getUUID().toString().equals(expected.getString("cowUUID"))){cow=(Cow)ent;check(cow.getAge()==expected.getInt("age")&&EntityAccelerationManager.get(cow).save().equals(expected.getCompound("effect")),"exact entity effect/age at load");cowRestored=true;}if(ent.getUUID().toString().equals(expected.getString("markerUUID"))){marker=(UltimateTimeWandEntity)ent;check(UltimateTimeWandEntity.saveState(marker.state()).equals(expected.getCompound("request")),"exact request at load: actual="+UltimateTimeWandEntity.saveState(marker.state())+" expected="+expected.getCompound("request"));markerRestored=true;}}
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent e)throws java.io.IOException{if(server==null||e.getServer()!=server||e.phase!=TickEvent.Phase.END)return;ticks++;check(ticks-at<2400,"stage timeout "+stage);
  if(stage==0&&!restart&&!activated){if(server.overworld().getEntity(cow.getUUID())!=cow)return;var l=server.overworld();
   check(wand.getItem().useOn(new UseOnContext(actor,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.SOUTH,pos,false))).consumesAction(),"real block item use without optional mods");check(UltimateTimeWandItem.applyToEntityTarget(l,actor,wand,cow),"real entity item use without optional mods");
   check(marker!=null,"actual fresh request join observed");expected=new CompoundTag();expected.put("wand",wand.save(new CompoundTag()));expected.putString("cowUUID",cow.getUUID().toString());expected.putString("markerUUID",marker.getUUID().toString());activated=true;return;
  }
  if(stage==0){if(restart){if(!cowRestored||!markerRestored)return;}if(cow.tickCount<2)return;age=cow.getAge();remaining=EntityAccelerationManager.get(cow).getRemainingTime();at=ticks;stage=1;}
  else if(stage==1&&ticks-at>=12){int natural=remaining-EntityAccelerationManager.get(cow).getRemainingTime();check(cow.getAge()-age==2*natural,"real natural 2x biological acceleration");check(wand.save(new CompoundTag()).equals(expected.getCompound("wand")),"timed work does not charge twice");if(restart){Files.writeString(Path.of("wand-verify-success"),"PASS: real optional-absence fresh production JVM restored exact wand, marker, entity effect and age, then resumed paid natural work\n");server.saveEverything(false,true,true);server.halt(false);stage=99;return;}force(false);at=ticks;stage=2;}
  else if(stage==2&&!server.overworld().getChunkSource().hasChunk(pos.getX()>>4,pos.getZ()>>4)){expected.putInt("age",cow.getAge());expected.put("effect",EntityAccelerationManager.get(cow).save());expected.put("request",UltimateTimeWandEntity.saveState(marker.state()));save();server.saveEverything(false,true,true);Files.writeString(Path.of("wand-seed-success"),"PASS: real optional-absence production server applied block/entity wand, executed natural 2x work without repeat payment and actually unloaded the source chunk\n");server.halt(false);stage=99;}
 }
}
