package com.jdte.common.utils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class BioCrusherDropCapture {
    private static final ThreadLocal<CaptureContext> ACTIVE_CAPTURE = new ThreadLocal<>();

    private BioCrusherDropCapture() {
    }

    public static <T> CaptureResult<T> capture(LivingEntity target, Supplier<T> action) {
        CaptureContext context = new CaptureContext(target);
        if (ACTIVE_CAPTURE.get() != null) {
            return new CaptureResult<>(action.get(), List.of(), 0);
        }

        ACTIVE_CAPTURE.set(context);
        try {
            T value = action.get();
            List<ItemStack> drops = context.dropEvent != null && context.dropEvent.isCanceled()
                    ? List.of()
                    : List.copyOf(context.drops);
            return new CaptureResult<>(value, drops, context.experience);
        } finally {
            ACTIVE_CAPTURE.remove();
        }
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        CaptureContext context = ACTIVE_CAPTURE.get();
        if (context == null || context.target != event.getEntity()) return;

        context.dropEvent = event;
        event.getDrops().forEach(drop -> context.drops.add(drop.getItem().copy()));
        event.getDrops().clear();
    }

    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        CaptureContext context = ACTIVE_CAPTURE.get();
        if (context != null && context.target == event.getEntity()) {
            context.experience = Math.max(0, event.getDroppedExperience());
            context.experienceCaptured = true;
            event.setCanceled(true);
        }
    }

    public static void captureExperienceIfAbsent(ServerLevel level, LivingEntity entity, Player player) {
        CaptureContext context = ACTIVE_CAPTURE.get();
        if (context == null || context.target != entity || context.experienceCaptured) return;

        int baseExperience = entity.getExperienceReward(level, player);
        int finalExperience = EventHooks.getExperienceDrop(entity, player, baseExperience);
        if (!context.experienceCaptured) {
            context.experience = Math.max(0, finalExperience);
            context.experienceCaptured = true;
        }
    }

    public record CaptureResult<T>(T value, List<ItemStack> drops, int experience) {
    }

    private static final class CaptureContext {
        private final LivingEntity target;
        private final List<ItemStack> drops = new ArrayList<>();
        private LivingDropsEvent dropEvent;
        private int experience;
        private boolean experienceCaptured;

        private CaptureContext(LivingEntity target) {
            this.target = target;
        }
    }
}
