package com.jdte.mixin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Intercept before Forge's outer break event so each primary/secondary target gets exactly one event. */
@Mixin(ServerPlayerGameMode.class)
public abstract class TimeMultitoolGameModeMixin {
    @Shadow @Final protected ServerPlayer player;
    @Shadow protected ServerLevel level;
    @Inject(method="destroyBlock",at=@At("HEAD"),cancellable=true)
    private void jdte$mineTool(BlockPos pos,CallbackInfoReturnable<Boolean> cir){var stack=player.getMainHandItem();if(stack.getItem() instanceof com.jdte.common.items.TimeMultitoolItem)cir.setReturnValue(com.jdte.common.items.TimeMultitoolMining.mine(level,pos,player,stack));}
}
