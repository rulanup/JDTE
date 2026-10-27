package com.jdte.gametest;

import appeng.api.config.Actionable;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.*;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.core.definitions.AEBlocks;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.integrations.ae2.*;
import com.jdte.common.items.MineralSurveyItem;
import com.jdte.common.minerals.*;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.List;
import java.util.concurrent.Future;

/** Actual AE2 planner/CPU/provider jobs; registered only when AE2 is present. */
@PrefixGameTestTemplate(false)
public final class AECraftingJobGameTests {
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=600)
    public static void submittedCraftingJobRunsMineralAndReturnsOutput(GameTestHelper h) { run(h,false); }
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=600)
    public static void cancelingSubmittedCraftingJobPausesMineralWithoutLosingWork(GameTestHelper h) { run(h,true); }

    private static void run(GameTestHelper h, boolean cancel) {
        AEUpgradeGameTests.grid(h);
        h.setBlock(new BlockPos(4,1,3),AEBlocks.CRAFTING_STORAGE_1K.block());
        BlockPos providerPos=new BlockPos(4,1,4);
        h.setBlock(providerPos,AEBlocks.PATTERN_PROVIDER.block());
        h.setBlock(providerPos.east(),Blocks.CHEST);
        var provider=(PatternProviderBlockEntity)h.getBlockEntity(providerPos);
        provider.getLogic().getPatternInv().insertItem(0,PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[]{new GenericStack(AEItemKey.of(Items.COBBLESTONE),1)},
                new GenericStack[]{new GenericStack(AEItemKey.of(Items.IRON_ORE),1)}),false);
        h.setBlock(new BlockPos(1,1,2),JDTEBlocks.MINERAL_EXTRACTOR.get());
        var machine=(MineralExtractorBE)h.getBlockEntity(new BlockPos(1,1,2));
        UpgradeHelper.getUpgradeHandler(machine).insertItem(2,new ItemStack(JDTEItems.ULTIMATE_CAPACITY_UPGRADE.get()),false);
        machine.getEnergyStorage().receiveEnergy(50_000_000,false);
        var read=new ItemStack(JDTEItems.AE_CRAFTING_READ_UPGRADE.get());
        var output=new ItemStack(JDTEItems.AE_OUTPUT_UPGRADE.get());
        AEWirelessLinkData.set(read,AEUpgradeGameTests.target(h));AEWirelessLinkData.set(output,AEUpgradeGameTests.target(h));
        UpgradeHelper.getUpgradeHandler(machine).insertItem(0,read,false);
        UpgradeHelper.getUpgradeHandler(machine).insertItem(1,output,false);
        var survey=new ItemStack(JDTEItems.MINERAL_SURVEY.get());
        MineralSurveyItem.setSurvey(survey,MineralSurveyData.create(MineralSurveyIndex.version(h.getLevel().getServer()),
                ResourceLocation.withDefaultNamespace("plains"),h.getLevel().dimension().location(),
                List.of(new MineralEntry(ResourceLocation.withDefaultNamespace("iron_ore"),1,-64,64,9,MineralEntry.Confidence.EXACT))));
        machine.getMachineHandler().setStackInSlot(0,survey);
        var state=new JobState();
        h.runAfterDelay(100,()->{
            var accessPoint=AEUpgradeGameTests.ap(h);var grid=accessPoint.getGrid();var crafting=grid.getCraftingService();
            h.assertTrue(provider.getMainNode().isOnline(),"provider online and has a channel");
            h.assertTrue(!provider.getLogic().getAvailablePatterns().isEmpty(),"provider decoded the real processing pattern");
            h.assertTrue(!crafting.getCraftingFor(AEItemKey.of(Items.IRON_ORE)).isEmpty(),"grid advertises the processing pattern");
            h.assertTrue(!crafting.getCpus().isEmpty(),"real crafting storage formed a CPU");
            h.assertTrue(!UpgradeHelper.mayRunWithUpgrades(machine)&&machine.getEnergyStorage().getEnergyStored()==50_000_000,"idle real grid pauses production and resource costs");
            var storage=grid.getStorageService().getInventory();
            h.assertTrue(storage.insert(AEItemKey.of(Items.COBBLESTONE),128,Actionable.MODULATE,IActionSource.empty())==128,"real job inputs stored");
            h.assertTrue(storage.insert(AEFluidKey.of(Fluids.WATER),1000,Actionable.MODULATE,IActionSource.empty())==1000,"extraction resources stored");
            state.plan=crafting.beginCraftingCalculation(h.getLevel(),()->IActionSource.ofMachine(accessPoint),AEItemKey.of(Items.IRON_ORE),16,CalculationStrategy.REPORT_MISSING_ITEMS);
            state.phase=1;
        });
        h.runAfterDelay(550,()->{
            var storage=AEUpgradeGameTests.ap(h).getGrid().getStorageService().getInventory();
            h.assertTrue(false,"job diagnostic: phase="+state.phase+", requested="+AEUpgradeGameTests.ap(h).getGrid().getCraftingService().getRequestedAmount(AEItemKey.of(Items.IRON_ORE))+", chest="+((net.minecraft.world.level.block.entity.ChestBlockEntity)h.getBlockEntity(providerPos.east())).getItem(0)+", energy="+machine.getEnergyStorage().getEnergyStored()
                    +", storedOre="+storage.extract(AEItemKey.of(Items.IRON_ORE),Long.MAX_VALUE,Actionable.SIMULATE,IActionSource.empty())
                    +", status="+(state.cpu==null?null:state.cpu.getJobStatus()));
        });
        h.onEachTick(()->{
            if(state.phase==0)return;
            var point=AEUpgradeGameTests.ap(h);var crafting=point.getGrid().getCraftingService();
            if(state.phase==1){
                if(!state.plan.isDone())return;
                try {
                    var plan=state.plan.get();
                    com.mojang.logging.LogUtils.getLogger().info("JDTE real job plan: output={}, bytes={}, patternTimes={}, used={}, missing={}",plan.finalOutput(),plan.bytes(),plan.patternTimes(),describe(plan.usedItems()),describe(plan.missingItems()));
                    h.assertTrue(!plan.simulation(),"AE2 produced executable plan; missing="+plan.missingItems().keySet());
                    state.cpu=crafting.getCpus().stream().filter(cpu->!cpu.isBusy()).findFirst().orElseThrow();
                    var submitted=crafting.submitJob(plan,null,state.cpu,false,IActionSource.ofMachine(point));
                    h.assertTrue(submitted.successful(),"actual CPU accepts job: "+submitted.errorCode()+" "+submitted.errorDetail());
                    state.phase=2;
                }catch(java.util.concurrent.ExecutionException error){throw new IllegalStateException("Real AE crafting plan failed",error.getCause());}
                catch(InterruptedException error){Thread.currentThread().interrupt();throw new IllegalStateException(error);}
            }else if(state.phase==2){
                if(!AE2CraftingReadNetwork.hasActiveCraftingTask(h.getLevel(),read))return;
                h.assertTrue(crafting.getCpus().stream().anyMatch(cpu->cpu.isBusy()&&cpu.getJobStatus()!=null),"actual CPU has a busy nonempty job");
                h.assertTrue(UpgradeHelper.mayRunWithUpgrades(machine),"real job enables machine");
                var tank=new ItemStack(JDTEItems.BIG_FLUID_TANK.get());
                var cap=tank.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(()->new IllegalStateException("fluid capability"));
                cap.fill(new FluidStack(Fluids.WATER,1),IFluidHandler.FluidAction.EXECUTE);
                AEWirelessLinkData.set(tank,AEUpgradeGameTests.target(h));AEWirelessLinkData.setExtractionEnabled(tank);
                AEExtractionNetwork.refill(net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel()),List.of(tank));
                h.assertTrue(cap.getFluidInTank(0).getAmount()==1001,"extraction operates on the real busy crafting grid");
                if(cancel)state.cpu.cancelJob();state.phase=3;
            }else if(state.phase==3){
                if(state.cpu.isBusy() || state.cpu.getJobStatus()!=null)return;
                if(UpgradeHelper.mayRunWithUpgrades(machine))return;
                if(!cancel){
                    h.assertTrue(machine.getEnergyStorage().getEnergyStored()<50_000_000,"real machine spent FE producing requested ore");
                    long available=point.getGrid().getStorageService().getInventory().extract(AEItemKey.of(Items.IRON_ORE),Long.MAX_VALUE,Actionable.SIMULATE,IActionSource.empty());
                    h.assertTrue(available>=16,"automatic AE Output returns the completed standalone job to storage");
                }
                state.energy=machine.getEnergyStorage().getEnergyStored();state.pending=machine.saveWithFullMetadata().getLong("pendingBaseWork");state.phase=4;
            }else if(state.phase==4&&++state.pausedTicks>=12){
                h.assertTrue(!UpgradeHelper.mayRunWithUpgrades(machine),"completed/canceled job keeps production paused");
                h.assertTrue(machine.getEnergyStorage().getEnergyStored()==state.energy&&machine.saveWithFullMetadata().getLong("pendingBaseWork")==state.pending,"pause preserves FE and pending work");h.succeed();
            }
        });
    }
    private static String describe(KeyCounter counter) {var text=new StringBuilder();for(var entry:counter)text.append(entry.getKey()).append('=').append(entry.getLongValue()).append(';');return text.toString();}
    private static final class JobState {
        Future<ICraftingPlan> plan;ICraftingCPU cpu;int phase,pausedTicks,energy;long pending;
    }
}
