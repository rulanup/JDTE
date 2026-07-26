package com.jdte.client.screens.util;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.JDTE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.utils.UpgradeSlotStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FilterPageWidgetHelper {
    private static final ResourceLocation FILTER_PREV = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/filter_prev.png");
    private static final ResourceLocation FILTER_NEXT = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/filter_next.png");

    private FilterPageWidgetHelper() {
    }

    public static int getMaxFilterPage(BaseMachineContainer container, BaseMachineBE baseMachineBE, int slotsPerPage) {
        int baseSlots = UpgradeSlotStorage.getBaseFilterSlots(container);
        if (baseSlots <= 0 && container.filterHandler != null) {
            baseSlots = UpgradeHelper.getBaseFilterSlots(container.filterHandler);
        }
        int activeSlots = UpgradeHelper.getActiveFilterSlots(baseMachineBE, baseSlots);
        return Math.max(0, (activeSlots - 1) / slotsPerPage);
    }

    public static void renderButtons(GuiGraphics guiGraphics, Font font, int prevX, int nextX, int y,
                                     int currentPage, int maxPage, int pressed) {
        boolean prevActive = currentPage > 0;
        boolean nextActive = currentPage < maxPage;

        RenderSystem.setShaderColor(1f, 1f, 1f, prevActive ? 1f : 0.3f);
        PoseStack poseStack = guiGraphics.pose();
        if (pressed == -1) {
            poseStack.pushPose();
            poseStack.translate(prevX + 6, y + 6, 0);
            poseStack.scale(0.75f, 0.75f, 1.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            guiGraphics.blit(FILTER_NEXT, -8, -8, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            poseStack.translate(prevX, y, 0);
            poseStack.scale(0.75f, 0.75f, 1.0f);
            guiGraphics.blit(FILTER_PREV, 0, 0, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, nextActive ? 1f : 0.3f);
        if (pressed == 1) {
            poseStack.pushPose();
            poseStack.translate(nextX, y, 0);
            poseStack.scale(0.75f, 0.75f, 1.0f);
            guiGraphics.blit(FILTER_NEXT, 0, 0, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            poseStack.translate(nextX + 6, y + 6, 0);
            poseStack.scale(0.75f, 0.75f, 1.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            guiGraphics.blit(FILTER_PREV, -8, -8, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.drawString(font, Component.literal((currentPage + 1) + "/" + (maxPage + 1)), nextX + 12 + 2, y + 2, 0xFF404040, false);
    }
}
