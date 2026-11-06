package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.interfaces.*;
import com.direwolf20.justdirethings.common.items.tools.EclipseAlloyPaxel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.*;
import net.minecraftforge.common.capabilities.*;
import java.util.*;

/** Source mining multitool; it does not submit Time Wand or machine acceleration requests. */
public class TimeMultitoolItem extends EclipseAlloyPaxel implements FluidContainingItem {
    public static final int MAX_ENERGY=500_000,MAX_TIME_FLUID=1_000_000;
    @Override public int getMaxEnergy(){return MAX_ENERGY;}
    @Override public int getMaxMB(){return MAX_TIME_FLUID;}
    @Override public ICapabilityProvider initCapabilities(ItemStack stack,net.minecraft.nbt.CompoundTag nbt){return new TimeMultitoolResources(stack);}
    private boolean working(ItemStack stack){return getAvailableEnergy(stack)>=getBlockBreakFECost();}
    @Override public boolean canPerformAction(ItemStack stack,ToolAction action){return working(stack)&&(super.canPerformAction(stack,action)||action==ToolActions.HOE_TILL);}
    @Override public boolean isCorrectToolForDrops(ItemStack stack,BlockState state){return working(stack)&&super.isCorrectToolForDrops(stack,state);}
    @Override public float getDestroySpeed(ItemStack stack,BlockState state){float speed=super.getDestroySpeed(stack,state);return speed<=1||working(stack)?speed:1;}
    public static TimeMultitoolSpeedMode getSelectedSpeedMode(ItemStack stack){return TimeMultitoolSpeedStorage.get(stack);}
    public TimeMultitoolMiningPolicy.Decision miningBatchDecision(ItemStack stack,int targets){return TimeMultitoolMiningPolicy.decideBatch(getSelectedSpeedMode(stack),Math.max(0,FluidContainingItem.getAvailableFluid(stack)),Math.max(0,getAvailableEnergy(stack)),getBlockBreakFECost(),targets);}
    @Override public Set<BlockPos> getBreakBlockPositions(ItemStack stack,Level level,BlockPos origin,LivingEntity actor,BlockState state){return TimeMultitoolTargets.collect(this,stack,level,origin,actor,state);}
    @Override public boolean canInstaBreak(ItemStack stack,Level level,Set<BlockPos> targets){if(!canUseAbility(stack,Ability.INSTABREAK))return false;double hardness=0;for(var p:targets){float h=TimeMultitoolTargets.state(level,p).getDestroySpeed(level,p);if(h>=0)hardness+=h;}return getAvailableEnergy(stack)>TimeMultitoolMining.instantCost(stack,(float)Math.min(Float.MAX_VALUE,hardness));}
    // Forge's native mineBlock callback occurs before removal; the game-mode interception owns the full transaction.
    @Override public boolean mineBlock(ItemStack stack,Level level,BlockState state,BlockPos pos,LivingEntity actor){return true;}
    @Override public void mineBlocksAbility(ItemStack stack,Level level,BlockPos pos,LivingEntity actor,BlockState state){if(level instanceof ServerLevel server&&actor instanceof ServerPlayer player)TimeMultitoolMining.mine(server,pos,player,stack);}
    private static boolean held(Player p,ItemStack s){return p!=null&&p.isAlive()&&!p.isRemoved()&&(p.getMainHandItem()==s||p.getOffhandItem()==s);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,net.minecraft.world.InteractionHand hand){
        var stack=player.getItemInHand(hand);if(!player.isShiftKeyDown())return super.use(level,player,hand);
        if(!level.isClientSide&&held(player,stack)&&TimeMultitoolResources.live(stack)&&!TimeMultitoolMining.active()){var mode=getSelectedSpeedMode(stack).next();TimeMultitoolSpeedStorage.set(stack,mode);player.displayClientMessage(Component.translatable("message.jdte.time_multitool.speed",mode.multiplier()),true);}
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
    @Override public InteractionResult useOn(UseOnContext context){
        var player=context.getPlayer();var level=context.getLevel();var stack=context.getItemInHand();
        if(!held(player,stack))return InteractionResult.FAIL;
        if(player.isShiftKeyDown()){if(!level.isClientSide&&TimeMultitoolResources.live(stack)&&!TimeMultitoolMining.active())openSettings(player);return InteractionResult.sidedSuccess(level.isClientSide);}
        if(!working(stack)||!TimeMultitoolTargets.loaded(level,context.getClickedPos()))return InteractionResult.FAIL;
        var result=till(context);if(result.consumesAction())return result;result=paxelTransform(context);return result.consumesAction()?result:super.useOn(context);
    }
    private InteractionResult till(UseOnContext context){
        var level=context.getLevel();var p=context.getPlayer();var pos=context.getClickedPos();
        if(context.getClickedFace()==net.minecraft.core.Direction.DOWN||!TimeMultitoolTargets.loaded(level,pos.above())||!TimeMultitoolTargets.state(level,pos.above()).isAir())return InteractionResult.PASS;
        var state=TimeMultitoolTargets.state(level,pos);if(!level.mayInteract(p,pos)||!p.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand()))return InteractionResult.FAIL;
        var tilled=state.getToolModifiedState(context,ToolActions.HOE_TILL,false);if(tilled==null)return InteractionResult.PASS;
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(!TimeMultitoolResources.live(context.getItemInHand())||TimeMultitoolMining.active()||TimeMultitoolTargets.state(level,pos)!=state)return InteractionResult.FAIL;
        try(var lease=TimeMultitoolResources.of(context.getItemInHand()).reserve(p.isCreative()?0:TimeMultitoolMining.energyCost(context.getItemInHand(),1),0)){if(lease==null)return InteractionResult.FAIL;if(!level.setBlock(pos,tilled,11))return InteractionResult.FAIL;lease.commit();}
        level.playSound(null,pos,SoundEvents.HOE_TILL,SoundSource.BLOCKS,1,1);level.gameEvent(GameEvent.BLOCK_CHANGE,pos,GameEvent.Context.of(p,tilled));return InteractionResult.SUCCESS;
    }
    /** Neo JDT added BasePaxel between EclipseAlloyPaxel and BasePickaxe; Forge1.1.4 lacks it. */
    private InteractionResult paxelTransform(UseOnContext ctx){
        var p=ctx.getPlayer();var level=ctx.getLevel();var pos=ctx.getClickedPos();var stack=ctx.getItemInHand();
        if(ctx.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND&&p.getOffhandItem().is(Items.SHIELD)&&!p.isSecondaryUseActive())return InteractionResult.PASS;
        if(!level.mayInteract(p,pos)||!p.mayUseItemAt(pos,ctx.getClickedFace(),stack))return InteractionResult.FAIL;
        var state=TimeMultitoolTargets.state(level,pos);BlockState result=null;net.minecraft.sounds.SoundEvent sound=null;int effect=0;
        var actions=new ToolAction[]{ToolActions.AXE_STRIP,ToolActions.AXE_SCRAPE,ToolActions.AXE_WAX_OFF};
        var sounds=new net.minecraft.sounds.SoundEvent[]{SoundEvents.AXE_STRIP,SoundEvents.AXE_SCRAPE,SoundEvents.AXE_WAX_OFF};
        for(int i=0;i<actions.length;i++){result=state.getToolModifiedState(ctx,actions[i],false);if(result!=null){sound=sounds[i];effect=i==1?3005:i==2?3004:0;break;}}
        if(result==null&&ctx.getClickedFace()!=net.minecraft.core.Direction.DOWN){
            var flattened=state.getToolModifiedState(ctx,ToolActions.SHOVEL_FLATTEN,false);
            if(flattened!=null&&TimeMultitoolTargets.loaded(level,pos.above())&&TimeMultitoolTargets.state(level,pos.above()).isAir()){result=flattened;sound=SoundEvents.SHOVEL_FLATTEN;}
            else if(state.is(net.minecraft.tags.BlockTags.CAMPFIRES)&&state.hasProperty(net.minecraft.world.level.block.CampfireBlock.LIT)&&state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)){result=state.setValue(net.minecraft.world.level.block.CampfireBlock.LIT,false);effect=1009;}
        }
        if(result==null)return InteractionResult.PASS;if(level.isClientSide)return InteractionResult.SUCCESS;
        if(!TimeMultitoolResources.live(stack)||TimeMultitoolMining.active()||TimeMultitoolTargets.state(level,pos)!=state)return InteractionResult.FAIL;
        try(var lease=TimeMultitoolResources.of(stack).reserve(p.isCreative()?0:TimeMultitoolMining.energyCost(stack,1),0)){if(lease==null)return InteractionResult.FAIL;if(!level.setBlock(pos,result,11))return InteractionResult.FAIL;lease.commit();}
        if(effect==1009)net.minecraft.world.level.block.CampfireBlock.dowse(p,level,pos,state);if(sound!=null)level.playSound(null,pos,sound,SoundSource.BLOCKS,1,1);if(effect!=0)level.levelEvent(p,effect,pos,0);level.gameEvent(GameEvent.BLOCK_CHANGE,pos,GameEvent.Context.of(p,result));return InteractionResult.SUCCESS;
    }
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){
        // The inherited Forge tooltip directly references Minecraft; keep common-side calls isolated.
        if(level!=null&&level.isClientSide)super.appendHoverText(stack,level,tooltip,flag);
        tooltip.add(Component.translatable("tooltip.jdte.time_multitool.speed",getSelectedSpeedMode(stack).multiplier()).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.jdte.time_multitool.fluid",Math.max(0,FluidContainingItem.getAvailableFluid(stack)),MAX_TIME_FLUID).withStyle(ChatFormatting.BLUE));tooltip.add(Component.translatable("tooltip.jdte.time_multitool.controls").withStyle(ChatFormatting.GRAY));
    }
}
