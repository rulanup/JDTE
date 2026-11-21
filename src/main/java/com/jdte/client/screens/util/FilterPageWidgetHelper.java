package com.jdte.client.screens.util;

import net.minecraft.client.renderer.RenderPipelines;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.JDTE;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.utils.UpgradeSlotStorage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class FilterPageWidgetHelper {
    private static final Identifier FILTER_PREV = Identifier.fromNamespaceAndPath(JDTE.MODID, "textures/gui/filter_prev.png");
    private static final Identifier FILTER_NEXT = Identifier.fromNamespaceAndPath(JDTE.MODID, "textures/gui/filter_next.png");

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

    public static void renderButtons(GuiGraphicsExtractor guiGraphics, Font font, int prevX, int nextX, int y,
                                     int currentPage, int maxPage, int pressed) {
        boolean prevActive = currentPage > 0;
        boolean nextActive = currentPage < maxPage;

        int prevColor = prevActive ? -1 : 0x4DFFFFFF;
        var poseStack = guiGraphics.pose();
        if (pressed == -1) {
            poseStack.pushMatrix();
            poseStack.translate(prevX + 6, y + 6);
            poseStack.scale(0.75f, 0.75f);
            poseStack.rotate((float) Math.toRadians(180));
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FILTER_NEXT, -8, -8, 0, 0, 16, 16, 16, 16, prevColor);
            poseStack.popMatrix();
        } else {
            poseStack.pushMatrix();
            poseStack.translate(prevX, y);
            poseStack.scale(0.75f, 0.75f);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FILTER_PREV, 0, 0, 0, 0, 16, 16, 16, 16, prevColor);
            poseStack.popMatrix();
        }

        int nextColor = nextActive ? -1 : 0x4DFFFFFF;
        if (pressed == 1) {
            poseStack.pushMatrix();
            poseStack.translate(nextX, y);
            poseStack.scale(0.75f, 0.75f);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FILTER_NEXT, 0, 0, 0, 0, 16, 16, 16, 16, nextColor);
            poseStack.popMatrix();
        } else {
            poseStack.pushMatrix();
            poseStack.translate(nextX + 6, y + 6);
            poseStack.scale(0.75f, 0.75f);
            poseStack.rotate((float) Math.toRadians(180));
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FILTER_PREV, -8, -8, 0, 0, 16, 16, 16, 16, nextColor);
            poseStack.popMatrix();
        }

        guiGraphics.text(font, Component.literal((currentPage + 1) + "/" + (maxPage + 1)), nextX + 12 + 2, y + 2, 0xFF404040, false);
    }
}
