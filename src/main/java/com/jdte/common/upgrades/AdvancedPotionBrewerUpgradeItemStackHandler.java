package com.jdte.common.upgrades;

import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.items.EnergyBrewingUpgradeItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 高级炼药机专用升级槽：标准升级类型之外，额外接受至多 1 张能量酿造升级。
 */
public class AdvancedPotionBrewerUpgradeItemStackHandler extends UpgradeItemStackHandler {
    public static final int MAX_ENERGY_BREWING = 1;

    public AdvancedPotionBrewerUpgradeItemStackHandler(AdvancedPotionBrewerBE machine) {
        super(machine);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (stack.getItem() instanceof EnergyBrewingUpgradeItem) {
            return countEnergyBrewing(slot) < MAX_ENERGY_BREWING;
        }
        return super.isItemValid(slot, stack);
    }

    public boolean hasEnergyBrewingUpgrade() {
        return countEnergyBrewing(-1) > 0;
    }

    private int countEnergyBrewing(int ignoredSlot) {
        int count = 0;
        for (int i = 0; i < getSlots(); i++) {
            if (i != ignoredSlot && getStackInSlot(i).getItem() instanceof EnergyBrewingUpgradeItem) {
                count++;
            }
        }
        return count;
    }
}
