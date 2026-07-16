package com.jdte.common.upgrades;

import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.items.LootingUpgradeItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.List;

public class BioFactoryUpgradeItemStackHandler extends UpgradeItemStackHandler {
    public static final int SLOT_COUNT = 8;
    public static final int MAX_PRODUCTIVITY = 4;
    public static final int MAX_LOOTING = 4;
    private static final Map<ResourceLocation, Integer> PRODUCTIVITY_TIERS = Map.of(
            ResourceLocation.fromNamespaceAndPath("productivelib", "upgrade_productivity"), 1,
            ResourceLocation.fromNamespaceAndPath("productivelib", "upgrade_productivity_2"), 2,
            ResourceLocation.fromNamespaceAndPath("productivelib", "upgrade_productivity_3"), 3,
            ResourceLocation.fromNamespaceAndPath("productivelib", "upgrade_productivity_4"), 4);

    public BioFactoryUpgradeItemStackHandler(BioFactoryBE machine) {
        super(machine, SLOT_COUNT);
    }

    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (getProductivityTier(stack) > 0) return countProductivity(slot) < MAX_PRODUCTIVITY;
        if (stack.getItem() instanceof LootingUpgradeItem) return countLooting(slot) < MAX_LOOTING;
        return super.isItemValid(slot, stack);
    }

    public int countProductivityTier(int tier) {
        int count = 0;
        for (int slot = 0; slot < getSlots(); slot++) {
            if (getProductivityTier(getStackInSlot(slot)) == tier) count++;
        }
        return count;
    }

    public int getProductivityCount() { return countProductivity(-1); }
    public int getLootingCount() { return countLooting(-1); }

    private int countProductivity(int ignoredSlot) {
        int count = 0;
        for (int slot = 0; slot < getSlots(); slot++) {
            if (slot != ignoredSlot && getProductivityTier(getStackInSlot(slot)) > 0) count++;
        }
        return count;
    }

    private int countLooting(int ignoredSlot) {
        int count = 0;
        for (int slot = 0; slot < getSlots(); slot++) {
            if (slot != ignoredSlot && getStackInSlot(slot).getItem() instanceof LootingUpgradeItem) count++;
        }
        return count;
    }

    public static int getProductivityTier(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return PRODUCTIVITY_TIERS.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), 0);
    }

    public static List<ResourceLocation> getProductivityUpgradeIds() {
        return PRODUCTIVITY_TIERS.keySet().stream().sorted().toList();
    }
}
