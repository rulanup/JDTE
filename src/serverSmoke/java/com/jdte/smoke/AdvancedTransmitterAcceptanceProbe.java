package com.jdte.smoke;

import appeng.api.config.Actionable;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.security.IActionSource;
import appeng.core.definitions.AEBlocks;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.blockentity.storage.DriveBlockEntity;
import com.glodblock.github.appflux.common.AFItemAndBlock;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.MiscHelpers;
import com.jdte.common.blockentities.*;
import com.jdte.setup.JDTEBlocks;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;
import java.nio.file.*;
import java.util.*;

/** Actual dedicated login/logout/Curios plus real ME FE disk across two production JVMs. */
public final class AdvancedTransmitterAcceptanceProbe {
    private static final BlockPos BASE=new BlockPos(5000,100,5000),AP=BASE.east(2),DRIVE=BASE.east(2).south(2);
    private String mode="";private MinecraftServer server;private Path control;private int ticks,stage,at;
    private UUID operator;private IEnergyStorage retained;private CompoundTag beforeUnload;
    private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException("Advanced dedicated acceptance: "+message);}
    private AdvancedEnergyTransmitterBE machine(){return (AdvancedEnergyTransmitterBE)server.overworld().getBlockEntity(BASE);}
    private AdvancedFluidMixerBE target(){return (AdvancedFluidMixerBE)server.overworld().getBlockEntity(BASE.above());}
    private static IEnergyStorage fe(ItemStack item){return item.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);}
    private top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler curios(ServerPlayer p){return CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new).getStacksHandler("transmitter_test").orElseThrow().getStacks();}
    private ItemStack item(){var stack=new ItemStack(Registration.EclipseAlloyPickaxe.get());var cap=fe(stack);cap.receiveEnergy(cap.getMaxEnergyStored()-1000,false);return stack;}
    private void phase(int n)throws Exception{Files.writeString(control.resolve("phase"),Integer.toString(n));}
    private long stored(){var point=(WirelessAccessPointBlockEntity)server.overworld().getBlockEntity(AP);return point.getGrid().getStorageService().getInventory().extract(FluxKey.of(EnergyType.FE),Long.MAX_VALUE,Actionable.SIMULATE,IActionSource.empty());}
    private void online(){
        var point=(WirelessAccessPointBlockEntity)server.overworld().getBlockEntity(AP);var drive=(DriveBlockEntity)server.overworld().getBlockEntity(DRIVE);
        var host=(IInWorldGridNodeHost)machine().getEnergyNetworkSource().getGridNodeHost();
        check(point.isActive()&&drive.getMainNode().isOnline()&&host.getGridNode(null)!=null&&host.getGridNode(null).isActive()&&host.getGridNode(null).getGrid()==point.getGrid()&&drive.getMainNode().getGrid()==point.getGrid(),"actual transmitter, powered AP and FE disk share a channelled grid");
    }
    private void full(ServerPlayer p){for(var stack:List.of(p.getInventory().getItem(0),p.getInventory().armor.get(0),p.getOffhandItem(),curios(p).getStackInSlot(0)))check(fe(stack).getEnergyStored()==fe(stack).getMaxEnergyStored(),"actual dedicated player's equipment/Curios charged");}
    @SubscribeEvent public void start(ServerStartedEvent event)throws Exception{
        mode=System.getProperty("jdte.advancedTransmitterAcceptance","");if(mode.isEmpty())return;
        check(Files.isRegularFile(Path.of(".jdte-lifecycle-fixture")),"marked disposable server required");
        control=Path.of(System.getProperty("jdte.advancedTransmitterControl"));check(Files.isRegularFile(control.resolve(".fixture")),"marked control required");server=event.getServer();var level=server.overworld();level.setChunkForced(BASE.getX()>>4,BASE.getZ()>>4,true);level.getChunkAt(BASE);
        if(mode.equals("seed")){
            for(int x=-2;x<=4;x++)for(int z=-2;z<=4;z++){level.setBlockAndUpdate(BASE.offset(x,-1,z),Blocks.STONE.defaultBlockState());for(int y=0;y<=3;y++){var p=BASE.offset(x,y,z);level.removeBlockEntity(p);level.setBlockAndUpdate(p,Blocks.AIR.defaultBlockState());}}
            level.setBlockAndUpdate(BASE,JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get().defaultBlockState());level.setBlockAndUpdate(BASE.above(),JDTEBlocks.ADVANCED_FLUID_MIXER.get().defaultBlockState());
            level.setBlockAndUpdate(AP,AEBlocks.WIRELESS_ACCESS_POINT.block().defaultBlockState());for(var p:List.of(BASE.east(),BASE.east().south(),BASE.east(2).south()))level.setBlockAndUpdate(p,AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
            level.setBlockAndUpdate(DRIVE,AEBlocks.DRIVE.block().getOrientationStrategy().setFacing(AEBlocks.DRIVE.block().defaultBlockState(),Direction.SOUTH));
            ((DriveBlockEntity)level.getBlockEntity(DRIVE)).getInternalInventory().insertItem(0,new ItemStack(AFItemAndBlock.FE_CELL_1k),false);
            machine().getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.IGNORED;machine().getFilterData().allowlist=true;phase(0);
        }else if(mode.equals("verify")){
            var expected=NbtIo.readCompressed(Path.of("advanced-transmitter-me-expected.nbt").toFile());
            for(var key:List.of("energy","networkEnergyReserve","boundPlayer","boundPlayerName","ForgeCaps"))check(Objects.equals(expected.get(key),machine().saveWithFullMetadata().get(key)),"second JVM restores source field "+key);
            operator=machine().getBoundPlayerIdForCopy().orElseThrow();check(target().getEnergyStorage().getEnergyStored()==100000,"target restored actual 100000 FE");stage=8;
        }else throw new IllegalArgumentException(mode);
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event)throws Exception{
        if(mode.isEmpty()||event.phase!=TickEvent.Phase.END||event.getServer()!=server)return;check(++ticks<3000,"timeout stage "+stage);
        var level=server.overworld();var p=operator==null?null:server.getPlayerList().getPlayer(operator);
        try{
            if(stage==0){
                var players=server.getPlayerList().getPlayers();if(players.isEmpty()||ticks<80)return;p=players.get(0);operator=p.getUUID();online();
                p.connection.teleport(BASE.getX()+.5,BASE.getY(),BASE.getZ()+3.5,180,0);p.getInventory().clearContent();p.getInventory().setItem(0,item());var boots=new ItemStack(Registration.EclipseAlloyBoots.get());fe(boots).receiveEnergy(fe(boots).getMaxEnergyStored()-1000,false);p.getInventory().armor.set(0,boots);p.setItemInHand(InteractionHand.OFF_HAND,item());
                var curio=item();check(curios(p).isItemValid(0,curio)&&curios(p).insertItem(0,curio,false).isEmpty(),"real validated dedicated Curios insertion");
                check(machine().togglePlayerBinding(p)==AdvancedEnergyTransmitterBE.BindingResult.BOUND,"real logged-in dedicated operator bound");machine().getEnergyStorage().setEnergy(4000);machine().setChanged();stage=1;
            }else if(stage==1){full(p);check(machine().getEnergyStorage().getEnergyStored()==0,"exact dedicated 4000 FE paid");retained=machine().getCapability(ForgeCapabilities.ENERGY,machine().getBlockState().getValue(BlockStateProperties.FACING)).orElseThrow(IllegalStateException::new);phase(1);stage=2;}
            else if(stage==2&&p==null){check(!machine().isBoundPlayerOnline(),"real disconnected operator resolves offline while server runs");machine().getEnergyStorage().setEnergy(7777);machine().setChanged();at=ticks;stage=3;}
            else if(stage==3&&ticks-at>=100){check(machine().getEnergyStorage().getEnergyStored()==7777&&!machine().isBoundPlayerOnline(),"100 real dedicated ticks offline do not charge or consume FE");beforeUnload=machine().saveWithFullMetadata();level.setChunkForced(BASE.getX()>>4,BASE.getZ()>>4,false);stage=4;}
            else if(stage==4){if(level.getChunkSource().hasChunk(BASE.getX()>>4,BASE.getZ()>>4))return;check(retained.receiveEnergy(100,false)==0&&retained.extractEnergy(100,false)==0,"offline unloaded held FE port rejects mutation");level.setChunkForced(BASE.getX()>>4,BASE.getZ()>>4,true);level.getChunkAt(BASE);check(machine().getEnergyStorage().getEnergyStored()==7777&&machine().getBoundPlayerIdForCopy().orElseThrow().equals(operator),"dedicated offline chunk reload restores FE/binding");phase(2);stage=5;}
            else if(stage==5&&p!=null){full(p);check(machine().isBoundPlayerOnline(),"same UUID real reconnect detected");check(fe(curios(p).getStackInSlot(0)).extractEnergy(1000,false)==1000,"real saved Curios item gap");stage=6;}
            else if(stage==6){full(p);check(machine().getEnergyStorage().getEnergyStored()==6777,"dedicated reconnect refill costs exactly 1000");machine().getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.HIGH;phase(3);stage=7;}
            else if(stage==7&&p==null){online();check(machine().getEnergyStorage().extractEnergy(6777,false)==6777,"remaining source FE explicitly withdrawn");check(stored()==0&&target().getEnergyStorage().getEnergyStored()==0,"no unintended block output or pre-existing ME FE");target().getEnergyStorage().setEnergy(60000);target().setChanged();var point=(WirelessAccessPointBlockEntity)level.getBlockEntity(AP);check(point.getGrid().getStorageService().getInventory().insert(FluxKey.of(EnergyType.FE),50000,Actionable.MODULATE,IActionSource.empty())==50000,"real production FE cell seeded");machine().getFilterData().allowlist=false;machine().getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.IGNORED;stage=9;}
            else if(stage==9){check(target().getEnergyStorage().getEnergyStored()==100000&&stored()==10000&&machine().getEnergyStorage().getEnergyStored()==0,"real ME batch delivers exactly demand 40000, retains disk 10000");machine().getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.HIGH;machine().setChanged();NbtIo.writeCompressed(machine().saveWithFullMetadata(),Path.of("advanced-transmitter-me-expected.nbt").toFile());finish("advanced-transmitter-seed-success.txt","PASS: actual dedicated player/Curios charge, real logout with 100 running offline ticks, true offline chunk unload/reload, same-client reconnect, exact refill; installed real ME disk spends 40000 and saves 10000 across shutdown");}
            else if(stage==8&&ticks>=80){online();check(stored()==10000&&!machine().isBoundPlayerOnline(),"second JVM restores real disk FE and offline binding");check(target().getEnergyStorage().extractEnergy(50000,false)==50000,"receiver explicitly withdraws 50000 FE");machine().getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.IGNORED;stage=10;}
            else if(stage==10){check(target().getEnergyStorage().getEnergyStored()==60000&&stored()==0&&machine().getEnergyStorage().getEnergyStored()==0&&machine().saveWithFullMetadata().getLong("networkEnergyReserve")==0,"second JVM reconnects same-grid source, spends only remaining 10000, no duplicate credit");finish("advanced-transmitter-restart-success.txt","PASS: actual AE2/Applied Flux drive and transmitter managed node restore across production JVMs, stored 10000 delivered once; exact final 60000 FE after external 50000 withdrawal");}
        }catch(Exception error){Files.writeString(control.resolve("server-failure"),error.toString());throw error;}
    }
    private void finish(String file,String message)throws Exception{Files.writeString(control.resolve(file),message+"\n");mode="";server.halt(false);}
}
