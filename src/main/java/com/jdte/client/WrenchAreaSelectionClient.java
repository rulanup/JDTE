package com.jdte.client;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.client.renderers.AreaPreviewRenderBatch;
import com.jdte.common.items.EclipseAlloyWrenchItem;
import com.jdte.common.network.data.WrenchAreaSelectionPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WrenchAreaSelectionClient {
    private static BlockPos firstCorner;
    private static BlockPos secondCorner;
    private static boolean attackHandled;

    private WrenchAreaSelectionClient() {
    }

    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || !isHoldingWrench(player)) {
            return;
        }
        if (attackHandled) {
            cancelAttack(event);
            return;
        }
        attackHandled = true;

        if (player.isShiftKeyDown()) {
            boolean hadSelection = firstCorner != null;
            clearSelection();
            player.displayClientMessage(
                    Component.translatable("message.jdte.wrench_selection.cancelled")
                            .withStyle(ChatFormatting.YELLOW), true);
            if (hadSelection) {
                player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.8F, 0.9F);
            }
            cancelAttack(event);
            return;
        }

        if (!(minecraft.hitResult instanceof BlockHitResult hitResult)
                || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos clickedPos = hitResult.getBlockPos().immutable();
        cancelAttack(event);
        if (firstCorner == null) {
            firstCorner = clickedPos;
            player.displayClientMessage(
                    Component.translatable("message.jdte.wrench_selection.first", format(clickedPos))
                            .withStyle(ChatFormatting.GREEN), true);
            player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.8F, 0.8F);
            return;
        }
        if (secondCorner == null) {
            secondCorner = clickedPos;
            int[] size = getSize(firstCorner, secondCorner);
            player.displayClientMessage(
                    Component.translatable("message.jdte.wrench_selection.second", size[0], size[1], size[2])
                            .withStyle(ChatFormatting.GREEN), true);
            player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.8F, 1.2F);
            return;
        }

        if (minecraft.level.getBlockEntity(clickedPos) instanceof AreaAffectingBE) {
            PacketDistributor.sendToServer(new WrenchAreaSelectionPayload(clickedPos, firstCorner, secondCorner));
            return;
        }

        if (minecraft.level.getBlockEntity(clickedPos) instanceof BaseMachineBE) {
            player.displayClientMessage(
                    Component.translatable("message.jdte.wrench_selection.not_area_machine")
                            .withStyle(ChatFormatting.RED), true);
            player.playSound(SoundEvents.ITEM_BREAK, 0.7F, 0.8F);
            return;
        }

        player.displayClientMessage(
                Component.translatable("message.jdte.wrench_selection.locked")
                        .withStyle(ChatFormatting.YELLOW), true);
        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 0.7F);
    }

    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (event.isCanceled() || secondCorner == null || !Screen.hasControlDown()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || !isHoldingWrench(player)) {
            return;
        }

        int delta = event.getScrollDeltaY() > 0.0D ? 1 : event.getScrollDeltaY() < 0.0D ? -1 : 0;
        if (delta == 0) return;

        BlockPos min = min(firstCorner, secondCorner);
        BlockPos max = max(firstCorner, secondCorner);
        AABB area = AABB.encapsulatingFullBlocks(min, max);
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        Direction face = findLookedAtFace(area, camera, player.getLookAngle(), 75.0D);
        if (face == null) return;

        // Match Create's selection behavior: scrolling from inside the box reverses the adjustment direction.
        if (area.contains(camera)) delta *= -1;
        BlockPos[] adjusted = adjustFace(min, max, face, delta, minecraft);
        firstCorner = adjusted[0];
        secondCorner = adjusted[1];

        int[] size = getSize(firstCorner, secondCorner);
        player.displayClientMessage(
                Component.translatable("message.jdte.wrench_selection.adjusted",
                        Component.translatable("message.jdte.wrench_selection.face." + face.getName()),
                        size[0], size[1], size[2]).withStyle(ChatFormatting.GREEN), true);
        event.setCanceled(true);
    }

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES || firstCorner == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !isHoldingWrench(minecraft.player)) {
            return;
        }

        BlockPos previewCorner = getPreviewCorner(minecraft);
        AABB area = AABB.encapsulatingFullBlocks(firstCorner, previewCorner);
        Direction selectedFace = secondCorner == null ? null : findLookedAtFace(area,
                minecraft.gameRenderer.getMainCamera().getPosition(), minecraft.player.getLookAngle(), 75.0D);
        AreaPreviewRenderBatch.enqueueMain(area, selectedFace);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.options.keyAttack.isDown()) {
            attackHandled = false;
        }
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (firstCorner == null || minecraft.player == null || minecraft.screen != null
                || !isHoldingWrench(minecraft.player)) {
            return;
        }

        int[] size = getSize(firstCorner, getPreviewCorner(minecraft));
        Component text = Component.translatable("message.jdte.wrench_selection.size", size[0], size[1], size[2]);
        int x = event.getGuiGraphics().guiWidth() / 2;
        int y = event.getGuiGraphics().guiHeight() / 2 + 28;
        event.getGuiGraphics().drawCenteredString(minecraft.font, text, x, y, 0x55FF55);
    }

    public static void clearSelection() {
        firstCorner = null;
        secondCorner = null;
    }

    private static void cancelAttack(InputEvent.InteractionKeyMappingTriggered event) {
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    private static BlockPos getPreviewCorner(Minecraft minecraft) {
        if (secondCorner != null) {
            return secondCorner;
        }
        if (minecraft.hitResult instanceof BlockHitResult hitResult
                && hitResult.getType() == HitResult.Type.BLOCK) {
            return hitResult.getBlockPos();
        }
        return firstCorner;
    }

    private static int[] getSize(BlockPos first, BlockPos second) {
        return new int[]{
                Math.abs(first.getX() - second.getX()) + 1,
                Math.abs(first.getY() - second.getY()) + 1,
                Math.abs(first.getZ() - second.getZ()) + 1
        };
    }

    private static Direction findLookedAtFace(AABB area, Vec3 origin, Vec3 direction, double reach) {
        Direction result = null;
        double nearest = Double.POSITIVE_INFINITY;
        for (Direction face : Direction.values()) {
            Direction.Axis axis = face.getAxis();
            double component = axisValue(direction, axis);
            if (Math.abs(component) < 1.0E-7D) continue;
            double plane = face.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                    ? axisMin(area, axis) : axisMax(area, axis);
            double distance = (plane - axisValue(origin, axis)) / component;
            if (distance < 0.0D || distance > reach || distance >= nearest) continue;
            Vec3 hit = origin.add(direction.scale(distance));
            if (insideFace(area, hit, axis)) {
                nearest = distance;
                result = face;
            }
        }
        return result;
    }

    private static BlockPos[] adjustFace(BlockPos min, BlockPos max, Direction face, int delta,
                                         Minecraft minecraft) {
        int minX = min.getX();
        int minY = min.getY();
        int minZ = min.getZ();
        int maxX = max.getX();
        int maxY = max.getY();
        int maxZ = max.getZ();
        switch (face) {
            case WEST -> minX = Math.min(maxX, minX + delta);
            case EAST -> maxX = Math.max(minX, maxX - delta);
            case DOWN -> minY = Math.max(minecraft.level.getMinBuildHeight(), Math.min(maxY, minY + delta));
            case UP -> maxY = Math.min(minecraft.level.getMaxBuildHeight() - 1, Math.max(minY, maxY - delta));
            case NORTH -> minZ = Math.min(maxZ, minZ + delta);
            case SOUTH -> maxZ = Math.max(minZ, maxZ - delta);
        }
        return new BlockPos[]{new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ)};
    }

    private static boolean insideFace(AABB area, Vec3 point, Direction.Axis ignoredAxis) {
        double epsilon = 1.0E-5D;
        return (ignoredAxis == Direction.Axis.X || point.x >= area.minX - epsilon && point.x <= area.maxX + epsilon)
                && (ignoredAxis == Direction.Axis.Y || point.y >= area.minY - epsilon && point.y <= area.maxY + epsilon)
                && (ignoredAxis == Direction.Axis.Z || point.z >= area.minZ - epsilon && point.z <= area.maxZ + epsilon);
    }

    private static double axisValue(Vec3 vector, Direction.Axis axis) {
        return switch (axis) {
            case X -> vector.x;
            case Y -> vector.y;
            case Z -> vector.z;
        };
    }

    private static double axisMin(AABB area, Direction.Axis axis) {
        return switch (axis) {
            case X -> area.minX;
            case Y -> area.minY;
            case Z -> area.minZ;
        };
    }

    private static double axisMax(AABB area, Direction.Axis axis) {
        return switch (axis) {
            case X -> area.maxX;
            case Y -> area.maxY;
            case Z -> area.maxZ;
        };
    }

    private static BlockPos min(BlockPos first, BlockPos second) {
        return new BlockPos(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()),
                Math.min(first.getZ(), second.getZ()));
    }

    private static BlockPos max(BlockPos first, BlockPos second) {
        return new BlockPos(Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()),
                Math.max(first.getZ(), second.getZ()));
    }

    private static String format(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    private static boolean isHoldingWrench(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.getItem() instanceof EclipseAlloyWrenchItem
                || offHand.getItem() instanceof EclipseAlloyWrenchItem;
    }
}
