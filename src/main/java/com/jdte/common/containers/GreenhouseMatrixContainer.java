package com.jdte.common.containers;

import com.jdte.common.blockentities.GreenhouseMatrixControllerBE;
import com.jdte.common.blockentities.GreenhouseMatrixPatternItemHandler;
import com.jdte.common.greenhouse.GreenhouseMatrixPatternSupport;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTEMenus;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GreenhouseMatrixContainer extends AbstractContainerMenu {
    private static final int CONTROLLER_START = 0;
    private static final int GLOBAL_START = GreenhouseMatrixControllerBE.CONTROLLER_UPGRADE_SLOTS;
    private static final int PATTERN_START = GLOBAL_START + GreenhouseMatrixControllerBE.GLOBAL_UPGRADE_SLOTS;
    private static final int PATTERN_SLOTS = GreenhouseMatrixPatternItemHandler.SLOTS;
    private static final int MACHINE_SLOTS = PATTERN_START + PATTERN_SLOTS;
    private final BlockPos pos;
    private final ContainerData data;
    private final IItemHandler controllerUpgrades;
    private final IItemHandler globalUpgrades;
    private final GreenhouseMatrixControllerBE controller;
    private final boolean clientSide;
    private final PagedPatternHandler patternHandler;
    private int autoCraftingPage;
    private int syncedAutoCraftingPageCount;
    private int syncedInvalidMask;

    public GreenhouseMatrixContainer(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), null);
    }

    public GreenhouseMatrixContainer(int id, Inventory inventory, BlockPos pos, GreenhouseMatrixControllerBE controller) {
        super(JDTEMenus.GREENHOUSE_MATRIX.get(), id);
        this.pos = pos;
        GreenhouseMatrixControllerBE resolved = controller;
        if (resolved == null && inventory.player.level().getBlockEntity(pos) instanceof GreenhouseMatrixControllerBE found) {
            resolved = found;
        }
        this.controller = resolved;
        this.clientSide = inventory.player.level().isClientSide();
        this.data = resolved == null ? new SimpleContainerData(18) : resolved.getMatrixData();
        this.controllerUpgrades = resolved == null
                ? new ItemStackHandler(GreenhouseMatrixControllerBE.CONTROLLER_UPGRADE_SLOTS)
                : resolved.getControllerUpgradeHandler();
        this.globalUpgrades = resolved == null
                ? new ItemStackHandler(GreenhouseMatrixControllerBE.GLOBAL_UPGRADE_SLOTS)
                : resolved.getGlobalUpgradeHandler();
        this.patternHandler = new PagedPatternHandler();
        addDataSlots(data);
        addPatternPageData();
        addUpgradeSlots();
        addPatternSlots();
        addPlayerSlots(inventory);
    }

    private void addPatternPageData() {
        addDataSlot(new DataSlot() {
            @Override public int get() { return autoCraftingPage; }
            @Override public void set(int value) { setAutoCraftingPageFromSync(value); }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() {
                return !clientSide && controller != null ? controller.getAutoCraftingPageCount() : syncedAutoCraftingPageCount;
            }
            @Override public void set(int value) { syncedAutoCraftingPageCount = Math.max(0, value); }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() {
                return !clientSide && controller != null
                        ? controller.getAutoCraftingInvalidMask(autoCraftingPage) : syncedInvalidMask;
            }
            @Override public void set(int value) { syncedInvalidMask = value & 0xFFFF; }
        });
    }

    private void addUpgradeSlots() {
        for (int slot = 0; slot < GreenhouseMatrixControllerBE.CONTROLLER_UPGRADE_SLOTS; slot++) {
            addSlot(new ControllerUpgradeSlot(controllerUpgrades, slot,
                    258 + (slot % 2) * 18, 143 + (slot / 2) * 18, this));
        }
        for (int slot = 0; slot < GreenhouseMatrixControllerBE.GLOBAL_UPGRADE_SLOTS; slot++) {
            addSlot(new GlobalUpgradeSlot(globalUpgrades, slot,
                    182 + (slot % 2) * 18, 143 + (slot / 2) * 18, this));
        }
    }

    private void addPatternSlots() {
        for (int slot = 0; slot < PATTERN_SLOTS; slot++) {
            addSlot(new SlotItemHandler(patternHandler, slot,
                    199 + (slot % 4) * 18, 33 + (slot / 4) * 18));
        }
    }

    private void addPlayerSlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 142 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 200));
    }

    public BlockPos getPos() { return pos; }
    public boolean isFormed() { return data.get(0) != 0; }
    public boolean isEnabled() { return data.get(1) != 0; }
    public boolean isRenderEnabled() { return data.get(2) != 0; }
    public int getGreenhouseCount() { return data.get(3); }
    public int getSpeedCount() { return data.get(4); }
    public int getEfficiencyCount() { return data.get(5); }
    public int getSeedCount() { return data.get(6); }
    public int getEssenceCount() { return data.get(7); }
    public int getSizeX() { return data.get(8); }
    public int getSizeY() { return data.get(9); }
    public int getSizeZ() { return data.get(10); }
    public int getErrorCode() { return data.get(11); }
    public boolean isAutoIoEnabled() { return data.get(12) != 0; }
    public int getProductionGroupCount() { return data.get(13); }
    public boolean isSimulationRebuilding() { return data.get(14) != 0; }
    public long getBufferedItemCount() {
        return (long) data.get(16) << 32 | Integer.toUnsignedLong(data.get(15));
    }
    public int getBufferedTypeCount() { return data.get(17); }
    public int getAutoCraftingPage() { return autoCraftingPage; }
    public int getAutoCraftingPageCount() {
        int count = !clientSide && controller != null
                ? controller.getAutoCraftingPageCount() : syncedAutoCraftingPageCount;
        return Math.max(0, count);
    }
    public int getEffectiveAutoCraftingPageCount() { return Math.max(1, getAutoCraftingPageCount()); }
    public int getAutoCraftingInvalidMask() {
        return !clientSide && controller != null
                ? controller.getAutoCraftingInvalidMask(autoCraftingPage) : syncedInvalidMask;
    }
    public boolean isPatternSlot(Slot slot) {
        int index = slots.indexOf(slot);
        return index >= PATTERN_START && index < PATTERN_START + PATTERN_SLOTS;
    }
    public int getPatternPageSlot(Slot slot) {
        int index = slots.indexOf(slot);
        return isPatternSlot(slot) ? index - PATTERN_START : -1;
    }
    public void setAutoCraftingPage(int page) {
        int clamped = com.jdte.common.network.handler.GreenhouseMatrixPatternPageRequestValidator.clampPage(
                page, getAutoCraftingPageCount());
        if (autoCraftingPage == clamped) return;
        autoCraftingPage = clamped;
        syncedInvalidMask = 0;
        patternHandler.clearClientMirror();
        broadcastChanges();
    }
    private void setAutoCraftingPageFromSync(int page) {
        int normalized = Math.max(0, page);
        if (autoCraftingPage == normalized) return;
        autoCraftingPage = normalized;
        patternHandler.clearClientMirror();
    }

    @Override
    public void broadcastChanges() {
        if (!clientSide) {
            int clamped = com.jdte.common.network.handler.GreenhouseMatrixPatternPageRequestValidator.clampPage(
                    autoCraftingPage, getAutoCraftingPageCount());
            if (clamped != autoCraftingPage) autoCraftingPage = clamped;
        }
        super.broadcastChanges();
    }
    public boolean isQuickInstallEnabled() {
        for (int slot = 0; slot < controllerUpgrades.getSlots(); slot++) {
            if (controllerUpgrades.getStackInSlot(slot)
                    .is(JDTEItems.GREENHOUSE_MATRIX_QUICK_INSTALL_UPGRADE.get())) return true;
        }
        return false;
    }
    public int getQueuedUpgradeCount() {
        int count = 0;
        for (int slot = 0; slot < globalUpgrades.getSlots(); slot++) {
            count += globalUpgrades.getStackInSlot(slot).getCount();
        }
        return count;
    }
    private boolean isGlobalBufferEmpty() {
        for (int slot = 0; slot < globalUpgrades.getSlots(); slot++) {
            if (!globalUpgrades.getStackInSlot(slot).isEmpty()) return false;
        }
        return true;
    }

    @Override public boolean stillValid(Player player) {
        return player.level().getBlockState(pos).is(JDTEBlocks.GREENHOUSE_MATRIX_CONTROLLER.get())
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else if (stack.is(JDTEItems.GREENHOUSE_MATRIX_QUICK_INSTALL_UPGRADE.get())) {
            if (!moveItemStackTo(stack, CONTROLLER_START, GLOBAL_START, false)) {
                return ItemStack.EMPTY;
            }
        } else if (UpgradeHelper.isUpgrade(stack, UpgradeType.AE_OUTPUT)) {
            boolean moved = moveItemStackTo(stack, CONTROLLER_START, GLOBAL_START, false);
            if (!moved && isQuickInstallEnabled()) {
                moved = moveItemStackTo(stack, GLOBAL_START, PATTERN_START, false);
            }
            if (!moved) return ItemStack.EMPTY;
        } else if (GreenhouseMatrixPatternSupport.isEncodedPattern(stack)) {
            if (!moveItemStackTo(stack, PATTERN_START, MACHINE_SLOTS, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof UpgradeCardItem && isQuickInstallEnabled()) {
            if (!moveItemStackTo(stack, GLOBAL_START, PATTERN_START, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) source.set(ItemStack.EMPTY); else source.setChanged();
        source.onTake(player, stack);
        return original;
    }

    private static final class ControllerUpgradeSlot extends SlotItemHandler {
        private final GreenhouseMatrixContainer container;
        private ControllerUpgradeSlot(IItemHandler handler, int slot, int x, int y,
                                      GreenhouseMatrixContainer container) {
            super(handler, slot, x, y);
            this.container = container;
        }
        @Override public boolean mayPickup(Player player) {
            return !getItem().is(JDTEItems.GREENHOUSE_MATRIX_QUICK_INSTALL_UPGRADE.get())
                    || container.isGlobalBufferEmpty();
        }
    }

    private static final class GlobalUpgradeSlot extends SlotItemHandler {
        private final GreenhouseMatrixContainer container;
        private GlobalUpgradeSlot(IItemHandler handler, int slot, int x, int y,
                                  GreenhouseMatrixContainer container) {
            super(handler, slot, x, y);
            this.container = container;
        }
        @Override public boolean isActive() { return container.isQuickInstallEnabled(); }
        @Override public boolean mayPlace(ItemStack stack) {
            return isActive() && super.mayPlace(stack);
        }
        @Override public boolean mayPickup(Player player) { return isActive(); }
    }

    private final class PagedPatternHandler implements IItemHandlerModifiable {
        private final ItemStackHandler clientMirror = new ItemStackHandler(PATTERN_SLOTS);

        private IItemHandlerModifiable delegate() {
            if (clientSide || controller == null) return null;
            return controller.getAutoCraftingPatternHandler(autoCraftingPage);
        }

        private void clearClientMirror() {
            if (!clientSide) return;
            for (int slot = 0; slot < PATTERN_SLOTS; slot++) clientMirror.setStackInSlot(slot, ItemStack.EMPTY);
        }

        @Override public int getSlots() { return PATTERN_SLOTS; }
        @Override public ItemStack getStackInSlot(int slot) {
            IItemHandlerModifiable delegate = delegate();
            return delegate == null ? clientMirror.getStackInSlot(slot) : delegate.getStackInSlot(slot);
        }
        @Override public void setStackInSlot(int slot, ItemStack stack) {
            IItemHandlerModifiable delegate = delegate();
            if (delegate == null) {
                if (clientSide) clientMirror.setStackInSlot(slot, stack);
            } else delegate.setStackInSlot(slot, stack);
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            IItemHandlerModifiable delegate = delegate();
            return delegate == null ? stack : delegate.insertItem(slot, stack, simulate);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            IItemHandlerModifiable delegate = delegate();
            return delegate == null ? ItemStack.EMPTY : delegate.extractItem(slot, amount, simulate);
        }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            IItemHandlerModifiable delegate = delegate();
            return delegate == null ? clientSide && GreenhouseMatrixPatternSupport.isEncodedPattern(stack)
                    : delegate.isItemValid(slot, stack);
        }
    }
}
