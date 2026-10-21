package com.jdte.common.containers;

import com.jdte.common.items.CraftingPatternData;
import com.jdte.common.items.CraftingPatternItem;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

public final class CraftingPatternContainer extends AbstractContainerMenu {
    private final Player owner;
    private final InteractionHand hand;
    private final ItemStack boundStack;
    private final SimpleContainer grid = new SimpleContainer(9);
    private final SimpleContainer result = new SimpleContainer(1);
    private Optional<CraftingPatternData> candidate = Optional.empty();

    public CraftingPatternContainer(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(id, inventory, data.readEnum(InteractionHand.class));
    }

    public CraftingPatternContainer(int id, Inventory inventory, InteractionHand hand) {
        super(JDTEMenus.CRAFTING_PATTERN.get(), id);
        this.owner = inventory.player;
        this.hand = hand;
        this.boundStack = owner.getItemInHand(hand);
        for (int slot = 0; slot < 9; slot++) addSlot(new GhostSlot(grid, slot, 30 + slot % 3 * 18, 20 + slot / 3 * 18));
        addSlot(new GhostSlot(result, 0, 124, 38));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 114 + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 172));
        CraftingPatternData stored = boundStack.get(JDTEDataComponents.CRAFTING_PATTERN);
        if (stored != null) for (int slot = 0; slot < 9; slot++) grid.setItem(slot, stored.ingredients().get(slot).copy());
        updateResult();
    }

    private void updateResult() {
        if (owner.level().isClientSide()) return;
        var ingredients = new ArrayList<ItemStack>(9);
        for (int slot = 0; slot < 9; slot++) ingredients.add(grid.getItem(slot).copy());
        candidate = CraftingPatternData.capture(owner.level(), ingredients);
        result.setItem(0, candidate.map(pattern -> pattern.result().copy()).orElse(ItemStack.EMPTY));
        broadcastChanges();
    }

    public boolean hasValidRecipe() { return !result.getItem(0).isEmpty(); }

    @Override
    public boolean stillValid(Player player) {
        return player == owner && boundStack.getItem() instanceof CraftingPatternItem
                && (player.level().isClientSide() ? player.getItemInHand(hand).getItem() == boundStack.getItem()
                : player.getItemInHand(hand) == boundStack);
    }

    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (!stillValid(player)) return;
        if (slotId >= 0 && slotId < 10) {
            if (slotId < 9 && (type == ClickType.PICKUP || type == ClickType.QUICK_MOVE)) {
                ItemStack cursor = type == ClickType.QUICK_MOVE ? ItemStack.EMPTY : getCarried();
                grid.setItem(slotId, cursor.isEmpty() ? ItemStack.EMPTY : cursor.copyWithCount(1));
                updateResult();
            }
            return;
        }
        if (slotId >= 0 && slotId < slots.size() && slots.get(slotId).getItem() == boundStack) return;
        if (type == ClickType.SWAP && (button == 40 || hand == InteractionHand.MAIN_HAND
                && button == player.getInventory().selected)) return;
        super.clicked(slotId, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 10 || index >= slots.size()) return ItemStack.EMPTY;
        ItemStack stack = slots.get(index).getItem();
        if (stack.isEmpty() || stack == boundStack) return ItemStack.EMPTY;
        for (int slot = 0; slot < 9; slot++) if (grid.getItem(slot).isEmpty()) {
            grid.setItem(slot, stack.copyWithCount(1));
            updateResult();
            break;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return slot.container != grid && slot.container != result && slot.getItem() != boundStack;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != grid && slot.container != result && slot.getItem() != boundStack;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!stillValid(player) || player.level().isClientSide()) return false;
        if (button == 0) {
            updateResult();
            if (candidate.isEmpty()) return false;
            boundStack.set(JDTEDataComponents.CRAFTING_PATTERN, candidate.get());
            player.displayClientMessage(Component.translatable("jdte.message.crafting_pattern.saved",
                    candidate.get().result().getHoverName(), candidate.get().result().getCount()), true);
        } else if (button == 1) {
            grid.clearContent();
            boundStack.remove(JDTEDataComponents.CRAFTING_PATTERN);
            updateResult();
            player.displayClientMessage(Component.translatable("jdte.message.crafting_pattern.cleared"), true);
        } else return false;
        player.getInventory().setChanged();
        broadcastChanges();
        return true;
    }

    private static final class GhostSlot extends Slot {
        private GhostSlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
