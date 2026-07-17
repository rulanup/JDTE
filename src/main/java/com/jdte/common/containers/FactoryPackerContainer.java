package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.FactoryPackerBE;
import com.jdte.common.items.FactoryPackageItem;
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
import net.neoforged.neoforge.items.SlotItemHandler;

public class FactoryPackerContainer extends BaseMachineContainer {
    public FactoryPackerContainer(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos());
    }

    public FactoryPackerContainer(int id, Inventory inventory, BlockPos pos) {
        super(JDTEMenus.FACTORY_PACKER.get(), id, inventory, pos);
        if (baseMachineBE instanceof FactoryPackerBE packer) addDataSlots(packer.getOperationData());
        addPlayerSlots(inventory);
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        addSlot(new PackageSlot((FactoryPackerBE) baseMachineBE, 80, 18));
    }

    public FactoryPackerBE getPacker() { return (FactoryPackerBE) baseMachineBE; }
    public int getPhase() { return getPacker().getOperationData().get(0); }
    public int getProgress() { return getPacker().getOperationData().get(1); }
    public int getProgressMax() { return Math.max(1, getPacker().getOperationData().get(2)); }
    public int getErrorCode() { return getPacker().getOperationData().get(3); }

    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.FACTORY_PACKER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerStart = 1 + 8;
        if (index < playerStart) {
            if (!moveStackTo(stack, playerStart, slots.size(), true)) return ItemStack.EMPTY;
        } else if (FactoryPackageItem.isFactoryPackage(stack)) {
            if (!moveStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof UpgradeCardItem) {
            if (!moveStackTo(stack, 1, playerStart, false)) return ItemStack.EMPTY;
        } else {
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

    private static final class PackageSlot extends SlotItemHandler {
        private final FactoryPackerBE packer;

        private PackageSlot(FactoryPackerBE packer, int x, int y) {
            super(packer.getMachineHandler(), 0, x, y);
            this.packer = packer;
        }

        @Override public boolean mayPlace(ItemStack stack) {
            return !packer.isBusy() && FactoryPackageItem.isFactoryPackage(stack);
        }

        @Override public boolean mayPickup(Player player) { return !packer.isBusy(); }
        @Override public int getMaxStackSize() { return 1; }
    }
}
