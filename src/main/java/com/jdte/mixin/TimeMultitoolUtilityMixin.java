package com.jdte.mixin;
import com.direwolf20.justdirethings.common.items.interfaces.AbilityMethods;
import com.jdte.common.items.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Keep original bindings; only this item gets loaded-only, permission-aware utility removal. */
@Mixin(value=AbilityMethods.class,remap=false)
public abstract class TimeMultitoolUtilityMixin {
    @Inject(method="lawnmower(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/BlockPos;)Z",at=@At("HEAD"),cancellable=true,remap=false)
    private static void jdte$lawn(Level level,Player player,ItemStack stack,BlockPos pos,CallbackInfoReturnable<Boolean> cir){if(stack.getItem() instanceof TimeMultitoolItem)cir.setReturnValue(TimeMultitoolMining.lawn(level,player,stack,pos));}
    @Inject(method="leafbreaker",at=@At("HEAD"),cancellable=true,remap=false)
    private static void jdte$leaves(UseOnContext ctx,CallbackInfoReturnable<Boolean> cir){if(ctx.getItemInHand().getItem() instanceof TimeMultitoolItem)cir.setReturnValue(TimeMultitoolMining.leaves(ctx));}
}
