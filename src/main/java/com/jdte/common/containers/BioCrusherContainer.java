package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.SharpnessUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public abstract class BioCrusherContainer extends BaseMachineContainer implements FilterPageHolder {
    protected ContainerData bioCrusherData;
    private int outputPage = 0;

    protected BioCrusherContainer(@Nullable MenuType<?> menuType, int windowId, Inventory playerInventory, BlockPos blockPos) {
        super(menuType, windowId, playerInventory, blockPos);
        if (baseMachineBE instanceof BioCrusherBE crusher) {
            bioCrusherData = crusher.getBioCrusherData();
            addDataSlots(bioCrusherData);
        }
        addPlayerSlots(playerInventory);
    }

    @Override
    public void addMachineSlots() {
        if (baseMachineBE instanceof BioCrusherBE crusher) {
            if (crusher.hasOutputInventory()) {
                for (int i = 0; i < BioCrusherBE.OUTPUT_SLOTS_PER_PAGE; i++) {
                    addSlot(new BioCrusherOutputSlot(crusher.getMachineHandler(), i, 8 + i * 18, 54, this, i));
                }
            }

            addSlot(new BioCrusherUpgradeSlot(crusher.getSharpnessHandler(), 0, 60, 36, UpgradeKind.SHARPNESS));
            addSlot(new BioCrusherUpgradeSlot(crusher.getLootingHandler(), 0, 98, 36, UpgradeKind.LOOTING));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack currentStack = slot.getItem();
        ItemStack originalStack = currentStack.copy();
        int playerStart = Math.max(0, slots.size() - 36);
        int playerEnd = slots.size();

        if (index < playerStart) {
            if (!moveStackToRange(currentStack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= playerStart) {
            if (!movePlayerStackToBioCrusher(currentStack)) {
                return super.quickMoveStack(player, index);
            }
        } else {
            return super.quickMoveStack(player, index);
        }

        if (currentStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (currentStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, currentStack);
        return originalStack;
    }

    private boolean movePlayerStackToBioCrusher(ItemStack stack) {
        if (stack.getItem() instanceof SharpnessUpgradeItem) {
            return moveStackToUpgradeSlot(stack, UpgradeKind.SHARPNESS);
        }
        if (stack.getItem() instanceof LootingUpgradeItem) {
            return moveStackToUpgradeSlot(stack, UpgradeKind.LOOTING);
        }
        return false;
    }

    private boolean moveStackToUpgradeSlot(ItemStack stack, UpgradeKind kind) {
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            if (slot instanceof BioCrusherUpgradeSlot upgradeSlot && upgradeSlot.getKind() == kind) {
                return moveStackToRange(stack, i, i + 1, false);
            }
        }
        return false;
    }

    private boolean moveStackToRange(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean moved = false;
        int i = reverseDirection ? endIndex - 1 : startIndex;

        while (!stack.isEmpty() && isInMoveRange(i, startIndex, endIndex, reverseDirection)) {
            Slot slot = slots.get(i);
            if (slot.hasItem() && slot.mayPlace(stack)) {
                ItemStack existing = slot.getItem();
                if (ItemStack.isSameItemSameComponents(stack, existing)) {
                    int maxSize = Math.min(slot.getMaxStackSize(existing), existing.getMaxStackSize());
                    int movable = Math.min(stack.getCount(), maxSize - existing.getCount());
                    if (movable > 0) {
                        stack.shrink(movable);
                        existing.grow(movable);
                        slot.setChanged();
                        moved = true;
                    }
                }
            }
            i += reverseDirection ? -1 : 1;
        }

        i = reverseDirection ? endIndex - 1 : startIndex;
        while (!stack.isEmpty() && isInMoveRange(i, startIndex, endIndex, reverseDirection)) {
            Slot slot = slots.get(i);
            if (!slot.hasItem() && slot.mayPlace(stack)) {
                int maxSize = Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize());
                int movable = Math.min(stack.getCount(), maxSize);
                if (movable > 0) {
                    ItemStack movedStack = stack.copyWithCount(movable);
                    stack.shrink(movable);
                    slot.set(movedStack);
                    slot.setChanged();
                    moved = true;
                }
            }
            i += reverseDirection ? -1 : 1;
        }

        return moved;
    }

    private boolean isInMoveRange(int index, int startIndex, int endIndex, boolean reverseDirection) {
        return reverseDirection ? index >= startIndex : index < endIndex;
    }

    public enum UpgradeKind {
        SHARPNESS,
        LOOTING
    }

    public int getOutputPage() {
        return outputPage;
    }

    public boolean hasOutputSlots() {
        return baseMachineBE instanceof BioCrusherBE crusher && crusher.hasOutputInventory();
    }

    public int getMaxOutputPage() {
        if (!hasOutputSlots()) {
            return 0;
        }
        return baseMachineBE instanceof BioCrusherBE crusher
                ? Math.max(0, (crusher.getActiveOutputSlotCount() - 1) / BioCrusherBE.OUTPUT_SLOTS_PER_PAGE)
                : 0;
    }

    public void setOutputPage(int outputPage) {
        this.outputPage = Math.clamp(outputPage, 0, getMaxOutputPage());
    }

    @Override
    public int jdte$getFilterPage() {
        return getOutputPage();
    }

    @Override
    public void jdte$setFilterPage(int page) {
        setOutputPage(page);
    }

    public static class BioCrusherUpgradeSlot extends SlotItemHandler {
        private final UpgradeKind kind;

        public BioCrusherUpgradeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, UpgradeKind kind) {
            super(itemHandler, index, xPosition, yPosition);
            this.kind = kind;
        }

        public UpgradeKind getKind() {
            return kind;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return switch (kind) {
                case SHARPNESS -> stack.getItem() instanceof SharpnessUpgradeItem;
                case LOOTING -> stack.getItem() instanceof LootingUpgradeItem;
            };
        }
    }

    public static class BioCrusherOutputSlot extends SlotItemHandler {
        private final BioCrusherContainer container;
        private final int pageColumn;

        private BioCrusherOutputSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, BioCrusherContainer container, int pageColumn) {
            super(itemHandler, index, xPosition, yPosition);
            this.container = container;
            this.pageColumn = pageColumn;
        }

        @Override
        public int getSlotIndex() {
            return container.getOutputPage() * BioCrusherBE.OUTPUT_SLOTS_PER_PAGE + pageColumn;
        }

        @Override
        public ItemStack getItem() {
            return isActiveSlot() ? getItemHandler().getStackInSlot(getSlotIndex()) : ItemStack.EMPTY;
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }

        @Override
        public void set(ItemStack stack) {
            if (!isActiveSlot()) {
                return;
            }
            ((net.neoforged.neoforge.items.IItemHandlerModifiable) getItemHandler()).setStackInSlot(getSlotIndex(), stack);
            setChanged();
        }

        @Override
        public void initialize(ItemStack stack) {
            set(stack);
        }

        @Override
        public int getMaxStackSize() {
            return isActiveSlot() ? getItemHandler().getSlotLimit(getSlotIndex()) : 0;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return isActiveSlot() ? Math.min(stack.getMaxStackSize(), getItemHandler().getSlotLimit(getSlotIndex())) : 0;
        }

        @Override
        public ItemStack remove(int amount) {
            return isActiveSlot() ? getItemHandler().extractItem(getSlotIndex(), amount, false) : ItemStack.EMPTY;
        }

        @Override
        public boolean mayPickup(Player player) {
            return isActiveSlot() && !getItemHandler().extractItem(getSlotIndex(), 1, true).isEmpty();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        private boolean isActiveSlot() {
            return container.baseMachineBE instanceof BioCrusherBE crusher
                    && getSlotIndex() < crusher.getActiveOutputSlotCount()
                    && getSlotIndex() < getItemHandler().getSlots();
        }
    }

    public int getMode() {
        return bioCrusherData == null ? 0 : bioCrusherData.get(0);
    }
}
