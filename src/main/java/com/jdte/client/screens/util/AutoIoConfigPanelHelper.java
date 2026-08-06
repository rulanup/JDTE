package com.jdte.client.screens.util;

import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.autoioconfig.AutoIoConfigData;
import com.jdte.common.autoioconfig.AutoIoConfigHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class AutoIoConfigPanelHelper {
    private static final ResourceLocation IO_NORTH = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-north.png");
    private static final ResourceLocation IO_SOUTH = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-south.png");
    private static final ResourceLocation IO_WEST = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-west.png");
    private static final ResourceLocation IO_EAST = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-east.png");
    private static final ResourceLocation IO_UP = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-up.png");
    private static final ResourceLocation IO_DOWN = ResourceLocation.fromNamespaceAndPath("justdirethings", "textures/gui/buttons/direction-down.png");

    public static final int BUTTON_SIZE = 12;
    public static final int BUTTON_SPACING = 12;
    public static final int PANEL_PADDING = 6;
    public static final int PANEL_SIZE = PANEL_PADDING * 2 + BUTTON_SPACING * 3;

    public static final int SIDE_NORTH = 0;
    public static final int SIDE_SOUTH = 1;
    public static final int SIDE_WEST = 2;
    public static final int SIDE_EAST = 3;
    public static final int SIDE_UP = 4;
    public static final int SIDE_DOWN = 5;

    private AutoIoConfigPanelHelper() {
    }

    public static void renderPanel(GuiGraphics guiGraphics, ResourceLocation background, int panelX, int panelY,
                                   int inputMask, int outputMask) {
        GuiSpriteCompat.blitSprite(guiGraphics, background, panelX, panelY, PANEL_SIZE, PANEL_SIZE);

        drawSide(guiGraphics, panelX, panelY, inputMask, outputMask, SIDE_NORTH);
        drawSide(guiGraphics, panelX, panelY, inputMask, outputMask, SIDE_WEST);
        drawSide(guiGraphics, panelX, panelY, inputMask, outputMask, SIDE_UP);
        drawSide(guiGraphics, panelX, panelY, inputMask, outputMask, SIDE_EAST);
        drawSide(guiGraphics, panelX, panelY, inputMask, outputMask, SIDE_SOUTH);
        drawSide(guiGraphics, panelX, panelY, inputMask, outputMask, SIDE_DOWN);
    }

    private static void drawSide(GuiGraphics guiGraphics, int panelX, int panelY, int inputMask, int outputMask, int side) {
        drawSideButton(guiGraphics, getSideX(panelX, side), getSideY(panelY, side),
                getSideIcon(side), AutoIoConfigHelper.getMode(inputMask, outputMask, side));
    }

    public static int getSideAt(int panelX, int panelY, double mouseX, double mouseY) {
        for (int side = 0; side < AutoIoConfigData.SIDE_COUNT; side++) {
            if (MiscTools.inBounds(getSideX(panelX, side), getSideY(panelY, side), BUTTON_SIZE, BUTTON_SIZE, mouseX, mouseY)) {
                return side;
            }
        }
        return -1;
    }

    public static int getSideX(int panelX, int side) {
        int col = switch (side) {
            case SIDE_WEST -> 0;
            case SIDE_NORTH, SIDE_SOUTH, SIDE_UP -> 1;
            default -> 2;
        };
        return panelX + PANEL_PADDING + col * BUTTON_SPACING;
    }

    public static int getSideY(int panelY, int side) {
        int row = switch (side) {
            case SIDE_NORTH -> 0;
            case SIDE_WEST, SIDE_EAST, SIDE_UP -> 1;
            default -> 2;
        };
        return panelY + PANEL_PADDING + row * BUTTON_SPACING;
    }

    private static ResourceLocation getSideIcon(int side) {
        return switch (side) {
            case SIDE_NORTH -> IO_NORTH;
            case SIDE_SOUTH -> IO_SOUTH;
            case SIDE_WEST -> IO_WEST;
            case SIDE_EAST -> IO_EAST;
            case SIDE_UP -> IO_UP;
            default -> IO_DOWN;
        };
    }

    private static void drawSideButton(GuiGraphics guiGraphics, int x, int y, ResourceLocation icon, int mode) {
        float red;
        float green;
        float blue;
        switch (mode) {
            case AutoIoConfigHelper.MODE_BOTH -> {
                red = 1.0F;
                green = 1.0F;
                blue = 1.0F;
            }
            case AutoIoConfigHelper.MODE_INPUT -> {
                red = 1.0F;
                green = 0.55F;
                blue = 0.1F;
            }
            case AutoIoConfigHelper.MODE_OUTPUT -> {
                red = 0.25F;
                green = 0.55F;
                blue = 1.0F;
            }
            default -> {
                red = 0.33F;
                green = 0.33F;
                blue = 0.33F;
            }
        }

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(0.75F, 0.75F, 1.0F);
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        guiGraphics.blit(icon, 0, 0, 0, 0, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    public static void drawSmallIconButton(GuiGraphics guiGraphics, int x, int y, ResourceLocation icon, boolean active) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(0.75f, 0.75f, 1.0f);
        RenderSystem.setShaderColor(active ? 1.0f : 0.33f, active ? 1.0f : 0.33f, active ? 1.0f : 0.33f, 1.0f);
        guiGraphics.blit(icon, 0, 0, 0, 0, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        poseStack.popPose();
    }

    public static String getModeTranslationKey(int mode) {
        return switch (mode) {
            case AutoIoConfigHelper.MODE_BOTH -> "jdte.screen.io_config.both";
            case AutoIoConfigHelper.MODE_INPUT -> "jdte.screen.io_config.input";
            case AutoIoConfigHelper.MODE_OUTPUT -> "jdte.screen.io_config.output";
            default -> "jdte.screen.io_config.disabled";
        };
    }

    public static String getAvailabilityTranslationKey(boolean input, boolean output) {
        if (input && output) return "jdte.screen.io_config.available.both";
        if (input) return "jdte.screen.io_config.available.input";
        if (output) return "jdte.screen.io_config.available.output";
        return "jdte.screen.io_config.available.none";
    }

    public static String getSideTranslationKey(int side) {
        return switch (side) {
            case SIDE_NORTH -> "jdte.screen.io_config.north";
            case SIDE_SOUTH -> "jdte.screen.io_config.south";
            case SIDE_WEST -> "jdte.screen.io_config.west";
            case SIDE_EAST -> "jdte.screen.io_config.east";
            case SIDE_UP -> "jdte.screen.io_config.up";
            default -> "jdte.screen.io_config.down";
        };
    }
}
