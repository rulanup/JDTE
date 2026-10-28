package com.jdte.common.blockentities;

import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.core.definitions.*;
import com.jdte.common.acceleration.*;
import com.jdte.common.integrations.ae2.ExtendedTimeAcceleratorAE2Integration;
import com.jdte.common.upgrades.*;
import com.jdte.setup.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.lang.reflect.*;
import java.util.*;

/** Real AE2 service and scheduler checks; discovered only when AE2 is installed. */
@PrefixGameTestTemplate(false)
public final class AEAccelerationGameTests {
    private static final BlockPos TARGET=new BlockPos(2,1,3), SOURCE=new BlockPos(1,1,2);
    private static void grid(GameTestHelper h){
        h.setBlock(new BlockPos(3,1,3),AEBlocks.CREATIVE_ENERGY_CELL.block());h.setBlock(TARGET,AEBlocks.INSCRIBER.block());
    }
    private static InscriberBlockEntity inscriber(GameTestHelper h){return (InscriberBlockEntity)h.getBlockEntity(TARGET);}
    private static void inputs(InscriberBlockEntity be){be.getInternalInventory().setItemDirect(0,AEItems.SILICON_PRESS.stack());be.getInternalInventory().setItemDirect(2,AEItems.SILICON.stack());}
    private static BasicTimeAcceleratorBE source(GameTestHelper h,BlockPos pos){
        h.setBlock(pos,JDTEBlocks.BASIC_TIME_ACCELERATOR.get());
        var be=new BasicTimeAcceleratorBE(h.absolutePos(pos),h.getBlockState(pos)){
            @Override public AABB getAABB(BlockPos ignored){return new AABB(h.absolutePos(TARGET));}
        };
        h.getLevel().setBlockEntity(be);be.getRedstoneControlData().redstoneMode=com.direwolf20.justdirethings.util.MiscHelpers.RedstoneMode.HIGH;
        be.fluidTank.setFluid(new FluidStack(com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(),1000));return be;
    }
    private static Object queue(){try{var c=Class.forName(ExtendedTimeAccelerationManager.class.getName()+"$LevelState");var ctor=c.getDeclaredConstructor();ctor.setAccessible(true);return ctor.newInstance();}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
    @SuppressWarnings("unchecked") private static void frame(Object state,ServerLevel level,List<TimeAcceleratorBE> sources,ExternalTimeAccelerationBackend backend,int budget){
        try{
            var field=state.getClass().getDeclaredField("submitted");field.setAccessible(true);((Set<TimeAcceleratorBE>)field.get(state)).addAll(sources);
            var prepare=state.getClass().getDeclaredMethod("prepare",ServerLevel.class,int.class,ExternalTimeAccelerationBackend.class);prepare.setAccessible(true);
            var execute=state.getClass().getDeclaredMethod("execute",ServerLevel.class,int.class);execute.setAccessible(true);
            ExternalTimeAccelerationFrame.run(level.getServer(),backend,List.of(new ExternalTimeAccelerationFrame.Work(
                    ()->invoke(prepare,state,level,4096,backend),()->invoke(execute,state,level,budget))));
        }catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    }
    private static void invoke(Method method,Object owner,Object... args){try{method.invoke(owner,args);}catch(ReflectiveOperationException e){throw new IllegalStateException(e instanceof InvocationTargetException?e.getCause():e);}}
    @GameTest(templateNamespace="jdte",template="mineral_empty") public static void cardLimitCompatibilityRecipeAndNbt(GameTestHelper h){
        for(var block:List.of(JDTEBlocks.BASIC_TIME_ACCELERATOR.get(),JDTEBlocks.ADVANCED_TIME_ACCELERATOR.get(),JDTEBlocks.EXTENDED_TIME_ACCELERATOR.get())){
            h.setBlock(SOURCE,block);var be=(TimeAcceleratorBE)h.getBlockEntity(SOURCE);var handler=UpgradeHelper.getUpgradeHandler(be);
            h.assertTrue(handler.insertItem(0,new ItemStack(JDTEItems.AE_ACCELERATION_UPGRADE.get(),2),false).getCount()==1,"one card admitted");
            h.assertTrue(!handler.isItemValid(1,new ItemStack(JDTEItems.AE_ACCELERATION_UPGRADE.get())),"limit applies across slots");
            var saved=be.saveWithFullMetadata();var restored=net.minecraft.world.level.block.entity.BlockEntity.loadStatic(be.getBlockPos(),be.getBlockState(),saved);
            h.assertTrue(UpgradeHelper.hasAEAccelerationUpgrade((TimeAcceleratorBE)restored),"installed card persists in active handler");
        }
        h.setBlock(SOURCE,JDTEBlocks.ADVANCED_FLUID_MIXER.get());h.assertTrue(!UpgradeHelper.isUpgradeCompatible((FluidMixerBE)h.getBlockEntity(SOURCE),UpgradeType.AE_ACCELERATION),"mixer rejected");
        h.assertTrue(h.getLevel().getRecipeManager().byKey(com.jdte.JDTE.id("ae_acceleration_upgrade")).isPresent(),"source AE-dependent survival recipe loaded");h.succeed();
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=220) public static void realInscriberEndpointDeduplicatesSidesAndDoesNotChargeSleepTicks(GameTestHelper h){
        grid(h);h.runAfterDelay(80,()->{
            var be=inscriber(h);h.assertTrue(be.getMainNode().isOnline(),"actual inscriber node online");
            var idle=ExtendedTimeAcceleratorAE2Integration.accelerate(h.getLevel(),be.getBlockPos(),12);
            h.assertTrue(idle.executed()==0&&idle.idle(),"sleep-only request is not counted as useful work");
            inputs(be);h.assertTrue(be.getTask()!=null,"native silicon recipe recognized");int before=be.getProcessingTime();
            var result=ExtendedTimeAcceleratorAE2Integration.accelerate(h.getLevel(),be.getBlockPos(),12);
            h.assertTrue(result.executed()==12&&be.getProcessingTime()-before==24,"one native service request per tick, not once for each exposed side: "+result+" progress="+be.getProcessingTime());h.succeed();
        });
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=220) public static void schedulerRequiresEachContributorCardAndDropsItsRemovedBacklog(GameTestHelper h){
        grid(h);var a=source(h,SOURCE);var b=source(h,new BlockPos(1,1,4));
        h.runAfterDelay(80,()->{
            var be=inscriber(h);inputs(be);var state=queue();int start=be.getProcessingTime();
            frame(state,h.getLevel(),List.of(a,b),null,64);h.assertTrue(be.getProcessingTime()==start&&a.fluidTank.getFluidAmount()==1000&&b.fluidTank.getFluidAmount()==1000,"no AE service acceleration or charges without card");
            var handler=UpgradeHelper.getUpgradeHandler(a);handler.insertItem(0,new ItemStack(JDTEItems.AE_ACCELERATION_UPGRADE.get()),false);
            frame(state,h.getLevel(),List.of(a,b),null,64);
            h.assertTrue(be.getProcessingTime()-start==2*a.getEffectiveMultiplier()&&b.fluidTank.getFluidAmount()==1000,"unupgraded overlapping contributor cannot borrow another card");
            h.assertTrue(a.saveWithFullMetadata().getDouble("pendingFluidCost")>0||a.fluidTank.getFluidAmount()<1000,"existing source fractional Time Fluid accounting used");
            frame(state,h.getLevel(),List.of(a),null,0);int before=be.getProcessingTime();handler.extractItem(0,1,false);
            frame(state,h.getLevel(),List.of(a),null,64);h.assertTrue(be.getProcessingTime()==before,"removing card invalidates queued AE contributions");h.succeed();
        });
    }
    @GameTest(templateNamespace="jdte",template="mineral_empty",timeoutTicks=220) public static void externalBackendOwnsRoutesAndSuppressesBuiltinDoubleAcceleration(GameTestHelper h){
        grid(h);var a=source(h,SOURCE);UpgradeHelper.getUpgradeHandler(a).insertItem(0,new ItemStack(JDTEItems.AE_ACCELERATION_UPGRADE.get()),false);
        h.runAfterDelay(80,()->{
            var be=inscriber(h);inputs(be);int start=be.getProcessingTime();var backend=new Backend();var state=queue();
            frame(state,h.getLevel(),List.of(a),backend,64);
            h.assertTrue(backend.order.equals(List.of("begin","resolve","submit:"+a.getEffectiveMultiplier(),"execute","clear"))&&be.getProcessingTime()==start,"external frame contract and no native duplicate");
            backend.order.clear();backend.inactive=true;var saved=a.saveWithFullMetadata();frame(state,h.getLevel(),List.of(a),backend,64);
            h.assertTrue(backend.order.equals(List.of("begin","resolve","execute","clear"))&&a.saveWithFullMetadata().getDouble("pendingFluidCost")==saved.getDouble("pendingFluidCost")&&be.getProcessingTime()==start,"inactive external target cannot fall back or charge");h.succeed();
        });
    }
    private static final class Backend implements ExternalTimeAccelerationBackend {
        final List<String> order=new ArrayList<>();final TargetHandle handle=new TargetHandle(){};boolean inactive;
        public void beginFrame(net.minecraft.server.MinecraftServer s){order.add("begin");}
        public TargetResolution resolve(ServerLevel l,BlockPos p){order.add("resolve");return inactive?TargetResolution.inactiveExternal():TargetResolution.active(handle);}
        public void submit(TargetHandle h,Object c,int n){order.add("submit:"+n);}
        public void executeFrame(net.minecraft.server.MinecraftServer s){order.add("execute");}
        public void clearFrame(net.minecraft.server.MinecraftServer s){order.add("clear");}
    }
}
