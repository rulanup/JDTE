package com.jdte.common.blockentities;

import com.jdte.common.integrations.curios.AdvancedEnergyTransmitterPlayerEquipmentSources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

final class AdvancedEnergyTransmitterPlayerCharger {
    private static final class PlannedItem {
        private EnergyHandler receiver;
        private int demand;

        private void set(EnergyHandler receiver, int demand) {
            this.receiver = receiver;
            this.demand = demand;
        }
    }

    private final List<PlannedItem> plan = new ArrayList<>();
    private final IdentityHashMap<ItemStack, Boolean> seenStacks = new IdentityHashMap<>();
    private final IdentityHashMap<EnergyHandler, Boolean> seenReceivers = new IdentityHashMap<>();
    private int plannedCount;
    private int inspectedCount;

    long plan(ServerPlayer player, int maxItems) {
        plannedCount = 0;
        inspectedCount = 0;
        seenStacks.clear();
        seenReceivers.clear();

        for (int slot = 0; slot < 9 && inspectedCount < maxItems; slot++) {
            inspect(player.getInventory().getItem(slot), ItemAccess.forPlayerSlot(player, slot), maxItems);
        }
        for (int slot = Inventory.INVENTORY_SIZE; slot <= Inventory.SLOT_OFFHAND && inspectedCount < maxItems; slot++) {
            inspect(player.getInventory().getItem(slot), ItemAccess.forPlayerSlot(player, slot), maxItems);
        }
        AdvancedEnergyTransmitterPlayerEquipmentSources.collectCurios(
                player, stack -> inspect(stack, ItemAccess.forStack(stack), maxItems));

        long total = 0L;
        for (int index = 0; index < plannedCount; index++) {
            total = AdvancedEnergyTransmitterScheduler.saturatingAdd(total, plan.get(index).demand);
        }
        return total;
    }

    long charge(long available, int maxCallsPerItem) {
        long transferred = 0L;
        for (int index = 0; index < plannedCount && available > 0L; index++) {
            PlannedItem item = plan.get(index);
            long accepted = AdvancedEnergyTransmitterScheduler.boundedReceive(
                    item.receiver, item.demand, available, maxCallsPerItem);
            available -= accepted;
            transferred = AdvancedEnergyTransmitterScheduler.saturatingAdd(
                    transferred, accepted);
        }
        return transferred;
    }

    private boolean inspect(ItemStack stack, ItemAccess access, int maxItems) {
        if (inspectedCount >= maxItems) {
            return false;
        }
        if (stack.isEmpty() || seenStacks.put(stack, Boolean.TRUE) != null) {
            return true;
        }
        inspectedCount++;
        EnergyHandler receiver = access.getCapability(Capabilities.Energy.ITEM);
        if (receiver == null
                || seenReceivers.put(receiver, Boolean.TRUE) != null) {
            return inspectedCount < maxItems;
        }
        int simulated = AdvancedEnergyTransmitterScheduler.receive(receiver, Integer.MAX_VALUE, true);
        if (simulated <= 0) {
            return inspectedCount < maxItems;
        }
        long reportedGap = Math.max(0L,
                receiver.getCapacityAsLong() - receiver.getAmountAsLong());
        int demand = AdvancedEnergyTransmitterScheduler.clampToInt(
                Math.max(simulated, reportedGap));
        ensureCapacity(plannedCount + 1);
        plan.get(plannedCount++).set(receiver, demand);
        return inspectedCount < maxItems;
    }

    private void ensureCapacity(int required) {
        while (plan.size() < required) {
            plan.add(new PlannedItem());
        }
    }
}
