package com.jdte.smoke;

import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.fluids.basefluids.RefinedFuel;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.common.blockentities.*;
import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.nio.file.*;
import java.util.*;

/** Opt-in production-JAR verification, never packaged in the main mod. */
public final class GeneratorLifecycleProbe {
    private static final BlockPos[] POS={new BlockPos(3104,100,3104),new BlockPos(3110,100,3104)};
    private String mode="";private MinecraftServer server;private CompoundTag expected;private int stage,ticks;
    private final net.minecraftforge.energy.IEnergyStorage[] retained=new net.minecraftforge.energy.IEnergyStorage[2];
    private final net.minecraftforge.items.IItemHandler[] items=new net.minecraftforge.items.IItemHandler[2];
    private IFluidHandler fluid;
    private static void check(boolean value,String message) { if(!value)throw new IllegalStateException("Generator lifecycle: "+message); }
    private BaseMachineBE be(int index) { return (BaseMachineBE)server.overworld().getBlockEntity(POS[index]); }
    private CompoundTag snapshot() { var tag=new CompoundTag();for(int i=0;i<2;i++)tag.put("machine"+i,be(i).saveWithoutMetadata());return tag; }
    private void force(boolean value) {for(var pos:POS)server.overworld().setChunkForced(pos.getX()>>4,pos.getZ()>>4,value);}
    private void remember() {for(int i=0;i<2;i++){retained[i]=be(i).getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);items[i]=be(i).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);}fluid=be(1).getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);}
    @SubscribeEvent public void started(ServerStartedEvent event)throws Exception {
        mode=System.getProperty("jdte.generatorLifecycle","");if(mode.isEmpty())return;
        check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked disposable fixture required");server=event.getServer();var level=server.overworld();force(true);for(var pos:POS)level.getChunkAt(pos);
        if(mode.equals("seed")) {
            for(int i=0;i<2;i++) {
                for(var pos:BlockPos.betweenClosed(POS[i].offset(-1,-1,-1),POS[i].offset(1,1,1))) {level.removeBlockEntity(pos);level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());}
                level.setBlockAndUpdate(POS[i],(i==0?JDTEBlocks.EXTENDED_GENERATOR.get():JDTEBlocks.EXTENDED_FLUID_GENERATOR.get()).defaultBlockState());
                var be=be(i);be.setPlacedBy(UUID.fromString("12ef3a01-54f1-4398-ad9b-000000000001"));var cards=UpgradeHelper.getUpgradeHandler(be);
                check(cards.insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false).isEmpty(),"ultimate capacity accepted");
                cards.insertItem(7,new ItemStack(JDTEItems.GENERATOR_UPGRADE.get()),false);
                ((RedstoneControlledBE)be).getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.HIGH;
                ((RedstoneControlledBE)be).evaluateRedstone();
                ((PoweredMachineBE)be).getEnergyStorage().setEnergy(((PoweredMachineBE)be).getMaxEnergy());
                if(i==0) {
                    var can=new ItemStack(JDTEItems.LARGE_FUEL_CANISTER.get());FuelCanister.setFuelLevel(can,12345678);FuelCanister.setBurnSpeed(can,12.5);can.setHoverName(net.minecraft.network.chat.Component.literal("Restart Fuel"));be.getMachineHandler().setStackInSlot(0,can);
                    var g=(ExtendedGeneratorBE)be;g.feRemaining=9876543;g.burnRemaining=400001;g.maxBurn=500000;
                }else {
                    var fuel=BuiltInRegistries.FLUID.stream().filter(f->f instanceof RefinedFuel r&&r.fePerMb()>0).findFirst().orElseThrow();((ExtendedFluidGeneratorBE)be).getFluidTank().setFluid(new FluidStack(fuel,123457));
                    be.getMachineHandler().setStackInSlot(0,new ItemStack(Items.BUCKET));
                }
                be.setChanged();
            }
            expected=snapshot();NbtIo.writeCompressed(expected,Path.of("generators-expected.nbt").toFile());stage=1;
        } else if(mode.equals("verify")) {
            expected=NbtIo.readCompressed(Path.of("generators-expected.nbt").toFile());check(expected.equals(snapshot()),"new production JVM restored exact item/fluid/FE/cards/progress NBT");stage=4;
        }else throw new IllegalArgumentException(mode);
    }
    private void pack() {
        var level=server.overworld();
        for(int i=0;i<2;i++) {
            final int index=i;var actor=new net.minecraft.server.level.ServerPlayer(server,level,new com.mojang.authlib.GameProfile(UUID.fromString("12ef3a01-54f1-4398-ad9b-000000000001"),"GeneratorProbe"));actor.setPos(POS[i].getX()+.5,101,POS[i].getZ()+.5);
            for(int slot=0;slot<36;slot++)actor.getInventory().setItem(slot,new ItemStack(Items.COBBLESTONE,64));
            var type=i==0?JDTEItems.EXTENDED_GENERATOR.get():JDTEItems.EXTENDED_FLUID_GENERATOR.get();
            for(var drop:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(POS[i]).inflate(3),e->e.getItem().is(type)))drop.discard();
            var old=be(i);var stale=old.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);
            check(EclipseAlloyWrenchItem.pickupMachine(level,POS[i],actor),"wrench pickup");
            var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(POS[i]).inflate(3),e->e.getItem().is(type));check(drops.size()==1,"full inventory creates one packed generator, got "+drops.size());
            var item=drops.get(0).getItem().copy();drops.get(0).discard();level.setBlockAndUpdate(POS[i],(i==0?JDTEBlocks.EXTENDED_GENERATOR.get():JDTEBlocks.EXTENDED_FLUID_GENERATOR.get()).defaultBlockState());
            var state=level.getBlockState(POS[i]);state.getBlock().setPlacedBy(level,POS[i],state,actor,item);
            check(expected.getCompound("machine"+i).equals(be(i).saveWithoutMetadata()),"full-inventory packed state conserved "+index);
            check(stale.extractEnergy(123,false)==0&&stale.receiveEnergy(123,false)==0,"packed old FE endpoint inert");
        }
        remember();
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event)throws Exception {
        if(mode.isEmpty()||event.phase!=TickEvent.Phase.END||event.getServer()!=server)return;
        check(++ticks<900,"timeout stage "+stage);var level=server.overworld();
        if(stage==1&&ticks>=100) {check(expected.equals(snapshot()),"full/disabled machine state stable");pack();server.saveEverything(false,true,true);force(false);stage=2;}
        else if(stage==2) {
            if(ticks%200==0) {
                var manager=level.getChunkSource().chunkMap.getDistanceManager();
                var method=Arrays.stream(net.minecraft.server.level.DistanceManager.class.getDeclaredMethods()).filter(m->m.getReturnType()==String.class&&Arrays.equals(m.getParameterTypes(),new Class<?>[]{long.class})).findFirst().orElseThrow();method.setAccessible(true);
                com.mojang.logging.LogUtils.getLogger().info("Generator unload diagnostic: forced={}, holder={}, ticket={}",level.getForcedChunks(),level.getChunkSource().getChunkDebugData(new net.minecraft.world.level.ChunkPos(POS[0])),method.invoke(manager,new net.minecraft.world.level.ChunkPos(POS[0]).toLong()));
            }
            for(var pos:POS)if(level.getChunkSource().hasChunk(pos.getX()>>4,pos.getZ()>>4))return;
            for(int i=0;i<2;i++)check(retained[i].extractEnergy(123,false)==0&&items[i].extractItem(0,64,false).isEmpty(),"retained unloaded endpoints inert");
            check(fluid.drain(10,IFluidHandler.FluidAction.EXECUTE).isEmpty(),"unloaded fluid interface inert");force(true);for(var pos:POS)level.getChunkAt(pos);stage=3;
        }else if(stage==3) {check(expected.equals(snapshot()),"real chunk unload/reload exact state");finish("generators-seed-success.txt","PASS: production JAR, both Extended generators, full inventory wrench drop/restore, real chunk unload/reload, stale item/FE/fluid endpoints, exact Large Fuel Canister, FE, fluid, eight-slot cards and prepaid work conservation; optional AE2/Curios/JEI/Jade absent");}
        else if(stage==4&&ticks>=20) {
            check(expected.equals(snapshot()),"saved machines remain stable after production restart");
            var solid=(ExtendedGeneratorBE)be(0);var liquid=(ExtendedFluidGeneratorBE)be(1);int credit=solid.feRemaining,fuel=FuelCanister.getFuelLevel(solid.getMachineHandler().getStackInSlot(0)),mb=liquid.getFluidTank().getFluidAmount();
            solid.getEnergyStorage().setEnergy(0);solid.doGenerate();check(solid.feRemaining==credit-solid.fePerTick()&&solid.getEnergyStorage().getEnergyStored()==solid.fePerTick()&&FuelCanister.getFuelLevel(solid.getMachineHandler().getStackInSlot(0))==fuel,"resumes prepaid work without purchasing fuel twice");
            liquid.getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.IGNORED;liquid.evaluateRedstone();liquid.getEnergyStorage().setEnergy(0);liquid.doGenerate();check(liquid.getEnergyStorage().getEnergyStored()==liquid.getFePerFuelTick()*3&&liquid.getFluidTank().getFluidAmount()==mb-2,"restored card consumes exactly two mB for triple FE");
            finish("generators-restart-success.txt","PASS: second production JVM restored exact generator state including large canister name/speed/fuel, FE, >16-bit fluid and progress, upgrades; resumed prepaid solid work without rebuying fuel and liquid x3 batch spends exactly two mB; no optional class linkage failure");
        }
    }
    private void finish(String file,String message)throws Exception {Files.writeString(Path.of(file),message+"\n");mode="";server.halt(false);}
}
