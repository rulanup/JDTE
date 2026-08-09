package com.jdte.common.integrations.ae2;

import appeng.api.config.Actionable;
import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.mojang.logging.LogUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class AEOutputNetworkIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();

    private AEOutputNetworkIntegration() {
    }

    static void registerLinkable(Item item) {
        GridLinkables.register(item, new IGridLinkableHandler() {
            @Override public boolean canLink(ItemStack stack) { return stack.is(item); }
            @Override public void link(ItemStack stack, GlobalPos pos) {
                stack.set(AEComponents.WIRELESS_LINK_TARGET, pos);
            }
            @Override public void unlink(ItemStack stack) {
                stack.remove(AEComponents.WIRELESS_LINK_TARGET);
            }
        });
    }

    static boolean isLinked(ItemStack upgrade) {
        return upgrade.has(AEComponents.WIRELESS_LINK_TARGET);
    }

    static int insertItem(ServerLevel origin, ItemStack upgrade, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return 0;
        AEItemKey key = AEItemKey.of(stack);
        if (key == null) return 0;
        MEStorage storage = storage(origin, upgrade);
        if (storage == null) return 0;
        IWirelessAccessPoint accessPoint = accessPoint(origin, upgrade);
        if (accessPoint == null) return 0;
        long inserted = storage.insert(key, stack.getCount(), simulate ? Actionable.SIMULATE : Actionable.MODULATE,
                IActionSource.ofMachine(accessPoint));
        return (int) Math.min(stack.getCount(), Math.max(0L, inserted));
    }

    static AEOutputNetwork.ItemTransferResult transferItems(ServerLevel origin, ItemStack upgrade,
                                                             List<AEOutputNetwork.ItemSource> itemSources) {
        IWirelessAccessPoint accessPoint = accessPoint(origin, upgrade);
        if (accessPoint == null || !accessPoint.isActive()) return AEOutputNetwork.ItemTransferResult.empty();
        IGrid grid = accessPoint.getGrid();
        if (grid == null) return AEOutputNetwork.ItemTransferResult.empty();
        MEStorage storage = grid.getStorageService().getInventory();
        IActionSource actionSource = IActionSource.ofMachine(accessPoint);

        List<HandlerSource> sources = new ArrayList<>(itemSources.size());
        for (AEOutputNetwork.ItemSource itemSource : itemSources) {
            if (itemSource == null || itemSource.owner() == null || itemSource.handler() == null
                    || itemSource.slot() < 0 || itemSource.slot() >= itemSource.handler().getSlots()) continue;
            ItemStack snapshot = itemSource.handler().getStackInSlot(itemSource.slot()).copy();
            if (snapshot.isEmpty()) continue;
            AEItemKey key = AEItemKey.of(snapshot);
            if (key != null) sources.add(new HandlerSource(itemSource, snapshot, key));
        }
        if (sources.isEmpty()) return AEOutputNetwork.ItemTransferResult.empty();

        AEItemBatchTransfer.Result<HandlerSource> result = AEItemBatchTransfer.transfer(sources,
                (key, amount, simulate) -> storage.insert(key, amount,
                        simulate ? Actionable.SIMULATE : Actionable.MODULATE, actionSource));
        Set<BaseMachineBE> changedMachines = new LinkedHashSet<>();
        for (HandlerSource source : result.changedSources()) changedMachines.add(source.source.owner());
        if (result.unrestored() > 0L) {
            LOGGER.error("AE output transfer could not restore {} items after a short commit at {}",
                    result.unrestored(), upgrade.get(AEComponents.WIRELESS_LINK_TARGET));
        }
        return new AEOutputNetwork.ItemTransferResult(result.moved(), Set.copyOf(changedMachines),
                result.unrestored());
    }

    static int insertFluid(ServerLevel origin, ItemStack upgrade, FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) return 0;
        AEFluidKey key = AEFluidKey.of(stack);
        if (key == null) return 0;
        MEStorage storage = storage(origin, upgrade);
        if (storage == null) return 0;
        long inserted = storage.insert(key, stack.getAmount(),
                simulate ? Actionable.SIMULATE : Actionable.MODULATE, actionSource(origin, upgrade));
        return (int) Math.min(stack.getAmount(), Math.max(0L, inserted));
    }

    private static MEStorage storage(ServerLevel origin, ItemStack upgrade) {
        IWirelessAccessPoint accessPoint = accessPoint(origin, upgrade);
        if (accessPoint == null || !accessPoint.isActive()) return null;
        IGrid grid = accessPoint.getGrid();
        return grid == null ? null : grid.getStorageService().getInventory();
    }

    private static IActionSource actionSource(ServerLevel origin, ItemStack upgrade) {
        IWirelessAccessPoint accessPoint = accessPoint(origin, upgrade);
        return accessPoint == null ? IActionSource.empty() : IActionSource.ofMachine(accessPoint);
    }

    private static IWirelessAccessPoint accessPoint(ServerLevel origin, ItemStack upgrade) {
        GlobalPos linked = upgrade.get(AEComponents.WIRELESS_LINK_TARGET);
        if (linked == null) return null;
        ServerLevel level = origin.getServer().getLevel(linked.dimension());
        if (level == null || !level.isLoaded(linked.pos())) return null;
        BlockEntity blockEntity = level.getBlockEntity(linked.pos());
        return blockEntity instanceof IWirelessAccessPoint accessPoint ? accessPoint : null;
    }

    private static final class HandlerSource implements AEItemBatchTransfer.Source<AEItemKey> {
        private final AEOutputNetwork.ItemSource source;
        private final ItemStack snapshot;
        private final AEItemKey key;

        private HandlerSource(AEOutputNetwork.ItemSource source, ItemStack snapshot, AEItemKey key) {
            this.source = source;
            this.snapshot = snapshot;
            this.key = key;
        }

        @Override public AEItemKey key() {
            return key;
        }

        @Override public long available() {
            ItemStack visible = source.handler().getStackInSlot(source.slot());
            return sameKey(visible) ? visible.getCount() : 0L;
        }

        @Override public long extract(long amount, boolean simulate) {
            if (amount <= 0L) return 0L;
            ItemStack visible = source.handler().getStackInSlot(source.slot());
            if (!sameKey(visible)) return 0L;
            int requested = (int) Math.min(Integer.MAX_VALUE, amount);
            ItemStack extracted = source.handler().extractItem(source.slot(), requested, simulate);
            return sameKey(extracted) ? extracted.getCount() : 0L;
        }

        @Override public long restore(long amount) {
            long remaining = Math.max(0L, amount);
            long restored = 0L;
            while (remaining > 0L) {
                int count = (int) Math.min(Integer.MAX_VALUE, remaining);
                ItemStack remainder = source.handler().insertItem(source.slot(), snapshot.copyWithCount(count), false);
                int inserted = count - Math.max(0, remainder.getCount());
                restored += inserted;
                remaining -= inserted;
                if (inserted < count) break;
            }
            return restored;
        }

        private boolean sameKey(ItemStack stack) {
            return !stack.isEmpty() && key.equals(AEItemKey.of(stack));
        }
    }
}
