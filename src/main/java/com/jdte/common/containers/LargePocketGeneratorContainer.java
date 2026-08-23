package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseContainer;
import com.direwolf20.justdirethings.common.containers.slots.FuelSlot;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.common.items.LargePocketGeneratorItem;
import com.jdte.setup.JDTEMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class LargePocketGeneratorContainer extends BaseContainer {
    public final ComponentItemHandler handler;
    public final ItemStack pocketGeneratorItemStack;
    public final Player playerEntity;
    private final LargePortableContainerBinding binding;

    public LargePocketGeneratorContainer(int windowId, Inventory inventory, FriendlyByteBuf extraData) {
        this(windowId, inventory, inventory.player,
                LargePortableContainerMenus.decodeOpenData((RegistryFriendlyByteBuf) extraData));
    }

    public LargePocketGeneratorContainer(int windowId, Inventory inventory, Player player, ItemStack stack) {
        this(windowId, inventory, player, stack,
                new LargePortableContainerBinding(stack, () -> stack,
                        itemStack -> !itemStack.isEmpty() && itemStack.getItem() instanceof LargePocketGeneratorItem));
    }

    private LargePocketGeneratorContainer(int windowId, Inventory inventory, Player player,
                                          LargePortableContainerMenus.DecodedOpenData decodedOpenData) {
        this(windowId, inventory, player, decodedOpenData.stack(),
                LargePortableContainerMenus.createClientBinding(
                        player,
                        decodedOpenData.stack(),
                        com.jdte.common.network.data.OpenLargePortableContainerPayload.ContainerKind.LARGE_POCKET_GENERATOR,
                        decodedOpenData.source()));
    }

    public LargePocketGeneratorContainer(int windowId, Inventory inventory, Player player, ItemStack stack,
                                         LargePortableContainerBinding binding) {
        super(JDTEMenus.LARGE_POCKET_GENERATOR.get(), windowId);
        this.playerEntity = player;
        this.pocketGeneratorItemStack = stack;
        this.binding = binding;
        this.handler = new ComponentItemHandler(stack, JustDireDataComponents.ITEMSTACK_HANDLER.get(), 1);
        addGeneratorSlots(handler, 0, 80, 35, 1, 18);
        addPlayerSlots(inventory, 8, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return binding.isStillValid();
    }

    protected int addGeneratorSlots(IItemHandler itemHandler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new FuelSlot(itemHandler, index++, x, y));
            x += dx;
        }
        return index;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        net.minecraft.world.inventory.Slot slot = slots.get(index);
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
}
