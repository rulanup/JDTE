package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseContainer;
import com.jdte.common.containers.handlers.LargeFuelCanisterHandler;
import com.jdte.common.items.LargeFuelCanisterItem;
import com.jdte.setup.JDTEMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LargeFuelCanisterContainer extends BaseContainer {
    public final LargeFuelCanisterHandler handler;
    public final ItemStack fuelCanisterItemstack;
    public final Player playerEntity;
    private final LargePortableContainerBinding binding;

    public LargeFuelCanisterContainer(int windowId, Inventory inventory, FriendlyByteBuf extraData) {
        this(windowId, inventory, inventory.player,
                ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) extraData));
    }

    public LargeFuelCanisterContainer(int windowId, Inventory inventory, Player player, ItemStack stack) {
        this(windowId, inventory, player, stack,
                new LargePortableContainerBinding(stack, () -> stack,
                        itemStack -> !itemStack.isEmpty() && itemStack.getItem() instanceof LargeFuelCanisterItem));
    }

    public LargeFuelCanisterContainer(int windowId, Inventory inventory, Player player, ItemStack stack,
                                      LargePortableContainerBinding binding) {
        super(JDTEMenus.LARGE_FUEL_CANISTER.get(), windowId);
        this.playerEntity = player;
        this.fuelCanisterItemstack = stack;
        this.binding = binding;
        this.handler = new LargeFuelCanisterHandler(1, stack);
        addSlotRange(handler, 0, 80, 35, 1, 18);
        addPlayerSlots(inventory, 8, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return binding.isStillValid();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copied = stack.copy();
            if (index < 1) {
                if (!moveItemStackTo(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == copied.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return copied;
    }

    @Override
    public void removed(Player player) {
        if (!player.level().isClientSide) {
            ItemStack remaining = handler.getStackInSlot(0);
            if (!remaining.isEmpty()) {
                handler.setStackInSlot(0, ItemStack.EMPTY);
                ItemHandlerHelper.giveItemToPlayer(player, remaining);
            }
        }
        super.removed(player);
    }

    public ItemStack getBoundStack() {
        return binding.boundStack();
    }
}
