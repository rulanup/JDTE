package com.jdte.client.screens.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Maps the GUI sprite IDs introduced after 1.20.1 to the texture files that
 * exist in the Forge JDT runtime.
 */
public final class GuiSpriteCompat {
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "justdirethings", "textures/gui/justslot.png");
    private static final ResourceLocation BREWING_STAND_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/gui/container/brewing_stand.png");
    private static final String VANILLA_NAMESPACE = "minecraft";
    private static final String SLOT_SPRITE = "container/slot";
    private static final String BACKGROUND_SPRITE = "background";
    private static final int BACKGROUND_OUTLINE = 0xFF000000;
    private static final int BACKGROUND_HIGHLIGHT = 0xFFFFFFFF;
    private static final int BACKGROUND_FILL = 0xFFC6C6C6;
    private static final int BACKGROUND_SHADOW = 0xFF555555;
    private static final int SLOT_TEXTURE_WIDTH = 256;
    private static final int SLOT_TEXTURE_HEIGHT = 256;
    private static final int POWER_BAR_TEXTURE_WIDTH = 34;
    private static final int POWER_BAR_TEXTURE_HEIGHT = 72;

    private GuiSpriteCompat() {
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        if (isSlot(sprite)) {
            // justslot.png is an atlas in the Forge JDT build.  Passing 18x18
            // as the texture size would scale the complete atlas into a slot.
            graphics.blit(SLOT_TEXTURE, x, y, 0, 0, width, height,
                    SLOT_TEXTURE_WIDTH, SLOT_TEXTURE_HEIGHT);
            return;
        }
        if (isBackground(sprite)) {
            blitBackground(graphics, x, y, width, height);
            return;
        }
        graphics.blit(sprite, x, y, 0, 0, width, height, 256, 256);
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite,
                                  int spriteWidth, int spriteHeight, int sourceX, int sourceY,
                                  int x, int y, int width, int height) {
        if (isBrewingSprite(sprite, "container/brewing_stand/fuel_length")) {
            graphics.blit(BREWING_STAND_TEXTURE, x, y, 176 + sourceX, 29 + sourceY, width, height, 256, 256);
            return;
        }
        if (isBrewingSprite(sprite, "container/brewing_stand/brew_progress")) {
            graphics.blit(BREWING_STAND_TEXTURE, x, y, 176 + sourceX, sourceY, width, height, 256, 256);
            return;
        }
        if (isBrewingSprite(sprite, "container/brewing_stand/bubbles")) {
            graphics.blit(BREWING_STAND_TEXTURE, x, y, 185 + sourceX, sourceY, width, height, 256, 256);
            return;
        }
        if (isSlot(sprite)) {
            graphics.blit(SLOT_TEXTURE, x, y, sourceX, sourceY, width, height,
                    SLOT_TEXTURE_WIDTH, SLOT_TEXTURE_HEIGHT);
            return;
        }
        graphics.blit(sprite, x, y, sourceX, sourceY, width, height, spriteWidth, spriteHeight);
    }

    public static void blitPowerBar(GuiGraphics graphics, ResourceLocation texture,
                                    int x, int y, int fillHeight) {
        int clampedFill = Math.max(0, Math.min(70, fillHeight));
        graphics.blit(texture, x, y, 0, 0, 18, POWER_BAR_TEXTURE_HEIGHT,
                POWER_BAR_TEXTURE_WIDTH, POWER_BAR_TEXTURE_HEIGHT);
        if (clampedFill > 0) {
            graphics.blit(texture, x + 1, y + 70 - clampedFill,
                    18, 70 - clampedFill, 16, clampedFill,
                    POWER_BAR_TEXTURE_WIDTH, POWER_BAR_TEXTURE_HEIGHT);
        }
    }

    private static boolean isSlot(ResourceLocation sprite) {
        return VANILLA_NAMESPACE.equals(sprite.getNamespace()) && SLOT_SPRITE.equals(sprite.getPath());
    }

    private static boolean isBackground(ResourceLocation sprite) {
        return "justdirethings".equals(sprite.getNamespace())
                && (BACKGROUND_SPRITE.equals(sprite.getPath())
                || "textures/gui/sprites/background.png".equals(sprite.getPath()));
    }

    private static boolean isBrewingSprite(ResourceLocation sprite, String path) {
        return VANILLA_NAMESPACE.equals(sprite.getNamespace()) && path.equals(sprite.getPath());
    }

    private static void blitBackground(GuiGraphics graphics, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int brightInset = Math.min(3, Math.min(width, height) / 2);
        int fillInset = Math.min(3, Math.min(width, height) / 2);

        // Forge's pre-1.21 GUI path has no sprite metadata support.  The JDT
        // background is a four-color frame, so draw that frame directly and
        // avoid UV repetition when JEI stretches recipe panels.
        graphics.fill(x, y, x + width, y + height, BACKGROUND_OUTLINE);
        graphics.fill(x + 1, y + 1, x + width - brightInset, y + height - brightInset,
                BACKGROUND_HIGHLIGHT);
        graphics.fill(x + fillInset, y + fillInset, x + width - fillInset, y + height - fillInset,
                BACKGROUND_FILL);
        if (width > 3 && height > 3) {
            graphics.fill(x + fillInset, y + height - fillInset, x + width - 1, y + height - 1,
                    BACKGROUND_SHADOW);
            graphics.fill(x + width - fillInset, y + fillInset, x + width - 1, y + height - fillInset,
                    BACKGROUND_SHADOW);
        }
    }
}
