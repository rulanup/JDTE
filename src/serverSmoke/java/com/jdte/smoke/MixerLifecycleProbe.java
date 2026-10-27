package com.jdte.smoke;

import com.jdte.common.blockentities.*;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import java.nio.file.*;

/** Separate production-JAR probe; opt-in in the marked disposable server. */
public final class MixerLifecycleProbe {
    private static final BlockPos[] POS={new BlockPos(1007,90,1000),new BlockPos(1009,90,1000)};
    private MinecraftServer server;private String mode="";private int stage,ticks;private CompoundTag expected;
    private IFluidHandler retained;
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException("Mixer lifecycle: "+message);}
    private FluidMixerBE machine(ServerLevel level,int index){return (FluidMixerBE)level.getBlockEntity(POS[index]);}
    private void force(ServerLevel level,boolean value){
        if(!value)for(long packed:level.getForcedChunks().toLongArray()){
            var chunk=new net.minecraft.world.level.ChunkPos(packed);
            if(Math.abs(chunk.x-(POS[0].getX()>>4))<=4&&Math.abs(chunk.z-(POS[0].getZ()>>4))<=4)level.setChunkForced(chunk.x,chunk.z,false);
        }
        for(var pos:POS)level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,value);
    }
    private CompoundTag fields(FluidMixerBE mixer){
        var all=mixer.saveWithFullMetadata();var out=new CompoundTag();
        for(String key:new String[]{"inventory","inputTankA","inputTankB","outputTank","energy","progress"})if(all.contains(key))out.put(key,all.get(key).copy());
        out.put("upgrades",UpgradeHelper.getUpgradeHandler(mixer).serializeNBT());return out;
    }
    private CompoundTag snapshot(ServerLevel level){var tag=new CompoundTag();for(int i=0;i<POS.length;i++)tag.put(Integer.toString(i),fields(machine(level,i)));return tag;}
    @SubscribeEvent public void started(ServerStartedEvent event){
        mode=System.getProperty("jdte.mixerLifecycle","");if(mode.isEmpty())return;server=event.getServer();
        check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked disposable server required");var level=server.overworld();force(level,true);for(var pos:POS)level.getChunkAt(pos);
        try {
            if(mode.equals("seed")){
                for(int i=0;i<POS.length;i++){
                    level.removeBlockEntity(POS[i]);level.setBlock(POS[i],net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2|64);
                    level.setBlockAndUpdate(POS[i],(i==0?JDTEBlocks.ADVANCED_FLUID_MIXER.get():JDTEBlocks.EXTENDED_FLUID_MIXER.get()).defaultBlockState());
                    var mixer=machine(level,i);var upgrades=UpgradeHelper.getUpgradeHandler(mixer);
                    upgrades.insertItem(0,new ItemStack(JDTEItems.AE_CRAFTING_READ_UPGRADE.get()),false);upgrades.insertItem(1,new ItemStack(JDTEItems.MIXING_UPGRADE.get()),false);upgrades.insertItem(2,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);
                    mixer.inputTankA.setFluid(new FluidStack(Fluids.WATER,12000));mixer.inputTankB.setFluid(new FluidStack(Fluids.LAVA,13000));
                    mixer.outputTank.setFluid(new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("justdirethings","time_fluid")),14000));
                    mixer.getMachineHandler().setStackInSlot(0,new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get(),3));mixer.getEnergyStorage().receiveEnergy(123456,false);
                    var data=mixer.saveWithFullMetadata();data.putInt("progress",73);mixer.load(data);mixer.setChanged();
                }
                expected=snapshot(level);NbtIo.writeCompressed(expected,Path.of("mixer-expected.nbt").toFile());
                retained=machine(level,0).getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(()->new IllegalStateException("combined capability"));stage=1;
            } else {
                expected=NbtIo.readCompressed(Path.of("mixer-expected.nbt").toFile());check(expected.equals(snapshot(level)),"fresh JVM restored all machine fields");
                for(int i=0;i<POS.length;i++){
                    var mixer=machine(level,i);mixer.inputTankA.setFluid(new FluidStack(Fluids.WATER,1000));mixer.inputTankB.setFluid(FluidStack.EMPTY);mixer.outputTank.setFluid(FluidStack.EMPTY);
                    mixer.getMachineHandler().setStackInSlot(0,new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get()));UpgradeHelper.getUpgradeHandler(mixer).extractItem(0,1,false);
                }stage=4;
            }
        } catch(java.io.IOException error){throw new IllegalStateException(error);}
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event){
        if(mode.isEmpty() || event.phase!=TickEvent.Phase.END || event.getServer()!=server)return;
        try {
            var level=server.overworld();ticks++;
            check(ticks<900,"probe timed out at stage "+stage);
            if(stage==1&&ticks>=10){check(expected.equals(snapshot(level)),"paused state changed before unload");force(level,false);stage=2;}
            else if(stage==2){
                boolean loaded=false;for(var pos:POS)loaded|=level.getChunkSource().hasChunk(pos.getX()>>4,pos.getZ()>>4);
                if(loaded)return;check(retained.drain(1,IFluidHandler.FluidAction.SIMULATE).isEmpty(),"unloaded retained endpoint still drains");force(level,true);for(var pos:POS)level.getChunkAt(pos);stage=3;
            }else if(stage==3){check(expected.equals(snapshot(level)),"unload/reload changed tanks, FE, catalyst, upgrades or progress");finish("mixer-seed-success.txt","PASS: both production mixer tiers unloaded/reloaded with exact tanks, FE, catalyst, upgrades and progress; retained endpoint rejected while unloaded");}
            else if(stage==4&&ticks>=35){
                if(ticks==35)Files.writeString(Path.of("mixer-resume-observation.txt"), snapshot(level).toString());
                boolean complete=true;for(int i=0;i<POS.length;i++)complete&=machine(level,i).outputTank.getFluidAmount()==250;
                if(!complete)return;
                for(int i=0;i<POS.length;i++){
                    var mixer=machine(level,i);check(mixer.inputTankA.isEmpty()&&mixer.inputTankB.isEmpty()&&mixer.getMachineHandler().getStackInSlot(0).isEmpty(),"resumed recipe inputs not consumed exactly once: "+fields(mixer));
                    check(mixer.outputTank.getFluidAmount()==250&&mixer.outputTank.getFluid().getFluid()==BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("justdirethings","time_fluid")),"resumed product is not real Time Fluid");
                    check(mixer.getEnergyStorage().getEnergyStored()==118456,"resumed recipe did not charge exactly 5000 FE");
                }finish("mixer-restart-success.txt","PASS: fresh production server restored both mixer tiers and naturally completed saved 73/100-tick work with exact fluid, catalyst and 5000 FE costs");
            }
        }catch(Exception error){try{Files.writeString(Path.of("mixer-failure.txt"),error.toString());}catch(java.io.IOException io){error.addSuppressed(io);}throw new IllegalStateException(error);}
    }
    private void finish(String file,String text)throws java.io.IOException{Files.writeString(Path.of(file),text+"\n");mode="";server.halt(false);}
}
