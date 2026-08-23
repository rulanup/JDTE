package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseContainer;
import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.common.containers.handlers.LargePotionCanisterHandler;
import com.jdte.common.items.LargePotionCanisterItem;
import com.jdte.setup.JDTEMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LargePotionCanisterContainer extends BaseContainer {
    public final LargePotionCanisterHandler handler;
    public final ItemStack potionCanister;
    public final Player playerEntity;
    private final LargePortableContainerBinding binding;

    public LargePotionCanisterContainer(int windowId, Inventory inventory, FriendlyByteBuf extraData) {
        this(windowId, inventory, inventory.player,
                LargePortableContainerMenus.decodeOpenData((RegistryFriendlyByteBuf) extraData));
    }

    public LargePotionCanisterContainer(int windowId, Inventory inventory, Player player, ItemStack stack) {
        this(windowId, inventory, player, stack,
                new LargePortableContainerBinding(stack, () -> stack,
                        itemStack -> !itemStack.isEmpty() && itemStack.getItem() instanceof LargePotionCanisterItem));
    }

    private LargePotionCanisterContainer(int windowId, Inventory inventory, Player player,
                                         LargePortableContainerMenus.DecodedOpenData decodedOpenData) {
        this(windowId, inventory, player, decodedOpenData.stack(),
                LargePortableContainerMenus.createClientBinding(
                        player,
                        decodedOpenData.stack(),
                        com.jdte.common.network.data.OpenLargePortableContainerPayload.ContainerKind.LARGE_POTION_CANISTER,
                        decodedOpenData.source()));
    }

    public LargePotionCanisterContainer(int windowId, Inventory inventory, Player player, ItemStack stack,
                                        LargePortableContainerBinding binding) {
        super(JDTEMenus.LARGE_POTION_CANISTER.get(), windowId);
        this.playerEntity = player;
        this.potionCanister = stack;
        this.binding = binding;
        this.handler = new LargePotionCanisterHandler(stack, JustDireDataComponents.TOOL_CONTENTS.get(), 1);
        addItemSlots(handler, 0, 80, 35, 1, 18);
        addPlayerSlots(inventory, 8, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return binding.isStillValid();
    }

    protected int addItemSlots(IItemHandler itemHandler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new PotionBatchSlot(itemHandler, i, x, y));
            x += dx;
            index++;
        }
        return index;
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
                slot.set(stack);
                slot.setChanged();
            }

            if (stack.getCount() == copied.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return copied;
    }

    public ItemStack getBoundStack() {
        return binding.boundStack();
    }

    public ItemStack getCurrentStack() {
        return binding.currentStack();
    }

    private static final class PotionBatchSlot extends SlotItemHandler {
        private PotionBatchSlot(IItemHandler itemHandler, int index, int x, int y) {
            super(itemHandler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof PotionItem || stack.is(Items.GLASS_BOTTLE);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            if (stack.getItem() instanceof PotionItem) {
                return 4;
            }
            return super.getMaxStackSize(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 4;
        }
    }
}
