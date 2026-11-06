package com.jdte.common.containers;
import com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler;
import com.jdte.setup.JDTEMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
public final class AdvancedUpgradeStorageContainer extends UpgradeStorageContainer {
    public static final int ROWS=6, COLUMNS=6, SLOTS=36;
    public AdvancedUpgradeStorageContainer(int id, Inventory inventory, FriendlyByteBuf buf) { this(id, inventory, inventory.player, buf.readEnum(InteractionHand.class)); }
    private AdvancedUpgradeStorageContainer(int id, Inventory inventory, Player player, InteractionHand hand) { this(id, inventory, player, player.getItemInHand(hand), hand); }
    public AdvancedUpgradeStorageContainer(int id, Inventory inventory, Player player, ItemStack stack, InteractionHand hand) {
        super(JDTEMenus.ADVANCED_UPGRADE_STORAGE.get(),id,inventory,player,stack,hand,ROWS,COLUMNS,35,140,new AdvancedUpgradeStorageHandler(stack));
    }
}
