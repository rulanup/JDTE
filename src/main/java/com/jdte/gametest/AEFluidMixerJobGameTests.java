package com.jdte.gametest;

import appeng.api.config.Actionable;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.*;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.core.definitions.AEBlocks;
import com.jdte.common.blockentities.*;
import com.jdte.common.integrations.ae2.AEWirelessLinkData;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.setup.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.concurrent.Future;

@PrefixGameTestTemplate(false)
public final class AEFluidMixerJobGameTests {
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=700)
    public static void actualFluidPatternFeedsMixerAndCompletesThroughAeOutput(GameTestHelper h){run(h,false);}
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=700)
    public static void actualFluidPatternCompletesThroughAdvancedOutputOnExtendedMixer(GameTestHelper h){run(h,true);}
    private static void run(GameTestHelper h,boolean extended) {
        AEUpgradeGameTests.grid(h);h.setBlock(new BlockPos(4,1,3),AEBlocks.CRAFTING_STORAGE_1K.block());
        h.setBlock(new BlockPos(4,1,4),AEBlocks.PATTERN_PROVIDER.block());
        var provider=(PatternProviderBlockEntity)h.getBlockEntity(new BlockPos(4,1,4));
        var pos=new BlockPos(5,1,4);h.setBlock(pos,extended?JDTEBlocks.EXTENDED_FLUID_MIXER.get():JDTEBlocks.ADVANCED_FLUID_MIXER.get());
        var mixer=(FluidMixerBE)h.getBlockEntity(pos);mixer.getEnergyStorage().receiveEnergy(100000,false);
        var read=new ItemStack(JDTEItems.AE_CRAFTING_READ_UPGRADE.get());var output=new ItemStack(extended?JDTEItems.ADVANCED_AE_OUTPUT_UPGRADE.get():JDTEItems.AE_OUTPUT_UPGRADE.get());
        AEWirelessLinkData.set(read,AEUpgradeGameTests.target(h));AEWirelessLinkData.set(output,AEUpgradeGameTests.target(h));
        UpgradeHelper.getUpgradeHandler(mixer).insertItem(0,read,false);UpgradeHelper.getUpgradeHandler(mixer).insertItem(1,output,false);
        var product=AEFluidKey.of(BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("justdirethings","time_fluid")));
        provider.getLogic().getPatternInv().insertItem(0,PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[]{new GenericStack(AEItemKey.of(JDTEItems.TIME_FLUID_CATALYST.get()),1),new GenericStack(AEFluidKey.of(Fluids.WATER),1000)},
                new GenericStack[]{new GenericStack(product,250)}),false);
        var state=new State();
        h.runAfterDelay(100,()->{
            var point=AEUpgradeGameTests.ap(h);var grid=point.getGrid();var storage=grid.getStorageService().getInventory();
            h.assertTrue(!UpgradeHelper.mayRunWithUpgrades(mixer)&&mixer.getEnergyStorage().getEnergyStored()==100000,"idle mixer remains paused");
            h.assertTrue(storage.insert(AEItemKey.of(JDTEItems.TIME_FLUID_CATALYST.get()),1,Actionable.MODULATE,IActionSource.empty())==1,"real pattern catalyst staged in ME");
            h.assertTrue(storage.insert(AEFluidKey.of(Fluids.WATER),1000,Actionable.MODULATE,IActionSource.empty())==1000,"real pattern fluid staged in ME");
            state.plan=grid.getCraftingService().beginCraftingCalculation(h.getLevel(),()->IActionSource.ofMachine(point),product,250,CalculationStrategy.REPORT_MISSING_ITEMS);state.phase=1;
        });
        h.onEachTick(()->{
            if(state.phase==0)return;var point=AEUpgradeGameTests.ap(h);var grid=point.getGrid();
            if(state.phase==1){
                if(!state.plan.isDone())return;
                try {
                    var plan=state.plan.get();h.assertTrue(!plan.simulation(),"fluid processing pattern has executable plan");
                    state.cpu=grid.getCraftingService().getCpus().stream().filter(cpu->!cpu.isBusy()).findFirst().orElseThrow();
                    var result=grid.getCraftingService().submitJob(plan,null,state.cpu,false,IActionSource.ofMachine(point));h.assertTrue(result.successful(),"actual fluid job submitted: "+result.errorCode());state.phase=2;
                }catch(java.util.concurrent.ExecutionException error){throw new IllegalStateException(error.getCause());}catch(InterruptedException error){Thread.currentThread().interrupt();throw new IllegalStateException(error);}
            } else if(state.phase==2) {
                if(!state.cpu.isBusy() && state.cpu.getJobStatus()==null && !UpgradeHelper.mayRunWithUpgrades(mixer)) {
                    var storage=grid.getStorageService().getInventory();
                    h.assertTrue(storage.extract(product,1000,Actionable.SIMULATE,IActionSource.empty())==250,"exact real product returned to completed job/storage");
                    h.assertTrue(mixer.inputTankA.isEmpty()&&mixer.inputTankB.isEmpty()&&mixer.outputTank.isEmpty()&&mixer.getMachineHandler().getStackInSlot(0).isEmpty(),"provider-fed inputs consumed and output exported exactly once");
                    h.assertTrue(mixer.getEnergyStorage().getEnergyStored()==95000,"real 100-tick recipe charges exactly 5000 FE");state.phase=3;
                }
            } else if(++state.pausedTicks>=10) {h.assertTrue(mixer.getEnergyStorage().getEnergyStored()==95000,"completed job leaves mixer paused without further costs");h.succeed();}
        });
    }
    private static class State {Future<ICraftingPlan> plan;ICraftingCPU cpu;int phase,pausedTicks;}
}
