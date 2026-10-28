package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.EnergyTransmitterBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.containers.*;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class EnergyTransmitterGameTests {
    private static final BlockPos POS=new BlockPos(2,1,2),TARGET=new BlockPos(4,1,2),SECOND=new BlockPos(6,1,2);
    private static AdvancedEnergyTransmitterBE advanced(GameTestHelper h) {
        h.setBlock(POS,JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get());
        var old=h.getBlockEntity(POS);
        var be=new AdvancedEnergyTransmitterBE(old.getBlockPos(),old.getBlockState()) {
            @Override public AABB getAABB(BlockPos ignored) { return new AABB(h.absolutePos(TARGET)).minmax(new AABB(h.absolutePos(SECOND))); }
        };
        h.getLevel().setBlockEntity(be);return be;
    }
    private static AdvancedFluidMixerBE receiver(GameTestHelper h,BlockPos pos) {
        h.setBlock(pos,JDTEBlocks.ADVANCED_FLUID_MIXER.get());return (AdvancedFluidMixerBE)h.getBlockEntity(pos);
    }
    private static void card(BaseMachineBE be,net.minecraft.world.item.Item item,int slot) {
        if(!UpgradeHelper.getUpgradeHandler(be).insertItem(slot,new ItemStack(item),false).isEmpty())throw new IllegalStateException("card rejected "+item);
    }
    private static void tick(AdvancedEnergyTransmitterBE be,int n) { for(int i=0;i<n;i++)be.tickServer(); }
    @GameTest(template="mineral_empty") public static void advancedDefaultCapacityAllSixSidedPortsAndRetainedRotationGuards(GameTestHelper h) {
        var be=advanced(h);Direction facing=be.getBlockState().getValue(BlockStateProperties.FACING);
        h.assertTrue(be.getMaxEnergy()==2000000000&&UpgradeHelper.getUpgradeHandler(be).getSlots()==8,"source 2B buffer and eight upgrade slots");
        for(Direction side:Direction.values())h.assertTrue(be.getCapability(ForgeCapabilities.ENERGY,side).isPresent()==(side==facing),"only facing face exposes FE: "+side);
        h.assertTrue(!be.getCapability(ForgeCapabilities.ENERGY).isPresent(),"unsided FE access is absent as in source");
        var port=be.getCapability(ForgeCapabilities.ENERGY,facing).orElseThrow(IllegalStateException::new);
        h.assertTrue(port.receiveEnergy(1000,true)==1000&&be.getEnergyStorage().getEnergyStored()==0&&port.receiveEnergy(1000,false)==1000,"simulate is inert and real input charges owned buffer");
        h.getLevel().setBlock(be.getBlockPos(),be.getBlockState().setValue(BlockStateProperties.FACING,facing.getOpposite()),Block.UPDATE_ALL);
        h.assertTrue(port.receiveEnergy(1000,false)==0&&port.extractEnergy(1000,false)==0&&be.getEnergyStorage().getEnergyStored()==1000,"retained old face cannot transfer after rotation");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void advancedRealTransferUsesReceiverSideFallbackAndConservesFe(GameTestHelper h) {
        var be=advanced(h);h.setBlock(TARGET,JDTEBlocks.ADVANCED_FLUID_MIXER.get());var old=h.getBlockEntity(TARGET);
        var target=new AdvancedFluidMixerBE(old.getBlockPos(),old.getBlockState()) {
            @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap,Direction side) {
                if(cap==ForgeCapabilities.ENERGY&&side!=Direction.DOWN)return net.minecraftforge.common.util.LazyOptional.empty();return super.getCapability(cap,side);
            }
        };h.getLevel().setBlockEntity(target);be.getEnergyStorage().setEnergy(8765);tick(be,1);
        h.assertTrue(be.getTargetCount()==1&&target.getEnergyStorage().getEnergyStored()==8765&&be.getEnergyStorage().getEnergyStored()==0&&be.getLastTransferred()==8765,"real world receiver side fallback spends exactly the available FE");
        tick(be,3);h.assertTrue(target.getEnergyStorage().getEnergyStored()==8765,"empty source cannot create further FE");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void overloadRemovesBudgetsAndRemovalRestoresActualLimits(GameTestHelper h) {
        int budget=JDTEConfig.COMMON.advancedEnergyTransmitterTransferBudgetPerTick.get(),targetCap=JDTEConfig.COMMON.advancedEnergyTransmitterMaxTransferPerTarget.get(),attempts=JDTEConfig.COMMON.advancedEnergyTransmitterMaxTargetsPerTick.get();
        try {
            JDTEConfig.COMMON.advancedEnergyTransmitterTransferBudgetPerTick.set(100);JDTEConfig.COMMON.advancedEnergyTransmitterMaxTransferPerTarget.set(50);JDTEConfig.COMMON.advancedEnergyTransmitterMaxTargetsPerTick.set(1);
            var be=advanced(h);var a=receiver(h,TARGET);var b=receiver(h,SECOND);be.getEnergyStorage().setEnergy(10000);tick(be,1);
            h.assertTrue(a.getEnergyStorage().getEnergyStored()+b.getEnergyStorage().getEnergyStored()==50&&be.getEnergyStorage().getEnergyStored()==9950,"normal cap bounds one real target");
            card(be,JDTEItems.ENERGY_OVERLOAD_UPGRADE.get(),0);tick(be,1);
            h.assertTrue(a.getEnergyStorage().getEnergyStored()+b.getEnergyStorage().getEnergyStored()==10000&&be.getEnergyStorage().getEnergyStored()==0,"overload delivers available FE without target/operation limit or creation");
            UpgradeHelper.getUpgradeHandler(be).extractItem(0,1,false);be.getEnergyStorage().setEnergy(1000);tick(be,1);
            h.assertTrue(be.getEnergyStorage().getEnergyStored()==950,"removal restores configured cap");
        } finally {JDTEConfig.COMMON.advancedEnergyTransmitterTransferBudgetPerTick.set(budget);JDTEConfig.COMMON.advancedEnergyTransmitterMaxTransferPerTarget.set(targetCap);JDTEConfig.COMMON.advancedEnergyTransmitterMaxTargetsPerTick.set(attempts);}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void fullAndRemovedTargetsDoNotConsumeEnergyItems(GameTestHelper h) {
        var be=advanced(h);var target=receiver(h,TARGET);target.getEnergyStorage().setEnergy(target.getMaxEnergy());
        var battery=new ItemStack(Registration.EclipseAlloyPickaxe.get());var itemFe=battery.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);itemFe.receiveEnergy(9000,false);be.getMachineHandler().setStackInSlot(0,battery);
        tick(be,1);h.assertTrue(itemFe.getEnergyStored()==9000&&be.getEnergyStorage().getEnergyStored()==0,"full target does not prepay or drain battery");
        target.getEnergyStorage().setEnergy(0);tick(be,1);h.assertTrue(itemFe.getEnergyStored()==0&&target.getEnergyStorage().getEnergyStored()==9000,"real item source supplies exact demand");
        h.setBlock(TARGET,Blocks.AIR);itemFe.receiveEnergy(1000,false);tick(be,1);h.assertTrue(itemFe.getEnergyStored()==1000,"removed cached position cannot drain the energy item");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void filtersEmptyWhitelistAndChangedBlockStatesUseCurrentRules(GameTestHelper h) {
        var be=advanced(h);var a=receiver(h,TARGET);be.getEnergyStorage().setEnergy(1000);be.getFilterData().allowlist=true;tick(be,1);
        h.assertTrue(be.getTargetCount()==0&&a.getEnergyStorage().getEnergyStored()==0,"empty whitelist blocks all targets");
        be.getFilterHandler().setStackInSlot(0,new ItemStack(JDTEItems.ADVANCED_FLUID_MIXER.get()));tick(be,1);
        h.assertTrue(a.getEnergyStorage().getEnergyStored()==1000,"matching block whitelist actually supplies FE");
        h.setBlock(TARGET,JDTEBlocks.EXTENDED_FLUID_MIXER.get());be.getEnergyStorage().setEnergy(500);tick(be,1);
        h.assertTrue(((FluidMixerBE)h.getBlockEntity(TARGET)).getEnergyStorage().getEnergyStored()==0&&be.getEnergyStorage().getEnergyStored()==500,"replacement with a nonmatching block cannot reuse cached eligibility");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void inputNeighborIsNotChargedAndTargetAttemptsAdvancePastFullRecipients(GameTestHelper h) {
        int attempts=JDTEConfig.COMMON.advancedEnergyTransmitterMaxTargetsPerTick.get();try {
            JDTEConfig.COMMON.advancedEnergyTransmitterMaxTargetsPerTick.set(1);var be=advanced(h);var a=receiver(h,TARGET);var b=receiver(h,SECOND);
            a.getEnergyStorage().setEnergy(a.getMaxEnergy());be.getEnergyStorage().setEnergy(700);tick(be,1);h.assertTrue(be.getLastAttemptedTargets()==1&&b.getEnergyStorage().getEnergyStored()==0,"full target counts toward bounded attempt budget");tick(be,1);
            h.assertTrue(b.getEnergyStorage().getEnergyStored()==700,"cursor advances past full target without starvation");
        }finally{JDTEConfig.COMMON.advancedEnergyTransmitterMaxTargetsPerTick.set(attempts);}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void savedLargeBufferReserveBindingAndMenuIntegersRemainExact(GameTestHelper h) {
        var be=advanced(h);be.getEnergyStorage().setEnergy(1900000000);be.setShowParticles(true);var id=java.util.UUID.randomUUID();be.applyCopiedPlayerBinding(Optional.of(id),"offline-bound-player");card(be,JDTEItems.ENERGY_OVERLOAD_UPGRADE.get(),7);
        var tag=be.saveWithFullMetadata();tag.putLong("networkEnergyReserve",5000000000L);be.load(tag);var saved=be.saveWithFullMetadata();
        var restored=(AdvancedEnergyTransmitterBE)BlockEntity.loadStatic(be.getBlockPos(),be.getBlockState(),saved);h.getLevel().setBlockEntity(restored);
        h.assertTrue(restored.getEnergyStorage().getEnergyStored()==1900000000&&restored.saveWithFullMetadata().getLong("networkEnergyReserve")==5000000000L&&restored.isShowingParticles()&&restored.getBoundPlayerIdForCopy().equals(Optional.of(id))&&UpgradeHelper.hasEnergyOverloadUpgrade(restored),"real large FE, long reserve, binding and eighth-slot card persist");
        var logical=new SimpleContainerData(8);var wire=new SplitIntContainerData(logical);restored.getTransmitterData().set(3,268435456);var sourceWire=new SplitIntContainerData(restored.getTransmitterData());for(int i=0;i<sourceWire.getCount();i++)wire.set(i,(short)sourceWire.get(i));
        h.assertTrue(logical.get(3)==268435456&&logical.get(4)==1&&logical.get(6)==1,"signed-short menu transport preserves 32-bit counters and flags");
        var actor=h.makeMockSurvivalPlayer();actor.setPos(Vec3.atCenterOf(restored.getBlockPos()));var menu=new AdvancedEnergyTransmitterContainer(7,actor.getInventory(),restored.getBlockPos());h.assertTrue(menu.getLastTransferred()==268435456&&menu.hasBoundPlayer()&&menu.stillValid(actor),"actual menu exposes counters and binding");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void staleEnergyAndInventoryPortsCannotModifyAReplacement(GameTestHelper h) {
        var be=advanced(h);be.getEnergyStorage().setEnergy(7000);var face=be.getBlockState().getValue(BlockStateProperties.FACING);var energy=be.getCapability(ForgeCapabilities.ENERGY,face).orElseThrow(IllegalStateException::new);var items=be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        var restored=(AdvancedEnergyTransmitterBE)BlockEntity.loadStatic(be.getBlockPos(),be.getBlockState(),be.saveWithFullMetadata());h.getLevel().setBlockEntity(restored);be.clearRemoved();
        h.assertTrue(energy.receiveEnergy(500,false)==0&&energy.extractEnergy(500,false)==0&&items.insertItem(0,new ItemStack(Registration.EclipseAlloyPickaxe.get()),false).getCount()==1,"detached ports cannot write even after removed flag is cleared");
        be.tickServer();be.accumulateAcceleratedTicks(40);be.flushAcceleratedTicks();h.assertTrue(restored.getEnergyStorage().getEnergyStored()==7000,"old ticker/accelerated reference cannot send saved FE twice");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void extendedNativeRealDeliveryOverloadAndMissingTargetAreSafe(GameTestHelper h) {
        h.setBlock(POS,JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get());var original=(ExtendedEnergyTransmitterBE)h.getBlockEntity(POS);var target=receiver(h,TARGET);
        var be=new ExtendedEnergyTransmitterBE(original.getBlockPos(),original.getBlockState()){@Override public AABB getAABB(BlockPos ignored){return new AABB(target.getBlockPos());}};h.getLevel().setBlockEntity(be);
        int rate=com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.get();double loss=com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.get();try {
            com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.set(1000);com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.set(0D);
            be.getEnergyStorage().setEnergy(9000);be.getBlocksToCharge();be.providePower();h.assertTrue(target.getEnergyStorage().getEnergyStored()==1000&&be.getEnergyStorage().getRealEnergyStored()==8000,"extended native delivers with its configured cap");
            card(be,JDTEItems.ENERGY_OVERLOAD_UPGRADE.get(),7);be.providePower();h.assertTrue(target.getEnergyStorage().getEnergyStored()==9000&&be.getEnergyStorage().getRealEnergyStored()==0,"eighth-slot overload lifts the real native rate without creating FE");
            h.setBlock(TARGET,Blocks.AIR);h.assertTrue(be.getTransmitterHandler(h.absolutePos(TARGET))==null&&be.getTransmitterHandler(be.getBlockPos())==be.getEnergyStorage(),"self is the local native storage; missing cache target returns null safely");
        }finally{com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_RF_PER_TICK.set(rate);com.direwolf20.justdirethings.setup.Config.ENERGY_TRANSMITTER_T1_LOSS_PER_BLOCK.set(loss);}h.succeed();
    }
    @GameTest(template="mineral_empty") public static void nativeToExtendedConversionMovesOnlyLocalEnergyNotTheNetworkTotal(GameTestHelper h) {
        h.setBlock(POS,Registration.EnergyTransmitter.get());h.setBlock(TARGET,Registration.EnergyTransmitter.get());var source=(EnergyTransmitterBE)h.getBlockEntity(POS);var peer=(EnergyTransmitterBE)h.getBlockEntity(TARGET);
        source.setAreaSettings(4,1,1,1,0,0,false);source.getEnergyStorage().setEnergy(12000);peer.getEnergyStorage().setEnergy(34000);source.getBlocksToCharge();
        int peerBefore=peer.getEnergyStorage().getRealEnergyStored(),sourceBefore=source.getEnergyStorage().getRealEnergyStored();card(source,JDTEItems.CAPACITY_UPGRADE.get(),3);
        var actor=h.makeMockSurvivalPlayer();var upgrade=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());actor.setItemInHand(InteractionHand.MAIN_HAND,upgrade);
        var result=upgrade.useOn(new UseOnContext(actor,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(source.getBlockPos()),Direction.UP,source.getBlockPos(),false)));
        h.assertTrue(result==InteractionResult.SUCCESS&&h.getBlockEntity(POS) instanceof ExtendedEnergyTransmitterBE,"actual native tier conversion completes");var next=(ExtendedEnergyTransmitterBE)h.getBlockEntity(POS);
        h.assertTrue(next.getEnergyStorage().getRealEnergyStored()==sourceBefore&&peer.getEnergyStorage().getRealEnergyStored()==peerBefore&&source.getEnergyStorage().getRealEnergyStored()==0,"conversion moves only source local FE and cannot duplicate peer network stock");
        h.assertTrue(UpgradeHelper.getUpgradeHandler(next).getSlots()==8&&UpgradeHelper.getUpgradeHandler(next).getStackInSlot(3).is(JDTEItems.CAPACITY_UPGRADE.get()),"native cards occupy effective extended slots");h.succeed();
    }
    @GameTest(template="mineral_empty") public static void ultimateUpgradesChangeActualCapacityBudgetAndNaturalTickerExecution(GameTestHelper h) {
        int delay=JDTEConfig.COMMON.advancedEnergyTransmitterBaseTickDelay.get(),budget=JDTEConfig.COMMON.advancedEnergyTransmitterTransferBudgetPerTick.get();
        try {
            JDTEConfig.COMMON.advancedEnergyTransmitterBaseTickDelay.set(20);JDTEConfig.COMMON.advancedEnergyTransmitterTransferBudgetPerTick.set(10);
            var be=advanced(h);var target=receiver(h,TARGET);card(be,JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get(),0);card(be,JDTEItems.ULTIMATE_OVERCLOCK_UPGRADE.get(),1);
            h.assertTrue(be.getMaxEnergy()==Integer.MAX_VALUE&&be.getEnergyStorage().getMaxEnergyStored()==Integer.MAX_VALUE,"ultimate capacity really saturates the owned FE buffer");
            be.getEnergyStorage().setEnergy(10000);
            @SuppressWarnings("unchecked") var ticker=(net.minecraft.world.level.block.entity.BlockEntityTicker<AdvancedEnergyTransmitterBE>)be.getBlockState().getTicker(h.getLevel(),be.getType());
            ticker.tick(h.getLevel(),be.getBlockPos(),be.getBlockState(),be);
            h.assertTrue(target.getEnergyStorage().getEnergyStored()==800&&be.getEnergyStorage().getEnergyStored()==9200&&be.getTickSpeed()==1,"source ultimate uses ten real executions, eightfold per-operation budget and locked delay");
        }finally{JDTEConfig.COMMON.advancedEnergyTransmitterBaseTickDelay.set(delay);JDTEConfig.COMMON.advancedEnergyTransmitterTransferBudgetPerTick.set(budget);}h.succeed();
    }

    @GameTest(template="mineral_empty") public static void explicitChunkUnloadRejectsRetainedPortsAndFreshReloadRestoresState(GameTestHelper h) {
        for (int tier=0;tier<2;tier++) {
            h.setBlock(POS,tier==0?JDTEBlocks.ADVANCED_ENERGY_TRANSMITTER.get():JDTEBlocks.EXTENDED_ENERGY_TRANSMITTER.get());
            var be=(BaseMachineBE)h.getBlockEntity(POS);var face=be.getBlockState().getValue(BlockStateProperties.FACING);
            var energy=be.getCapability(ForgeCapabilities.ENERGY,face).orElseThrow(IllegalStateException::new);
            var inventory=be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
            if(be instanceof ExtendedEnergyTransmitterBE e)e.getBlocksToCharge();
            h.assertTrue(energy.receiveEnergy(5000,false)==5000,"seed actual sided port");
            var saved=be.saveWithFullMetadata();
            be.onChunkUnloaded();
            h.assertTrue(energy.receiveEnergy(100,false)==0&&energy.extractEnergy(100,false)==0
                    &&inventory.insertItem(0,new ItemStack(Registration.EclipseAlloyPickaxe.get()),false).getCount()==1,
                    "explicit unload rejects retained interfaces even before world unload completes");
            if(be instanceof AdvancedEnergyTransmitterBE a){a.tickServer();a.accumulateAcceleratedTicks(40);a.flushAcceleratedTicks();}
            else ((ExtendedEnergyTransmitterBE)be).tickServer();
            var restored=BlockEntity.loadStatic(be.getBlockPos(),be.getBlockState(),saved);h.getLevel().setBlockEntity(restored);
            if(restored instanceof ExtendedEnergyTransmitterBE e)e.getBlocksToCharge();
            var fresh=restored.getCapability(ForgeCapabilities.ENERGY,face).orElseThrow(IllegalStateException::new);
            h.assertTrue(fresh.getEnergyStored()==5000&&fresh.extractEnergy(123,false)==123,"fresh loaded BE restores owned energy exactly once, tier="+tier+", FE="+fresh.getEnergyStored());
        }
        h.succeed();
    }

}
