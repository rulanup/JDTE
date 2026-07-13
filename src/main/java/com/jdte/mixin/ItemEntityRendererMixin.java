package com.jdte.mixin;

import com.jdte.common.blockentities.EntitySuppressorManager;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {
    @ModifyVariable(
            method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), argsOnly = true, index = 3)
    private float jdte$freezeItemRenderPartialTick(float partialTick, ItemEntity itemEntity) {
        return EntitySuppressorManager.shouldSuppressEntityVisual(itemEntity) ? 0.0F : partialTick;
    }
}
