package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.items.UpgradeCardItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class UpgradeItemStackHandler extends ItemStackHandler {
    public static final int SLOT_COUNT = 4;
    public static final int BASE_CLICKER_FLUID_CAPACITY = 8000;
    private final BaseMachineBE machine;

    public UpgradeItemStackHandler(BaseMachineBE machine) {
        this(machine, SLOT_COUNT);
    }

    protected UpgradeItemStackHandler(BaseMachineBE machine, int slotCount) {
        super(slotCount);
        this.machine = machine;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (UpgradeHelper.isSmelterUpgrade(stack) && machine instanceof com.jdte.common.blockentities.MineralExtractorBE) {
            for (int other = 0; other < getSlots(); other++)
                if (other != slot && UpgradeHelper.isSmelterUpgrade(getStackInSlot(other))) return false;
            return true;
        }
        if (UpgradeHelper.isMixingUpgrade(stack)) {
            if (!(machine instanceof com.jdte.common.blockentities.FluidMixerBE)) return false;
            for(int other=0;other<getSlots();other++)if(other!=slot && UpgradeHelper.isMixingUpgrade(getStackInSlot(other)))return false;
            return true;
        }
        if (!(stack.getItem() instanceof UpgradeCardItem upgradeCard)) {
            return false;
        }

        UpgradeType type = upgradeCard.getType();
        if (!UpgradeHelper.isUpgradeCompatible(machine, type)) {
            return false;
        }
        if (type.isAEOutputUpgrade()) {
            for (int other=0;other<getSlots();other++)
                if (other!=slot && getStackInSlot(other).getItem() instanceof UpgradeCardItem card && card.getType().isAEOutputUpgrade()) return false;
        }
        if (type.isSpeedUpgrade() && hasOppositeSpeedUpgrade(type, slot)) {
            return false;
        }
        if (hasConflictingHarvestUpgrade(type)) {
            return false;
        }

        return count(type, slot) < UpgradeHelper.getMaxUpgrades(machine, type);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (!stack.isEmpty()) {
            if (!isItemValid(slot, stack)) {
                return;
            }
            stack = stack.copyWithCount(1);
        }
        super.setStackInSlot(slot, stack);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!isItemValid(slot, stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (machine != null) {
            UpgradeHelper.syncCapacities(machine);
            UpgradeHelper.trimInactiveFilterSlots(machine);
            machine.markDirtyClient();
            com.jdte.common.manager.AEOutputManager.refresh(machine);
        }
    }

    private int count(UpgradeType type, int ignoredSlot) {
        int count = 0;
        for (int i = 0; i < getSlots(); i++) {
            if (i == ignoredSlot) {
                continue;
            }
            if (UpgradeHelper.isUpgrade(getStackInSlot(i), type)) {
                count++;
            }
        }
        return count;
    }

    private boolean hasOppositeSpeedUpgrade(UpgradeType type, int ignoredSlot) {
        for (UpgradeType other : new UpgradeType[]{UpgradeType.OVERCLOCK, UpgradeType.UNDERCLOCK, UpgradeType.ULTIMATE_OVERCLOCK}) {
            if (other != type && count(other, ignoredSlot) > 0) return true;
        }
        return false;
    }

    private boolean hasConflictingHarvestUpgrade(UpgradeType type) {
        if (type == UpgradeType.FORTUNE) {
            return count(UpgradeType.PRECISION, -1) > 0;
        }
        if (type == UpgradeType.PRECISION) {
            return count(UpgradeType.FORTUNE, -1) > 0;
        }
        return false;
    }
}
