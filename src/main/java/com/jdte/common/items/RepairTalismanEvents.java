package com.jdte.common.items;

import com.jdte.setup.JDTEConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 修复护符的玩家刻处理：背包中携带修复护符时，按配置周期修复全部受损物品
 * （主背包、盔甲与副手）。修复消耗 FE，从背包中已充能的能量物品按槽位顺序
 * 抽取；每点耐久的费用可配置，设为 0 时免费修复。
 */
public final class RepairTalismanEvents {
    private RepairTalismanEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.isRemoved() || player.isDeadOrDying()) {
            return;
        }
        int interval = JDTEConfig.COMMON.repairTalismanCycleInterval.get();
        int amount = JDTEConfig.COMMON.repairTalismanAmountPerCycle.get();
        if (interval <= 0 || amount <= 0) {
            return;
        }
        if (player.tickCount % interval != 0) {
            return;
        }
        Inventory inventory = player.getInventory();
        if (!hasTalisman(inventory)) {
            return;
        }

        List<ItemStack> damaged = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || !stack.isDamageableItem() || stack.has(DataComponents.UNBREAKABLE)) {
                continue;
            }
            if (stack.getDamageValue() > 0) {
                damaged.add(stack);
            }
        }
        if (damaged.isEmpty()) {
            return;
        }

        int costPerDurability = JDTEConfig.COMMON.repairTalismanEnergyPerDurability.get();
        boolean repaired = false;
        if (costPerDurability <= 0) {
            for (ItemStack stack : damaged) {
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - amount));
                repaired = true;
            }
        } else {
            List<IEnergyStorage> sources = collectEnergySources(inventory);
            long available = 0;
            for (IEnergyStorage source : sources) {
                available += source.extractEnergy(Integer.MAX_VALUE, true);
            }
            for (ItemStack stack : damaged) {
                if (available < costPerDurability) {
                    break;
                }
                int repair = Math.min(amount, stack.getDamageValue());
                long cost = (long) repair * costPerDurability;
                long payable = Math.min(cost, available);
                int affordable = (int) (payable / costPerDurability);
                if (affordable <= 0) {
                    continue;
                }
                long needed = (long) affordable * costPerDurability;
                if (!extractFromSources(sources, needed)) {
                    continue;
                }
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - affordable));
                available -= needed;
                repaired = true;
            }
        }
        if (repaired) {
            inventory.setChanged();
        }
    }

    private static boolean hasTalisman(Inventory inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).getItem() instanceof RepairTalismanItem) {
                return true;
            }
        }
        return false;
    }

    /** 收集背包中可放电的能量物品（电池、便携发电机等），按槽位顺序。 */
    private static List<IEnergyStorage> collectEnergySources(Inventory inventory) {
        List<IEnergyStorage> sources = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (storage != null && storage.canExtract()) {
                sources.add(storage);
            }
        }
        return sources;
    }

    /** 从能量物品中按顺序抽取指定 FE，抽取不足时不产生部分支付。 */
    private static boolean extractFromSources(List<IEnergyStorage> sources, long amount) {
        long remaining = amount;
        for (IEnergyStorage source : sources) {
            while (remaining > 0) {
                int step = (int) Math.min(remaining, Integer.MAX_VALUE);
                int extracted = source.extractEnergy(step, false);
                if (extracted <= 0) {
                    break;
                }
                remaining -= extracted;
            }
            if (remaining <= 0) {
                return true;
            }
        }
        // 能量不足：无法回滚已抽取部分，理论仅在并发修改时发生；尽力而为
        return remaining <= 0;
    }
}
