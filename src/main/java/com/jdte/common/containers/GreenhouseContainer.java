package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.client.utils.GuiUpgradeLayoutConfig;
import com.jdte.common.blockentities.GreenhouseBE;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.recipes.GreenhouseCropResolver;
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
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GreenhouseContainer extends BaseMachineContainer implements FilterPageHolder {
    private int outputPage;

    public GreenhouseContainer(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos());
    }

    public GreenhouseContainer(int id, Inventory inventory, BlockPos pos) {
        super(JDTEMenus.GREENHOUSE.get(), id, inventory, pos);
        if (baseMachineBE instanceof GreenhouseBE greenhouse) addDataSlots(greenhouse.getGreenhouseData());
        addPlayerSlots(player.getInventory());
    }

    @Override
    public void addMachineSlots() {
        machineHandler = baseMachineBE.getMachineHandler();
        var layout = GuiUpgradeLayoutConfig.getInstance();
        for (int i = 0; i < GreenhouseBE.INPUT_SLOTS; i++) {
            addSlot(new SeedSlot(machineHandler, i, layout.getLootFabricatorInputStartX(),
                    layout.getLootFabricatorInputStartY() + i * layout.getLootFabricatorInputSpacing()));
        }
        int outputSlots = getOutputSlotsPerPage();
        for (int i = 0; i < outputSlots; i++) {
            addSlot(new OutputSlot(machineHandler, i,
                    layout.getLootFabricatorOutputStartX()
                            + (i % layout.getLootFabricatorOutputColumns()) * layout.getLootFabricatorOutputSpacing(),
                    layout.getLootFabricatorOutputStartY()
                            + (i / layout.getLootFabricatorOutputColumns()) * layout.getLootFabricatorOutputSpacing(), this));
        }
    }

    public int getProgress() { return getMachineData(0, 0); }
    public int getProgressMax() { return getMachineData(1, 1); }
    public int getActiveOutputSlots() { return getMachineData(2, GreenhouseBE.BASE_OUTPUT_SLOTS); }
    public int getFluidAmount() { return getMachineData(3, 0); }
    public int getFluidCapacity() { return getMachineData(4, 1); }
    public int getMultiplier() { return getMachineData(5, 0); }
    public int getMaxMultiplier() { return getMachineData(6, 32); }
    public GreenhouseBE getGreenhouse() { return (GreenhouseBE) baseMachineBE; }
    private int getMachineData(int index, int fallback) {
        return baseMachineBE instanceof GreenhouseBE greenhouse ? greenhouse.getGreenhouseData().get(index) : fallback;
    }

    public int getOutputPage() { return outputPage; }
    public int getOutputSlotsPerPage() {
        var layout = GuiUpgradeLayoutConfig.getInstance();
        return layout.getLootFabricatorOutputColumns() * layout.getLootFabricatorOutputRows();
    }
    public int getMaxOutputPage() { return Math.max(0, (getActiveOutputSlots() - 1) / getOutputSlotsPerPage()); }
    public void setOutputPage(int page) {
        int clamped = Math.clamp(page, 0, getMaxOutputPage());
        if (outputPage == clamped) return;
        outputPage = clamped;
        broadcastChanges();
    }
    @Override public int jdte$getFilterPage() { return outputPage; }
    @Override public void jdte$setFilterPage(int page) { setOutputPage(page); }

    public boolean isPlantTemplateSlot(Slot slot) {
        int menuIndex = slots.indexOf(slot);
        return menuIndex >= 0 && menuIndex < GreenhouseBE.INPUT_SLOTS;
    }

    public boolean isOutputSlot(Slot slot) {
        int menuIndex = slots.indexOf(slot);
        return menuIndex >= GreenhouseBE.INPUT_SLOTS
                && menuIndex < GreenhouseBE.INPUT_SLOTS + getOutputSlotsPerPage();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, JDTEBlocks.GREENHOUSE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int machineSlotCount = GreenhouseBE.INPUT_SLOTS + getOutputSlotsPerPage();
        int playerStart = machineSlotCount + GreenhouseBE.UPGRADE_SLOTS;
        if (index < playerStart) {
            if (!moveStackTo(stack, playerStart, slots.size(), true)) return ItemStack.EMPTY;
        } else if (GreenhouseCropResolver.find(player.level(), stack) != null) {
            if (!moveStackTo(stack, 0, GreenhouseBE.INPUT_SLOTS, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof UpgradeCardItem) {
            if (!moveStackTo(stack, machineSlotCount, playerStart, false)) return ItemStack.EMPTY;
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

    private static final class SeedSlot extends SlotItemHandler {
        private SeedSlot(IItemHandler handler, int slot, int x, int y) { super(handler, slot, x, y); }
    }

    private static final class OutputSlot extends SlotItemHandler {
        private final GreenhouseContainer container;
        private final int pageSlot;

        private OutputSlot(IItemHandler handler, int pageSlot, int x, int y, GreenhouseContainer container) {
            super(handler, GreenhouseBE.OUTPUT_START_SLOT + pageSlot, x, y);
            this.container = container;
            this.pageSlot = pageSlot;
        }

        @Override public int getSlotIndex() {
            return GreenhouseBE.OUTPUT_START_SLOT + container.getOutputPage() * container.getOutputSlotsPerPage() + pageSlot;
        }
        @Override public ItemStack getItem() { return isActiveSlot() ? getItemHandler().getStackInSlot(getSlotIndex()) : ItemStack.EMPTY; }
        @Override public boolean hasItem() { return !getItem().isEmpty(); }
        @Override public void set(ItemStack stack) {
            if (!isActiveSlot()) return;
            ((IItemHandlerModifiable) getItemHandler()).setStackInSlot(getSlotIndex(), stack);
            setChanged();
        }
        @Override public void initialize(ItemStack stack) { set(stack); }
        @Override public ItemStack remove(int amount) {
            return isActiveSlot() ? getItemHandler().extractItem(getSlotIndex(), amount, false) : ItemStack.EMPTY;
        }
        @Override public int getMaxStackSize() { return isActiveSlot() ? getItemHandler().getSlotLimit(getSlotIndex()) : 0; }
        @Override public int getMaxStackSize(ItemStack stack) {
            return isActiveSlot() ? Math.min(stack.getMaxStackSize(), getItemHandler().getSlotLimit(getSlotIndex())) : 0;
        }
        @Override public boolean mayPickup(Player player) {
            return isActiveSlot() && !getItemHandler().extractItem(getSlotIndex(), 1, true).isEmpty();
        }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        private boolean isActiveSlot() {
            return getSlotIndex() < GreenhouseBE.OUTPUT_START_SLOT + container.getActiveOutputSlots()
                    && getSlotIndex() < getItemHandler().getSlots();
        }
    }
}
