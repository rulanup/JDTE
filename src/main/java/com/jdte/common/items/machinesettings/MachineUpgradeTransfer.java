package com.jdte.common.items.machinesettings;

import com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler;
import com.jdte.common.containers.handlers.UpgradeStorageHandler;
import com.jdte.common.items.AdvancedUpgradeStorageItem;
import com.jdte.common.items.UpgradeStorageItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Plans all upgrade withdrawals before touching the inventory or a component-backed storage case. */
public final class MachineUpgradeTransfer {
    private final List<Withdrawal> withdrawals;

    private MachineUpgradeTransfer(List<Withdrawal> withdrawals) {
        this.withdrawals = List.copyOf(withdrawals);
    }

    public static Optional<MachineUpgradeTransfer> prepare(Player player, Container inventory,
                                                            List<ItemStack> requirements) {
        if (requirements.isEmpty()) return Optional.of(new MachineUpgradeTransfer(List.of()));
        if (inventory == null) return Optional.empty();
        List<ResourceHandler<ItemResource>> sources = new ArrayList<>();
        sources.add(VanillaContainerWrapper.of(inventory));
        Set<ItemStack> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            addStorage(inventory.getItem(slot), sources, seen);
        }
        if (player != null) {
            for (ItemStack storage : AdvancedUpgradeStorageItem.findAllStorages(player)) {
                addStorage(storage, sources, seen);
            }
        }
        int[][] reserved = sources.stream().map(handler -> new int[handler.size()]).toArray(int[][]::new);
        for (ItemStack required : requirements) {
            int remaining = required.getCount();
            for (int source = 0; source < sources.size() && remaining > 0; source++) {
                ResourceHandler<ItemResource> handler = sources.get(source);
                for (int slot = 0; slot < handler.size() && remaining > 0; slot++) {
                    ItemStack available = handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
                    if (!ItemStack.isSameItemSameComponents(available, required)) continue;
                    int amount = Math.min(remaining, available.getCount() - reserved[source][slot]);
                    if (amount <= 0) continue;
                    reserved[source][slot] += amount;
                    remaining -= amount;
                }
            }
            if (remaining > 0) return Optional.empty();
        }
        List<Withdrawal> withdrawals = new ArrayList<>();
        for (int source = 0; source < sources.size(); source++) {
            ResourceHandler<ItemResource> handler = sources.get(source);
            for (int slot = 0; slot < handler.size(); slot++) {
                if (reserved[source][slot] > 0) withdrawals.add(new Withdrawal(handler, slot,
                        reserved[source][slot], handler.getResource(slot).toStack(handler.getAmountAsInt(slot))));
            }
        }
        return Optional.of(new MachineUpgradeTransfer(withdrawals));
    }

    private static void addStorage(ItemStack stack, List<ResourceHandler<ItemResource>> sources, Set<ItemStack> seen) {
        if (stack.isEmpty() || !(stack.getItem() instanceof UpgradeStorageItem) || !seen.add(stack)) return;
        sources.add(stack.getItem() instanceof AdvancedUpgradeStorageItem
                ? new AdvancedUpgradeStorageHandler(stack) : new UpgradeStorageHandler(stack));
    }

    public void consume(TransactionContext tx) {
        for (Withdrawal withdrawal : withdrawals) {
            var handler = withdrawal.handler();
            int slot = withdrawal.slot();
            ItemResource expected = ItemResource.of(withdrawal.before());
            if (!handler.getResource(slot).equals(expected) || handler.getAmountAsInt(slot) != withdrawal.before().getCount()) {
                throw new IllegalStateException("Upgrade source changed during settings paste");
            }
            if (handler.extract(slot, expected, withdrawal.count(), tx) != withdrawal.count()) {
                throw new IllegalStateException("Upgrade source rejected a planned withdrawal");
            }
        }
    }

    private record Withdrawal(ResourceHandler<ItemResource> handler, int slot, int count, ItemStack before) {}
}
