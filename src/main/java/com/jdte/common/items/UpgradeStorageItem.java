package com.jdte.common.items;

import net.minecraft.client.Minecraft;
import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import com.direwolf20.justdirethings.common.items.abilityupgrades.Upgrade;
import com.jdte.common.containers.UpgradeStorageContainer;
import com.jdte.setup.JDTEDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.List;

public class UpgradeStorageItem extends Item {
    public static final TagKey<Item> UPGRADES_TAG = TagKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath("jdte", "upgrades"));

    public UpgradeStorageItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean isAllowedUpgrade(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof UpgradeStorageItem) {
            return false;
        }
        if (item instanceof UpgradeCardItem) {
            return true;
        }
        if (item instanceof LootingUpgradeItem) {
            return true;
        }
        if (item instanceof SharpnessUpgradeItem) {
            return true;
        }
        if (item instanceof EnergyBrewingUpgradeItem) {
            return true;
        }
        if (item instanceof EnergyOverloadUpgradeItem) {
            return true;
        }
        if (item instanceof MixingUpgradeItem || item instanceof UltimateMixingParallelUpgradeItem) {
            return true;
        }
        if (item instanceof AEExtractionUpgradeItem) {
            return true;
        }
        if (item instanceof EntityAccelerationUpgradeItem) {
            return true;
        }
        if (item instanceof ExtendedUpgradeItem) {
            return true;
        }
        if (item instanceof Upgrade) {
            return true;
        }
        return stack.is(UPGRADES_TAG);
    }

    public static ItemStack findStorage(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof UpgradeStorageItem && !(main.getItem() instanceof AdvancedUpgradeStorageItem)) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof UpgradeStorageItem && !(off.getItem() instanceof AdvancedUpgradeStorageItem)) {
            return off;
        }
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && stack.getItem() instanceof UpgradeStorageItem && !(stack.getItem() instanceof AdvancedUpgradeStorageItem)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
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
                            (windowId, inv, p) -> new UpgradeStorageContainer(windowId, inv, p, stack, hand),
                            Component.translatable("jdte.screen.upgrade_storage")
                    ),
                    buf -> buf.writeEnum(hand)
            );
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        ItemContainerContents contents = stack.getOrDefault(
                JDTEDataComponents.UPGRADE_STORAGE_CONTENTS.get(), ItemContainerContents.EMPTY);

        int filledSlots = 0;
        int totalItems = 0;
        for (var item : contents.nonEmptyItems()) {
            filledSlots++;
            totalItems += item.count();
        }

        if (filledSlots == 0) {
            tooltip.accept(Component.translatable("tooltip.jdte.upgrade_storage.empty")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("tooltip.jdte.upgrade_storage.count", filledSlots, UpgradeStorageContainer.SLOTS)
                    .withStyle(ChatFormatting.AQUA));
            tooltip.accept(Component.translatable("tooltip.jdte.upgrade_storage.total", totalItems)
                    .withStyle(ChatFormatting.DARK_AQUA));

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

        tooltip.accept(Component.translatable("tooltip.jdte.upgrade_storage.open")
                .withStyle(ChatFormatting.GOLD));
    }
}
