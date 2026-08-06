package com.jdte.common.blockentities;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 全局时间定格协调器。
 *
 * <p>所有已加载的时间定格器在此注册；每 tick 由激活的定格器驱动全局冻结状态。
 * 冻结目标按维度记录：第一个激活的定格器记录当时的昼夜时间和（主世界）天气，
 * 之后所有激活的定格器都把世界状态拉回该目标，避免多台机器互相覆盖导致时间跳变。
 * 时间与天气两个冻结目标相互独立，可分别由机器上的两个开关控制。</p>
 */
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

    /** 由服务端 tickServer 调用：定格器已支付资源，需要保持世界冻结。 */
    public static void activate(TimeFreezerBE freezer, ServerLevel serverLevel) {
        ACTIVE.add(freezer);
        ResourceKey<Level> dimension = serverLevel.dimension();
        if (freezer.isTimeFreezeEnabled() && !FROZEN_DAY_TIME.containsKey(dimension)) {
            FROZEN_DAY_TIME.put(dimension, serverLevel.getDayTime());
        }
        if (freezer.isWeatherFreezeEnabled() && dimension == Level.OVERWORLD
                && !FROZEN_WEATHER.containsKey(dimension)) {
            FROZEN_WEATHER.put(dimension, FrozenWeather.capture(serverLevel));
        }
        apply(serverLevel);
    }

    /** 由服务端 tickServer 调用：定格器因开关、红石或资源不足停止工作。 */
    public static void deactivate(TimeFreezerBE freezer) {
        if (ACTIVE.remove(freezer)) {
            cleanupDimension(freezer.getLevel());
        }
    }

    /** 机器的天气开关变化时重新评估冻结目标（仍活跃时）。 */
    public static void refreshActive(TimeFreezerBE freezer, ServerLevel serverLevel) {
        if (!ACTIVE.contains(freezer)) {
            return;
        }
        ResourceKey<Level> dimension = serverLevel.dimension();
        if (freezer.isWeatherFreezeEnabled() && dimension == Level.OVERWORLD
                && !FROZEN_WEATHER.containsKey(dimension)) {
            FROZEN_WEATHER.put(dimension, FrozenWeather.capture(serverLevel));
        }
        if (!freezer.isWeatherFreezeEnabled() && dimension == Level.OVERWORLD) {
            cleanupDimension(serverLevel);
        }
        apply(serverLevel);
    }

    public static boolean isActive(TimeFreezerBE freezer) {
        return ACTIVE.contains(freezer);
    }

    /** 已加载的所有时间定格器（含未激活），供管理指令查询。 */
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

    private static void apply(ServerLevel serverLevel) {
        ResourceKey<Level> dimension = serverLevel.dimension();
        Long frozenTime = FROZEN_DAY_TIME.get(dimension);
        if (frozenTime != null) {
            serverLevel.setDayTime(frozenTime);
        }
        if (dimension == Level.OVERWORLD) {
            FrozenWeather weather = FROZEN_WEATHER.get(dimension);
            if (weather != null) {
                weather.apply(serverLevel);
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
            return new FrozenWeather(
                    Math.max(1, data.getClearWeatherTime()),
                    Math.max(1, data.getRainTime()),
                    data.isRaining(),
                    data.isThundering());
        }

        void apply(ServerLevel level) {
            level.setWeatherParameters(clearTime, rainTime, raining, thundering);
        }
    }
}
