package com.jdte.common.integrations.ae2;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.setup.JDTEItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;
import java.util.Set;

public final class AEOutputNetwork {
    private static final boolean AVAILABLE = ModList.get().isLoaded("ae2");

    private AEOutputNetwork() {
    }

    public static void registerLinkable() {
        if (AVAILABLE) AEOutputNetworkIntegration.registerLinkable(JDTEItems.AE_OUTPUT_UPGRADE.get());
    }

    public static boolean isLinked(ItemStack upgrade) {
        return AVAILABLE && AEOutputNetworkIntegration.isLinked(upgrade);
    }

    public static int insertItem(ServerLevel level, ItemStack upgrade, ItemStack stack, boolean simulate) {
        return AVAILABLE ? AEOutputNetworkIntegration.insertItem(level, upgrade, stack, simulate) : 0;
    }

    public static ItemTransferResult transferItems(ServerLevel level, ItemStack upgrade, List<ItemSource> sources) {
        if (!AVAILABLE || sources.isEmpty()) return ItemTransferResult.empty();
        return AEOutputNetworkIntegration.transferItems(level, upgrade, sources);
    }

    public static ItemTransferResult transferInfiniteItems(ServerLevel level, ItemStack upgrade,
                                                            List<ItemStack> prototypes) {
        if (!AVAILABLE || prototypes.isEmpty()) return ItemTransferResult.empty();
        return AEOutputNetworkIntegration.transferInfiniteItems(level, upgrade, prototypes);
    }

    public static int insertFluid(ServerLevel level, ItemStack upgrade, FluidStack stack, boolean simulate) {
        return AVAILABLE ? AEOutputNetworkIntegration.insertFluid(level, upgrade, stack, simulate) : 0;
    }

    public record ItemSource(BaseMachineBE owner, ItemStackHandler handler, int slot) {
    }

    public record ItemTransferResult(long moved, Set<BaseMachineBE> changedMachines, long unrestored) {
        public static ItemTransferResult empty() {
            return new ItemTransferResult(0L, Set.of(), 0L);
        }
    }
}
