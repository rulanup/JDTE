package com.jdte.mixin;

import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge lacks Neo BlockDropsEvent; preserve actual playerDestroy while batching native block-pop drops. */
@Mixin(Block.class)
public abstract class TimeMultitoolDropsMixin {
    @Inject(method="popResource",at=@At("HEAD"),cancellable=true)
    private static void jdte$captureDrop(Level level,BlockPos pos,ItemStack stack,CallbackInfo ci){if(com.jdte.common.items.TimeMultitoolMining.captureDrop(level,stack))ci.cancel();}
    @Inject(method="popResourceFromFace",at=@At("HEAD"),cancellable=true)
    private static void jdte$captureFaceDrop(Level level,BlockPos pos,Direction face,ItemStack stack,CallbackInfo ci){if(com.jdte.common.items.TimeMultitoolMining.captureDrop(level,stack))ci.cancel();}
}
