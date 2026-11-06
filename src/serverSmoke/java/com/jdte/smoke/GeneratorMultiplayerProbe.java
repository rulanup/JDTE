package com.jdte.smoke;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import java.nio.file.*;
import java.util.*;

/** Both players interact through real remote menu/item-use packets. */
public final class GeneratorMultiplayerProbe {
    public static final BlockPos A=new BlockPos(1023,80,1024),B=A.east(2),SOURCE=A.south(4);
    private final Path control=Path.of(System.getProperty("jdte.remoteControl","/tmp/jdte-generator-remote-control"));
    private int drawn;
    private MinecraftServer server;private int stage,ticks,at;private boolean restart;private List<ServerPlayer> originals=List.of();private CompoundTag expected;
    private void check(boolean v,String s){if(!v)throw new IllegalStateException("Generator remote: "+s);}
    private void write(String n,String v){try{Files.writeString(control.resolve(n+".tmp"),v);Files.move(control.resolve(n+".tmp"),control.resolve(n),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(java.io.IOException e){throw new IllegalStateException(e);}}
    private void phase(int n){stage=n;at=ticks;write("phase",Integer.toString(n));}
    private boolean marked(String n){return Files.isRegularFile(control.resolve(n));}
    private boolean both(int n){return marked("One-"+n)&&marked("Two-"+n);}
    private List<ServerPlayer> players(){return server.getPlayerList().getPlayers().stream().filter(p->p.getGameProfile().getName().startsWith("JDTE")).sorted(Comparator.comparing(p->p.getGameProfile().getName())).toList();}
    private com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE machine(BlockPos p){return (com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE)server.overworld().getBlockEntity(p);}
    private void force(boolean v){var level=server.overworld();if(!v)for(long packed:level.getForcedChunks().toLongArray()){var c=new net.minecraft.world.level.ChunkPos(packed);if(Math.abs(c.x-(A.getX()>>4))<=4&&Math.abs(c.z-(A.getZ()>>4))<=4)level.setChunkForced(c.x,c.z,false);}for(var p:List.of(A,B,SOURCE))level.setChunkForced(p.getX()>>4,p.getZ()>>4,v);}
    @SubscribeEvent public void started(ServerStartedEvent e){
        String mode=System.getProperty("jdte.generatorMultiplayer","");if(mode.isEmpty())return;server=e.getServer();check(Files.isRegularFile(control.resolve(".fixture"))&&Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked disposable fixtures required");restart=mode.equals("restart");drawn=restart?37:0;force(true);for(var p:List.of(A,B,SOURCE))server.overworld().getChunkAt(p);
        if(restart){try{expected=NbtIo.readCompressed(control.resolve("expected.nbt").toFile());}catch(java.io.IOException x){throw new IllegalStateException(x);}compare();phase(9);return;}
        var level=server.overworld();for(int x=-3;x<=5;x++)for(int z=-2;z<=6;z++){level.setBlockAndUpdate(A.offset(x,-1,z),Blocks.STONE.defaultBlockState());for(int y=0;y<=3;y++){var p=A.offset(x,y,z);level.removeBlockEntity(p);level.setBlock(p,Blocks.AIR.defaultBlockState(),2|64);}}
        for(var p:List.of(A,B,SOURCE)) {
            level.setBlockAndUpdate(p,(p.equals(B)?JDTEBlocks.EXTENDED_FLUID_GENERATOR.get():JDTEBlocks.EXTENDED_GENERATOR.get()).defaultBlockState());var be=machine(p);
            UpgradeHelper.getUpgradeHandler(be).insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);
            ((com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE)be).getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;
            var powered=(com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE)be;powered.getEnergyStorage().setEnergy(powered.getMaxEnergy());
            if(p.equals(A))be.getMachineHandler().setStackInSlot(0,new ItemStack(net.minecraft.world.item.Items.COAL,64));
            if(p.equals(B)){var liquid=(com.jdte.common.blockentities.ExtendedFluidGeneratorBE)be;liquid.getFluidTank().setFluid(new FluidStack(fuel(),liquid.getMaxMB()));be.getMachineHandler().setStackInSlot(0,new ItemStack(fuel().getBucket()));}
            be.setChanged();
        }
        UpgradeHelper.getUpgradeHandler(machine(SOURCE)).insertItem(2,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()),false);
        for(var drop:level.getEntitiesOfClass(ItemEntity.class,new AABB(A).inflate(12)))drop.discard();phase(0);
    }
    private List<ServerPlayer> owners(){var map=new LinkedHashMap<UUID,ServerPlayer>();for(var p:originals)map.put(p.getUUID(),p);for(var p:players())map.put(p.getUUID(),p);return List.copyOf(map.values());}
    private int ledger(boolean mixing){var item=mixing?JDTEItems.GENERATOR_UPGRADE.get():net.minecraft.world.item.Items.COAL;int count=0;for(var p:owners()){
        count+=p.getInventory().countItem(item);if(p.containerMenu.getCarried().is(item))count+=p.containerMenu.getCarried().getCount();
        if(mixing)for(var bag:com.jdte.common.items.AdvancedUpgradeStorageItem.findAllStorages(p)){var h=new AdvancedUpgradeStorageHandler(bag);for(int i=0;i<h.getSlots();i++)if(h.getStackInSlot(i).is(item))count+=h.getStackInSlot(i).getCount();}
    }
        for(var pos:List.of(A,B,SOURCE)){var be=machine(pos);if(mixing){var u=UpgradeHelper.getUpgradeHandler(be);for(int i=0;i<u.getSlots();i++)if(u.getStackInSlot(i).is(item))count+=u.getStackInSlot(i).getCount();}else if(be.getMachineHandler().getStackInSlot(0).is(item))count+=be.getMachineHandler().getStackInSlot(0).getCount();}
        for(var drop:server.overworld().getEntitiesOfClass(ItemEntity.class,new AABB(A).inflate(12)))if(drop.getItem().is(item))count+=drop.getItem().getCount();return count;
    }
    private net.minecraft.world.level.material.Fluid fuel(){return BuiltInRegistries.FLUID.stream().filter(f->f instanceof com.direwolf20.justdirethings.common.fluids.basefluids.RefinedFuel r&&r.fePerMb()>0).findFirst().orElseThrow();}
    private int bucketLedger(){int count=0;var item=fuel().getBucket();for(var p:owners()){count+=p.getInventory().countItem(item);if(p.containerMenu.getCarried().is(item))count+=p.containerMenu.getCarried().getCount();}for(var p:List.of(A,B,SOURCE)){var stack=machine(p).getMachineHandler().getStackInSlot(0);if(stack.is(item))count+=stack.getCount();}for(var drop:server.overworld().getEntitiesOfClass(ItemEntity.class,new AABB(A).inflate(12)))if(drop.getItem().is(item))count+=drop.getItem().getCount();return count;}
    private void conserved(){check(ledger(false)==64,"coal ledger="+ledger(false));check(bucketLedger()==1,"fuel bucket ledger="+bucketLedger());check(ledger(true)==5,"generator-card ledger="+ledger(true));for(var pos:List.of(A,B,SOURCE)){var be=machine(pos);var powered=(com.direwolf20.justdirethings.common.blockentities.basebe.PoweredMachineBE)be;check(powered.getEnergyStorage().getEnergyStored()==powered.getMaxEnergy()-(pos.equals(SOURCE)?0:drawn),"owned FE conserved "+pos);}var liquid=(com.jdte.common.blockentities.ExtendedFluidGeneratorBE)machine(B);check(liquid.getFluidTank().getFluidAmount()==liquid.getMaxMB()-(drawn==0?0:7),"fluid plus seven-mB external automation holding conserved");}
    private void automation(){for(var pos:List.of(A,B)){var be=machine(pos);var port=be.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY,Direction.UP).orElseThrow(IllegalStateException::new);check(port.receiveEnergy(100,false)==0&&port.extractEnergy(37,false)==37,"real output-only external FE extraction");var items=be.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER,Direction.DOWN).orElseThrow(IllegalStateException::new);var taken=items.extractItem(0,1,false);check(!taken.isEmpty()&&items.insertItem(0,taken,false).isEmpty(),"real automation item round trip while menus open");}var port=machine(B).getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER,Direction.NORTH).orElseThrow(IllegalStateException::new);check(port.fill(new FluidStack(Fluids.WATER,1000),net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE)==0&&port.drain(7,net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE).getAmount()==7,"actual fluid drain and invalid-input rejection");drawn=37;}
    private CompoundTag fields(BlockPos p){var tag=machine(p).saveWithFullMetadata();for(String key:List.of("id","x","y","z","placedBy"))tag.remove(key);return tag;}
    private CompoundTag snapshot(){var tag=new CompoundTag();for(var entry:Map.of("advanced",A,"extended",B,"source",SOURCE).entrySet())tag.put(entry.getKey(),fields(entry.getValue()));for(var p:players())tag.put(p.getGameProfile().getName(),p.getInventory().save(new ListTag()));return tag;}
    private void compare(){for(var entry:Map.of("advanced",A,"extended",B,"source",SOURCE).entrySet())check(expected.getCompound(entry.getKey()).equals(fields(entry.getValue())),"restart machine "+entry.getKey());}
    private void openOne(ServerPlayer player,BlockPos pos){net.minecraftforge.network.NetworkHooks.openScreen(player,new net.minecraft.world.SimpleMenuProvider((id,inv,p)->pos.equals(B)?new com.jdte.common.containers.ExtendedFluidGeneratorContainer(id,inv,pos):new com.jdte.common.containers.ExtendedGeneratorContainer(id,inv,pos),net.minecraft.network.chat.Component.literal("Generator concurrency fixture")),buf->buf.writeBlockPos(pos));}
    private void open(BlockPos pos){for(var p:players())openOne(p,pos);}
    private void equip(ServerPlayer p,ItemStack item){p.setItemInHand(InteractionHand.MAIN_HAND,item);p.inventoryMenu.broadcastChanges();}
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){if(server==null||e.phase!=TickEvent.Phase.END||e.getServer()!=server)return;try{run();}catch(Exception x){write("server-failure",x.toString());throw new IllegalStateException(x);}}
    private void run()throws java.io.IOException{
        ticks++;check(ticks-at<3600,"timeout stage="+stage);var people=players();var level=server.overworld();
        if(stage==0&&people.size()==2){originals=people;for(var p:people){p.getInventory().clearContent();p.teleportTo(A.getX()+0.5,A.getY(),A.getZ()+2.5);p.getInventory().setItem(9,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()));var bag=new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());new AdvancedUpgradeStorageHandler(bag).setStackInSlot(0,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()));p.getInventory().setItem(35,bag);p.inventoryMenu.broadcastChanges();}phase(-2);}
        else if(stage==-2&&both(-2)){for(var drop:level.getEntitiesOfClass(ItemEntity.class,new AABB(A).inflate(12)))drop.discard();conserved();open(A);phase(1);}
        else if(stage==1&&both(1)){conserved();check(machine(A).getMachineHandler().getStackInSlot(0).isEmpty(),"one winner extracted shared catalyst stack");phase(2);}
        else if(stage==2&&both(2)){conserved();check(machine(A).getMachineHandler().getStackInSlot(0).getCount()==64,"shared catalyst returned once");phase(3);}
        else if(stage==3&&both(3)){conserved();check(UpgradeHelper.hasGeneratorUpgrade(machine(A)),"concurrent Shift accepted exactly one generator card");automation();conserved();for(var p:people){p.closeContainer();var copier=new ItemStack(JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get());com.direwolf20.justdirethings.common.items.MachineSettingsCopier.setSettings(copier,true,true,true,true);JDTEItems.ADVANCED_MACHINE_SETTINGS_COPIER.get().saveSettings(machine(SOURCE),copier);equip(p,copier);}phase(4);}
        else if(stage==4&&both(4)){conserved();check(UpgradeHelper.hasGeneratorUpgrade(machine(A)),"same preset reused installed mixing card");open(B);phase(5);}
        else if(stage==5&&both(5)){conserved();check(machine(B).getMachineHandler().getStackInSlot(0).getCount()==1&&UpgradeHelper.hasGeneratorUpgrade(machine(B)),"extended concurrent catalyst and bag panel round trip");phase(6);}
        else if(stage==6&&marked("One-6")&&people.size()==1){conserved();check(originals.get(0).inventoryMenu.getCarried().isEmpty(),"logout settles real carried catalyst");phase(7);}
        else if(stage==7&&people.size()==2&&both(7)){
            conserved();var drops=level.getEntitiesOfClass(ItemEntity.class,new AABB(A).inflate(12),drop->drop.getItem().is(fuel().getBucket()));
            if(!drops.isEmpty()){var drop=drops.get(0);people.get(0).teleportTo(drop.getX(),drop.getY(),drop.getZ());return;}
            for(var p:people)p.closeContainer();expected=snapshot();NbtIo.writeCompressed(expected,control.resolve("expected.nbt").toFile());phase(8);for(var p:people)p.connection.disconnect(net.minecraft.network.chat.Component.literal("Generator restart fixture"));force(false);
        }else if(stage==8&&people.isEmpty()){
            for(var p:List.of(A,B,SOURCE))if(level.getChunkSource().hasChunk(p.getX()>>4,p.getZ()>>4))return;server.saveEverything(false,true,true);write("seed-success","PASS: two independent clients raced shared coal and fuel buckets and Shift upgrades, pasted configs, repeated Extended generator transfers, disconnected with cursor, reconnected, conserved 64 coal/one real fuel bucket/five generator cards, owned FE plus 74 external FE and seven external mB, then observed chunk unload and saved shutdown\n");server.halt(false);stage=99;
        }else if(stage==9&&people.size()==2&&both(9)){originals=people;compare();for(var p:people)check(expected.getList(p.getGameProfile().getName(),Tag.TAG_COMPOUND).equals(p.getInventory().save(new ListTag())),"restored exact player inventory "+p.getGameProfile().getName());for(int i=0;i<people.size();i++)openOne(people.get(i),i==0?A:B);phase(10);}
        else if(stage==10&&both(10)){conserved();compare();write("restart-success","PASS: fresh dedicated server restored both generator tiers and exact player inventories; both independent clients reopened synchronized fuel/FE menus with correct upgrade slots and empty cursors\n");phase(11);for(var p:people)p.connection.disconnect(net.minecraft.network.chat.Component.literal("Generator concurrency passed"));server.saveEverything(false,true,true);server.halt(false);stage=99;}
    }
}
