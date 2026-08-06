package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.upgrades.BioFactoryUpgradeItemStackHandler;
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

public class BioFactoryContainer extends BaseMachineContainer implements FilterPageHolder {
    public static final int SPECIMEN_X = 15;
    public static final int INPUT_X = 15;
    public static final int INPUT_Y = -20;
    public static final int OUTPUT_X = 79;
    public static final int OUTPUT_Y = -20;
    private int outputPage;

    public BioFactoryContainer(int id, Inventory inventory, FriendlyByteBuf data) { this(id, inventory, data.readBlockPos()); }
    public BioFactoryContainer(int id, Inventory inventory, BlockPos pos) {
        super(JDTEMenus.BIO_FACTORY.get(), id, inventory, pos);
        if (baseMachineBE instanceof BioFactoryBE factory) addDataSlots(factory.getMachineData());
        addPlayerSlots(player.getInventory());
    }

    @Override public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        addSlot(new SpecimenSlot(machineHandler, BioFactoryBE.SPECIMEN_SLOT, SPECIMEN_X, INPUT_Y));
        for (int input = 0; input < BioFactoryBE.INPUT_SLOTS; input++) {
            addSlot(new SlotItemHandler(machineHandler, BioFactoryBE.inputStorageSlot(input),
                    INPUT_X, INPUT_Y + (input + 1) * 18));
        }
        for (int i = 0; i < BioFactoryBE.BASE_OUTPUT_SLOTS; i++) {
            addSlot(new OutputSlot(machineHandler, i, OUTPUT_X + (i % 2) * 18,
                    OUTPUT_Y + (i / 2) * 18, this));
        }
    }

    public BioFactoryBE getFactory() { return (BioFactoryBE) baseMachineBE; }
    public int getProgress() { return getData(0, 0); }
    public int getProcessTicks() { return getData(1, 600); }
    public int getLifeFluid() { return getData(3, 0); }
    public int getTimeFluid() { return getData(4, 0); }
    public int getProcessFluid() { return getData(5, 0); }
    public int getProductFluid() { return getData(6, 0); }
    public int getFluidCapacity() { return getData(7, 1); }
    public double getProductivityMultiplier() { return getData(8, 100) / 100.0D; }
    public int getMultiplier() { return getData(9, 1); }
    public int getMaxMultiplier() { return getData(10, 32); }
    private int getData(int index, int fallback) {
        return baseMachineBE instanceof BioFactoryBE factory ? factory.getMachineData().get(index) : fallback;
    }

    public boolean isSpecimenSlot(Slot slot) { return slots.indexOf(slot) == 0; }
    public boolean isFoodSlot(Slot slot) {
        int index = slots.indexOf(slot);
        return index >= 1 && index <= BioFactoryBE.INPUT_SLOTS;
    }
    public boolean isOutputSlot(Slot slot) {
        int index = slots.indexOf(slot);
        return index >= 1 + BioFactoryBE.INPUT_SLOTS
                && index < 1 + BioFactoryBE.INPUT_SLOTS + BioFactoryBE.BASE_OUTPUT_SLOTS;
    }
    public int getOutputPage() { return outputPage; }
    public int getMaxOutputPage() { return Math.max(0, (getData(2, BioFactoryBE.BASE_OUTPUT_SLOTS) - 1) / BioFactoryBE.BASE_OUTPUT_SLOTS); }
    public void setOutputPage(int page) {
        int clamped = Math.clamp(page, 0, getMaxOutputPage());
        if (outputPage != clamped) {
            outputPage = clamped;
            broadcastChanges();
        }
    }
    @Override public int jdte$getFilterPage() { return outputPage; }
    @Override public void jdte$setFilterPage(int page) { setOutputPage(page); }

    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.BIO_FACTORY.get());
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int machineSlots = 1 + BioFactoryBE.INPUT_SLOTS + BioFactoryBE.BASE_OUTPUT_SLOTS;
        int playerStart = machineSlots + BioFactoryBE.UPGRADE_SLOTS;
        if (index < playerStart) {
            if (!moveStackTo(stack, playerStart, slots.size(), true)) return ItemStack.EMPTY;
        } else if (getFactory().getMachineHandler().isItemValid(BioFactoryBE.SPECIMEN_SLOT, stack)) {
            if (!moveStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof UpgradeCardItem || stack.getItem() instanceof LootingUpgradeItem
                || BioFactoryUpgradeItemStackHandler.getProductivityTier(stack) > 0) {
            if (!moveStackTo(stack, machineSlots, playerStart, false)) return ItemStack.EMPTY;
        } else if (!moveStackTo(stack, 1, 1 + BioFactoryBE.INPUT_SLOTS, false)) {
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

    private static final class SpecimenSlot extends SlotItemHandler {
        private SpecimenSlot(IItemHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
        @Override public int getMaxStackSize() { return 1; }
    }

    private static final class OutputSlot extends SlotItemHandler {
        private final int pageSlot;
        private final BioFactoryContainer container;
        private OutputSlot(IItemHandler handler, int pageSlot, int x, int y, BioFactoryContainer container) {
            super(handler, BioFactoryBE.OUTPUT_START_SLOT + pageSlot, x, y);
            this.pageSlot = pageSlot;
            this.container = container;
        }
        @Override public int getSlotIndex() {
            return BioFactoryBE.OUTPUT_START_SLOT + container.outputPage * BioFactoryBE.BASE_OUTPUT_SLOTS + pageSlot;
        }
        @Override public ItemStack getItem() { return active() ? getItemHandler().getStackInSlot(getSlotIndex()) : ItemStack.EMPTY; }
        @Override public boolean hasItem() { return !getItem().isEmpty(); }
        @Override public void set(ItemStack stack) {
            if (active()) ((net.neoforged.neoforge.items.IItemHandlerModifiable) getItemHandler()).setStackInSlot(getSlotIndex(), stack);
        }
        @Override public void initialize(ItemStack stack) { set(stack); }
        @Override public ItemStack remove(int amount) { return active() ? getItemHandler().extractItem(getSlotIndex(), amount, false) : ItemStack.EMPTY; }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return active() && !getItemHandler().extractItem(getSlotIndex(), 1, true).isEmpty(); }
        private boolean active() { return getSlotIndex() < BioFactoryBE.OUTPUT_START_SLOT + container.getData(2, BioFactoryBE.BASE_OUTPUT_SLOTS); }
    }
}
