package com.jdte.common.items;

import net.minecraft.client.Minecraft;
import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import com.jdte.common.containers.AdvancedUpgradeStorageContainer;
import com.jdte.common.integrations.curios.AdvancedUpgradeStorageCuriosSources;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEMenus;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.List;

public class AdvancedUpgradeStorageItem extends UpgradeStorageItem {
    public AdvancedUpgradeStorageItem(Properties properties) {
        super(properties);
    }

    public static List<ItemStack> findAllStorages(Player player) {
        if (player == null) {
            return List.of();
        }
        List<ItemStack> list = new java.util.ArrayList<>();
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof AdvancedUpgradeStorageItem) {
            list.add(main);
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof AdvancedUpgradeStorageItem && !containsIdentity(list, off)) {
            list.add(off);
        }
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && stack.getItem() instanceof AdvancedUpgradeStorageItem && !containsIdentity(list, stack)) {
                list.add(stack);
            }
        }
        List<ItemStack> curiosList = AdvancedUpgradeStorageCuriosSources.findAll(player, stack -> stack.getItem() instanceof AdvancedUpgradeStorageItem);
        for (ItemStack stack : curiosList) {
            if (!stack.isEmpty() && !containsIdentity(list, stack)) {
                list.add(stack);
            }
        }
        return list;
    }

    private static boolean containsIdentity(List<ItemStack> list, ItemStack target) {
        for (ItemStack s : list) {
            if (s == target) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack findStorage(Player player, int index) {
        List<ItemStack> storages = findAllStorages(player);
        if (storages.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int clamped = Math.max(0, Math.min(index, storages.size() - 1));
        return storages.get(clamped);
    }

    public static ItemStack findStorage(Player player) {
        return findStorage(player, 0);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
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
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        ItemContainerContents contents = stack.getOrDefault(
                JDTEDataComponents.UPGRADE_STORAGE_CONTENTS.get(), ItemContainerContents.EMPTY);

        int filledSlots = 0;
        int totalItems = 0;
        for (var item : contents.nonEmptyItems()) {
            filledSlots++;
            totalItems += item.count();
        }

        if (filledSlots == 0) {
            tooltip.accept(Component.translatable("tooltip.jdte.advanced_upgrade_storage.empty")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("tooltip.jdte.advanced_upgrade_storage.count", filledSlots, AdvancedUpgradeStorageContainer.SLOTS)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.accept(Component.translatable("tooltip.jdte.advanced_upgrade_storage.total", totalItems)
                    .withStyle(ChatFormatting.AQUA));

            if (Minecraft.getInstance().hasShiftDown()) {
                for (var item : contents.nonEmptyItems()) {
                    tooltip.accept(Component.literal(" • ")
                            .withStyle(ChatFormatting.DARK_GRAY)
                            .append(com.jdte.common.containers.handlers.BatchComponentItemHandler.stack(item).getHoverName().copy().withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(" x" + item.count()).withStyle(ChatFormatting.YELLOW)));
                }
            } else {
                tooltip.accept(Component.translatable("tooltip.jdte.upgrade_storage.shift_hint")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        tooltip.accept(Component.translatable("tooltip.jdte.advanced_upgrade_storage.open")
                .withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("tooltip.jdte.advanced_upgrade_storage.machine_panel_hint")
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
