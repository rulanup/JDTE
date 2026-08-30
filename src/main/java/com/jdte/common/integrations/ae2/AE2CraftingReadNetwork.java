package com.jdte.common.integrations.ae2;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/** Optional AE2 facade used by machines to query crafting activity. */
public final class AE2CraftingReadNetwork {
    private static final boolean AVAILABLE = ModList.get().isLoaded("ae2");

    private AE2CraftingReadNetwork() {
    }

    public static void registerLinkable() {
        if (AVAILABLE) AE2CraftingReadNetworkIntegration.registerLinkable();
    }

    public static void registerLinkable(net.minecraft.world.item.Item item) {
        if (AVAILABLE && item != null) AE2CraftingReadNetworkIntegration.registerLinkable(item);
    }

    public static void bind(ItemStack upgrade, net.minecraft.core.GlobalPos target) {
        if (upgrade != null && target != null) AE2CraftingReadNetworkIntegration.bind(upgrade, target);
    }

    public static void unlink(ItemStack upgrade) {
        if (upgrade != null) AE2CraftingReadNetworkIntegration.unlink(upgrade);
    }

    public static boolean hasActiveCraftingTask(ServerLevel level, ItemStack upgrade) {
        return AVAILABLE && level != null && upgrade != null && !upgrade.isEmpty()
                && level.getServer().isSameThread()
                && AE2CraftingReadNetworkIntegration.hasActiveCraftingTask(level, upgrade);
    }

    public static boolean hasActiveCraftingTask(ItemStack upgrade, long gameTime, TargetResolver resolver) {
        return upgrade != null && !upgrade.isEmpty()
                && AE2CraftingReadNetworkIntegration.hasActiveCraftingTask(upgrade, gameTime, resolver);
    }

    public static net.minecraft.core.GlobalPos linkedTarget(ItemStack upgrade) {
        return upgrade == null ? null : AE2CraftingReadNetworkIntegration.linkedTarget(upgrade);
    }

    public static boolean hasActiveCraftingTask(net.minecraft.core.GlobalPos target, long gameTime,
                                                TargetResolver resolver) {
        return target != null && AE2CraftingReadNetworkIntegration.hasActiveCraftingTask(target, gameTime, resolver);
    }

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
