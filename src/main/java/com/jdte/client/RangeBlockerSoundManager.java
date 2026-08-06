package com.jdte.client;

import com.jdte.JDTE;
import com.jdte.common.blockentities.RangeBlockerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = JDTE.MODID, value = Dist.CLIENT)
public final class RangeBlockerSoundManager {
    private static final int ACTIVE_CHECKS_PER_TICK = 128;
    private static final int IDLE_CLEANUP_INTERVAL = 100;
    private static final List<SoundInstance> TRACKED = new ArrayList<>();
    private static final Set<SoundInstance> TRACKED_SET =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private static int cursor;
    private static long lastIdleCleanup;

    private RangeBlockerSoundManager() {}

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        Minecraft minecraft = Minecraft.getInstance();
        if (sound != null && minecraft.level != null && isPositional(sound)
                && RangeBlockerManager.shouldSuppressSound(minecraft.level, position(sound))) {
            event.setSound(null);
        }
    }

    @SubscribeEvent
    public static synchronized void onPlaySoundSource(PlaySoundSourceEvent event) {
        track(event.getSound());
    }

    @SubscribeEvent
    public static synchronized void onPlayStreamingSource(PlayStreamingSourceEvent event) {
        track(event.getSound());
    }

    @SubscribeEvent
    public static synchronized void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            clear();
            return;
        }

        SoundManager soundManager = minecraft.getSoundManager();
        if (!RangeBlockerManager.hasActiveSilenceField(minecraft.level)) {
            long gameTime = minecraft.level.getGameTime();
            if (gameTime - lastIdleCleanup >= IDLE_CLEANUP_INTERVAL) {
                lastIdleCleanup = gameTime;
                cleanupInactive(soundManager);
            }
            return;
        }

        int checks = Math.min(ACTIVE_CHECKS_PER_TICK, TRACKED.size());
        for (int checked = 0; checked < checks && !TRACKED.isEmpty(); checked++) {
            if (cursor >= TRACKED.size()) cursor = 0;
            SoundInstance sound = TRACKED.get(cursor);
            if (!soundManager.isActive(sound)) {
                removeAt(cursor);
                continue;
            }
            if (RangeBlockerManager.shouldSuppressSound(minecraft.level, position(sound))) {
                soundManager.stop(sound);
                removeAt(cursor);
                continue;
            }
            cursor++;
        }
    }

    private static void track(SoundInstance sound) {
        if (!isPositional(sound) || !TRACKED_SET.add(sound)) return;
        TRACKED.add(sound);
    }

    private static boolean isPositional(SoundInstance sound) {
        return !sound.isRelative() && Double.isFinite(sound.getX())
                && Double.isFinite(sound.getY()) && Double.isFinite(sound.getZ());
    }

    private static Vec3 position(SoundInstance sound) {
        return new Vec3(sound.getX(), sound.getY(), sound.getZ());
    }

    private static void cleanupInactive(SoundManager soundManager) {
        for (int index = TRACKED.size() - 1; index >= 0; index--) {
            if (!soundManager.isActive(TRACKED.get(index))) removeAt(index);
        }
        if (cursor >= TRACKED.size()) cursor = 0;
    }

    private static void removeAt(int index) {
        int last = TRACKED.size() - 1;
        SoundInstance removed = TRACKED.get(index);
        TRACKED_SET.remove(removed);
        if (index != last) TRACKED.set(index, TRACKED.get(last));
        TRACKED.remove(last);
        if (cursor > index) cursor--;
    }

    private static void clear() {
        TRACKED.clear();
        TRACKED_SET.clear();
        cursor = 0;
        lastIdleCleanup = 0L;
    }
}
