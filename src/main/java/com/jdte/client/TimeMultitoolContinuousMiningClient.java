package com.jdte.client;

import com.jdte.common.items.TimeMultitoolItem;
import com.jdte.setup.JDTEConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

public final class TimeMultitoolContinuousMiningClient {
    private static final TimeMultitoolContinuousMiningGate GATE = new TimeMultitoolContinuousMiningGate();
    private static ClientLevel activeLevel;
    private static int activeSlot = -1;

    private TimeMultitoolContinuousMiningClient() { }

    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || event.isCanceled()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (!activate(minecraft)) return;
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        int delay = JDTEConfig.LOCAL_SPEC.isLoaded()
                ? JDTEConfig.LOCAL.timeMultitool.continuousMiningHoldDelayMillis.get() : 250;
        if (!GATE.allowAttack(hit.getBlockPos().asLong(), Util.getMillis(), delay)) {
            event.setCanceled(true);
            event.setSwingHand(false);
            minecraft.gameMode.stopDestroyBlock();
        }
    }

    public static void onMouseButton(InputEvent.MouseButton.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.keyAttack.matchesMouse(event.getButton())) inputChanged(minecraft, event.getAction());
    }

    public static void onKey(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.keyAttack.matches(event.getKey(), event.getScanCode())) inputChanged(minecraft, event.getAction());
    }

    private static void inputChanged(Minecraft minecraft, int action) {
        if (action == GLFW.GLFW_RELEASE) {
            if (activeSlot >= 0 && minecraft.gameMode != null) minecraft.gameMode.stopDestroyBlock();
            reset();
        } else if (action == GLFW.GLFW_PRESS && activate(minecraft)) {
            GATE.press(Util.getMillis());
        }
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!activate(minecraft) || !minecraft.options.keyAttack.isDown()) reset();
    }

    private static boolean activate(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null
                || minecraft.screen != null || !minecraft.isWindowActive()
                || !(minecraft.player.getMainHandItem().getItem() instanceof TimeMultitoolItem)) {
            reset();
            return false;
        }
        int slot = minecraft.player.getInventory().selected;
        if (activeLevel != minecraft.level || activeSlot != slot) {
            GATE.reset();
            activeLevel = minecraft.level;
            activeSlot = slot;
        }
        return true;
    }

    private static void reset() {
        GATE.reset();
        activeLevel = null;
        activeSlot = -1;
    }
}
