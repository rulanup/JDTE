package com.jdte.smoke;
import com.jdte.common.blockentities.*;
import com.jdte.common.upgrades.UpgradeHelper;
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
/** Actual packaged Forge lifecycle and saved entity-cooldown verification. */
public final class BioExtractorLifecycleProbe {
 private static final BlockPos[] POS={new BlockPos(4096,110,4112),new BlockPos(4102,110,4112)};
 private static final UUID ACTOR=UUID.fromString("12ef3a01-54f1-4398-ad9b-000000000002");
 private MinecraftServer server;private String mode="";private int stage,ticks,at;private CompoundTag expected;private final UUID[] cows=new UUID[2];
 private final net.minecraftforge.energy.IEnergyStorage[] energy=new net.minecraftforge.energy.IEnergyStorage[2];private final net.minecraftforge.items.IItemHandler[] items=new net.minecraftforge.items.IItemHandler[2];private final IFluidHandler[] fluids=new IFluidHandler[2];
 private static void check(boolean v,String s){if(!v)throw new IllegalStateException("Bio lifecycle: "+s);}
 private BioExtractorBE be(int i){return (BioExtractorBE)server.overworld().getBlockEntity(POS[i]);}
 private Cow cow(int i){return (Cow)server.overworld().getEntity(cows[i]);}
 private void force(boolean v){for(var p:POS)server.overworld().setChunkForced(p.getX()>>4,p.getZ()>>4,v);}
 private void pause(BioExtractorBE b){b.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;b.evaluateRedstone();}
 private void start(BioExtractorBE b){b.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.IGNORED;b.evaluateRedstone();}
 private CompoundTag snapshot(){var all=new CompoundTag();for(int i=0;i<2;i++){all.put("machine"+i,be(i).saveWithoutMetadata());all.putUUID("cow"+i,cows[i]);all.putLong("cooldown"+i,cow(i).getPersistentData().getLong(BioExtractorBE.COOLDOWN_KEY));}return all;}
 @SubscribeEvent public void started(ServerStartedEvent event)throws Exception{
  mode=System.getProperty("jdte.bioLifecycle","");if(mode.isEmpty())return;check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked disposable server required");server=event.getServer();force(true);for(var p:POS)server.overworld().getChunkAt(p);
  if(mode.equals("seed")){var level=server.overworld();for(int i=0;i<2;i++){for(var p:BlockPos.betweenClosed(POS[i].offset(-1,-1,-1),POS[i].offset(1,3,1))){level.removeBlockEntity(p);level.setBlockAndUpdate(p,p.getY()==109?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());}level.setBlockAndUpdate(POS[i],(i==0?JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get():JDTEBlocks.EXTENDED_BIO_EXTRACTOR.get()).defaultBlockState());var b=be(i);b.setPlacedBy(ACTOR);var u=UpgradeHelper.getUpgradeHandler(b);u.insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);u.insertItem(1,new ItemStack(JDTEItems.FILTER_UPGRADE.get()),false);u.insertItem(2,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);b.getFilterData().allowlist=true;b.getFilterHandler().setStackInSlot(17,new ItemStack(Items.COW_SPAWN_EGG));b.getAreaAffectingData().xRadius=1;b.getAreaAffectingData().yRadius=2;b.getAreaAffectingData().zRadius=1;b.getEnergyStorage().setEnergy(b.getMaxEnergy());b.getFluidTank().setFluid(new FluidStack(net.minecraftforge.common.ForgeMod.MILK.get(),123457));b.getMachineHandler().setStackInSlot(b.getOutputSlotCount()-1,new ItemStack(Items.EGG,31));start(b);var c=EntityType.COW.create(level);c.setPos(POS[i].getX()+.5,111.1,POS[i].getZ()+.5);c.setNoAi(true);c.setPersistenceRequired();level.addFreshEntity(c);cows[i]=c.getUUID();}stage=0;}
  else if(mode.equals("verify")){expected=NbtIo.readCompressed(Path.of("bio-expected.nbt").toFile());for(int i=0;i<2;i++)cows[i]=expected.getUUID("cow"+i);stage=4;}
  else throw new IllegalArgumentException(mode);
 }
 private void remember(){for(int i=0;i<2;i++){energy[i]=be(i).getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);items[i]=be(i).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);fluids[i]=be(i).getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);}}
 private void pack(){var level=server.overworld();for(int i=0;i<2;i++){var actor=new net.minecraft.server.level.ServerPlayer(server,level,new com.mojang.authlib.GameProfile(ACTOR,"BioProbe"));actor.setPos(POS[i].getX()+.5,111,POS[i].getZ()+.5);for(int slot=0;slot<36;slot++)actor.getInventory().setItem(slot,new ItemStack(Items.COBBLESTONE,64));var type=i==0?JDTEItems.ADVANCED_BIO_EXTRACTOR.get():JDTEItems.EXTENDED_BIO_EXTRACTOR.get();var old=be(i);var stale=old.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);check(com.jdte.common.items.EclipseAlloyWrenchItem.pickupMachine(level,POS[i],actor),"wrench pack");var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(POS[i]).inflate(3),e->e.getItem().is(type));check(drops.size()==1,"exactly one full-inventory packed drop");var stack=drops.get(0).getItem().copy();drops.get(0).discard();level.setBlockAndUpdate(POS[i],(i==0?JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get():JDTEBlocks.EXTENDED_BIO_EXTRACTOR.get()).defaultBlockState());var state=level.getBlockState(POS[i]);state.getBlock().setPlacedBy(level,POS[i],state,actor,stack);check(expected.getCompound("machine"+i).equals(be(i).saveWithoutMetadata()),"full packed equality for tier "+i);check(stale.extractItem(old.getOutputSlotCount()-1,31,false).isEmpty(),"old output interface inert");}remember();}
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent event)throws Exception{
  if(mode.isEmpty()||event.phase!=TickEvent.Phase.END||event.getServer()!=server)return;check(++ticks<1100,"timeout stage "+stage);var level=server.overworld();
  if(stage==0){for(int i=0;i<2;i++)if(be(i).getFluidTank().getFluidAmount()!=124457)return;for(int i=0;i<2;i++){var b=be(i);check(b.getEnergyStorage().getEnergyStored()==b.getMaxEnergy()-5000&&cow(i).isAlive(),"real natural tick production exactly once before save");pause(b);var data=b.saveWithFullMetadata();data.putInt("tickCounter",17);data.putInt("acceleratedTicks",11);b.load(data);}expected=snapshot();NbtIo.writeCompressed(expected,Path.of("bio-expected.nbt").toFile());stage=1;at=ticks;}
  else if(stage==1&&ticks-at>=100){check(expected.equals(snapshot()),"paused whole state stable");pack();server.saveEverything(false,true,true);force(false);stage=2;}
  else if(stage==2){for(var p:POS)if(level.getChunkSource().hasChunk(p.getX()>>4,p.getZ()>>4))return;for(int i=0;i<2;i++)check(energy[i].extractEnergy(50,false)==0&&items[i].extractItem(i==0?8:17,31,false).isEmpty()&&fluids[i].drain(10,IFluidHandler.FluidAction.EXECUTE).isEmpty(),"all retained unloaded ports inert");force(true);for(var p:POS)level.getChunkAt(p);stage=3;}
  else if(stage==3){if(cow(0)==null||cow(1)==null)return;check(expected.equals(snapshot()),"real unloaded/reloaded machine and entity cooldown equality");finish("bio-seed-success.txt","PASS: both packaged Bio tiers perform real cow extraction, exact FE/milk, full-inventory wrench/placement, real chunk unload/reload, stale ports and complete machine/entity-cooldown state conservation; optional AE2/Curios/JEI/Jade absent");}
  else if(stage==4){if(cow(0)==null||cow(1)==null)return;check(expected.equals(snapshot()),"second JVM exact saved machine and entity cooldown equality");for(int i=0;i<2;i++){check(cow(i).getPersistentData().getLong(BioExtractorBE.COOLDOWN_KEY)>level.getGameTime()+25,"saved cooldown still active");start(be(i));}stage=5;at=ticks;}
  else if(stage==5&&ticks-at>=25){for(int i=0;i<2;i++){check(be(i).getFluidTank().getFluidAmount()==124457&&be(i).getEnergyStorage().getEnergyStored()==be(i).getMaxEnergy()-5000,"restored live cooldown prevents repeat purchase");cow(i).getPersistentData().putLong(BioExtractorBE.COOLDOWN_KEY,level.getGameTime());}stage=6;}
  else if(stage==6){for(int i=0;i<2;i++)if(be(i).getFluidTank().getFluidAmount()!=125457)return;for(int i=0;i<2;i++)check(be(i).getEnergyStorage().getEnergyStored()==be(i).getMaxEnergy()-10000&&be(i).getMachineHandler().getStackInSlot(i==0?8:17).getCount()==31&&cow(i).isAlive(),"after explicit expiry exactly one further extraction, no output/inventory loss");finish("bio-restart-success.txt","PASS: second production JVM restores both tiers, 9/18 slots, FE/fluid/filters/cards/area and scan/accelerated progress plus real entity cooldown; active cooldown prevents duplicates, explicit timestamp expiry permits one further exact-cost natural extraction");}
 }
 private void finish(String file,String message)throws Exception{Files.writeString(Path.of(file),message+"\n");mode="";server.halt(false);}
}
