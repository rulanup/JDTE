package com.jdte.gametest;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.core.definitions.AEBlocks;
import com.glodblock.github.appflux.common.AFItemAndBlock;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import com.jdte.common.blockentities.*;
import com.jdte.setup.JDTEBlocks;
import com.direwolf20.justdirethings.util.MiscHelpers;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import static com.jdte.gametest.AEUpgradeGameTests.*;

/** Loaded only when both AE2 and Applied Flux are present. Uses a real powered ME grid and FE disk. */
@PrefixGameTestTemplate(false)
public final class TransmitterAppliedFluxGameTests {
    private static final BlockPos RECEIVER=new BlockPos(1,1,4);
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=240)
    public static void actualAppliedFluxDiskSuppliesOnlyRealReceiverDemand(GameTestHelper h){run(h,false);}
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=240)
    public static void failedRefundRemainsOwnedAcrossSnapshotAndNodeTeardown(GameTestHelper h){run(h,true);}
    private static void run(GameTestHelper h,boolean failRefund){
        grid(h);h.setBlock(new BlockPos(2,1,2),AEBlocks.CREATIVE_ENERGY_CELL.block());
        var drive=(DriveBlockEntity)h.getBlockEntity(DRIVE);
        drive.getInternalInventory().insertItem(2,new ItemStack(AFItemAndBlock.FE_CELL_1k),false);
        h.setBlock(MACHINE,JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get());var original=h.getBlockEntity(MACHINE);
        var source=new AdvancedEnergyTransmitterBE(original.getBlockPos(),original.getBlockState()){
            @Override public AABB getAABB(BlockPos ignored){return new AABB(h.absolutePos(RECEIVER));}
        };
        h.getLevel().setBlockEntity(source);source.getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.HIGH;
        int[] accepted={0};int[] calls={0,0,0};ItemStack[] removedCell={ItemStack.EMPTY};
        var storage=new IEnergyStorage(){
            public int receiveEnergy(int n,boolean simulate){
                calls[simulate?0:1]++;calls[2]=n;
                int received=Math.min(n,failRefund?1000:50000);
                if(!simulate){received=Math.min(received,failRefund?40:50000);accepted[0]+=received;
                    if(failRefund)removedCell[0]=drive.getInternalInventory().extractItem(2,1,false);}
                return received;
            }
            public int extractEnergy(int n,boolean simulate){return 0;}
            public int getEnergyStored(){return accepted[0];}
            public int getMaxEnergyStored(){return 50000;}
            public boolean canReceive(){return true;}
            public boolean canExtract(){return false;}
        };
        h.setBlock(RECEIVER,JDTEBlocks.ADVANCED_FLUID_MIXER.get());var old=h.getBlockEntity(RECEIVER);
        var target=new AdvancedFluidMixerBE(old.getBlockPos(),old.getBlockState()){
            private final LazyOptional<IEnergyStorage> energy=LazyOptional.of(()->storage);
            @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,Direction side){return cap==ForgeCapabilities.ENERGY?energy.cast():super.getCapability(cap,side);}
        };h.getLevel().setBlockEntity(target);
        h.runAfterDelay(80,()->{
            var me=ap(h).getGrid().getStorageService().getInventory();var action=IActionSource.empty();var key=FluxKey.of(EnergyType.FE);
            h.assertTrue(source.getEnergyNetworkStatus()==com.jdte.common.integrations.ae2.AdvancedEnergyTransmitterEnergySource.Status.ONLINE,"real adjacent Forge node has power and channel");
            int amount=failRefund?1000:50000;
            h.assertTrue(me.insert(key,amount,Actionable.MODULATE,action)==amount,"seed real FE disk");
            source.setTickSpeed(1);source.getRedstoneControlData().checkedRedstone=false;
            source.getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.IGNORED;source.tickServer();
            source.getRedstoneControlData().redstoneMode=MiscHelpers.RedstoneMode.HIGH;
            h.assertTrue(accepted[0]==(failRefund?40:50000)&&source.getEnergyStorage().getEnergyStored()==0,"real receiver charge: accepted="+accepted[0]+", buffer="+source.getEnergyStorage().getEnergyStored()+", targets="+source.getTargetCount()+", last="+source.getLastTransferred()+", reserve="+source.saveWithFullMetadata().getLong("networkEnergyReserve")+", calls="+java.util.Arrays.toString(calls)+", attempted="+source.getLastAttemptedTargets()+", tickSpeed="+source.getTickSpeed()+", available="+me.extract(key,amount,Actionable.SIMULATE,action));
            if(!failRefund){h.assertTrue(me.extract(key,50000,Actionable.SIMULATE,action)==0,"disk paid exactly real delivery");h.succeed();return;}
            var snapshot=source.saveWithFullMetadata();h.assertTrue(snapshot.getLong("networkEnergyReserve")==960&&!removedCell[0].isEmpty(),"failed refund retained 960 FE and disk is removed");
            drive.getInternalInventory().insertItem(2,removedCell[0],false);
            var retained=source.getCapability(ForgeCapabilities.ENERGY,source.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)).orElseThrow(IllegalStateException::new);
            source.onChunkUnloaded();source.tickServer();
            h.assertTrue(me.extract(key,1000,Actionable.SIMULATE,action)==0&&retained.extractEnergy(100,false)==0,"teardown after snapshot does not refund or recreate old node");
            var restored=new AdvancedEnergyTransmitterBE(source.getBlockPos(),source.getBlockState());restored.load(snapshot);h.getLevel().setBlockEntity(restored);
            h.runAfterDelay(40,()->{
                h.assertTrue(restored.getEnergyNetworkStatus()==com.jdte.common.integrations.ae2.AdvancedEnergyTransmitterEnergySource.Status.ONLINE,"restored managed node reconnects");
                h.assertTrue(restored.saveWithFullMetadata().getLong("networkEnergyReserve")==0&&me.extract(key,1000,Actionable.SIMULATE,action)==960&&accepted[0]==40,"saved reserve refunded once: disk 960 plus receiver 40 equals original 1000");h.succeed();
            });
        });
    }
}
