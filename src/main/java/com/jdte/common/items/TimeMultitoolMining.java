package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.interfaces.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import java.util.*;

/** Synchronous authoritative mining, with scoped resources and no retained world/entity targets. */
public final class TimeMultitoolMining {
    private static final ThreadLocal<Boolean> ACTIVE=ThreadLocal.withInitial(()->false);
    private TimeMultitoolMining() {}
    public static boolean active(){return ACTIVE.get();}
    private static boolean valid(ServerLevel level,ServerPlayer player,ItemStack stack){return TimeMultitoolResources.live(stack)&&player.level()==level&&player.isAlive()&&!player.isRemoved()&&!player.isSpectator()&&player.getMainHandItem()==stack;}
    public static boolean mine(ServerLevel level,BlockPos origin,ServerPlayer player,ItemStack stack){
        if(active()||!valid(level,player,stack)||!TimeMultitoolTargets.loaded(level,origin))return false;
        if(!(player instanceof FakePlayer)&&player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(origin))>Math.pow(player.getBlockReach()+1.5,2))return false;
        var tool=(TimeMultitoolItem)stack.getItem();var initial=TimeMultitoolTargets.state(level,origin);if(initial.isAir())return false;
        var targets=tool.getBreakBlockPositions(stack,level,origin,player,initial);var decision=tool.miningBatchDecision(stack,(int)targets.stream().filter(p->!TimeMultitoolTargets.state(level,p).isAir()).count());if(!decision.powered())return false;
        boolean instant=tool.canInstaBreak(stack,level,targets);return execute(level,origin,player,stack,targets,decision.timeFluidCost(),instant,null);
    }
    public static boolean lawn(Level world,net.minecraft.world.entity.player.Player actor,ItemStack stack,BlockPos origin){
        if(!(world instanceof ServerLevel level)||!(actor instanceof ServerPlayer player)||active()||!valid(level,player,stack))return false;
        var tag=com.direwolf20.justdirethings.datagen.JustDireBlockTags.LAWNMOWERABLE;
        var targets=TimeMultitoolTargets.find(level,origin,64,5,s->s.is(tag));execute(level,origin,player,stack,targets,0,false,Ability.LAWNMOWER);return true;
    }
    public static boolean leaves(net.minecraft.world.item.context.UseOnContext ctx){
        if(!(ctx.getLevel() instanceof ServerLevel level)||!(ctx.getPlayer() instanceof ServerPlayer player)||active()||!valid(level,player,ctx.getItemInHand()))return false;
        var origin=ctx.getClickedPos();var state=TimeMultitoolTargets.state(level,origin);if(!state.is(BlockTags.LEAVES))return false;
        execute(level,origin,player,ctx.getItemInHand(),TimeMultitoolTargets.find(level,origin,64,2,s->s.is(state.getBlock())),0,false,Ability.LEAFBREAKER);return true;
    }
    private record Target(BlockPos pos,BlockState state,BlockEntity entity) {}
    private static boolean execute(ServerLevel level,BlockPos origin,ServerPlayer player,ItemStack stack,Set<BlockPos> positions,int fluidCost,boolean instant,Ability utility){
        var tool=(TimeMultitoolItem)stack.getItem();var snapshots=positions.stream().map(p->new Target(p.immutable(),TimeMultitoolTargets.state(level,p),TimeMultitoolTargets.entity(level,p))).toList();
        var drops=new ArrayList<ItemStack>();var removedPositions=new LinkedHashSet<BlockPos>();int totalXp=0;boolean primary=false;ACTIVE.set(true);
        try{
            for(var target:snapshots){
                if(!valid(level,player,stack)||tool.getAvailableEnergy(stack)<(utility==null?tool.getBlockBreakFECost():utility.getFeCost()))break;
                var pos=target.pos();var state=target.state();
                if(state.isAir()||!TimeMultitoolTargets.loaded(level,pos)||TimeMultitoolTargets.state(level,pos)!=state||TimeMultitoolTargets.entity(level,pos)!=target.entity()||(!Float.isFinite(state.getDestroySpeed(level,pos))||state.getDestroySpeed(level,pos)<0)||!level.mayInteract(player,pos)||player.blockActionRestricted(level,pos,player.gameMode.getGameModeForPlayer())||state.getBlock() instanceof GameMasterBlock&&!player.canUseGameMasterBlocks()){if(utility==null&&pos.equals(origin))break;continue;}
                int xp=ForgeHooks.onBlockBreakEvent(level,player.gameMode.getGameModeForPlayer(),player,pos);if(xp<0||!valid(level,player,stack)||!TimeMultitoolTargets.loaded(level,pos)||TimeMultitoolTargets.state(level,pos)!=state||TimeMultitoolTargets.entity(level,pos)!=target.entity()){if(utility==null&&pos.equals(origin))break;continue;}
                if(tool.getAvailableEnergy(stack)<(utility==null?tool.getBlockBreakFECost():utility.getFeCost()))break;
                int fe=0;
                if(!player.isCreative()&&state.getDestroySpeed(level,pos)!=0){
                    if(utility==null){fe=energyCost(stack,tool.getBlockBreakFECost());if(instant)fe=(int)Math.min(Integer.MAX_VALUE,(long)fe+energyCost(stack,instantCost(stack,state.getDestroySpeed(level,pos))));}
                    else if(level.random.nextFloat()<0.1F)fe=energyCost(stack,utility.getFeCost());
                }
                var lootTool=stack.copy();
                boolean harvest=!player.isCreative()&&state.canHarvestBlock(level,pos,player);
                var lease=TimeMultitoolResources.of(stack).reserve(fe,fluidCost);if(lease==null)break;
                boolean removed=false;
                try(lease){
                    state.getBlock().playerWillDestroy(level,pos,state,player);
                    if(!valid(level,player,stack)||!TimeMultitoolTargets.loaded(level,pos)||TimeMultitoolTargets.state(level,pos)!=state||TimeMultitoolTargets.entity(level,pos)!=target.entity()){if(utility==null&&pos.equals(origin))break;continue;}
                    TimeMultitoolBreakTracker.begin(pos);
                    try{removed=state.onDestroyedByPlayer(level,pos,player,harvest,state.getFluidState());TimeMultitoolBreakTracker.recordRemoval(pos,removed);}finally{removed=TimeMultitoolBreakTracker.finish();}
                    if(!removed){if(utility==null&&pos.equals(origin))break;continue;}lease.commit();
                }
                state.getBlock().destroy(level,pos,state);removedPositions.add(pos);if(pos.equals(origin))primary=true;
                if(!player.isCreative()){
                    if(harvest){Helpers.combineDrops(drops,Block.getDrops(state,level,pos,target.entity(),player,lootTool));state.spawnAfterBreak(level,pos,lootTool,true);totalXp=(int)Math.min(Integer.MAX_VALUE,(long)totalXp+Math.max(0,xp));}
                    player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));player.causeFoodExhaustion(0.005F);
                }
            }
            deliver(level,origin,player,stack,removedPositions,drops,totalXp);return primary;
        }finally{ACTIVE.remove();}
    }
    public static int energyCost(ItemStack stack,int raw){int unbreaking=stack.getEnchantmentLevel(Enchantments.UNBREAKING);return (int)Math.max(0,raw*(1-Math.min(1,Math.max(0,unbreaking)*.1)));}
    public static int instantCost(ItemStack stack,float hardness){return TimeMultitoolMiningPolicy.instantCost(Ability.INSTABREAK.getFeCost(),hardness,stack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY));}
    private static void deliver(ServerLevel level,BlockPos origin,ServerPlayer player,ItemStack stack,Set<BlockPos> removed,List<ItemStack> drops,int xp){
        var tool=(TimeMultitoolItem)stack.getItem();if(tool.canUseAbility(stack,Ability.SMELTER)&&stack.getDamageValue()<stack.getMaxDamage()){boolean[] flag={false};drops=Helpers.smeltDrops(level,drops,stack,player,flag);if(flag[0])ToggleableTool.smelterParticles(level,removed);}
        if(tool.canUseAbility(stack,Ability.DROPTELEPORT)&&!drops.isEmpty()){var handler=bound(level,stack);if(handler!=null)Helpers.teleportDrops(drops,handler,stack,player);}
        if(!drops.isEmpty())Helpers.dropDrops(drops,level,origin);if(xp>0)net.minecraft.world.entity.ExperienceOrb.award(level,net.minecraft.world.phys.Vec3.atCenterOf(origin),xp);
    }
    private static net.minecraftforge.items.IItemHandler bound(ServerLevel level,ItemStack stack){
        if(!stack.hasTag())return null;var data=stack.getTag().getCompound("boundinventory");var location=data.getCompound("boundinventory");String raw=location.contains("dimension")?location.getString("dimension"):location.getString("level");var id=ResourceLocation.tryParse(raw);int side=data.getInt("boundinventory_side");if(id==null||!data.contains("boundinventory_side",Tag.TAG_INT)||side<0||side>=6||!location.contains("blockpos",Tag.TAG_COMPOUND))return null;
        var destination=level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION,id));var pos=NbtUtils.readBlockPos(location.getCompound("blockpos"));if(destination==null||!TimeMultitoolTargets.loaded(destination,pos))return null;var be=TimeMultitoolTargets.entity(destination,pos);return be==null?null:be.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.values()[side]).orElse(null);
    }
}
