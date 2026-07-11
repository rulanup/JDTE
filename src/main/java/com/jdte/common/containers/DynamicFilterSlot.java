package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.direwolf20.justdirethings.common.containers.handlers.FilterBasicHandler;
import com.direwolf20.justdirethings.common.containers.slots.FilterBasicSlot;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeSlot;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class DynamicFilterSlot extends FilterBasicSlot {
    private final BaseMachineContainer container;
    private final int baseFilterSlots;
    private final int col;

    public DynamicFilterSlot(IItemHandler itemHandler, int col, int xPosition, int yPosition, BaseMachineContainer container, int baseFilterSlots) {
        super(itemHandler, col, xPosition, yPosition);
        this.container = container;
        this.baseFilterSlots = baseFilterSlots;
        this.col = col;
    }

    private int jdte$getPage() {
        if (container instanceof FilterPageHolder holder) {
            return holder.jdte$getFilterPage();
        }
        return 0;
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

    private int jdte$getActiveFilterSlots() {
        return baseFilterSlots + jdte$getFilterUpgradeCount() * UpgradeHelper.getFilterSlotsPerUpgrade();
    }

    private int jdte$getHandlerIndex() {
        return jdte$getPage() * UpgradeHelper.getFilterSlotsPerUpgrade() + col;
    }

    @Override
    public int getSlotIndex() {
        return jdte$getHandlerIndex();
    }

    @Override
    public boolean isActive() {
        return jdte$getHandlerIndex() < jdte$getActiveFilterSlots();
    }

    // SlotItemHandler uses its own `index` field instead of calling getSlotIndex(),
    // so we must override all methods that access the handler by index.

    @Override
    public @NotNull ItemStack getItem() {
        return getItemHandler().getStackInSlot(getSlotIndex());
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        if (isActive() || stack.isEmpty()) {
            ((IItemHandlerModifiable) getItemHandler()).setStackInSlot(getSlotIndex(), stack);
            setChanged();
        }
    }

    @Override
    public void initialize(@NotNull ItemStack stack) {
        if (!(getItemHandler() instanceof IItemHandlerModifiable modifiable)) return;
        modifiable.setStackInSlot(getSlotIndex(), stack);
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return getItemHandler().getSlotLimit(getSlotIndex());
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(stack.getMaxStackSize(), getItemHandler().getSlotLimit(getSlotIndex()));
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        return getItemHandler().extractItem(getSlotIndex(), amount, false);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if (stack.isEmpty() || !isActive()) return false;
        return getItemHandler().isItemValid(getSlotIndex(), stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}
