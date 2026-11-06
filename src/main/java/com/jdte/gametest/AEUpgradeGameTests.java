package com.jdte.gametest;

import appeng.api.config.Actionable;
import appeng.api.features.GridLinkables;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.*;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.core.definitions.*;
import com.jdte.common.blockentities.*;
import com.jdte.common.integrations.ae2.*;
import com.jdte.common.items.*;
import com.jdte.common.manager.AEOutputManager;
import com.jdte.common.recipes.AEExtractionSmithingRecipe;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.*;
import java.util.*;

@PrefixGameTestTemplate(false)
public final class AEUpgradeGameTests {
    static final BlockPos AP=new BlockPos(3,1,2), DRIVE=new BlockPos(3,1,4), MACHINE=new BlockPos(1,1,2);
    static GlobalPos target(GameTestHelper h){return GlobalPos.of(h.getLevel().dimension(),h.absolutePos(AP));}
    private static ItemStack linked(GameTestHelper h,Item item){
        var stack=new ItemStack(item);var handler=GridLinkables.get(item);
        h.assertTrue(handler!=null&&handler.canLink(stack),"real AE2 GridLinkables registration");
        handler.link(stack,target(h));return stack;
    }
    static void grid(GameTestHelper h){
        h.setBlock(AP,AEBlocks.WIRELESS_ACCESS_POINT.block());
        h.setBlock(new BlockPos(3,1,3),AEBlocks.CREATIVE_ENERGY_CELL.block());
        h.setBlock(DRIVE,AEBlocks.DRIVE.block().getOrientationStrategy().setFacing(AEBlocks.DRIVE.block().defaultBlockState(),Direction.SOUTH));
        var drive=(DriveBlockEntity)h.getBlockEntity(DRIVE);
        drive.getInternalInventory().insertItem(0,AEItems.ITEM_CELL_256K.stack(),false);
        drive.getInternalInventory().insertItem(1,AEItems.FLUID_CELL_256K.stack(),false);
    }
    static WirelessAccessPointBlockEntity ap(GameTestHelper h){
        var point=(WirelessAccessPointBlockEntity)h.getBlockEntity(AP);
        h.assertTrue(point.isActive()&&point.getGrid()!=null,"real powered/channelled AE2 wireless access point");
        var drive=(DriveBlockEntity)h.getBlockEntity(DRIVE);
        h.assertTrue(drive.getMainNode().isOnline(),"drive online: powered="+drive.getMainNode().isPowered()+", facing="+appeng.api.orientation.BlockOrientation.get(drive.getBlockState()).getSide(appeng.api.orientation.RelativeSide.FRONT)+", sameGrid="+(drive.getMainNode().getGrid()==point.getGrid()));
        h.assertTrue(drive.getMainNode().getGrid()==point.getGrid(),"drive belongs to wireless grid");
        h.assertTrue(drive.getCellInventory(0)!=null&&drive.getCellInventory(1)!=null,"drive has usable item and fluid cells");return point;
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty") public static void wirelessBindingNbtAndOutputExclusion(GameTestHelper h){
        h.setBlock(MACHINE,JDTEBlocks.MINERAL_EXTRACTOR.get());var machine=(MineralExtractorBE)h.getBlockEntity(MACHINE);
        var card=linked(h,JDTEItems.AE_OUTPUT_UPGRADE.get());
        h.assertTrue(target(h).equals(AEWirelessLinkData.get(ItemStack.of(card.save(new net.minecraft.nbt.CompoundTag())))),"wireless dimension/position round trip");
        var upgrades=UpgradeHelper.getUpgradeHandler(machine);upgrades.insertItem(0,card,false);
        h.assertTrue(!upgrades.isItemValid(1,new ItemStack(JDTEItems.ADVANCED_AE_OUTPUT_UPGRADE.get())),"two output tiers cannot coexist");
        GridLinkables.get(card.getItem()).unlink(card);h.assertTrue(AEWirelessLinkData.get(card)==null,"AE2 unlink clears binding");h.succeed();
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty") public static void unboundCraftingReadFreezesMineralWorkAndResources(GameTestHelper h){
        h.setBlock(MACHINE,JDTEBlocks.MINERAL_EXTRACTOR.get());var machine=(MineralExtractorBE)h.getBlockEntity(MACHINE);
        machine.getEnergyStorage().receiveEnergy(100000,false);UpgradeHelper.getUpgradeHandler(machine).insertItem(0,new ItemStack(JDTEItems.AE_CRAFTING_READ_UPGRADE.get()),false);
        var before=machine.saveWithFullMetadata();for(int i=0;i<8;i++)machine.tickServer();machine.accumulateAcceleratedTicks(27);machine.flushAcceleratedTicks();
        var after=machine.saveWithFullMetadata();
        h.assertTrue(before.getLong("pendingBaseWork")==after.getLong("pendingBaseWork")&&before.getLong("pendingAcceleratedWork")==after.getLong("pendingAcceleratedWork"),"read card freezes accumulated production");
        h.assertTrue(machine.getEnergyStorage().getEnergyStored()==100000,"paused mineral consumes no FE");h.assertTrue(!UpgradeHelper.mayRunWithUpgrades(machine),"unbound is fail closed");h.succeed();
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=240) public static void realAeGridAcceptsOversizedMineralOutputAndRetainsSurveys(GameTestHelper h){ actualMineralOutput(h,JDTEItems.AE_OUTPUT_UPGRADE.get()); }
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=240) public static void advancedAeGridAcceptsOversizedMineralOutput(GameTestHelper h){ actualMineralOutput(h,JDTEItems.ADVANCED_AE_OUTPUT_UPGRADE.get()); }
    private static void actualMineralOutput(GameTestHelper h,Item outputCard){
        grid(h);h.setBlock(MACHINE,JDTEBlocks.MINERAL_EXTRACTOR.get());var machine=(MineralExtractorBE)h.getBlockEntity(MACHINE);
        var card=linked(h,outputCard);
        h.runAfterDelay(80,()->{
            var point=ap(h);UpgradeHelper.getUpgradeHandler(machine).insertItem(0,card,false);
            UpgradeHelper.getUpgradeHandler(machine).insertItem(1,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);
            machine.getMachineHandler().setStackInSlot(machine.outputStartSlot(),new ItemStack(Items.IRON_INGOT,1_500_000));
            var survey=new ItemStack(JDTEItems.MINERAL_SURVEY.get());machine.getMachineHandler().setStackInSlot(0,survey);
            h.assertTrue(AEOutputManager.flush(h.getLevel(),machine),"actual network output moves");
            h.assertTrue(machine.getMachineHandler().getStackInSlot(machine.outputStartSlot()).isEmpty(),"oversized slot drained once");
            long total=point.getGrid().getStorageService().getInventory().extract(AEItemKey.of(Items.IRON_INGOT),Long.MAX_VALUE,Actionable.SIMULATE,IActionSource.empty());
            h.assertTrue(total==1_500_000,"long ME accounting conserves all items");h.assertTrue(machine.getMachineHandler().getStackInSlot(0).is(JDTEItems.MINERAL_SURVEY.get()),"survey input excluded");
            h.assertTrue(!AE2CraftingReadNetwork.hasActiveCraftingTask(h.getLevel(),linked(h,JDTEItems.AE_CRAFTING_READ_UPGRADE.get())),"idle real grid does not enable crafting read");
            machine.getMachineHandler().setStackInSlot(machine.outputStartSlot(),new ItemStack(Items.GOLD_INGOT,13));
            var replacement=new MineralExtractorBE(machine.getBlockPos(),machine.getBlockState());replacement.load(machine.saveWithFullMetadata());
            h.getLevel().setBlockEntity(replacement);machine.clearRemoved();
            com.jdte.common.autoioconfig.AutoIoTransferHelper.tick(machine);
            h.assertTrue(!com.jdte.common.autoioconfig.AutoIoTransferHelper.flushEventDrivenGreenhouseOutput(machine,new int[]{machine.outputStartSlot()}).moved(),"detached auto output rejected");
            h.assertTrue(!AEOutputManager.flush(h.getLevel(),machine),"detached machine cannot export even when its removed flag is cleared");
            h.assertTrue(AEOutputManager.flush(h.getLevel(),replacement),"replacement machine exports the one live inventory");
            h.assertTrue(point.getGrid().getStorageService().getInventory().extract(AEItemKey.of(Items.GOLD_INGOT),100,Actionable.SIMULATE,IActionSource.empty())==13,"replacement export cannot duplicate items");h.succeed();
        });
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=240) public static void conversionRunsBeforeActualMeOutput(GameTestHelper h){
        grid(h);h.setBlock(MACHINE,JDTEBlocks.GREENHOUSE.get());var machine=(GreenhouseBE)h.getBlockEntity(MACHINE);
        var ingredients=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));ingredients.set(0,new ItemStack(Items.OAK_PLANKS));
        var conversion=new ItemStack(JDTEItems.ESSENCE_CONVERSION_UPGRADE.get());EssenceConversionUpgradeItem.setPatterns(conversion,List.of(CraftingPatternData.capture(h.getLevel(),ingredients).orElseThrow()));
        UpgradeHelper.getUpgradeHandler(machine).insertItem(0,conversion,false);
        var card=linked(h,JDTEItems.AE_OUTPUT_UPGRADE.get());
        h.runAfterDelay(80,()->{
            var point=ap(h);UpgradeHelper.getUpgradeHandler(machine).insertItem(1,card,false);
            machine.getMachineHandler().setStackInSlot(GreenhouseBE.OUTPUT_START_SLOT,new ItemStack(Items.OAK_PLANKS,511));
            h.assertTrue(AEOutputManager.flush(h.getLevel(),machine),"converted output moved to ME");var storage=point.getGrid().getStorageService().getInventory();
            h.assertTrue(storage.extract(AEItemKey.of(Items.OAK_BUTTON),1000,Actionable.SIMULATE,IActionSource.empty())==511,"ME gets converted products");
            h.assertTrue(storage.extract(AEItemKey.of(Items.OAK_PLANKS),1000,Actionable.SIMULATE,IActionSource.empty())==0,"no unconverted input leaks");h.succeed();
        });
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty") public static void extractionSmithingPreservesTankAndBinding(GameTestHelper h){
        var tank=new ItemStack(JDTEItems.BIG_FLUID_TANK.get());tank.setHoverName(net.minecraft.network.chat.Component.literal("Preserved"));
        var capability=tank.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(()->new IllegalStateException("tank"));
        capability.fill(new FluidStack(Fluids.WATER,1234),IFluidHandler.FluidAction.EXECUTE);
        var input=new SimpleContainer(linked(h,JDTEItems.AE_EXTRACTION_UPGRADE.get()),tank,ItemStack.EMPTY);
        var recipe=new AEExtractionSmithingRecipe(com.jdte.JDTE.id("ae_extraction_upgrade_apply"));
        h.assertTrue(recipe.matches(input,h.getLevel()),"bound template and tank match");var result=recipe.assemble(input,h.getLevel().registryAccess());
        h.assertTrue(AEWirelessLinkData.extractionEnabled(result)&&target(h).equals(AEWirelessLinkData.get(result)),"enabled link saved on equipment");
        h.assertTrue(result.getHoverName().getString().equals("Preserved")&&result.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(()->new IllegalStateException("tank")).getFluidInTank(0).getAmount()==1234,"existing item state retained");
        input.setItem(1,result);h.assertTrue(!recipe.matches(input,h.getLevel()),"cannot apply twice");h.succeed();
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=240) public static void extractionRefillsExistingFluidFromRealMeStorage(GameTestHelper h){
        grid(h);h.runAfterDelay(80,()->{
            var point=ap(h);var storage=point.getGrid().getStorageService().getInventory();var action=IActionSource.empty();
            h.assertTrue(storage.insert(AEFluidKey.of(Fluids.WATER),8000,Actionable.MODULATE,action)==8000,"seed real fluid cell");
            var tank=new ItemStack(JDTEItems.BIG_FLUID_TANK.get());var cap=tank.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(()->new IllegalStateException("tank"));
            cap.fill(new FluidStack(Fluids.WATER,1),IFluidHandler.FluidAction.EXECUTE);AEWirelessLinkData.set(tank,target(h));AEWirelessLinkData.setExtractionEnabled(tank);
            var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());AEExtractionNetwork.refill(player,List.of(tank));
            h.assertTrue(cap.getFluidInTank(0).getAmount()==8001,"existing type refilled exactly");h.assertTrue(storage.extract(AEFluidKey.of(Fluids.WATER),8000,Actionable.SIMULATE,action)==0,"ME fluid consumed once");h.succeed();
        });
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty") public static void fluidSelectionAndEquipmentDiscoveryAreSafe(GameTestHelper h){
        var empty=new net.minecraftforge.fluids.capability.templates.FluidTank(1000);
        h.assertTrue(AEExtractionFluidPolicy.select(empty,0,List.of(Fluids.WATER,Fluids.LAVA))==null,"ambiguous empty tank remains empty");
        var water=new net.minecraftforge.fluids.capability.templates.FluidTank(1000,fluid->fluid.getFluid()==Fluids.WATER||fluid.getFluid()==Fluids.FLOWING_WATER);
        h.assertTrue(AEExtractionFluidPolicy.select(water,0,List.of(Fluids.FLOWING_WATER,Fluids.WATER)).getFluid()==Fluids.WATER,"unique source fluid preferred");
        empty.fill(new FluidStack(Fluids.WATER,2),IFluidHandler.FluidAction.EXECUTE);
        h.assertTrue(AEExtractionFluidPolicy.select(empty,0,List.of(Fluids.LAVA)).getFluid()==Fluids.WATER,"existing tank type retained");
        var stack=new ItemStack(JDTEItems.BIG_FLUID_TANK.get());AEWirelessLinkData.setExtractionEnabled(stack);
        h.assertTrue(com.jdte.common.manager.AEExtractionPlayerManager.collectDistinct(List.of(stack,stack),List.of(stack)).size()==1,"same equipped item discovered only once");h.succeed();
    }
}
