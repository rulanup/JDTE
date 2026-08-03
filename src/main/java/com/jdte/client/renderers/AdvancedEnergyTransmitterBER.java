package com.jdte.client.renderers;

import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AdvancedEnergyTransmitterBER extends AreaAffectingBER {
    private static final ItemStack TIME_CRYSTAL = new ItemStack(Registration.TimeCrystal.get());

    public AdvancedEnergyTransmitterBER(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        super.render(blockEntity, partialTicks, poseStack, buffers, packedLight, packedOverlay);
        if (blockEntity instanceof AdvancedEnergyTransmitterBE transmitter) {
            renderTimeCrystal(transmitter, poseStack, buffers, packedOverlay);
        }
    }

    private static void renderTimeCrystal(AdvancedEnergyTransmitterBE transmitter, PoseStack poseStack,
                                          MultiBufferSource buffers, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Direction direction = transmitter.getBlockState().getValue(BlockStateProperties.FACING).getOpposite();

        poseStack.pushPose();
        poseStack.translate(
                0.5F + direction.getStepX() * 0.3F,
                0.5F + direction.getStepY() * 0.3F,
                0.5F + direction.getStepZ() * 0.3F);
        poseStack.mulPose(Axis.XP.rotationDegrees(direction.getStepZ() * -90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(direction.getStepX() * 90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(direction.getStepY() == 1 ? 0.0F : 180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees((System.currentTimeMillis() / 15L) % 360L));
        poseStack.scale(0.15F, 0.15F, 0.15F);
        itemRenderer.renderStatic(TIME_CRYSTAL, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
                packedOverlay, poseStack, buffers, Minecraft.getInstance().level, 0);
        poseStack.popPose();
    }
}