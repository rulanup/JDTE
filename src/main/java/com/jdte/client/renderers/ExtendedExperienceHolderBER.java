package com.jdte.client.renderers;

import com.direwolf20.justdirethings.client.blockentityrenders.baseber.AreaAffectingBER;
import com.jdte.common.blockentities.ExtendedExperienceHolderBE;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ExtendedExperienceHolderBER extends AreaAffectingBER {
    public static final ItemStack ITEM_STACK = new ItemStack(Items.EXPERIENCE_BOTTLE);

    public ExtendedExperienceHolderBER(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        super.render(blockEntity, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
        if (blockEntity instanceof ExtendedExperienceHolderBE experienceHolder) {
            renderItemStack(experienceHolder, poseStack, buffer, combinedOverlay);
        }
    }

    private void renderItemStack(ExtendedExperienceHolderBE blockEntity, PoseStack poseStack,
                                 MultiBufferSource buffer, int combinedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Direction direction = blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite();
        long millis = System.currentTimeMillis();

        poseStack.pushPose();
        poseStack.translate(
                0.5F + direction.getStepX() * 0.3F,
                0.5F + direction.getStepY() * 0.3F,
                0.5F + direction.getStepZ() * 0.3F);
        poseStack.mulPose(Axis.XP.rotationDegrees(direction.getStepZ() * -90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(direction.getStepX() * 90));
        poseStack.mulPose(Axis.XP.rotationDegrees(direction.getStepY() == 1 ? 0 : 180));
        float angle = (millis / 15) % 360;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.scale(0.15F, 0.15F, 0.15F);
        itemRenderer.renderStatic(ITEM_STACK, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
                combinedOverlay, poseStack, buffer, Minecraft.getInstance().level, 0);
        poseStack.popPose();
    }
}
