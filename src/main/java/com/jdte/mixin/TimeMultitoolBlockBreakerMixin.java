package com.jdte.mixin;
import com.direwolf20.justdirethings.common.blockentities.BlockBreakerT1BE;
import com.jdte.common.items.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Avoid the older native breaker's second drop/removal pass for the Neo-style multitool transaction. */
@Mixin(value=BlockBreakerT1BE.class,remap=false)
public abstract class TimeMultitoolBlockBreakerMixin {
    @Inject(method="tryBreakBlock",at=@At("HEAD"),cancellable=true,remap=false)
    private void jdte$toolTransaction(ItemStack tool,FakePlayer actor,BlockPos pos,BlockState state,CallbackInfoReturnable<Boolean> cir){
        if(!(tool.getItem() instanceof TimeMultitoolItem))return;var owner=(BlockBreakerT1BE)(Object)this;
        if(!(owner.getLevel() instanceof ServerLevel level)||owner.isRemoved()||!TimeMultitoolTargets.loaded(level,owner.getBlockPos())||level.getBlockEntity(owner.getBlockPos())!=owner||!TimeMultitoolTargets.loaded(level,pos)||TimeMultitoolTargets.state(level,pos)!=state){cir.setReturnValue(false);return;}
        owner.setFakePlayerData(tool,actor,pos,owner.getFacing());cir.setReturnValue(TimeMultitoolMining.mine(level,pos,actor,tool));
    }
}
