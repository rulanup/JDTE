package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.LifeBreederBE;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LifeBreederContainer extends BaseMachineContainer {
    public LifeBreederContainer(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos());
    }

    public LifeBreederContainer(int id, Inventory inventory, BlockPos pos) {
        super(JDTEMenus.LIFE_BREEDER.get(), id, inventory, pos);
        if (baseMachineBE instanceof LifeBreederBE breeder) addDataSlots(breeder.getBreederData());
        addPlayerSlots(inventory);
    }

    @Override public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        for (int slot = 0; slot < LifeBreederBE.FEED_SLOTS; slot++) {
            addSlot(new SlotItemHandler(machineHandler, slot,
                    8 + (slot % 2) * 18, 15 + (slot / 2) * 18));
        }
        for (int slot = 0; slot < LifeBreederBE.OUTPUT_SLOTS; slot++) {
            addSlot(new OutputSlot(machineHandler, LifeBreederBE.OUTPUT_START_SLOT + slot,
                    62 + (slot % 4) * 18, 15 + (slot / 4) * 18));
        }
    }

    public LifeBreederBE getBreeder() { return (LifeBreederBE) baseMachineBE; }
    public int getFluidAmount() { return data(0, 0); }
    public int getFluidCapacity() { return data(1, 1); }
    public int getMultiplier() { return data(2, 1); }
    public int getMaxMultiplier() { return data(3, 32); }
    public int getMode() { return data(4, 0); }
    private int data(int index, int fallback) {
        return baseMachineBE instanceof LifeBreederBE breeder ? breeder.getBreederData().get(index) : fallback;
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.LIFE_BREEDER.get());
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int machineEnd = LifeBreederBE.TOTAL_SLOTS;
        int filterEnd = machineEnd + FILTER_SLOTS;
        int upgradeEnd = filterEnd + LifeBreederBE.UPGRADE_SLOTS;
        if (index < machineEnd) {
            if (!moveStackTo(stack, upgradeEnd, slots.size(), true)) return ItemStack.EMPTY;
        } else if (index < filterEnd) {
            return ItemStack.EMPTY;
        } else if (index < upgradeEnd) {
            if (!moveStackTo(stack, upgradeEnd, slots.size(), true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof UpgradeCardItem) {
            if (!moveStackTo(stack, filterEnd, upgradeEnd, false)) return ItemStack.EMPTY;
        } else if (!moveStackTo(stack, 0, LifeBreederBE.FEED_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }

    private boolean moveStackTo(ItemStack stack, int start, int end, boolean reverse) {
        boolean moved = false;
        int index = reverse ? end - 1 : start;
        while (!stack.isEmpty() && (reverse ? index >= start : index < end)) {
            Slot target = slots.get(index);
            if (target.mayPlace(stack) && target.hasItem()) {
                ItemStack existing = target.getItem();
                if (ItemStack.isSameItemSameComponents(stack, existing)) {
                    int max = Math.min(target.getMaxStackSize(stack), stack.getMaxStackSize());
                    int transferable = Math.min(stack.getCount(), max - existing.getCount());
                    if (transferable > 0) {
                        existing.grow(transferable);
                        stack.shrink(transferable);
                        target.setChanged();
                        moved = true;
                    }
                }
            }
            index += reverse ? -1 : 1;
        }
        index = reverse ? end - 1 : start;
        while (!stack.isEmpty() && (reverse ? index >= start : index < end)) {
            Slot target = slots.get(index);
            if (!target.hasItem() && target.mayPlace(stack)) {
                int count = Math.min(stack.getCount(), target.getMaxStackSize(stack));
                target.set(stack.copyWithCount(count));
                stack.shrink(count);
                target.setChanged();
                moved = true;
            }
            index += reverse ? -1 : 1;
        }
        return moved;
    }

    private static final class OutputSlot extends SlotItemHandler {
        private OutputSlot(IItemHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
    }
}
