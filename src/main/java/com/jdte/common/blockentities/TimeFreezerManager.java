package com.jdte.common.blockentities;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public final class TimeFreezerManager {
    private static final Set<TimeFreezerBE> REGISTERED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<TimeFreezerBE> ACTIVE = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<ResourceKey<Level>, Long> FROZEN_DAY_TIME = new HashMap<>();
    private static final Map<ResourceKey<Level>, FrozenWeather> FROZEN_WEATHER = new HashMap<>();

    private TimeFreezerManager() {
    }

    public static void register(TimeFreezerBE freezer) {
        if (freezer.getLevel() == null || freezer.getLevel().isClientSide) {
            return;
        }
        REGISTERED.add(freezer);
    }

    public static void unregister(TimeFreezerBE freezer) {
        REGISTERED.remove(freezer);
        if (ACTIVE.remove(freezer)) {
            cleanupDimension(freezer.getLevel());
        }
    }

    public static void activate(TimeFreezerBE freezer, ServerLevel level) {
        ACTIVE.add(freezer);
        ResourceKey<Level> dimension = level.dimension();
        if (freezer.isTimeFreezeEnabled() && !FROZEN_DAY_TIME.containsKey(dimension)) {
            FROZEN_DAY_TIME.put(dimension, level.getDayTime());
        }
        if (freezer.isWeatherFreezeEnabled() && dimension == Level.OVERWORLD
                && !FROZEN_WEATHER.containsKey(dimension)) {
            FROZEN_WEATHER.put(dimension, FrozenWeather.capture(level));
        }
        apply(level);
    }

    public static void deactivate(TimeFreezerBE freezer) {
        if (ACTIVE.remove(freezer)) {
            cleanupDimension(freezer.getLevel());
        }
    }

    public static void refreshActive(TimeFreezerBE freezer, ServerLevel level) {
        if (!ACTIVE.contains(freezer)) {
            return;
        }
        ResourceKey<Level> dimension = level.dimension();
        if (freezer.isTimeFreezeEnabled() && !FROZEN_DAY_TIME.containsKey(dimension)) {
            FROZEN_DAY_TIME.put(dimension, level.getDayTime());
        }
        if (freezer.isWeatherFreezeEnabled() && dimension == Level.OVERWORLD
                && !FROZEN_WEATHER.containsKey(dimension)) {
            FROZEN_WEATHER.put(dimension, FrozenWeather.capture(level));
        }
        cleanupDimension(level);
        apply(level);
    }

    public static boolean isActive(TimeFreezerBE freezer) {
        return ACTIVE.contains(freezer);
    }

    public static Set<TimeFreezerBE> getRegistered() {
        return Collections.unmodifiableSet(REGISTERED);
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }
        REGISTERED.removeIf(freezer -> freezer.getLevel() == level);
        boolean removed = ACTIVE.removeIf(freezer -> freezer.getLevel() == level);
        if (removed) {
            cleanupDimension(level);
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        REGISTERED.clear();
        ACTIVE.clear();
        FROZEN_DAY_TIME.clear();
        FROZEN_WEATHER.clear();
    }

    private static void apply(ServerLevel level) {
        Long frozenTime = FROZEN_DAY_TIME.get(level.dimension());
        if (frozenTime != null) {
            level.setDayTime(frozenTime);
        }
        if (level.dimension() == Level.OVERWORLD) {
            FrozenWeather weather = FROZEN_WEATHER.get(level.dimension());
            if (weather != null) {
                weather.apply(level);
            }
        }
    }

    private static void cleanupDimension(Level level) {
        if (level == null) {
            return;
        }
        ResourceKey<Level> dimension = level.dimension();
        boolean anyTime = ACTIVE.stream().anyMatch(freezer -> freezer.getLevel() != null
                && freezer.getLevel().dimension() == dimension && freezer.isTimeFreezeEnabled());
        if (!anyTime) {
            FROZEN_DAY_TIME.remove(dimension);
        }
        boolean anyWeather = ACTIVE.stream().anyMatch(freezer -> freezer.getLevel() != null
                && freezer.getLevel().dimension() == dimension && freezer.isWeatherFreezeEnabled());
        if (!anyWeather) {
            FROZEN_WEATHER.remove(dimension);
        }
    }

    private record FrozenWeather(int clearTime, int rainTime, boolean raining, boolean thundering) {
        static FrozenWeather capture(ServerLevel level) {
            ServerLevelData data = (ServerLevelData) level.getLevelData();
            return new FrozenWeather(Math.max(1, data.getClearWeatherTime()),
                    Math.max(1, data.getRainTime()), data.isRaining(), data.isThundering());
        }

        void apply(ServerLevel level) {
            level.setWeatherParameters(clearTime, rainTime, raining, thundering);
        }
    }
}
