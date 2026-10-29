package com.jdte.smoke;
import com.jdte.common.blockentities.*;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.*;
import java.util.*;
/** Real reobfuscated production JVMs; no client classes or simulated entity ticks. */
public final class EntityAcceleratorLifecycleProbe {
 private static final BlockPos[] POS={new BlockPos(6000,110,6016),new BlockPos(6012,110,6016)};
 private MinecraftServer server;private String mode="";private int stage,ticks,at;private CompoundTag expected;private final UUID[] cows=new UUID[2];
 private final net.minecraftforge.energy.IEnergyStorage[] energy=new net.minecraftforge.energy.IEnergyStorage[2];private final IFluidHandler[] fluid=new IFluidHandler[2];
 private static void check(boolean ok,String m){if(!ok)throw new IllegalStateException("Entity accelerator lifecycle: "+m);}
 private AdvancedEntityAcceleratorBE be(int i){return (AdvancedEntityAcceleratorBE)server.overworld().getBlockEntity(POS[i]);}
 private void force(boolean value){for(var p:POS)server.overworld().setChunkForced(p.getX()>>4,p.getZ()>>4,value);}
 private void pause(int i){be(i).getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;be(i).evaluateRedstone();}
 private void resume(int i){be(i).getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.IGNORED;be(i).evaluateRedstone();}
 private CompoundTag snapshot(){var t=new CompoundTag();for(int i=0;i<2;i++){t.put("machine"+i,be(i).saveWithoutMetadata());t.putUUID("cow"+i,cows[i]);}return t;}
 @SubscribeEvent public void started(ServerStartedEvent e)throws Exception{mode=System.getProperty("jdte.entityLifecycle","");if(mode.isEmpty())return;check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked fixture");server=e.getServer();force(true);for(var p:POS)server.overworld().getChunkAt(p);if(mode.equals("verify")){expected=NbtIo.readCompressed(Path.of("entity-expected.nbt").toFile());for(int i=0;i<2;i++)cows[i]=expected.getUUID("cow"+i);stage=4;return;}var l=server.overworld();for(int i=0;i<2;i++){for(var pos:BlockPos.betweenClosed(POS[i].offset(-1,-1,-1),POS[i].offset(2,3,1))){l.removeBlockEntity(pos);l.setBlockAndUpdate(pos,pos.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}l.setBlockAndUpdate(POS[i],(i==0?JDTEBlocks.ADVANCED_ENTITY_ACCELERATOR.get():JDTEBlocks.EXTENDED_ENTITY_ACCELERATOR.get()).defaultBlockState());var b=be(i);UpgradeHelper.getUpgradeHandler(b).insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);UpgradeHelper.getUpgradeHandler(b).insertItem(i==0?3:7,new ItemStack(JDTEItems.FILTER_UPGRADE.get()),false);b.getFilterHandler().setStackInSlot(0,new ItemStack(Items.COW_SPAWN_EGG));b.getFilterData().allowlist=true;b.setMultiplier(i==0?17:19);b.getAreaAffectingData().xRadius=2;b.getAreaAffectingData().yRadius=2;b.getAreaAffectingData().zRadius=2;b.getEnergyStorage().setEnergy(b.getMaxEnergy());b.getFluidTank().setFluid(new FluidStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(),1234));var cow=new Cow(EntityType.COW,l);cow.setAge(-100000);cow.setNoAi(true);cow.setPersistenceRequired();cow.moveTo(POS[i].getX()+.5,111,POS[i].getZ()+.5);l.addFreshEntity(cow);cows[i]=cow.getUUID();}}
 private void pack()throws Exception{var l=server.overworld();var actor=net.minecraftforge.common.util.FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.fromString("12ef3a01-54f1-4398-ad9b-000000000003"),"EntityPack"));for(int i=0;i<2;i++){actor.setPos(POS[i].getX()+.5,111,POS[i].getZ()+.5);for(int s=0;s<36;s++)actor.getInventory().setItem(s,new ItemStack(Items.COBBLESTONE,64));var item=i==0?JDTEItems.ADVANCED_ENTITY_ACCELERATOR.get():JDTEItems.EXTENDED_ENTITY_ACCELERATOR.get();check(com.jdte.common.items.EclipseAlloyWrenchItem.pickupMachine(l,POS[i],actor),"actual full-inventory wrench");var drops=l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(POS[i]).inflate(3),d->d.getItem().is(item));check(drops.size()==1,"one packed machine");var packed=drops.get(0).getItem().copy();drops.get(0).discard();l.setBlockAndUpdate(POS[i],(i==0?JDTEBlocks.ADVANCED_ENTITY_ACCELERATOR.get():JDTEBlocks.EXTENDED_ENTITY_ACCELERATOR.get()).defaultBlockState());var state=l.getBlockState(POS[i]);state.getBlock().setPlacedBy(l,POS[i],state,actor,packed);check(expected.getCompound("machine"+i).equals(be(i).saveWithoutMetadata()),"packing preserves exact state");energy[i]=be(i).getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);fluid[i]=be(i).getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);}}
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent e)throws Exception{if(mode.isEmpty()||e.phase!=TickEvent.Phase.END||e.getServer()!=server)return;check(++ticks<1000,"timeout "+stage);var l=server.overworld();if(stage==0&&ticks>=30){for(int i=0;i<2;i++){var b=be(i);int work=b.getMultiplier()-1;int cost=TimeAcceleratorCostMath.energyCost(work,com.direwolf20.justdirethings.setup.Config.TIME_WAND_FE_COST.get());int spent=b.getMaxEnergy()-b.getEnergyStorage().getEnergyStored();check(spent>0&&spent%cost==0,"natural machine ticks pay exact requested batch costs");var cow=(Cow)l.getEntity(cows[i]);check(cow!=null&&cow.getAge()<-90000&&cow.getAge()>-100000,"actual creature timer advances");check(b.getFluidTank().getFluidAmount()<=1234&&b.getPendingFluidCost()>=0&&b.getPendingFluidCost()<1,"bounded fraction");pause(i);}expected=snapshot();NbtIo.writeCompressed(expected,Path.of("entity-expected.nbt").toFile());pack();server.saveEverything(false,true,true);force(false);stage=1;}
 else if(stage==1){for(var p:POS)if(l.getChunkSource().hasChunk(p.getX()>>4,p.getZ()>>4))return;for(int i=0;i<2;i++)check(energy[i].receiveEnergy(1,false)==0&&energy[i].extractEnergy(1,false)==0&&fluid[i].drain(1,IFluidHandler.FluidAction.EXECUTE).isEmpty(),"retained unloaded ports inert");force(true);for(var p:POS)l.getChunkAt(p);stage=2;}
 else if(stage==2){for(var id:cows)if(l.getEntity(id)==null)return;check(expected.equals(snapshot()),"real unload/reload exact machine/cow identity");finish("entity-seed-success.txt","PASS: packaged tiers naturally accelerate real cows with exact FE and fractional Time Fluid, preserve full-inventory wrench placement and actual unload/reload, retained ports inert; optional AE2/Curios/JEI/Jade absent");}
 else if(stage==4){for(var id:cows)if(l.getEntity(id)==null)return;check(expected.equals(snapshot()),"second JVM exact buffers/filter/cards/multiplier/fraction");for(int i=0;i<2;i++)resume(i);stage=5;at=ticks;}
 else if(stage==5&&ticks-at>=10){for(int i=0;i<2;i++)check(be(i).getEnergyStorage().getEnergyStored()<expected.getCompound("machine"+i).getInt("energy"),"restored accelerator resumes real paid work");check(!com.jdte.common.manager.EntityAccelerationManager.isSuppressingAi(),"no leaked sub-tick scope");finish("entity-restart-success.txt","PASS: second production JVM restores both entity accelerators and live targets, all buffers/cards/filter/multiplier and fractional debt, then resumes natural paid work without stale scope");}}
 private void finish(String file,String message)throws Exception{Files.writeString(Path.of(file),message+"\n");mode="";server.halt(false);}
}
