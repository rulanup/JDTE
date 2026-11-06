package com.jdte.common.integrations.ae2;

import appeng.api.config.Actionable;
import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;

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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
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
            @Override public void link(ItemStack stack, GlobalPos pos) { AEWirelessLinkData.set(stack,pos); }
            @Override public void unlink(ItemStack stack) { AEWirelessLinkData.set(stack,null); }
        });
    }

    static boolean isLinked(ItemStack stack) { return AEWirelessLinkData.get(stack)!=null; }

    static void copyLink(ItemStack source, ItemStack target) {
        GlobalPos link = AEWirelessLinkData.get(source);
        if (link != null) AEWirelessLinkData.set(target,link);
    }

    static void refill(ServerPlayer player, List<ItemStack> stacks) {
        Map<GlobalPos, List<ItemStack>> groups = new HashMap<>();
        for (ItemStack stack : stacks) {
            GlobalPos link = AEWirelessLinkData.get(stack);
            if (link != null) groups.computeIfAbsent(link, ignored -> new ArrayList<>()).add(stack);
        }
        for (var entry : groups.entrySet()) {
            GlobalPos link = entry.getKey();
            ServerLevel level = player.server.getLevel(link.dimension());
            if (level == null || !level.isLoaded(link.pos())) continue;
            BlockEntity blockEntity = level.getBlockEntity(link.pos());
            if (!(blockEntity instanceof IWirelessAccessPoint accessPoint) || !accessPoint.isActive()) continue;
            IGrid grid = accessPoint.getGrid();
            if (grid == null) continue;
            MEStorage storage = grid.getStorageService().getInventory();
            IActionSource action = IActionSource.ofPlayer(player, accessPoint);
            for (ItemStack stack : entry.getValue()) {
                refillFluids(storage, action, link, player.serverLevel().getGameTime(), stack);
                if (ModList.get().isLoaded("appflux")) {
                    AEExtractionEnergyIntegration.refill(storage, action, link,
                            player.serverLevel().getGameTime(), player, stack);
                }
            }
        }
    }

    private static void refillFluids(MEStorage storage, IActionSource action, GlobalPos link, long gameTime,
                                     ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null) return;
        List<Fluid> candidates = BuiltInRegistries.FLUID.stream().toList();
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            boolean cached = AEExtractionFluidPolicy.hasCached(stack, tank);
            FluidStack selected = AEExtractionFluidPolicy.select(handler, tank, candidates);
            if (selected == null || selected.isEmpty()) continue;
            AEFluidKey key = AEFluidKey.of(selected);
            if (key == null) continue;
            if (cached && handler.getFluidInTank(tank).isEmpty()
                    && handler.fill(FluidCopies.withAmount(selected,1),
                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE) == 0) {
                AEExtractionFluidPolicy.invalidate(stack, tank);
                selected = AEExtractionFluidPolicy.select(handler, tank, candidates, false);
                if (selected == null || selected.isEmpty()) continue;
                key = AEFluidKey.of(selected);
                if (key == null) continue;
            }
            final FluidStack selectedFluid = selected;
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
                            IFluidHandlerItem current = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
                            if (current == null) return 0;
                            int accepted = current.fill(FluidCopies.withAmount(selectedFluid,(int) Math.min(MAX, amount)),
                                    simulate ? net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE
                                            : net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                            if (!simulate && amount > 0 && accepted == 0) actualFillRejected[0] = true;
                            return accepted;
                        }
                    });
            if (actualFillRejected[0]) {
                AEExtractionFluidPolicy.invalidate(stack, tank);
            }
            if (result.unrestored() > 0) logRefundFailure(link, fluidKey.toString(), result.unrestored(), gameTime);
            handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
            if (handler == null) return;
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
