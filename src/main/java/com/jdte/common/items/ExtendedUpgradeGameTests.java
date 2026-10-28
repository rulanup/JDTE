package com.jdte.common.items;

import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.blockentities.*;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("jdte") @PrefixGameTestTemplate(false)
public final class ExtendedUpgradeGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);
    private static final BlockPos OTHER = new BlockPos(4, 1, 2);
    private static UseOnContext context(GameTestHelper h, ItemStack card) {
        var player = h.makeMockSurvivalPlayer(); player.setItemInHand(InteractionHand.MAIN_HAND, card);
        return new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(h.absolutePos(POS)),Direction.UP,h.absolutePos(POS),false));
    }
    private static BaseMachineBE place(GameTestHelper h, Block block) {
        h.setBlock(POS,Blocks.AIR);h.setBlock(POS,block);return (BaseMachineBE)h.getBlockEntity(POS);
    }
    private static void fillCards(BaseMachineBE be) {
        var handler=UpgradeHelper.getUpgradeHandler(be);
        for(int i=0;i<4;i++) {
            var stack=new ItemStack(i==1?JDTEItems.AE_CRAFTING_READ_UPGRADE.get():JDTEItems.CAPACITY_UPGRADE.get());
            stack.getOrCreateTag().putString("migration_slot",Integer.toString(i));
            if(!handler.insertItem(i,stack,false).isEmpty())throw new IllegalStateException("Fixture card rejected "+be.getType()+" slot "+i);
        }
    }
    @GameTest(template="mineral_empty")
    public static void allFifteenAvailableConversionsMoveActualInventoryCardsFeAndFluids(GameTestHelper h) {
        List<Block> sources=List.of(Registration.ClickerT2.get(),Registration.BlockBreakerT2.get(),Registration.BlockPlacerT2.get(),Registration.BlockSwapperT2.get(),Registration.DropperT2.get(),Registration.SensorT2.get(),Registration.FluidCollectorT2.get(),Registration.FluidPlacerT2.get(),JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get(),JDTEBlocks.TIME_FREEZER.get(),JDTEBlocks.ADVANCED_FLUID_STABILIZER.get(),JDTEBlocks.ADVANCED_LIFE_EXTRACTOR.get(),JDTEBlocks.ADVANCED_INFUSION_MACHINE.get(),JDTEBlocks.ADVANCED_GEL_GENERATOR.get(),JDTEBlocks.ADVANCED_FLUID_MIXER.get());
        var failures=new ArrayList<String>();
        for(var block:sources) {
            try {
            var source=place(h,block);fillCards(source);
            var original=source.getMachineHandler();
            for(int slot=0;slot<original.getSlots();slot++) {var stack=new ItemStack(Items.STONE,slot+1);stack.getOrCreateTag().putString("fixture",block.getDescriptionId()+slot);original.setStackInSlot(slot,stack);}
            var inventory=original.serializeNBT();var oldUpgrades=UpgradeHelper.getUpgradeHandler(source);
            var cards=oldUpgrades.serializeNBT();
            var powered=(PoweredMachineBE)source;int fe=powered.getMaxEnergy()-1;powered.getEnergyStorage().setEnergy(fe);var oldEnergy=powered.getEnergyStorage();
            if(source instanceof FluidMachineBE fluid)fluid.getFluidTank().setFluid(new FluidStack(Registration.TIME_FLUID_SOURCE.get(),fluid.getMaxMB()-1));
            var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());var result=card.useOn(context(h,card));var target=(BaseMachineBE)h.getBlockEntity(POS);
            h.assertTrue(result==InteractionResult.SUCCESS&&card.isEmpty()&&target instanceof ExtendedUpgradeMachine&&target.getBlockState().getBlock()==ExtendedUpgradeItem.targetFor(block),"real conversion completed: "+block);
            h.assertTrue(target.getMachineHandler().serializeNBT().equals(inventory),"all physical inventory slots/tags preserved: "+block);
            var effective=UpgradeHelper.getUpgradeHandler(target);var expected=cards.copy();expected.putInt("Size",8);
            h.assertTrue(effective.getSlots()==8&&effective.serializeNBT().equals(expected),"cards occupy the effective eight-slot interface: "+block);
            h.assertTrue(JDTEAttachments.upgrades(target).serializeNBT().getList("Items",10).isEmpty(),"inactive four-slot storage is empty");
            h.assertTrue(((PoweredMachineBE)target).getEnergyStorage().getEnergyStored()==fe,"actual FE storage preserved: "+block);
            h.assertTrue(original.serializeNBT().getList("Items",10).isEmpty()&&oldUpgrades.serializeNBT().getList("Items",10).isEmpty()&&oldEnergy.getEnergyStored()==0,"detached original buffers cannot duplicate resources");
            if(target.getMachineHandler().getSlots()>0) {
                var exposed=target.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
                h.assertTrue(exposed.getSlots()==target.getMachineHandler().getSlots()&&ItemStack.matches(exposed.getStackInSlot(0),target.getMachineHandler().getStackInSlot(0)),"automation exposes the actual migrated inventory: "+block);
            }
            h.assertTrue(effective.insertItem(7,new ItemStack(JDTEItems.OVERCLOCK_UPGRADE.get()),false).isEmpty(),"new eighth slot accepts a real card: "+block);
            var saved=target.saveWithFullMetadata();var restored=(BaseMachineBE)BlockEntity.loadStatic(target.getBlockPos(),target.getBlockState(),saved);h.getLevel().setBlockEntity(restored);
            h.assertTrue(UpgradeHelper.getUpgradeHandler(restored).getSlots()==8&&UpgradeHelper.getUpgradeHandler(restored).getStackInSlot(7).is(JDTEItems.OVERCLOCK_UPGRADE.get())&&((PoweredMachineBE)restored).getEnergyStorage().getEnergyStored()==fe,"eight-slot inventory and actual FE survive a fresh BE: "+block+" FE="+((PoweredMachineBE)restored).getEnergyStorage().getEnergyStored()+" expected="+fe+" slots="+UpgradeHelper.getUpgradeHandler(restored).getSlots()+" last="+UpgradeHelper.getUpgradeHandler(restored).getStackInSlot(7)+" saved="+saved);
            } catch(RuntimeException failure) { failures.add(block+": "+failure.getMessage()); }
        }
        h.assertTrue(failures.isEmpty(),"all conversions must pass: "+failures);
        h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void nativeFilterAndEnergyFieldsMigrateIntoLiveForgeAttachments(GameTestHelper h) {
        var source=place(h,Registration.BlockPlacerT2.get());var filterable=(FilterableBE)source;
        var upgrades=UpgradeHelper.getUpgradeHandler(source);upgrades.insertItem(0,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);upgrades.insertItem(1,new ItemStack(JDTEItems.FILTER_UPGRADE.get()),false);upgrades.insertItem(2,new ItemStack(JDTEItems.FILTER_UPGRADE.get()),false);
        var filter=filterable.getFilterHandler();var data=filter.serializeNBT();data.putInt("Size",27);filter.deserializeNBT(data);var template=new ItemStack(Items.DIAMOND);template.getOrCreateTag().putString("filter","last-page");filter.setStackInSlot(26,template);
        ((PoweredMachineBE)source).getEnergyStorage().setEnergy(190000);var retained=source.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);source.getMachineHandler().setStackInSlot(0,new ItemStack(Items.COBBLESTONE,23));
        var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());card.useOn(context(h,card));var target=(ExtendedBlockPlacerBE)h.getBlockEntity(POS);
        h.assertTrue(target.getFilterHandler()==JDTEAttachments.filter(target)&&ItemStack.matches(target.getFilterHandler().getStackInSlot(26),template),"all actual filter pages reach the active Forge filter handler");
        var energy=target.getCapability(ForgeCapabilities.ENERGY).orElseThrow(IllegalStateException::new);h.assertTrue(energy.extractEnergy(123,false)==123&&target.getEnergyStorage().getEnergyStored()==189877,"live energy capability spends migrated FE");
        h.assertTrue(retained.extractItem(0,64,false).isEmpty()&&target.getMachineHandler().getStackInSlot(0).getCount()==23,"retained original item capability cannot take duplicated contents");h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void deniedAndUnexpectedReplacementsRestoreAllStateAndKeepCard(GameTestHelper h) {
        for(boolean corrupt:List.of(false,true)) {
            var source=place(h,Registration.BlockPlacerT2.get());fillCards(source);source.getMachineHandler().setStackInSlot(0,new ItemStack(Items.DIAMOND,47));((PoweredMachineBE)source).getEnergyStorage().setEnergy(321123);
            var before=source.saveWithFullMetadata();var retained=source.getMachineHandler();
            MachineTierConversion.Replacement failing=(level,pos,state)->{if(corrupt)level.setBlock(pos,Blocks.STONE.defaultBlockState(),Block.UPDATE_ALL);return corrupt;};
            var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());var result=MachineTierConversion.convert(context(h,card),JDTEBlocks.EXTENDED_BLOCK_PLACER.get(),failing);var restored=(BaseMachineBE)h.getBlockEntity(POS);
            h.assertTrue(result==InteractionResult.FAIL&&card.getCount()==1&&restored.saveWithFullMetadata().equals(before),"failed replacement restores exact source NBT and does not spend the card, corrupt="+corrupt);
            h.assertTrue(retained.getStackInSlot(0).isEmpty()&&restored.getMachineHandler().getStackInSlot(0).getCount()==47,"rollback moves ownership to one restored inventory");
        }h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void occupiedInactiveUpgradeStorageRejectsConversionWithoutErasingIt(GameTestHelper h) {
        var source=place(h,Registration.ClickerT2.get());fillCards(source);
        JDTEAttachments.extendedUpgrades(source).setStackInSlot(7,new ItemStack(JDTEItems.AE_OUTPUT_UPGRADE.get()));var before=source.saveWithFullMetadata();
        var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());h.assertTrue(card.useOn(context(h,card))==InteractionResult.FAIL&&card.getCount()==1&&h.getBlockEntity(POS)==source&&source.saveWithFullMetadata().equals(before),"unexpected occupied inactive handler is rejected intact before replacement");h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void facingAndClickerAuxiliaryFluidSurviveConversion(GameTestHelper h) {
        var source=place(h,Registration.ClickerT2.get());var upgrades=UpgradeHelper.getUpgradeHandler(source);upgrades.insertItem(0,new ItemStack(JDTEItems.FLUID_STORAGE_UPGRADE.get()),false);upgrades.insertItem(3,new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()),false);
        JDTEAttachments.clickerFluidTank(source).setFluid(new FluidStack(Registration.TIME_FLUID_SOURCE.get(),12345));
        Direction facing=source.getBlockState().getValue(BlockStateProperties.FACING);var retained=JDTEAttachments.clickerFluidTank(source);
        var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());h.assertTrue(card.useOn(context(h,card))==InteractionResult.SUCCESS,"clicker upgrade completes");var target=(BaseMachineBE)h.getBlockEntity(POS);
        h.assertTrue(target.getBlockState().getValue(BlockStateProperties.FACING)==facing&&JDTEAttachments.clickerFluidTank(target).getFluidAmount()==12345&&retained.isEmpty(),"facing and actual auxiliary tank migrate without a second usable source");
        h.assertTrue(MachineTierConversion.targetState(Blocks.STONE.defaultBlockState(),Blocks.DIRT.defaultBlockState())!=null&&MachineTierConversion.targetState(Blocks.STONE.defaultBlockState(),JDTEBlocks.EXTENDED_CLICKER.get().defaultBlockState())==null,"compatible absence of facing works; mismatched property shapes reject");h.succeed();
    }
    @GameTest(template="mineral_empty")
    public static void cancelledForgePlacementRestoresInventoryFeAndCardExactlyOnce(GameTestHelper h) {
        for(var sourceBlock:List.<Block>of(Registration.BlockPlacerT2.get(),Registration.DropperT2.get())) {
            var source=place(h,sourceBlock);fillCards(source);
            source.getMachineHandler().setStackInSlot(0,new ItemStack(Items.DIAMOND,37));
            ((PoweredMachineBE)source).getEnergyStorage().setEnergy(321123);
            var retained=source.getMachineHandler();var oldEnergy=((PoweredMachineBE)source).getEnergyStorage();
            var before=source.saveWithFullMetadata();var called=new int[1];
            java.util.function.Consumer<net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent> listener=event->{
                if(event.getLevel()==h.getLevel()&&event.getPos().equals(source.getBlockPos())&&event.getPlacedBlock().getBlock()==ExtendedUpgradeItem.targetFor(sourceBlock)) {called[0]++;event.setCanceled(true);}
            };
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL,false,net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent.class,listener);
            try {
                var card=new ItemStack(JDTEItems.EXTENDED_UPGRADE.get());var result=card.useOn(context(h,card));
                var restored=(BaseMachineBE)h.getBlockEntity(POS);
                h.assertTrue(called[0]==1&&result==InteractionResult.FAIL&&card.getCount()==1,"exactly one real protection event can deny conversion");
                h.assertTrue(restored.getBlockState().getBlock()==sourceBlock&&restored.saveWithFullMetadata().equals(before)&&((PoweredMachineBE)restored).getEnergyStorage().getEnergyStored()==321123,"denied conversion restores actual source inventory, cards, tags and FE");
                h.assertTrue(retained.getStackInSlot(0).isEmpty()&&oldEnergy.getEnergyStored()==0,"detached endpoints contain no second source inventory or FE");
            } finally {net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener);}
        }
        h.succeed();
    }

}
