package com.jdte.client;

import com.direwolf20.justdirethings.client.screens.ClickerT2Screen;
import com.jdte.common.blockentities.ExtendedClickerBE;
import com.jdte.mixin.BaseMachineScreenAccessor;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class ScreenEventHandlers {
    private ScreenEventHandlers() {
    }

    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (event.getScreen() instanceof UpgradePopupDragHandler handler
                && handler.jdte$dragUpgradePopup(event.getMouseX(), event.getMouseY(), event.getMouseButton())) {
            event.setCanceled(true);
        }
    }

    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getScreen() instanceof UpgradePopupDragHandler handler) {
            handler.jdte$releaseUpgradePopup(event.getButton());
        }
    }

    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof ClickerT2Screen screen
                && ((BaseMachineScreenAccessor) screen).jdte$getBaseMachineBE() instanceof ExtendedClickerBE clicker) {
            screen.clickType = clicker.clickType;
            screen.clickTarget = clicker.clickTarget.ordinal();
            screen.sneaking = clicker.sneaking;
            screen.showFakePlayer = clicker.showFakePlayer;
            screen.maxHoldTicks = clicker.maxHoldTicks;
        }
    }
}
