package com.jdte.common.items;
import net.minecraftforge.event.entity.player.PlayerEvent;
/** Multiplier runs after native JDT's own area/instant/energy speed adjustment. */
public final class TimeMultitoolMiningEvents {
    private TimeMultitoolMiningEvents() {}
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event){
        var player=event.getEntity();var stack=player.getMainHandItem();if(!(stack.getItem() instanceof TimeMultitoolItem tool))return;
        var single=tool.miningBatchDecision(stack,1);if(!single.powered()||single.speedMultiplier()<=1)return;
        var pos=event.getPosition().orElse(null);if(pos==null||!TimeMultitoolTargets.loaded(player.level(),pos))return;
        var targets=tool.getBreakBlockPositions(stack,player.level(),pos,player,event.getState());var decision=tool.miningBatchDecision(stack,(int)targets.stream().filter(p->!TimeMultitoolTargets.state(player.level(),p).isAir()).count());
        if(decision.powered()&&decision.speedMultiplier()>1&&Float.isFinite(event.getNewSpeed()))event.setNewSpeed((float)Math.min(Float.MAX_VALUE,(double)event.getNewSpeed()*decision.speedMultiplier()));
    }
}
