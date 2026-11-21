package com.jdte.common.blockentities;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** The active production slots for one greenhouse settlement. */
record GreenhouseOutputInventory(MachineInventoryHandler inventory, int start, int size) {
    ItemStack copyStack(int index) {
        return inventory.copyStack(start + index);
    }

    int slotLimit(int index) {
        return inventory.getCapacityAsInt(start + index, ItemResource.EMPTY);
    }

    boolean replace(List<ItemStack> consumed, List<ItemStack> produced) {
        Set<ItemResource> ingredients = consumed.stream().map(ItemResource::of).collect(Collectors.toSet());
        try (Transaction tx = Transaction.openRoot()) {
            for (int slot = start; slot < start + size; slot++) {
                ItemResource resource = inventory.getResource(slot);
                if (resource.isEmpty() || !ingredients.contains(resource)) continue;
                int amount = inventory.getAmountAsInt(slot);
                if (inventory.extractForProduction(slot, resource, amount, tx) != amount) return false;
            }
            if (!inventory.insertBatch(start, start + size, produced, tx)) return false;
            tx.commit();
            return true;
        }
    }
}
