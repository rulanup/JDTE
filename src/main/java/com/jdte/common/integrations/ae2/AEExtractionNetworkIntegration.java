package com.jdte.common.integrations.ae2;

import appeng.api.config.Actionable;
import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.storage.MEStorage;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class AEExtractionNetworkIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long MAX = Integer.MAX_VALUE;
    private static final Map<String, Long> REFUND_LOG_TICKS = new HashMap<>();

    private AEExtractionNetworkIntegration() {
    }

    static void registerLinkable(Item item) {
        GridLinkables.register(item, new IGridLinkableHandler() {
            @Override public boolean canLink(ItemStack stack) { return stack.is(item); }
            @Override public void link(ItemStack stack, GlobalPos pos) { stack.set(AEComponents.WIRELESS_LINK_TARGET, pos); }
            @Override public void unlink(ItemStack stack) { stack.remove(AEComponents.WIRELESS_LINK_TARGET); }
        });
    }

    static boolean isLinked(ItemStack stack) { return stack.has(AEComponents.WIRELESS_LINK_TARGET); }

    static void copyLink(ItemStack source, ItemStack target) {
        GlobalPos link = source.get(AEComponents.WIRELESS_LINK_TARGET);
        if (link != null) target.set(AEComponents.WIRELESS_LINK_TARGET, link);
    }

    static void refill(ServerPlayer player, List<ItemStack> stacks) {
        Map<GlobalPos, List<ItemStack>> groups = new HashMap<>();
        for (ItemStack stack : stacks) {
            GlobalPos link = stack.get(AEComponents.WIRELESS_LINK_TARGET);
            if (link != null) groups.computeIfAbsent(link, ignored -> new ArrayList<>()).add(stack);
        }
        for (var entry : groups.entrySet()) {
            GlobalPos link = entry.getKey();
            ServerLevel level = player.level().getServer().getLevel(link.dimension());
            if (level == null || !level.isLoaded(link.pos())) continue;
            BlockEntity blockEntity = level.getBlockEntity(link.pos());
            if (!(blockEntity instanceof IWirelessAccessPoint accessPoint) || !accessPoint.isActive()) continue;
            IGrid grid = accessPoint.getGrid();
            if (grid == null) continue;
            MEStorage storage = grid.getStorageService().getInventory();
            IActionSource action = IActionSource.ofPlayer(player, accessPoint);
            for (ItemStack stack : entry.getValue()) {
                refillFluids(storage, action, link, player.level().getGameTime(), stack);
                if (ModList.get().isLoaded("appflux")) {
                    AEExtractionEnergyIntegration.refill(storage, action, link,
                            player.level().getGameTime(), stack);
                }
            }
        }
    }

    private static void refillFluids(MEStorage storage, IActionSource action, GlobalPos link, long gameTime,
                                     ItemStack stack) {
        var access = ItemAccess.forStack(stack);
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) return;
        List<Fluid> candidates = BuiltInRegistries.FLUID.stream().toList();
        for (int tank = 0; tank < handler.size(); tank++) {
            boolean cached = AEExtractionFluidPolicy.hasCached(stack, tank);
            FluidStack selected = AEExtractionFluidPolicy.select(stack, handler, tank, candidates);
            if (selected == null || selected.isEmpty()) continue;
            AEFluidKey key = AEFluidKey.of(selected);
            if (key == null) continue;
            if (cached && handler.getResource(tank).isEmpty()
                    && fill(handler, tank, FluidResource.of(selected), 1, true) == 0) {
                AEExtractionFluidPolicy.invalidate(stack, tank);
                selected = AEExtractionFluidPolicy.select(stack, handler, tank, candidates, false);
                if (selected == null || selected.isEmpty()) continue;
                key = AEFluidKey.of(selected);
                if (key == null) continue;
            }
            final FluidResource selectedFluid = FluidResource.of(selected);
            final int tankIndex = tank;
            final AEFluidKey fluidKey = key;
            boolean[] actualFillRejected = {false};
            AEExtractionTransfer.Result result = AEExtractionTransfer.move(MAX,
                    new AEExtractionTransfer.Source() {
                        @Override public long extract(long amount, boolean simulate) {
                            return storage.extract(fluidKey, Math.min(MAX, amount),
                                    simulate ? Actionable.SIMULATE : Actionable.MODULATE, action);
                        }
                        @Override public long restore(long amount) {
                            return storage.insert(fluidKey, Math.min(MAX, amount), Actionable.MODULATE, action);
                        }
                    },
                    new AEExtractionTransfer.Sink() {
                        @Override public long insert(long amount, boolean simulate) {
                            var current = access.getCapability(Capabilities.Fluid.ITEM);
                            if (current == null || tankIndex >= current.size()) return 0;
                            int accepted = fill(current, tankIndex, selectedFluid, (int) Math.min(MAX, amount), simulate);
                            if (!simulate && amount > 0 && accepted == 0) actualFillRejected[0] = true;
                            return accepted;
                        }
                    });
            if (actualFillRejected[0]) {
                AEExtractionFluidPolicy.invalidate(stack, tank);
            }
            if (result.unrestored() > 0) logRefundFailure(link, fluidKey.toString(), result.unrestored(), gameTime);
            handler = access.getCapability(Capabilities.Fluid.ITEM);
            if (handler == null) return;
        }
    }

    private static int fill(ResourceHandler<FluidResource> handler, int tank, FluidResource resource,
                            int amount, boolean simulate) {
        try (var transaction = Transaction.openRoot()) {
            int inserted = handler.insert(tank, resource, amount, transaction);
            if (!simulate) transaction.commit();
            return inserted;
        }
    }

    static void logRefundFailure(GlobalPos link, String resource, long amount, long now) {
        String id = link + "|" + resource;
        Long prior = REFUND_LOG_TICKS.get(id);
        if (prior == null || now - prior >= 200) {
            REFUND_LOG_TICKS.put(id, now);
            LOGGER.error("AE extraction refund failed for {}: {} {}", link, amount, resource);
        }
    }
}
