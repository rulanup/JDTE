package com.jdte.common.upgrades;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.jdte.common.blockentities.MineralExtractorBE;
import com.jdte.common.blockentities.AEOutputManager;
import com.jdte.common.items.UpgradeCardItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class UpgradeItemStackHandler extends ItemStackHandler {
    public static final int SLOT_COUNT = 4;
    public static final int BASE_CLICKER_FLUID_CAPACITY = 8000;
    private final BaseMachineBE machine;
    private int contentVersion;
    private int countedVersion = -1;
    private final int[] typeCounts = new int[UpgradeType.values().length];

    public UpgradeItemStackHandler(BaseMachineBE machine) {
        this(machine, SLOT_COUNT);
    }

    protected UpgradeItemStackHandler(BaseMachineBE machine, int slotCount) {
        super(slotCount);
        this.machine = machine;
    }

    /**
     * Monotonic counter bumped whenever slot contents may have changed; lets callers
     * memoize derived values (counts, capacities, costs) without re-scanning slots.
     */
    public int getContentVersion() {
        return contentVersion;
    }

    /**
     * Raw per-type upgrade card count, cached until the next content change.
     * Matches {@link UpgradeHelper#isUpgrade(ItemStack, UpgradeType)} semantics.
     */
    public int getCachedUpgradeCount(UpgradeType type) {
        if (countedVersion != contentVersion) {
            java.util.Arrays.fill(typeCounts, 0);
            for (int slot = 0; slot < getSlots(); slot++) {
                ItemStack stack = getStackInSlot(slot);
                if (stack.getItem() instanceof UpgradeCardItem upgradeCard) {
                    typeCounts[upgradeCard.getType().ordinal()]++;
                }
            }
            countedVersion = contentVersion;
        }
        return typeCounts[type.ordinal()];
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (machine instanceof MineralExtractorBE && UpgradeHelper.isSmelterUpgrade(stack)) {
            return countSmelter(slot) == 0;
        }
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
        contentVersion++;
        if (machine != null) {
            UpgradeHelper.syncCapacities(machine);
            UpgradeHelper.trimInactiveFilterSlots(machine);
            if (machine.getLevel() != null) {
                machine.getLevel().invalidateCapabilities(machine.getBlockPos());
            }
            machine.markDirtyClient();
            AEOutputManager.refresh(machine);
        }
    }

    @Override
    protected void onLoad() {
        contentVersion++;
        if (machine != null) AEOutputManager.refresh(machine);
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        super.deserializeNBT(provider, tag);
        contentVersion++;
    }

    private int countSmelter(int ignoredSlot) {
        int count = 0;
        for (int slot = 0; slot < getSlots(); slot++) {
            if (slot != ignoredSlot && UpgradeHelper.isSmelterUpgrade(getStackInSlot(slot))) count++;
        }
        return count;
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
