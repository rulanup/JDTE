package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.setup.*;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class TransmitterNetworkGameTests {
    private static final BlockPos SOURCE=new BlockPos(2,1,2),PEER=new BlockPos(4,1,2);
    private static ExtendedEnergyTransmitterBE source(GameTestHelper h){
        h.setBlock(SOURCE,JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get());h.setBlock(PEER,Registration.EnergyTransmitter.get());
        var source=(ExtendedEnergyTransmitterBE)h.getBlockEntity(SOURCE);source.setAreaSettings(4,1,1,1,0,0,false);source.getBlocksToCharge();return source;
    }
    private static EnergyTransmitterBE peer(GameTestHelper h){return (EnergyTransmitterBE)h.getBlockEntity(PEER);}
    private static long stock(GameTestHelper h){return (long)((ExtendedEnergyTransmitterBE)h.getBlockEntity(SOURCE)).getEnergyStorage().getRealEnergyStored()+peer(h).getEnergyStorage().getRealEnergyStored();}
    @GameTest(template="mineral_empty") public static void pooledExtractionDebitsActualOwnersNeverNegativeAndReloadConserves(GameTestHelper h){
        var source=source(h);source.getEnergyStorage().setEnergy(1000);peer(h).getEnergyStorage().setEnergy(8000);
        h.assertTrue(source.getTotalEnergyStored()==9000,"real discovered native peer contributes to network");
        var face=source.getBlockState().getValue(BlockStateProperties.FACING);var port=source.getCapability(ForgeCapabilities.ENERGY,face).orElseThrow(IllegalStateException::new);
        h.assertTrue(port.extractEnergy(5000,true)==5000&&stock(h)==9000,"network extraction simulation is inert");
        h.assertTrue(port.extractEnergy(5000,false)==5000&&stock(h)==4000&&source.getEnergyStorage().getRealEnergyStored()>=0&&peer(h).getEnergyStorage().getRealEnergyStored()>=0,"pooled debit must not make a local owner negative");
        var saved=source.saveWithFullMetadata();var restored=(ExtendedEnergyTransmitterBE)BlockEntity.loadStatic(source.getBlockPos(),source.getBlockState(),saved);h.getLevel().setBlockEntity(restored);restored.getBlocksToCharge();
        h.assertTrue(stock(h)==4000,"fresh load cannot turn negative local debt into duplicated network FE");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void wideNetworkTotalsAndBalancingDoNotOverflowOrOverfill(GameTestHelper h){
        int original=Config.ENERGY_TRANSMITTER_T1_MAX_RF.get();
        try{
            Config.ENERGY_TRANSMITTER_T1_MAX_RF.set(1500000000);var source=source(h);var peer=peer(h);UpgradeHelper.syncCapacities(source);UpgradeHelper.syncCapacities(peer);
            source.getEnergyStorage().setEnergy(1400000000);peer.getEnergyStorage().setEnergy(1500000000);
            h.assertTrue(source.getTotalEnergyStored()==Integer.MAX_VALUE&&source.getTotalMaxEnergyStored()==Integer.MAX_VALUE,"FE API reports saturated positive totals above 2B");
            source.balanceEnergy();h.assertTrue(stock(h)==2900000000L&&source.getEnergyStorage().getRealEnergyStored()==1450000000&&peer.getEnergyStorage().getRealEnergyStored()==1450000000,"balancing uses wide actual totals, not saturated API view");
            var cap=source.getCapability(ForgeCapabilities.ENERGY,source.getBlockState().getValue(BlockStateProperties.FACING)).orElseThrow(IllegalStateException::new);
            h.assertTrue(cap.receiveEnergy(Integer.MAX_VALUE,true)==100000000&&stock(h)==2900000000L,"network simulation sums real gaps when total capacity exceeds int");
            h.assertTrue(cap.receiveEnergy(Integer.MAX_VALUE,false)==100000000&&stock(h)==3000000000L&&cap.receiveEnergy(1,false)==0,"real FE input is bounded by exact aggregate room");
        }finally{Config.ENERGY_TRANSMITTER_T1_MAX_RF.set(original);}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void staleNativePeerRotationAndReplacementCannotTransferOldStock(GameTestHelper h){
        var source=source(h);var old=peer(h);old.getEnergyStorage().setEnergy(9000);h.assertTrue(source.getTotalEnergyStored()==9000,"prime native peer cache");
        h.setBlock(PEER,Blocks.AIR);h.setBlock(PEER,Registration.EnergyTransmitter.get());var fresh=peer(h);fresh.getEnergyStorage().setEnergy(4000);
        h.getLevel().setBlock(fresh.getBlockPos(),fresh.getBlockState().setValue(BlockStateProperties.FACING,Direction.UP),Block.UPDATE_ALL);
        h.assertTrue(source.getTotalEnergyStored()==4000&&source.extractEnergy(1000)==1000&&fresh.getEnergyStorage().getRealEnergyStored()==3000&&old.getEnergyStorage().getRealEnergyStored()==9000,"current native peer/facing is resolved without draining detached cached storage");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void heterogeneousCapacityBalanceAndNegativePacketsCannotCreateFe(GameTestHelper h){
        var source=source(h);var peer=peer(h);
        UpgradeHelper.getUpgradeHandler(source).insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);UpgradeHelper.syncCapacities(source);
        source.getEnergyStorage().setEnergy(4500000);peer.getEnergyStorage().setEnergy(500000);source.balanceEnergy();
        h.assertTrue(stock(h)==5000000&&source.getEnergyStorage().getRealEnergyStored()<=source.getEnergyStorage().getRealMaxEnergyStored()&&peer.getEnergyStorage().getRealEnergyStored()<=peer.getEnergyStorage().getRealMaxEnergyStored(),"balance cannot overfill a smaller real peer");
        var port=source.getCapability(ForgeCapabilities.ENERGY,source.getBlockState().getValue(BlockStateProperties.FACING)).orElseThrow(IllegalStateException::new);
        h.assertTrue(port.extractEnergy(-100,false)==0&&port.receiveEnergy(-100,false)==0&&stock(h)==5000000,"invalid negative FE packets do not create or delete energy");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void actualNetworkDeliveryAccountsForDistanceLossRateAndFullTargets(GameTestHelper h) {
        int rate=Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.get();double loss=Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.get();
        try {
            Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.set(1000);Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.set(10D);
            var initial=source(h);var output=new BlockPos(6,1,2);h.setBlock(output,JDTEBlocks.ADVANCED_FLUID_MIXER.get());
            var source=new ExtendedEnergyTransmitterBE(initial.getBlockPos(),initial.getBlockState()) {
                @Override public net.minecraft.world.phys.AABB getAABB(BlockPos ignored){return new net.minecraft.world.phys.AABB(h.absolutePos(PEER)).minmax(new net.minecraft.world.phys.AABB(h.absolutePos(output)));}
            };h.getLevel().setBlockEntity(source);source.getBlocksToCharge();peer(h).getEnergyStorage().setEnergy(5000);
            var receiver=(AdvancedFluidMixerBE)h.getBlockEntity(output);source.providePower();
            h.assertTrue(stock(h)==4000&&receiver.getEnergyStorage().getEnergyStored()==600,"rate-limited 1000 debit delivers 600, with exactly 400 distance loss");
            UpgradeHelper.getUpgradeHandler(source).insertItem(0,new ItemStack(JDTEItems.ENERGY_OVERLOAD_UPGRADE.get()),false);source.providePower();
            h.assertTrue(stock(h)==0&&receiver.getEnergyStorage().getEnergyStored()==3000,"overload consumes the remaining 4000 once: 2400 delivered, 1600 intentional loss");
            receiver.getEnergyStorage().setEnergy(receiver.getMaxEnergy());source.getEnergyStorage().setEnergy(200);source.providePower();
            h.assertTrue(stock(h)==200,"full receiver does not consume network FE");
            receiver.getEnergyStorage().setEnergy(0);source.providePower();
            h.assertTrue(stock(h)==0&&receiver.getEnergyStorage().getEnergyStored()==120,"insufficient FE is debited once with distance loss, no negative buffer");
        } finally {Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.set(rate);Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.set(loss);}
        h.succeed();
    }
    @GameTest(template="mineral_empty") public static void actualFePortsMarkChunksDirtyOnlyAfterRealMutation(GameTestHelper h) {
        for(int tier=0;tier<2;tier++) {
            h.setBlock(SOURCE,tier==0?JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get():JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get());
            var be=h.getBlockEntity(SOURCE);var face=be.getBlockState().getValue(BlockStateProperties.FACING);
            var port=be.getCapability(ForgeCapabilities.ENERGY,face).orElseThrow(IllegalStateException::new);
            var chunk=h.getLevel().getChunkAt(be.getBlockPos());chunk.setUnsaved(false);
            h.assertTrue(port.receiveEnergy(500,true)==500&&!chunk.isUnsaved(),"simulation must not dirty the chunk");
            h.assertTrue(port.receiveEnergy(500,false)==500&&chunk.isUnsaved(),"actual FE input must be eligible for disk persistence");
            chunk.setUnsaved(false);h.assertTrue(port.extractEnergy(100,false)==100&&chunk.isUnsaved(),"real extraction also marks the owner dirty");
        }h.succeed();
    }

    @GameTest(template="mineral_empty") public static void nativeOriginatedThreeNodeTrafficAndDiskNbtConserveEveryOwner(GameTestHelper h) {
        var third=new BlockPos(6,1,2);for(var pos:java.util.List.of(SOURCE,PEER,third))h.setBlock(pos,Registration.EnergyTransmitter.get());
        var nodes=new java.util.ArrayList<EnergyTransmitterBE>();
        for(var pos:java.util.List.of(SOURCE,PEER,third)){var be=(EnergyTransmitterBE)h.getBlockEntity(pos);be.setAreaSettings(6,1,1,0,0,0,false);be.getBlocksToCharge();nodes.add(be);}
        nodes.get(0).getEnergyStorage().setEnergy(1000);nodes.get(1).getEnergyStorage().setEnergy(8000);nodes.get(2).getEnergyStorage().setEnergy(11000);
        h.assertTrue(nodes.get(0).getTotalEnergyStored()==20000,"three actual native nodes discovered");
        h.assertTrue(nodes.get(0).getEnergyStorage().extractEnergy(5000,false)==5000,"native-originated pooled extraction");
        h.assertTrue(nodes.stream().allMatch(n->n.getEnergyStorage().getRealEnergyStored()>=0)&&nodes.stream().mapToLong(n->n.getEnergyStorage().getRealEnergyStored()).sum()==15000,"no native owner becomes negative");
        for(int cycle=0;cycle<32;cycle++)for(var node:nodes){int sent=node.getEnergyStorage().extractEnergy(77,false);h.assertTrue(sent==77&&node.getEnergyStorage().receiveEnergy(sent,false)==sent,"round-trip packet debits and credits once");node.balanceEnergy();}
        h.assertTrue(nodes.stream().mapToLong(n->n.getEnergyStorage().getRealEnergyStored()).sum()==15000,"repeated overlapping native pools have no duplicate FE");
        for(var node:nodes){var reloaded=(EnergyTransmitterBE)BlockEntity.loadStatic(node.getBlockPos(),node.getBlockState(),node.saveWithFullMetadata());h.getLevel().setBlockEntity(reloaded);h.assertTrue(reloaded.getEnergyStorage().getRealEnergyStored()==5000,"native peer persists its actual owned 5000 FE");}
        h.succeed();
    }

    @GameTest(template="mineral_empty") public static void extendedUltimateCapacityAndNaturalOverclockInstallRemovalHaveRealEffects(GameTestHelper h) {
        int rate=Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.get();double loss=Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.get();
        try {
            Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.set(1000);Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.set(0D);
            h.setBlock(SOURCE,JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get());h.setBlock(PEER,JDTEBlocks.ADVANCED_FLUID_MIXER.get());var old=h.getBlockEntity(SOURCE);
            var source=new ExtendedEnergyTransmitterBE(old.getBlockPos(),old.getBlockState()){@Override public net.minecraft.world.phys.AABB getAABB(BlockPos ignored){return new net.minecraft.world.phys.AABB(h.absolutePos(PEER));}};h.getLevel().setBlockEntity(source);
            var upgrades=UpgradeHelper.getUpgradeHandler(source);h.assertTrue(upgrades.insertItem(0,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false).isEmpty()&&upgrades.insertItem(1,new ItemStack(JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get()),false).isEmpty(),"ultimate cards accepted by actual eight-slot handler");
            UpgradeHelper.syncCapacities(source);h.assertTrue(source.getMaxEnergy()==Config.ENERGY_TRANSMITTER_T1_MAX_RF.get()*50&&source.getEnergyStorage().getRealMaxEnergyStored()==source.getMaxEnergy(),"ultimate capacity changes real buffer");
            source.getEnergyStorage().setEnergy(10000);
            @SuppressWarnings("unchecked") var ticker=(net.minecraft.world.level.block.entity.BlockEntityTicker<ExtendedEnergyTransmitterBE>)source.getBlockState().getTicker(h.getLevel(),source.getType());ticker.tick(h.getLevel(),source.getBlockPos(),source.getBlockState(),source);
            var receiver=(AdvancedFluidMixerBE)h.getBlockEntity(PEER);h.assertTrue(receiver.getEnergyStorage().getEnergyStored()==10000&&source.getEnergyStorage().getRealEnergyStored()==0&&source.getTickSpeed()==1,"ten actual native executions each transfer 1000 without duplicate FE");
            upgrades.extractItem(1,1,false);source.getEnergyStorage().setEnergy(2000);ticker.tick(h.getLevel(),source.getBlockPos(),source.getBlockState(),source);
            h.assertTrue(receiver.getEnergyStorage().getEnergyStored()==11000&&source.getEnergyStorage().getRealEnergyStored()==1000&&source.getTickSpeed()==50,"removal restores one native execution and normal delay");
            upgrades.extractItem(0,1,false);UpgradeHelper.syncCapacities(source);h.assertTrue(source.getEnergyStorage().getRealMaxEnergyStored()==Config.ENERGY_TRANSMITTER_T1_MAX_RF.get(),"capacity removal restores actual base buffer");
        } finally {Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.set(rate);Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.set(loss);}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void extendedAllSixFacesAndHeldRotatedPortsObeyDirection(GameTestHelper h) {
        var source=source(h);source.getEnergyStorage().setEnergy(5000);net.minecraftforge.energy.IEnergyStorage previous=null;
        for(var face:Direction.values()) {
            h.getLevel().setBlock(source.getBlockPos(),source.getBlockState().setValue(BlockStateProperties.FACING,face),Block.UPDATE_ALL);
            if(previous!=null)h.assertTrue(previous.receiveEnergy(100,false)==0&&previous.extractEnergy(100,false)==0,"held previous-facing capability is inert");
            for(var side:Direction.values())h.assertTrue(source.getCapability(ForgeCapabilities.ENERGY,side).isPresent()==(side==face),"only current input face exposes the native-compatible FE endpoint");
            h.assertTrue(!source.getCapability(ForgeCapabilities.ENERGY).isPresent(),"no unsided energy fallback");previous=source.getCapability(ForgeCapabilities.ENERGY,face).orElseThrow(IllegalStateException::new);
        }
        h.assertTrue(source.getEnergyStorage().getRealEnergyStored()==5000,"rotation never copies or loses owned FE");h.succeed();
    }

}
