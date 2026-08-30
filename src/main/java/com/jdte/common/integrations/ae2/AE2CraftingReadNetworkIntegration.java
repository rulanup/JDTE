package com.jdte.common.integrations.ae2;

import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import com.jdte.JDTE;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashMap;
import java.util.Map;

final class AE2CraftingReadNetworkIntegration {
    private static final long CACHE_TICKS = 5L;
    private static final Map<GlobalPos, CacheEntry> CACHE = new HashMap<>();
    private static boolean registered;

    private AE2CraftingReadNetworkIntegration() {
    }

    static void registerLinkable() {
        if (registered) return;
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "ae_crafting_read_upgrade"));
        if (item == null || item == BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getDefaultKey())) return;
        GridLinkables.register(item, new IGridLinkableHandler() {
            @Override public boolean canLink(ItemStack stack) { return stack.is(item); }
            @Override public void link(ItemStack stack, GlobalPos pos) { stack.set(AEComponents.WIRELESS_LINK_TARGET, pos); }
            @Override public void unlink(ItemStack stack) { stack.remove(AEComponents.WIRELESS_LINK_TARGET); }
        });
        registered = true;
    }

    static boolean hasActiveCraftingTask(ServerLevel origin, ItemStack upgrade) {
        GlobalPos target = upgrade.get(AEComponents.WIRELESS_LINK_TARGET);
        if (target == null || origin.getServer() == null) return false;
        ServerLevel level = origin.getServer().getLevel(target.dimension());
        if (level == null || !level.isLoaded(target.pos())) return false;
        BlockEntity blockEntity = level.getBlockEntity(target.pos());
        if (!(blockEntity instanceof IWirelessAccessPoint accessPoint) || !accessPoint.isActive()) return false;
        IGrid grid = accessPoint.getGrid();
        if (grid == null || grid.getPathingService().isNetworkBooting()) return false;
        long now = origin.getGameTime();
        CacheEntry cached = CACHE.get(target);
        if (cached != null && cached.grid == grid && cached.expiresAt > now) return cached.active;
        boolean active = grid.getCraftingService().getCpus().stream()
                .anyMatch(cpu -> cpu.isBusy() && cpu.getJobStatus() != null);
        CACHE.put(target, new CacheEntry(grid, active, now + CACHE_TICKS));
        return active;
    }

    private record CacheEntry(IGrid grid, boolean active, long expiresAt) { }
}
