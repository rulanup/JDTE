package com.jdte.common.items.machinesettings;

import com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler;
import com.jdte.common.containers.handlers.UpgradeStorageHandler;
import com.jdte.common.items.AdvancedUpgradeStorageItem;
import com.jdte.common.items.UpgradeStorageItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Plans all upgrade withdrawals before touching the inventory or a component-backed storage case. */
public final class MachineUpgradeTransfer {
    private final List<Withdrawal> withdrawals;
    private final List<Withdrawal> applied = new ArrayList<>();

    private MachineUpgradeTransfer(List<Withdrawal> withdrawals) {
        this.withdrawals = List.copyOf(withdrawals);
    }

    public static Optional<MachineUpgradeTransfer> prepare(Player player, Inventory inventory,
                                                            List<ItemStack> requirements) {
        if (requirements.isEmpty()) return Optional.of(new MachineUpgradeTransfer(List.of()));
        if (inventory == null) return Optional.empty();
        List<IItemHandlerModifiable> sources = new ArrayList<>();
        sources.add(new InvWrapper(inventory));
        Set<ItemStack> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            addStorage(inventory.getItem(slot), sources, seen);
        }
        if (player != null) {
            for (ItemStack storage : AdvancedUpgradeStorageItem.findAllStorages(player)) {
                addStorage(storage, sources, seen);
            }
        }
        int[][] reserved = sources.stream().map(handler -> new int[handler.getSlots()]).toArray(int[][]::new);
        for (ItemStack required : requirements) {
            int remaining = required.getCount();
            for (int source = 0; source < sources.size() && remaining > 0; source++) {
                IItemHandlerModifiable handler = sources.get(source);
                for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
                    ItemStack available = handler.getStackInSlot(slot);
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
            IItemHandlerModifiable handler = sources.get(source);
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (reserved[source][slot] > 0) withdrawals.add(new Withdrawal(handler, slot,
                        reserved[source][slot], handler.getStackInSlot(slot).copy()));
            }
        }
        return Optional.of(new MachineUpgradeTransfer(withdrawals));
    }

    private static void addStorage(ItemStack stack, List<IItemHandlerModifiable> sources, Set<ItemStack> seen) {
        if (stack.isEmpty() || !(stack.getItem() instanceof UpgradeStorageItem) || !seen.add(stack)) return;
        sources.add(stack.getItem() instanceof AdvancedUpgradeStorageItem
                ? new AdvancedUpgradeStorageHandler(stack) : new UpgradeStorageHandler(stack));
    }

    public void consume() {
        try {
            for (Withdrawal withdrawal : withdrawals) {
                ItemStack current = withdrawal.handler().getStackInSlot(withdrawal.slot());
                if (!ItemStack.matches(current, withdrawal.before())) {
                    throw new IllegalStateException("Upgrade source changed during settings paste");
                }
                applied.add(withdrawal);
                ItemStack extracted = withdrawal.handler().extractItem(withdrawal.slot(), withdrawal.count(), false);
                if (extracted.getCount() != withdrawal.count()
                        || !ItemStack.isSameItemSameComponents(extracted, withdrawal.before())) {
                    throw new IllegalStateException("Upgrade source rejected a planned withdrawal");
                }
            }
        } catch (RuntimeException failure) {
            rollback();
            throw failure;
        }
    }

    public void rollback() {
        for (int index = applied.size() - 1; index >= 0; index--) {
            Withdrawal withdrawal = applied.get(index);
            withdrawal.handler().setStackInSlot(withdrawal.slot(), withdrawal.before().copy());
        }
        applied.clear();
    }

    private record Withdrawal(IItemHandlerModifiable handler, int slot, int count, ItemStack before) {}
}
