package com.jdte.mixin;

import com.jdte.common.blockentities.EntitySuppressorManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = @At("HEAD"), cancellable = true)
    private void jdte$suppressBlockEntityRender(BlockEntity blockEntity, float partialTick,
                                                PoseStack poseStack, MultiBufferSource bufferSource,
                                                CallbackInfo ci) {
        if (EntitySuppressorManager.shouldSuppressBlockEntityRender(blockEntity)) {
            ci.cancel();
        }
    }
}
