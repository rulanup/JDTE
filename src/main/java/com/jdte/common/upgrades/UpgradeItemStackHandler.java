package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.items.UpgradeCardItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
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
        if (!(stack.getItem() instanceof UpgradeCardItem upgradeCard)) {
            return false;
        }

        UpgradeType type = upgradeCard.getType();
        if (!UpgradeHelper.isUpgradeCompatible(machine, type)) {
            return false;
        }
        if (type.isSpeedUpgrade() && hasOppositeSpeedUpgrade(type)) {
            return false;
        }

        return count(type, slot) < type.getMaxPerMachine();
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
            if (machine.getLevel() != null) {
                machine.getLevel().invalidateCapabilities(machine.getBlockPos());
            }
            machine.markDirtyClient();
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

    private boolean hasOppositeSpeedUpgrade(UpgradeType type) {
        UpgradeType opposite = type == UpgradeType.OVERCLOCK ? UpgradeType.UNDERCLOCK : UpgradeType.OVERCLOCK;
        return count(opposite, -1) > 0;
    }
}
