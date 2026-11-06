package com.jdte.common.manager;

import com.jdte.common.integrations.ae2.AEExtractionNetwork;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/** Discovers enabled extraction stacks carried by a server player. */
public final class AEExtractionPlayerManager {
    private AEExtractionPlayerManager() {
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase!=TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.isRemoved() || !player.isAlive()) return;
        List<ItemStack> candidates = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            candidates.add(inventory.getItem(slot));
        }
        candidates.addAll(com.jdte.common.integrations.curios.AdvancedUpgradeStorageCuriosSources.findAll(player, stack -> true));
        List<ItemStack> enabled = collectDistinct(candidates);
        if (!enabled.isEmpty()) AEExtractionNetwork.refill(player, enabled);
    }

    public static void onTooltip(ItemTooltipEvent event) {
        if (com.jdte.common.integrations.ae2.AEWirelessLinkData.extractionEnabled(event.getItemStack())) {
            event.getToolTip().add(net.minecraft.network.chat.Component.translatable(
                    "tooltip.jdte.ae_extraction.enabled"));
        }
    }

    public static List<ItemStack> collectDistinct(List<ItemStack> candidates) {
        List<ItemStack> result = new ArrayList<>();
        IdentityHashMap<ItemStack, Boolean> seenStacks = new IdentityHashMap<>();
        IdentityHashMap<Object, Boolean> seenCapabilities = new IdentityHashMap<>();
        for (ItemStack stack : candidates) {
            if (stack.isEmpty() || !com.jdte.common.integrations.ae2.AEWirelessLinkData.extractionEnabled(stack)
                    || seenStacks.put(stack, Boolean.TRUE) != null) continue;
            Object energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            Object fluid = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
            boolean duplicateCapability = energy != null && seenCapabilities.containsKey(energy)
                    || fluid != null && seenCapabilities.containsKey(fluid);
            if (duplicateCapability) continue;
            if (energy != null) seenCapabilities.put(energy, Boolean.TRUE);
            if (fluid != null) seenCapabilities.put(fluid, Boolean.TRUE);
            result.add(stack);
        }
        return result;
    }

    public static List<ItemStack> collectDistinct(List<ItemStack> inventory, List<ItemStack> curios) {
        List<ItemStack> all = new ArrayList<>(inventory.size() + curios.size());
        all.addAll(inventory);
        all.addAll(curios);
        return collectDistinct(all);
    }
}
