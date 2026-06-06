package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.common.containers.slots.FilterBasicSlot;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeSlot;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class DynamicFilterSlot extends FilterBasicSlot {
    private final BaseMachineContainer container;
    private final int baseFilterSlots;

    public DynamicFilterSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, BaseMachineContainer container, int baseFilterSlots) {
        super(itemHandler, index, xPosition, yPosition);
        this.container = container;
        this.baseFilterSlots = baseFilterSlots;
    }

    @Override
    public boolean isActive() {
        return getSlotIndex() < baseFilterSlots + jdte$getFilterUpgradeCount() * UpgradeHelper.getFilterSlotsPerUpgrade();
    }

    private int jdte$getFilterUpgradeCount() {
        int count = 0;
        boolean sawUpgradeSlot = false;
        for (Slot slot : container.slots) {
            if (slot instanceof UpgradeSlot) {
                sawUpgradeSlot = true;
                if (UpgradeHelper.isUpgrade(slot.getItem(), UpgradeType.FILTER)) {
                    count++;
                }
            }
        }
        if (!sawUpgradeSlot && container.baseMachineBE != null) {
            count = UpgradeHelper.countUpgrades(container.baseMachineBE, UpgradeType.FILTER);
        }
        return Math.min(count, UpgradeType.FILTER.getMaxPerMachine());
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return isActive() && super.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        if (isActive() || stack.isEmpty()) {
            super.set(stack);
        }
    }
}
