package com.jdte.common.integrations.ae2;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.GlobalPos;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/** Optional AE2 facade used by machines to query crafting activity. */
public final class AE2CraftingReadNetwork {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean AVAILABLE = ModList.get().isLoaded("ae2");
    private static final AtomicBoolean REFLECTION_FAILURE_LOGGED = new AtomicBoolean();

    private AE2CraftingReadNetwork() {
    }

    public static void registerLinkable() {
        invoke("registerLinkable", new Class<?>[0]);
    }

    public static void registerLinkable(net.minecraft.world.item.Item item) {
        if (AVAILABLE && item != null) invoke("registerLinkable", new Class<?>[]{net.minecraft.world.item.Item.class}, item);
    }

    private static Object invoke(String name, Class<?>[] types, Object... args) {
        if (!AVAILABLE) return null;
        try {
            Class<?> type = Class.forName("com.jdte.common.integrations.ae2.AE2CraftingReadNetworkIntegration");
            return type.getDeclaredMethod(name, types).invoke(null, args);
        } catch (ReflectiveOperationException | LinkageError error) {
            if (REFLECTION_FAILURE_LOGGED.compareAndSet(false, true)) {
                LOGGER.error("AE2 is loaded, but the AE crafting read integration could not be initialized", error);
            }
            return null;
        }
    }

    public static void bind(ItemStack upgrade, net.minecraft.core.GlobalPos target) {
        if (upgrade != null && target != null) invoke("bind", new Class<?>[]{ItemStack.class, GlobalPos.class}, upgrade, target);
    }

    public static void unlink(ItemStack upgrade) {
        if (upgrade != null) invoke("unlink", new Class<?>[]{ItemStack.class}, upgrade);
    }

    public static boolean hasActiveCraftingTask(ServerLevel level, ItemStack upgrade) {
        return AVAILABLE && level != null && upgrade != null && !upgrade.isEmpty()
                && level.getServer().isSameThread()
                && Boolean.TRUE.equals(invoke("hasActiveCraftingTask", new Class<?>[]{ServerLevel.class, ItemStack.class}, level, upgrade));
    }

    public static boolean hasActiveCraftingTask(ItemStack upgrade, long gameTime, TargetResolver resolver) {
        return upgrade != null && !upgrade.isEmpty()
                && hasActiveCraftingTask(linkedTarget(upgrade), gameTime, resolver);
    }

    public static net.minecraft.core.GlobalPos linkedTarget(ItemStack upgrade) {
        Object result = upgrade == null ? null : invoke("linkedTarget", new Class<?>[]{ItemStack.class}, upgrade);
        return result instanceof GlobalPos pos ? pos : null;
    }

    public static boolean hasActiveCraftingTask(GlobalPos target, long gameTime, TargetResolver resolver) {
        return hasActiveCraftingTask(resolver, target, gameTime, resolver);
    }

    public static boolean hasActiveCraftingTask(Object scopeIdentity, GlobalPos target, long gameTime,
                                                TargetResolver resolver) {
        if (scopeIdentity == null || target == null || resolver == null) return false;
        NetworkState state = resolver.resolve(target);
        CacheKey key = new CacheKey(scopeIdentity, target);
        if (state == null || !state.loaded() || !state.active() || state.booting() || state.cpus() == null) {
            invalidate(scopeIdentity, target);
            return false;
        }
        synchronized (CACHE) {
            CacheEntry cached = CACHE.get(key);
            if (cached != null && cached.gridIdentity == state.gridIdentity() && cached.expiresAt > gameTime) {
                return cached.active;
            }
            boolean active = hasActiveCraftingTask(state.cpus());
            CACHE.entrySet().removeIf(entry -> entry.getValue().expiresAt <= gameTime);
            CACHE.put(key, new CacheEntry(state.gridIdentity(), active, gameTime + CACHE_TICKS));
            return active;
        }
    }

    public static void invalidate(Object scopeIdentity, GlobalPos target) {
        if (scopeIdentity == null || target == null) return;
        synchronized (CACHE) {
            CACHE.remove(new CacheKey(scopeIdentity, target));
        }
    }

    public static void clearCacheForTests() {
        synchronized (CACHE) {
            CACHE.clear();
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        Object server = event.getServer();
        synchronized (CACHE) {
            CACHE.keySet().removeIf(key -> key.scopeIdentity == server);
        }
    }

    private static final long CACHE_TICKS = 5L;
    private static final int MAX_CACHE_ENTRIES = 256;
    private static final Map<CacheKey, CacheEntry> CACHE = new LinkedHashMap<>(16, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<CacheKey, CacheEntry> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    };

    private static final class CacheKey {
        private final Object scopeIdentity;
        private final GlobalPos target;

        private CacheKey(Object scopeIdentity, GlobalPos target) {
            this.scopeIdentity = scopeIdentity;
            this.target = target;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof CacheKey key
                    && scopeIdentity == key.scopeIdentity
                    && target.equals(key.target);
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(scopeIdentity) + target.hashCode();
        }
    }

    private record CacheEntry(Object gridIdentity, boolean active, long expiresAt) {}

    public interface TargetResolver {
        NetworkState resolve(net.minecraft.core.GlobalPos target);
    }

    public record NetworkState(Object gridIdentity, boolean loaded, boolean active, boolean booting,
                               Iterable<CraftingCpuSnapshot> cpus) {
    }

    public static boolean hasActiveCraftingTask(Iterable<CraftingCpuSnapshot> cpus) {
        if (cpus == null) return false;
        for (CraftingCpuSnapshot cpu : cpus) {
            if (cpu != null && cpu.busy() && cpu.hasJob()) return true;
        }
        return false;
    }

    public record CraftingCpuSnapshot(boolean busy, boolean hasJob) {
    }
}
