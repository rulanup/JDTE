package com.jdte.common.blockentities;

import com.jdte.common.integrations.curios.AdvancedEnergyTransmitterPlayerEquipmentSources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

final class AdvancedEnergyTransmitterPlayerCharger {
    private static final class PlannedItem {
        private IEnergyStorage receiver;
        private int demand;

        private void set(IEnergyStorage receiver, int demand) {
            this.receiver = receiver;
            this.demand = demand;
        }
    }

    private final List<PlannedItem> plan = new ArrayList<>();
    private final IdentityHashMap<ItemStack, Boolean> seenStacks = new IdentityHashMap<>();
    private final IdentityHashMap<IEnergyStorage, Boolean> seenReceivers = new IdentityHashMap<>();
    private int plannedCount;
    private int inspectedCount;

    long plan(ServerPlayer player, int maxItems) {
        plannedCount = 0;
        inspectedCount = 0;
        seenStacks.clear();
        seenReceivers.clear();

        for (int slot = 0; slot < 9 && inspectedCount < maxItems; slot++) {
            inspect(player.getInventory().getItem(slot), maxItems);
        }
        for (ItemStack armor : player.getInventory().armor) {
            if (!inspect(armor, maxItems)) {
                break;
            }
        }
        inspect(player.getOffhandItem(), maxItems);
        AdvancedEnergyTransmitterPlayerEquipmentSources.collectCurios(
                player, stack -> inspect(stack, maxItems));

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

    private boolean inspect(ItemStack stack, int maxItems) {
        if (inspectedCount >= maxItems) {
            return false;
        }
        if (stack.isEmpty() || seenStacks.put(stack, Boolean.TRUE) != null) {
            return true;
        }
        inspectedCount++;
        IEnergyStorage receiver = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (receiver == null || !receiver.canReceive()
                || seenReceivers.put(receiver, Boolean.TRUE) != null) {
            return inspectedCount < maxItems;
        }
        int simulated = receiver.receiveEnergy(Integer.MAX_VALUE, true);
        if (simulated <= 0) {
            return inspectedCount < maxItems;
        }
        long reportedGap = Math.max(0L,
                (long) receiver.getMaxEnergyStored() - Math.max(0, receiver.getEnergyStored()));
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