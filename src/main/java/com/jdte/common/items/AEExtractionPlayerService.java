package com.jdte.common.items;

import com.jdte.common.integrations.ae2.AEExtractionNetwork;
import com.jdte.common.integrations.curios.AdvancedEnergyTransmitterPlayerEquipmentSources;
import com.jdte.setup.JDTEDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/** Discovers enabled extraction stacks carried by a server player. */
public final class AEExtractionPlayerService {
    private AEExtractionPlayerService() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.isRemoved() || !player.isAlive()) return;
        List<ItemStack> candidates = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            candidates.add(inventory.getItem(slot));
        }
        AdvancedEnergyTransmitterPlayerEquipmentSources.collectCurios(player, candidates::add);
        List<ItemStack> enabled = collectDistinct(candidates);
        if (!enabled.isEmpty()) AEExtractionNetwork.refill(player, enabled);
    }

    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getOrDefault(JDTEDataComponents.AE_EXTRACTION_ENABLED.get(), false)) {
            event.getToolTip().add(net.minecraft.network.chat.Component.translatable(
                    "tooltip.jdte.ae_extraction.enabled"));
        }
    }

    public static List<ItemStack> collectDistinct(List<ItemStack> candidates) {
        List<ItemStack> result = new ArrayList<>();
        IdentityHashMap<ItemStack, Boolean> seenStacks = new IdentityHashMap<>();
        IdentityHashMap<Object, Boolean> seenCapabilities = new IdentityHashMap<>();
        for (ItemStack stack : candidates) {
            if (stack.isEmpty() || !stack.getOrDefault(JDTEDataComponents.AE_EXTRACTION_ENABLED.get(), false)
                    || seenStacks.put(stack, Boolean.TRUE) != null) continue;
            Object energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            Object fluid = stack.getCapability(Capabilities.FluidHandler.ITEM);
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
