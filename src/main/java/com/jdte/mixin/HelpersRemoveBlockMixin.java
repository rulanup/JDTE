package com.jdte.mixin;

import com.direwolf20.justdirethings.common.items.interfaces.Helpers;
import com.jdte.common.items.TimeMultitoolBreakTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Helpers.class, remap = false)
public abstract class HelpersRemoveBlockMixin {
    @Inject(method = "removeBlock", at = @At("RETURN"))
    private static void jdte$captureTimeMultitoolRemoval(ServerLevel level, ServerPlayer player,
                                                         BlockPos pos, BlockState state, boolean canHarvest,
                                                         CallbackInfoReturnable<Boolean> cir) {
        TimeMultitoolBreakTracker.recordRemoval(pos, cir.getReturnValueZ());
    }
}
