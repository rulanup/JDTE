package com.jdte.common.items;

import com.jdte.common.containers.AdvancedUpgradeStorageContainer;
import com.jdte.common.integrations.curios.AdvancedUpgradeStorageCuriosSources;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEMenus;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.List;

public class AdvancedUpgradeStorageItem extends UpgradeStorageItem {
    public AdvancedUpgradeStorageItem() {
        super();
    }

    public static ItemStack findStorage(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof AdvancedUpgradeStorageItem) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof AdvancedUpgradeStorageItem) {
            return off;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof AdvancedUpgradeStorageItem) {
                return stack;
            }
        }
        return AdvancedUpgradeStorageCuriosSources.find(player, stack -> stack.getItem() instanceof AdvancedUpgradeStorageItem);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (windowId, inv, p) -> new AdvancedUpgradeStorageContainer(windowId, inv, p, stack, hand),
                            Component.translatable("jdte.screen.advanced_upgrade_storage")
                    ),
                    buf -> buf.writeEnum(hand)
            );
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ItemContainerContents contents = stack.getOrDefault(
                JDTEDataComponents.UPGRADE_STORAGE_CONTENTS.get(), ItemContainerContents.EMPTY);

        int filledSlots = 0;
        int totalItems = 0;
        for (ItemStack item : contents.nonEmptyItems()) {
            filledSlots++;
            totalItems += item.getCount();
        }

        if (filledSlots == 0) {
            tooltip.add(Component.translatable("tooltip.jdte.advanced_upgrade_storage.empty")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.jdte.advanced_upgrade_storage.count", filledSlots, AdvancedUpgradeStorageContainer.SLOTS)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.translatable("tooltip.jdte.advanced_upgrade_storage.total", totalItems)
                    .withStyle(ChatFormatting.AQUA));

            if (Screen.hasShiftDown()) {
                for (ItemStack item : contents.nonEmptyItems()) {
                    tooltip.add(Component.literal(" • ")
                            .withStyle(ChatFormatting.DARK_GRAY)
                            .append(item.getHoverName().copy().withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(" x" + item.getCount()).withStyle(ChatFormatting.YELLOW)));
                }
            } else {
                tooltip.add(Component.translatable("tooltip.jdte.upgrade_storage.shift_hint")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        tooltip.add(Component.translatable("tooltip.jdte.advanced_upgrade_storage.open")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.jdte.advanced_upgrade_storage.machine_panel_hint")
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
