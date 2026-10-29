package com.jdte.smoke;

import com.jdte.common.blockentities.*;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.*;
import java.util.*;

/** Live natural production, shared targets and real remote packets; no redstone pause. */
public final class BioMultiplayerProbe {
 public static final BlockPos A=new BlockPos(1023,80,1024),B=A.east(2);
 private final Path control=Path.of(System.getProperty("jdte.remoteControl","/tmp/jdte-bio-remote-control"));
 private MinecraftServer server;private int stage,ticks,at,cows,sheep,extractedMilk;private boolean restart;
 private final Map<UUID,Long> observed=new HashMap<>();private List<ServerPlayer> originals=List.of();private CompoundTag expected;
 private void check(boolean ok,String message){if(!ok)throw new IllegalStateException("Bio multiplayer: "+message);}
 private void write(String name,String value){try{Files.writeString(control.resolve(name+".tmp"),value);Files.move(control.resolve(name+".tmp"),control.resolve(name),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(java.io.IOException x){throw new IllegalStateException(x);}}
 private void phase(int n){stage=n;at=ticks;write("phase",Integer.toString(n));}
 private boolean both(int n){return Files.exists(control.resolve("One-"+n))&&Files.exists(control.resolve("Two-"+n));}
 private List<ServerPlayer> players(){return server.getPlayerList().getPlayers().stream().filter(p->p.getGameProfile().getName().startsWith("JDTE")).sorted(Comparator.comparing(p->p.getGameProfile().getName())).toList();}
 private BioExtractorBE machine(BlockPos pos){return (BioExtractorBE)server.overworld().getBlockEntity(pos);}
 private void force(boolean v){for(long packed:server.overworld().getForcedChunks().toLongArray()){var c=new net.minecraft.world.level.ChunkPos(packed);if(Math.abs(c.x-(A.getX()>>4))<=4&&Math.abs(c.z-(A.getZ()>>4))<=4)server.overworld().setChunkForced(c.x,c.z,false);}for(var pos:List.of(A,B))server.overworld().setChunkForced(pos.getX()>>4,pos.getZ()>>4,v);}
 @SubscribeEvent public void start(ServerStartedEvent e){String mode=System.getProperty("jdte.bioMultiplayer","");if(mode.isEmpty())return;server=e.getServer();check(Files.isRegularFile(control.resolve(".fixture"))&&Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked fixtures");restart=mode.equals("restart");force(true);for(var pos:List.of(A,B))server.overworld().getChunkAt(pos);
  if(restart){try{expected=NbtIo.readCompressed(control.resolve("expected.nbt").toFile());}catch(java.io.IOException x){throw new IllegalStateException(x);}phase(9);return;}
  var l=server.overworld();l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_MAX_ENTITY_CRAMMING).set(0,server);for(var ent:l.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,new AABB(A).inflate(12)))if(!(ent instanceof ServerPlayer))ent.discard();
  for(int x=-3;x<11;x++)for(int z=-3;z<7;z++)for(int y=-1;y<4;y++){var pos=A.offset(x,y,z);l.removeBlockEntity(pos);l.setBlock(pos,y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2|64);}
  l.setBlockAndUpdate(A,JDTEBlocks.ADVANCED_BIO_EXTRACTOR.get().defaultBlockState());l.setBlockAndUpdate(B,JDTEBlocks.EXTENDED_BIO_EXTRACTOR.get().defaultBlockState());
  for(var pos:List.of(A,B)){var be=machine(pos);UpgradeHelper.getUpgradeHandler(be).insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);be.getEnergyStorage().setEnergy(be.getMaxEnergy());be.getAreaAffectingData().xRadius=3;be.getAreaAffectingData().yRadius=3;be.getAreaAffectingData().zRadius=3;l.setBlockAndUpdate(pos.south(),Blocks.CHEST.defaultBlockState());}
  phase(0);
 }
 private void spawn(){var l=server.overworld();var cow=new Cow(net.minecraft.world.entity.EntityType.COW,l);cow.setNoAi(true);cow.setPersistenceRequired();cow.moveTo(A.getX()+1.5,A.getY(),A.getZ()+2.5);l.addFreshEntity(cow);var wool=new Sheep(net.minecraft.world.entity.EntityType.SHEEP,l);wool.setNoAi(true);wool.setPersistenceRequired();wool.setColor(DyeColor.BLUE);wool.moveTo(A.getX()+1.5,A.getY(),A.getZ()+2.5);l.addFreshEntity(wool);}
 private List<net.minecraft.world.entity.LivingEntity> animals(){return server.overworld().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new AABB(A).inflate(10),e->e instanceof Cow||e instanceof Sheep);}
 private void count(){for(var e:animals()){long cd=e.getPersistentData().getLong("jdte:bio_cooldown");if(cd>observed.getOrDefault(e.getUUID(),0L)){observed.put(e.getUUID(),cd);if(e instanceof Cow)cows++;else sheep++;}}}
 private int items(Item item){int n=0;var owners=new LinkedHashMap<UUID,ServerPlayer>();for(var p:originals)owners.put(p.getUUID(),p);for(var p:players())owners.put(p.getUUID(),p);for(var p:owners.values()){n+=p.getInventory().countItem(item);if(p.containerMenu.getCarried().is(item))n+=p.containerMenu.getCarried().getCount();}for(var pos:List.of(A,B)){var be=machine(pos);var h=be.getMachineHandler();for(int i=0;i<h.getSlots();i++)if(h.getStackInSlot(i).is(item))n+=h.getStackInSlot(i).getCount();var u=UpgradeHelper.getUpgradeHandler(be);for(int i=0;i<u.getSlots();i++)if(u.getStackInSlot(i).is(item))n+=u.getStackInSlot(i).getCount();var chest=(ChestBlockEntity)server.overworld().getBlockEntity(pos.south());for(int i=0;i<chest.getContainerSize();i++)if(chest.getItem(i).is(item))n+=chest.getItem(i).getCount();}for(var drop:server.overworld().getEntitiesOfClass(ItemEntity.class,new AABB(A).inflate(12)))if(drop.getItem().is(item))n+=drop.getItem().getCount();return n;}
 private void conserved(){long fe=0,milk=extractedMilk;for(var pos:List.of(A,B)){fe+=machine(pos).getEnergyStorage().getEnergyStored();milk+=machine(pos).getFluidTank().getFluidAmount();}check(fe==15000000L-5000L*cows-3000L*sheep,"FE ledger "+fe+" cows="+cows+" sheep="+sheep);check(milk==1000L*cows,"milk ledger "+milk);check(items(Items.BLUE_WOOL)==2*sheep,"wool ledger");check(items(JDTEItems.FILTER_UPGRADE.get())==4,"card conservation");}
 private void open(BlockPos pos){for(var p:players())net.minecraftforge.network.NetworkHooks.openScreen(p,new net.minecraft.world.SimpleMenuProvider((id,inv,player)->pos.equals(A)?new com.jdte.common.containers.AdvancedBioExtractorContainer(id,inv,pos):new com.jdte.common.containers.ExtendedBioExtractorContainer(id,inv,pos),net.minecraft.network.chat.Component.literal("Live Bio multiplayer")),buf->buf.writeBlockPos(pos));}
 private CompoundTag fields(BlockPos pos){var t=machine(pos).saveWithFullMetadata();t.remove("scanProgress");t.remove("tickCounter");return t;}
 private CompoundTag snapshot(){var t=new CompoundTag();t.put("advanced",fields(A));t.put("extended",fields(B));for(var p:players())t.put(p.getGameProfile().getName(),p.getInventory().save(new ListTag()));var entities=new CompoundTag();for(var e:animals())entities.putLong(e.getUUID().toString(),e.getPersistentData().getLong("jdte:bio_cooldown"));t.put("cooldowns",entities);return t;}
 private net.minecraft.world.entity.Entity levelEntity(String id){return server.overworld().getEntity(UUID.fromString(id));}
 private void compare(){check(expected.getCompound("advanced").equals(fields(A)),"Advanced exact saved fields");check(expected.getCompound("extended").equals(fields(B)),"Extended exact saved fields");for(String id:expected.getCompound("cooldowns").getAllKeys()){var e=server.overworld().getEntity(UUID.fromString(id));check(e!=null&&e.getPersistentData().getLong("jdte:bio_cooldown")==expected.getCompound("cooldowns").getLong(id),"restored shared cooldown "+id+" entity="+e+" actual="+(e==null?-1:e.getPersistentData().getLong("jdte:bio_cooldown"))+" expected="+expected.getCompound("cooldowns").getLong(id));}}
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){if(server==null||e.phase!=TickEvent.Phase.END||e.getServer()!=server)return;try{run();}catch(Exception x){write("server-failure",x.toString());throw new IllegalStateException(x);}}
 private void run()throws java.io.IOException{ticks++;check(ticks-at<3600,"timeout stage "+stage);var people=players();if(!restart&&stage>=1&&stage<=7){if(ticks%30==0)spawn();count();conserved();}
  if(stage==0&&people.size()==2){originals=people;for(var p:people){p.getInventory().clearContent();p.teleportTo(A.getX()+0.5,A.getY(),A.getZ()+1.5);p.getInventory().setItem(9,new ItemStack(JDTEItems.FILTER_UPGRADE.get(),2));p.inventoryMenu.broadcastChanges();}phase(-2);}
  else if(stage==-2&&both(-2)){open(A);spawn();phase(1);}
  else if(stage==1&&both(1)){check(cows>0&&sheep>0,"real natural production during shared output clicks");phase(2);}
  else if(stage==2&&both(2)){check(UpgradeHelper.countUpgrades(machine(A),UpgradeType.FILTER)==2,"two source-limit filter cards installed concurrently");for(var pos:List.of(A,B))com.jdte.common.autoioconfig.AutoIoConfigHelper.setMasks(machine(pos),0,1<<Direction.SOUTH.ordinal());open(B);phase(3);}
  else if(stage==3&&both(3)){check(cows>=3,"production continues with actual automatic output");phase(4);}
  else if(stage==4&&both(4)){check(UpgradeHelper.countUpgrades(machine(B),UpgradeType.FILTER)==2,"extended concurrent cards");phase(5);}
  else if(stage==5&&both(5)){check(UpgradeHelper.countUpgrades(machine(B),UpgradeType.FILTER)==0,"concurrent upgrade removal");var fluid=machine(A).getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER,Direction.NORTH).orElseThrow(IllegalStateException::new);int drained=fluid.drain(13,net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE).getAmount();check(drained==13,"real external milk extraction");extractedMilk+=drained;conserved();phase(6);}
  else if(stage==6&&Files.exists(control.resolve("One-6"))&&people.size()==1){phase(7);}
  else if(stage==7&&people.size()==2&&both(7)){count();conserved();check(cows>=5&&sheep>=5,"sustained natural shared-target production");for(var p:people)p.closeContainer();for(var ent:animals())ent.moveTo(A.getX()+8.5,A.getY(),A.getZ()+1.5);write("sync-advanced",machine(A).getEnergyStorage().getEnergyStored()+" "+machine(A).getFluidTank().getFluidAmount());expected=snapshot();NbtIo.writeCompressed(expected,control.resolve("expected.nbt").toFile());write("ledger","cow operations="+cows+" sheep operations="+sheep+" external milk="+extractedMilk+"; FE=15000000-5000*cows-3000*sheep; wool=2*sheep; no redstone pause during competition\n");phase(8);for(var p:people)p.connection.disconnect(net.minecraft.network.chat.Component.literal("Bio restart fixture"));force(false);}
  else if(stage==8&&people.isEmpty()){for(var pos:List.of(A,B))if(server.overworld().getChunkSource().hasChunk(pos.getX()>>4,pos.getZ()>>4))return;server.saveEverything(false,true,true);write("seed-success","PASS: two independent clients operated each shared live Bio machine, raced natural output, repeated upgrade install/remove, automatic chest output, real milk extraction, shared cooldown resource ledger, disconnect/reconnect and real unload; no redstone pause\n");server.halt(false);stage=99;}
  else if(stage==9&&people.size()==2&&both(9)){for(String id:expected.getCompound("cooldowns").getAllKeys())if(levelEntity(id)==null)return;for(var p:people)check(expected.getList(p.getGameProfile().getName(),Tag.TAG_COMPOUND).equals(p.getInventory().save(new ListTag())),"restored player inventory");compare();open(A);phase(10);}
  else if(stage==10&&both(10)){compare();write("restart-success","PASS: fresh dedicated JVM restored both Bio inventories/FE/milk/cards/config and shared entity cooldown; both original clients rejoined synchronized Advanced menu\n");phase(11);for(var p:people)p.connection.disconnect(net.minecraft.network.chat.Component.literal("Bio multiplayer passed"));server.saveEverything(false,true,true);server.halt(false);stage=99;}
 }
}
