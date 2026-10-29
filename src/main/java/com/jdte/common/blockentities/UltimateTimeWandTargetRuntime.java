package com.jdte.common.blockentities;

import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.integrations.ae2.ExtendedTimeAcceleratorAE2Integration;
import com.jdte.common.upgrades.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import java.util.*;

/** Loaded-target resolution and execution shared with the existing server acceleration queue. */
public final class UltimateTimeWandTargetRuntime {
    private UltimateTimeWandTargetRuntime(){}
    public enum Route { ORDINARY, AE2 }
    public record Resolved(TimeAccelerationTarget location,Route route,BlockEntity blockEntity){}
    public record Result(int executed,boolean valid,boolean idle,CoalescedAcceleratedMachine coalescedTarget){}
    private static boolean loaded(ServerLevel level,BlockPos pos){return level!=null&&pos!=null&&level.getServer().isSameThread()&&level.getServer().isRunning()
            &&level.getServer().getLevel(level.dimension())==level&&level.isInWorldBounds(pos)&&level.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4)!=null;}
    public static Optional<Resolved> resolve(ServerLevel level,BlockPos pos){
        var visited=new HashSet<TimeAccelerationTarget>();var original=level;
        for(int depth=0;depth<=10;depth++){
            if(!loaded(level,pos)||level.getServer()!=original.getServer())return Optional.empty();
            var location=new TimeAccelerationTarget(level,pos);if(!visited.add(location))return Optional.empty();
            var tile=level.getBlockEntity(pos);if(tile!=null&&tile.isRemoved())return Optional.empty();
            if(tile instanceof TimeAccelerationTargetProxy proxy){var next=proxy.getTimeAccelerationTarget();if(next==null||depth==10)return Optional.empty();level=next.level();pos=next.pos();continue;}
            if(tile instanceof TimeAcceleratorMachine)return Optional.empty();
            var state=level.getBlockState(pos);
            var deny=net.minecraft.tags.BlockTags.create(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("justdirethings","tick_speed_deny"));
            if(!state.is(deny)&&ModList.get().isLoaded("ae2")&&ExtendedTimeAcceleratorAE2Integration.hasTickable(level,pos))return Optional.of(new Resolved(location,Route.AE2,tile));
            if(!MiscTools.isValidTickAccelBlock(level,state,tile))return Optional.empty();
            if(tile!=null&&ticker(level,tile)!=null || tile==null&&!state.hasBlockEntity()&&state.isRandomlyTicking())return Optional.of(new Resolved(location,Route.ORDINARY,tile));
            return Optional.empty();
        }
        return Optional.empty();
    }
    @SuppressWarnings("unchecked") private static BlockEntityTicker<BlockEntity> ticker(ServerLevel level,BlockEntity tile){return tile.getBlockState().getTicker(level,(BlockEntityType<BlockEntity>)tile.getType());}
    public static Result execute(Resolved target,int requested){
        var level=target.location.level();var pos=target.location.pos();var current=resolve(level,pos);
        if(requested<=0)return new Result(0,true,true,null);
        if(current.isEmpty()||current.get().route()!=target.route||current.get().blockEntity()!=target.blockEntity)return new Result(0,false,true,null);
        if(target.route==Route.AE2){var result=ExtendedTimeAcceleratorAE2Integration.accelerate(level,pos,requested);return new Result(result.executed(),result.valid(),result.idle(),null);}
        var tile=target.blockEntity;
        if(tile!=null){
            var tick=ticker(level,tile);if(tick==null)return new Result(0,false,true,null);
            if(tile instanceof CoalescedAcceleratedMachine coalesced){
                int executions=1;
                if(tile instanceof com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE machine){
                    if(machine instanceof com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE redstone){redstone.evaluateRedstone();if(!redstone.isActiveRedstoneTestOnly())return new Result(requested,true,true,null);}
                    if(!UpgradeHelper.mayRunWithUpgrades(machine))return new Result(0,true,true,null);
                    executions=UpgradeHelper.hasUltimateOverclock(machine)?10:UpgradeHelper.hasOverclock(machine)&&UpgradeHelper.countUpgrades(machine,UpgradeType.UNDERCLOCK)==0?2:1;
                }
                coalesced.accumulateAcceleratedTicks((int)Math.min(Integer.MAX_VALUE,(long)requested*executions));return new Result(requested,true,false,coalesced);
            }
            int completed=0,idle=0;
            for(;completed<requested;completed++){
                if(!loaded(level,pos)||tile.isRemoved()||level.getBlockEntity(pos)!=tile)return new Result(completed,false,true,null);
                if(tile instanceof com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE machine){
                    if(machine instanceof com.direwolf20.justdirethings.common.blockentities.basebe.RedstoneControlledBE redstone){redstone.evaluateRedstone();if(!redstone.isActiveRedstoneTestOnly())break;}
                    if(!UpgradeHelper.mayRunWithUpgrades(machine))break;
                    if(!machine.canRun()){if(++idle>=2)break;}else idle=0;
                }
                tick.tick(level,pos,tile.getBlockState(),tile);
            }
            return new Result(completed,true,false,null);
        }
        int completed=0;
        for(;completed<requested;completed++){
            if(!loaded(level,pos))return new Result(completed,false,true,null);
            BlockState state=level.getBlockState(pos);
            if(state.hasBlockEntity()||!state.isRandomlyTicking()||!MiscTools.isValidTickAccelBlock(level,state,null))return new Result(completed,false,true,null);
            state.randomTick(level,pos,level.random);
        }
        return new Result(completed,true,false,null);
    }
}
