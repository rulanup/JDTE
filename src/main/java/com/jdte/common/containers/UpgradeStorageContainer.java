package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseContainer;
import com.jdte.common.containers.handlers.UpgradeStorageHandler;
import com.jdte.common.items.UpgradeStorageItem;
import com.jdte.setup.JDTEMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class UpgradeStorageContainer extends BaseContainer {
    public static final int ROWS = 4;
    public static final int COLUMNS = 5;
    public static final int SLOTS = ROWS * COLUMNS; // 20

    public final UpgradeStorageHandler handler;
    public final ItemStack upgradeStorageItemStack;
    public final Player playerEntity;
    public final InteractionHand hand;

    public UpgradeStorageContainer(int windowId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(windowId, inventory, inventory.player, extraData.readEnum(InteractionHand.class));
    }

    private UpgradeStorageContainer(int windowId, Inventory inventory, Player player, InteractionHand hand) {
        this(windowId, inventory, player, player.getItemInHand(hand), hand);
    }

    public UpgradeStorageContainer(int windowId, Inventory inventory, Player player, ItemStack stack, InteractionHand hand) {
        super(JDTEMenus.UPGRADE_STORAGE.get(), windowId);
        this.playerEntity = player;
        this.upgradeStorageItemStack = stack;
        this.hand = hand;
        this.handler = new UpgradeStorageHandler(stack);

        int slotIndex = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                addSlot(new UpgradeStorageSlot(handler, slotIndex++, 44 + c * 18, 18 + r * 18));
            }
        }

        addPlayerSlots(inventory, 8, 102);
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack current = player.getItemInHand(hand);
        return !current.isEmpty() && current.getItem() instanceof UpgradeStorageItem;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (hand == InteractionHand.MAIN_HAND) {
            int heldSlotIndex = SLOTS + 27 + player.getInventory().selected;
            if (slotId == heldSlotIndex) {
                return;
            }
            if (clickType == ClickType.SWAP && button == player.getInventory().selected) {
                return;
            }
        }
        if (slotId >= 0 && slotId < slots.size()) {
            Slot slot = slots.get(slotId);
            if (slot.getItem() == upgradeStorageItemStack) {
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            copied = slotStack.copy();

            if (index < SLOTS) {
                // Storage -> Player Inventory: move 1-by-1 since player slots limit is 1
                while (!slotStack.isEmpty()) {
                    ItemStack single = slotStack.split(1);
                    if (!moveItemStackTo(single, SLOTS, slots.size(), true)) {
                        slotStack.grow(1);
                        break;
                    }
                }
            } else {
                // Player Inventory -> Storage
                if (!UpgradeStorageItem.isAllowedUpgrade(slotStack)) {
                    return ItemStack.EMPTY;
                }
                if (!moveItemStackToStorage(slotStack)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == copied.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
        }
        return copied;
    }

    private boolean moveItemStackToStorage(ItemStack stack) {
        if (!UpgradeStorageItem.isAllowedUpgrade(stack)) {
            return false;
        }
        boolean moved = false;
        // Step 1: Merge into existing matching slots
        for (int i = 0; i < SLOTS; i++) {
            Slot storageSlot = slots.get(i);
            ItemStack existing = storageSlot.getItem();
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(stack, existing)) {
                int max = storageSlot.getMaxStackSize(existing);
                int space = max - existing.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, stack.getCount());
                    existing.grow(toAdd);
                    stack.shrink(toAdd);
                    storageSlot.setChanged();
                    moved = true;
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        // Step 2: Insert into first empty slot
        if (!stack.isEmpty()) {
            for (int i = 0; i < SLOTS; i++) {
                Slot storageSlot = slots.get(i);
                ItemStack existing = storageSlot.getItem();
                if (existing.isEmpty() && storageSlot.mayPlace(stack)) {
                    int max = storageSlot.getMaxStackSize(stack);
                    int toAdd = Math.min(max, stack.getCount());
                    storageSlot.setByPlayer(stack.split(toAdd));
                    storageSlot.setChanged();
                    moved = true;
                    if (stack.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return moved;
    }

    public static class UpgradeStorageSlot extends SlotItemHandler {
        public UpgradeStorageSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return UpgradeStorageItem.isAllowedUpgrade(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) {
            return 64;
        }
    }
}
