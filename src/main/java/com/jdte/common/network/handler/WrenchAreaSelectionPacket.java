package com.jdte.common.network.handler;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.network.data.WrenchAreaSelectionPayload;
import com.jdte.common.upgrades.UpgradeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class WrenchAreaSelectionPacket {
    private static final WrenchAreaSelectionPacket INSTANCE = new WrenchAreaSelectionPacket();

    public static WrenchAreaSelectionPacket get() {
        return INSTANCE;
    }

    public void handleServer(WrenchAreaSelectionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!player.mayBuild() || !isHoldingWrench(player)
                    || player.distanceToSqr(payload.machinePos().getCenter()) > 64.0D) {
                showFailure(player, payload.machinePos(), "message.jdte.wrench_selection.failed");
                return;
            }

            BlockEntity blockEntity = player.level().getBlockEntity(payload.machinePos());
            if (!(blockEntity instanceof BaseMachineBE machine) || !(blockEntity instanceof AreaAffectingBE areaMachine)) {
                showFailure(player, payload.machinePos(), "message.jdte.wrench_selection.not_area_machine");
                return;
            }

            BlockPos min = min(payload.firstCorner(), payload.secondCorner());
            BlockPos max = max(payload.firstCorner(), payload.secondCorner());
            int sizeX = max.getX() - min.getX() + 1;
            int sizeY = max.getY() - min.getY() + 1;
            int sizeZ = max.getZ() - min.getZ() + 1;
            double radiusX = (sizeX - 1) / 2.0D;
            double radiusY = (sizeY - 1) / 2.0D;
            double radiusZ = (sizeZ - 1) / 2.0D;
            int offsetX = min.getX() - payload.machinePos().getX() + (int) Math.floor(radiusX);
            int offsetY = min.getY() - payload.machinePos().getY() + (int) Math.floor(radiusY);
            int offsetZ = min.getZ() - payload.machinePos().getZ() + (int) Math.floor(radiusZ);

            double maxRadius = UpgradeHelper.getMaxAreaRadius(machine);
            int maxOffset = UpgradeHelper.getMaxAreaOffset(machine);
            if (radiusX > maxRadius || radiusY > maxRadius || radiusZ > maxRadius
                    || Math.abs(offsetX) > maxOffset || Math.abs(offsetY) > maxOffset
                    || Math.abs(offsetZ) > maxOffset) {
                player.displayClientMessage(
                        Component.translatable("message.jdte.wrench_selection.too_large", maxRadius, maxOffset)
                                .withStyle(ChatFormatting.RED), true);
                player.level().playSound(null, payload.machinePos(), SoundEvents.ITEM_BREAK,
                        SoundSource.BLOCKS, 0.8F, 0.8F);
                return;
            }

            areaMachine.setAreaSettings(radiusX, radiusY, radiusZ, offsetX, offsetY, offsetZ,
                    areaMachine.getAreaAffectingData().renderArea);
            player.displayClientMessage(
                    Component.translatable("message.jdte.wrench_selection.applied", sizeX, sizeY, sizeZ)
                            .withStyle(ChatFormatting.GREEN), true);
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
        });
    }

    private static boolean isHoldingWrench(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.getItem() instanceof EclipseAlloyWrenchItem
                || offHand.getItem() instanceof EclipseAlloyWrenchItem;
    }

    private static void showFailure(ServerPlayer player, BlockPos pos, String translationKey) {
        player.displayClientMessage(Component.translatable(translationKey).withStyle(ChatFormatting.RED), true);
        player.level().playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8F, 0.8F);
    }

    private static BlockPos min(BlockPos first, BlockPos second) {
        return new BlockPos(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()),
                Math.min(first.getZ(), second.getZ()));
    }

    private static BlockPos max(BlockPos first, BlockPos second) {
        return new BlockPos(Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()),
                Math.max(first.getZ(), second.getZ()));
    }
}
