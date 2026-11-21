package com.jdte.common.containers;

import com.direwolf20.justdirethings.common.containers.basecontainers.BaseContainer;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.common.items.LargePocketGeneratorItem;
import com.jdte.setup.JDTEMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.jdte.common.containers.handlers.BatchComponentItemHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class LargePocketGeneratorContainer extends BaseContainer {
    public final BatchComponentItemHandler handler;
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
        this.handler = new BatchComponentItemHandler(ItemAccess.forStack(stack), JustDireDataComponents.ITEMSTACK_HANDLER.get(), 1);
        addSlot(new ResourceHandlerSlot(handler, handler::set, 0, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack fuel) {
                return LargePocketGeneratorItem.resolveFuelBurnTime(fuel, player.level().fuelValues()) > 0;
            }
        });
        addPlayerSlots(inventory, 8, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return binding.isStillValid();
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
