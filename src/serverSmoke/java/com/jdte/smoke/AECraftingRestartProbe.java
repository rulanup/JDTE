package com.jdte.smoke;

import appeng.api.config.Actionable;
import appeng.api.crafting.*;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.*;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.core.definitions.*;
import com.jdte.common.blockentities.*;
import com.jdte.common.integrations.ae2.AEWirelessLinkData;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
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
import java.nio.file.*;
import java.util.concurrent.Future;

/** Real AE CPU persistence against separately reobfuscated production JDTE. */
public final class AECraftingRestartProbe {
    private static final BlockPos P=new BlockPos(1100,90,1100),AP=P.north(),DRIVE=P.south(),CPU=P.east(),PROVIDER=CPU.south(),MIXER=PROVIDER.east();
    private MinecraftServer server;private String mode="";private int ticks,stage,at;private Future<ICraftingPlan> plan;private CompoundTag expected;private int stoppedEnergy;
    private void check(boolean value,String message){if(!value)throw new IllegalStateException("AE restart: "+message);}
    private FluidMixerBE mixer(){return (FluidMixerBE)server.overworld().getBlockEntity(MIXER);}
    private WirelessAccessPointBlockEntity point(){return (WirelessAccessPointBlockEntity)server.overworld().getBlockEntity(AP);}
    private AEFluidKey product(){return AEFluidKey.of(BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("justdirethings","time_fluid")));}
    private boolean busy(){return point().getGrid()!=null&&point().getGrid().getCraftingService().getCpus().stream().anyMatch(cpu->cpu.isBusy()&&cpu.getJobStatus()!=null);}
    private CompoundTag fields(){var all=mixer().saveWithFullMetadata();var out=new CompoundTag();for(String key:new String[]{"inventory","inputTankA","inputTankB","outputTank","energy","progress","ForgeCaps"})if(all.contains(key))out.put(key,all.get(key).copy());return out;}
    private void redstone(boolean pause){mixer().getRedstoneControlData().redstoneMode=pause?com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH:com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.IGNORED;mixer().setChanged();}
    @SubscribeEvent public void started(ServerStartedEvent event){
        mode=System.getProperty("jdte.craftingLifecycle","");if(mode.isEmpty())return;server=event.getServer();
        check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked disposable fixture required");var level=server.overworld();
        for(var p:new BlockPos[]{P,AP,DRIVE,CPU,PROVIDER,MIXER}){level.setChunkForced(p.getX()>>4,p.getZ()>>4,true);level.getChunkAt(p);}
        try{
            if(mode.equals("seed")){
                for(int x=-1;x<=4;x++)for(int z=-2;z<=3;z++)for(int y=0;y<=2;y++){var p=P.offset(x,y,z);level.removeBlockEntity(p);level.setBlock(p,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2|64);}
                level.setBlockAndUpdate(P,AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());level.setBlockAndUpdate(AP,AEBlocks.WIRELESS_ACCESS_POINT.block().defaultBlockState());
                level.setBlockAndUpdate(DRIVE,AEBlocks.DRIVE.block().getOrientationStrategy().setFacing(AEBlocks.DRIVE.block().defaultBlockState(),Direction.SOUTH));
                var drive=(DriveBlockEntity)level.getBlockEntity(DRIVE);drive.getInternalInventory().insertItem(0,AEItems.ITEM_CELL_256K.stack(),false);drive.getInternalInventory().insertItem(1,AEItems.FLUID_CELL_256K.stack(),false);
                level.setBlockAndUpdate(CPU,AEBlocks.CRAFTING_STORAGE_1K.block().defaultBlockState());level.setBlockAndUpdate(PROVIDER,AEBlocks.PATTERN_PROVIDER.block().defaultBlockState());
                level.setBlockAndUpdate(MIXER,JDTEBlocks.EXTENDED_FLUID_MIXER.get().defaultBlockState());mixer().getEnergyStorage().receiveEnergy(100000,false);
                var read=new ItemStack(JDTEItems.AE_CRAFTING_READ_UPGRADE.get());var output=new ItemStack(JDTEItems.ADVANCED_AE_OUTPUT_UPGRADE.get());var link=GlobalPos.of(level.dimension(),AP);AEWirelessLinkData.set(read,link);AEWirelessLinkData.set(output,link);
                UpgradeHelper.getUpgradeHandler(mixer()).insertItem(0,read,false);UpgradeHelper.getUpgradeHandler(mixer()).insertItem(1,output,false);
                ((PatternProviderBlockEntity)level.getBlockEntity(PROVIDER)).getLogic().getPatternInv().insertItem(0,PatternDetailsHelper.encodeProcessingPattern(new GenericStack[]{new GenericStack(AEItemKey.of(JDTEItems.TIME_FLUID_CATALYST.get()),1),new GenericStack(AEFluidKey.of(Fluids.WATER),1000)},new GenericStack[]{new GenericStack(product(),250)}),false);
            }else{expected=NbtIo.readCompressed(Path.of("crafting-expected.nbt").toFile());check(expected.equals(fields()),"exact mixer state restored before grid boot");stage=4;}
        }catch(java.io.IOException e){throw new IllegalStateException(e);}
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event){
        if(mode.isEmpty()||event.phase!=TickEvent.Phase.END||event.getServer()!=server)return;
        try{
            ticks++;check(ticks<1600,"timeout stage="+stage+" state="+fields());
            if(mode.equals("seed")){
                if(stage==0&&ticks>=100){
                    check(point().isActive(),"real network active");check(!UpgradeHelper.mayRunWithUpgrades(mixer()),"idle read gate pauses mixer");
                    var grid=point().getGrid();var inv=grid.getStorageService().getInventory();
                    check(inv.insert(AEItemKey.of(JDTEItems.TIME_FLUID_CATALYST.get()),2,Actionable.MODULATE,IActionSource.empty())==2,"stage catalysts");check(inv.insert(AEFluidKey.of(Fluids.WATER),2000,Actionable.MODULATE,IActionSource.empty())==2000,"stage water");var host=point();
                    plan=grid.getCraftingService().beginCraftingCalculation(server.overworld(),()->IActionSource.ofMachine(host),product(),500,CalculationStrategy.REPORT_MISSING_ITEMS);stage=1;
                }else if(stage==1&&plan.isDone()){
                    var p=plan.get();check(!p.simulation(),"executable real plan");var service=point().getGrid().getCraftingService();var cpu=service.getCpus().stream().filter(c->!c.isBusy()).findFirst().orElseThrow();check(service.submitJob(p,null,cpu,false,IActionSource.ofMachine(point())).successful(),"real CPU submission");stage=2;
                }else if(stage==2&&mixer().saveWithFullMetadata().getInt("progress")>=37){
                    check(busy()&&UpgradeHelper.mayRunWithUpgrades(mixer()),"CPU still running with read gate open");check(mixer().getEnergyStorage().getEnergyStored()==100000,"snapshot is before first completion");
                    redstone(true);expected=fields();NbtIo.writeCompressed(expected,Path.of("crafting-expected.nbt").toFile());server.saveEverything(false,true,true);check(busy(),"save while active retains busy job");at=ticks;stage=3;
                }else if(stage==3&&ticks-at>=15){check(busy()&&expected.equals(fields()),"saved live job retains pending machine state before shutdown");finish("crafting-seed-success.txt","PASS: real CPU job supplied mixer, read gate opened, partial recipe saved while CPU remained busy, exact state held through normal saved shutdown");}
            }else{
                if(stage==4&&ticks>=100){check(point().isActive()&&busy(),"fresh server restored actual nonempty busy CPU job");check(expected.equals(fields()),"all mixer fields persist while restarted grid boots");check(UpgradeHelper.mayRunWithUpgrades(mixer()),"read upgrade resolves restored grid without stale cache");redstone(false);stage=5;}
                else if(stage==5&&!busy()&&!UpgradeHelper.mayRunWithUpgrades(mixer())){
                    var inv=point().getGrid().getStorageService().getInventory();check(inv.extract(product(),10000,Actionable.SIMULATE,IActionSource.empty())==500,"completed restored CPU returns exactly 500 mB to ME");
                    check(mixer().inputTankA.isEmpty()&&mixer().inputTankB.isEmpty()&&mixer().outputTank.isEmpty()&&mixer().getMachineHandler().getStackInSlot(0).isEmpty(),"all inputs consumed exactly once and product exported");check(mixer().getEnergyStorage().getEnergyStored()==90000,"two batches charge exactly 10000 FE across restart");stoppedEnergy=mixer().getEnergyStorage().getEnergyStored();at=ticks;stage=6;
                }else if(stage==6&&ticks-at>=15){check(mixer().getEnergyStorage().getEnergyStored()==stoppedEnergy,"completed job leaves machine paused");finish("crafting-restart-success.txt","PASS: fresh server restored actual busy CPU and mixer NBT, resumed partial work, completed real job through Advanced AE Output with exactly 500 mB product and 10000 FE cost, then paused");}
            }
        }catch(Exception e){try{Files.writeString(Path.of("crafting-failure.txt"),e.toString());}catch(java.io.IOException io){e.addSuppressed(io);}throw new IllegalStateException(e);}
    }
    private void finish(String name,String text)throws java.io.IOException{Files.writeString(Path.of(name),text+"\n");mode="";server.halt(false);}
}
