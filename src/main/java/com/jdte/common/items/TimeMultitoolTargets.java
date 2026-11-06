package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.interfaces.*;
import com.direwolf20.justdirethings.setup.Config;
import net.minecraft.core.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.function.Predicate;

/** Pinned vein/tree/hammer/sky algorithms, with non-loading reads on both logical sides. */
public final class TimeMultitoolTargets {
    private TimeMultitoolTargets() {}
    public static boolean loaded(Level level,BlockPos pos){return !level.isOutsideBuildHeight(pos)&&chunk(level,pos)!=null;}
    private static LevelChunk chunk(Level level,BlockPos pos){var c=level.getChunk(pos.getX()>>4,pos.getZ()>>4,ChunkStatus.FULL,false);return c instanceof LevelChunk l&&!(l instanceof EmptyLevelChunk)?l:null;}
    public static BlockState state(Level level,BlockPos pos){var c=chunk(level,pos);return c==null?Blocks.AIR.defaultBlockState():c.getBlockState(pos);}
    public static BlockEntity entity(Level level,BlockPos pos){var c=chunk(level,pos);return c==null?null:c.getBlockEntity(pos);}
    public static Set<BlockPos> find(Level level,BlockPos start,int max,int radius,Predicate<BlockState> matches){
        var result=new LinkedHashSet<BlockPos>();var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();if(!loaded(level,start))return result;
        // Native helpers include the seed even when tagged discovery starts on non-tagged ground.
        if(matches.test(state(level,start)))result.add(start.immutable());
        queue.add(start.immutable());
        while(!queue.isEmpty()&&seen.size()<200&&result.size()<max){var current=queue.remove();if(!seen.add(current))continue;
            for(var p:BlockPos.betweenClosed(current.offset(-radius,-radius,-radius),current.offset(radius,radius,radius))){if(seen.contains(p)||!loaded(level,p)||!matches.test(state(level,p)))continue;var next=p.immutable();result.add(next);if(result.size()>=max)return result;queue.add(next);}
        }return result;
    }
    public static Set<BlockPos> collect(TimeMultitoolItem tool,ItemStack stack,Level level,BlockPos origin,LivingEntity actor,BlockState initial){
        var targets=new LinkedHashSet<BlockPos>();if(!loaded(level,origin))return targets;targets.add(origin.immutable());int max=Config.TOOL_MAX_BREAK_ECLIPSEALLOY.get();
        if(tool.canUseAbility(stack,Ability.OREMINER)&&Helpers.oreCondition.test(initial)&&tool.isCorrectToolForDrops(stack,initial)
            ||tool.canUseAbility(stack,Ability.TREEFELLER)&&Helpers.logCondition.test(initial)&&tool.isCorrectToolForDrops(stack,initial))targets.addAll(find(level,origin,max,2,s->s.is(initial.getBlock())));
        if(tool.canUseAbility(stack,Ability.HAMMER)&&initial.getDestroySpeed(level,origin)!=0){
            int range=ToggleableTool.getToolValue(stack,Ability.HAMMER.getName()),mid=(range-1)/2;Direction side=lookDirection(level,actor);boolean vertical=side.getAxis().isVertical();Direction up=vertical?actor.getDirection():Direction.UP;Direction right=vertical?up.getClockWise():side.getCounterClockWise();int down=mid,above=mid;
            if(!vertical&&range>3&&Math.abs(actor.getY()-origin.getY())<2){down=1;above=range-2;}
            for(var p:BlockPos.betweenClosed(origin.relative(up,above).relative(right.getOpposite(),mid),origin.relative(up.getOpposite(),down).relative(right,mid)))if(loaded(level,p)&&entity(level,p)==null&&tool.isCorrectToolForDrops(stack,state(level,p)))targets.add(p.immutable());
        }
        if(tool.canUseAbility(stack,Ability.SKYSWEEPER)&&tool.isCorrectToolForDrops(stack,initial)){
            var sky=new LinkedHashSet<BlockPos>();for(var base:targets){for(int i=1;i<=max;i++){var p=base.above(i);if(!loaded(level,p)||!(state(level,p).getBlock() instanceof FallingBlock))break;sky.add(p);}}targets.addAll(sky);
        }return targets;
    }
    private static Direction lookDirection(Level level,LivingEntity actor){
        BlockGetter view=new BlockGetter(){public BlockEntity getBlockEntity(BlockPos p){return entity(level,p);}public BlockState getBlockState(BlockPos p){return state(level,p);}public net.minecraft.world.level.material.FluidState getFluidState(BlockPos p){return state(level,p).getFluidState();}public int getHeight(){return level.getHeight();}public int getMinBuildHeight(){return level.getMinBuildHeight();}};
        Vec3 eye=actor.getEyePosition(),look=actor.getViewVector(1);double reach=actor instanceof net.minecraft.world.entity.player.Player p?p.getBlockReach():1;
        return view.clip(new ClipContext(eye,eye.add(look.scale(reach)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,actor)).getDirection().getOpposite();
    }
}
