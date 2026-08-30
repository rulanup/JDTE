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
        if (AVAILABLE) {
            AE2CraftingReadNetworkIntegration.registerLinkable();
        }
    }

    public static boolean hasActiveCraftingTask(ServerLevel level, ItemStack upgrade) {
        return AVAILABLE && level != null && upgrade != null && !upgrade.isEmpty()
                && AE2CraftingReadNetworkIntegration.hasActiveCraftingTask(level, upgrade);
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
