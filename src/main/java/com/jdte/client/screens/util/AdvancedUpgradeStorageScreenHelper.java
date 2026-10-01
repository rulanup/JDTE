package com.jdte.client.screens.util;

import com.direwolf20.justdirethings.client.screens.basescreens.BaseMachineScreen;
import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.util.MiscTools;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.blockentities.BioCrusherBE;
import com.jdte.common.blockentities.BioFactoryBE;
import com.jdte.common.blockentities.LootFabricatorBE;
import com.jdte.common.containers.handlers.AdvancedUpgradeStorageHandler;
import com.jdte.common.items.AdvancedUpgradeStorageItem;
import com.jdte.common.items.EnergyBrewingUpgradeItem;
import com.jdte.common.items.EnergyOverloadUpgradeItem;
import com.jdte.common.items.LootingUpgradeItem;
import com.jdte.common.items.SharpnessUpgradeItem;
import com.jdte.common.items.UpgradeCardItem;
import com.jdte.common.items.UpgradeStorageItem;
import com.jdte.common.network.data.AdvancedUpgradeStorageActionPayload;
import com.jdte.common.upgrades.LootFabricatorUpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.utils.UpgradeSlotStorage;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class AdvancedUpgradeStorageScreenHelper {
    public static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    public static final int COLUMNS = 6;
    public static final int ROWS = 6;
    public static final int TOTAL_SLOTS = COLUMNS * ROWS; // 36
    public static final int SLOT_SIZE = 18;
    public static final int PANEL_PADDING = 6;
    public static final int HEADER_HEIGHT = 16;
    public static final int PANEL_WIDTH = PANEL_PADDING * 2 + COLUMNS * SLOT_SIZE; // 120
    public static final int PANEL_HEIGHT = HEADER_HEIGHT + ROWS * SLOT_SIZE + PANEL_PADDING; // 130
    public static final int TAB_SIZE = 22;

    private static boolean expanded = true;
    private static ItemStack cachedIconStack = ItemStack.EMPTY;

    private AdvancedUpgradeStorageScreenHelper() {
    }

    public static boolean isExpanded() {
        return expanded;
    }

    public static void setExpanded(boolean state) {
        expanded = state;
    }

    private static ItemStack getIconStack() {
        if (cachedIconStack.isEmpty()) {
            cachedIconStack = new ItemStack(JDTEItems.ADVANCED_UPGRADE_STORAGE.get());
        }
        return cachedIconStack;
    }

    public static Rect2i getPanelArea(BaseMachineScreen<?> screen) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        ItemStack storageStack = AdvancedUpgradeStorageItem.findStorage(player);
        if (storageStack.isEmpty()) {
            return null;
        }

        int upgradeSlots = UpgradeSlotStorage.getUpgradeSlots(screen.getMenu());
        int[] pos = calculatePosition(screen, upgradeSlots);
        int w = expanded ? PANEL_WIDTH : TAB_SIZE;
        int h = expanded ? PANEL_HEIGHT : TAB_SIZE;
        return new Rect2i(pos[0], pos[1], w, h);
    }

    private static int[] calculatePosition(BaseMachineScreen<?> screen, int upgradeSlots) {
        int rightPanelX = screen.getGuiLeft() + 210;
        int rightPanelY = screen.getGuiTop() + 16;
        int targetWidth = expanded ? PANEL_WIDTH : TAB_SIZE;

        if (rightPanelX + targetWidth + 4 <= screen.width) {
            return new int[]{rightPanelX, rightPanelY};
        }

        int leftBoundary = (upgradeSlots > 4) ? (screen.getGuiLeft() - 32) : screen.getGuiLeft();
        int leftPanelX = Math.max(2, leftBoundary - targetWidth - 4);
        return new int[]{leftPanelX, rightPanelY};
    }

    public static void renderBg(GuiGraphics guiGraphics, BaseMachineScreen<?> screen, ResourceLocation socialBackground,
                                int mouseX, int mouseY, float partialTicks) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack storageStack = AdvancedUpgradeStorageItem.findStorage(player);
        if (storageStack.isEmpty()) {
            return;
        }

        int upgradeSlots = UpgradeSlotStorage.getUpgradeSlots(screen.getMenu());
        int[] pos = calculatePosition(screen, upgradeSlots);
        int panelX = pos[0];
        int panelY = pos[1];
        Font font = Minecraft.getInstance().font;

        if (!expanded) {
            guiGraphics.blitSprite(socialBackground, panelX, panelY, TAB_SIZE, TAB_SIZE);
            guiGraphics.renderItem(getIconStack(), panelX + 3, panelY + 3);
            if (MiscTools.inBounds(panelX, panelY, TAB_SIZE, TAB_SIZE, mouseX, mouseY)) {
                guiGraphics.fill(panelX + 2, panelY + 2, panelX + TAB_SIZE - 2, panelY + TAB_SIZE - 2, 0x40FFFFFF);
            }
            return;
        }

        // Draw main panel background
        guiGraphics.blitSprite(socialBackground, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

        // Header icon, title, and collapse button
        guiGraphics.renderItem(getIconStack(), panelX + 3, panelY);
        guiGraphics.drawString(font, Component.translatable("jdte.screen.advanced_upgrade_storage.short"),
                panelX + 22, panelY + 4, 0xFFE080, false);

        int closeX = panelX + PANEL_WIDTH - 14;
        int closeY = panelY + 3;
        boolean closeHovered = MiscTools.inBounds(closeX, closeY, 10, 10, mouseX, mouseY);
        guiGraphics.fill(closeX, closeY, closeX + 10, closeY + 10, closeHovered ? 0x80555555 : 0x50333333);
        guiGraphics.drawString(font, "-", closeX + 3, closeY + 1, closeHovered ? 0xFFFFFF : 0xAAAAAA, false);

        // Slot grid and items
        AdvancedUpgradeStorageHandler storageHandler = new AdvancedUpgradeStorageHandler(storageStack);
        BaseMachineBE baseMachineBE = screen.getMenu().baseMachineBE;
        ItemStack carried = screen.getMenu().getCarried();
        boolean hasAllowedCarried = !carried.isEmpty() && UpgradeStorageItem.isAllowedUpgrade(carried);

        int startX = panelX + PANEL_PADDING;
        int startY = panelY + HEADER_HEIGHT;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                int slotIndex = r * COLUMNS + c;
                int slotX = startX + c * SLOT_SIZE;
                int slotY = startY + r * SLOT_SIZE;

                guiGraphics.blitSprite(SLOT_SPRITE, slotX, slotY, SLOT_SIZE, SLOT_SIZE);

                ItemStack stack = storageHandler.getStackInSlot(slotIndex);
                if (!stack.isEmpty()) {
                    guiGraphics.renderItem(stack, slotX + 1, slotY + 1);
                    guiGraphics.renderItemDecorations(font, stack, slotX + 1, slotY + 1);

                    if (canInsertIntoMachine(baseMachineBE, stack)) {
                        guiGraphics.fill(slotX + 13, slotY + 2, slotX + 16, slotY + 5, 0xFF55FF55);
                    }
                }

                if (MiscTools.inBounds(slotX, slotY, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) {
                    if (stack.isEmpty() && hasAllowedCarried) {
                        guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0x4000FF00);
                    } else if (!stack.isEmpty()) {
                        guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0x80FFFFFF);
                    }
                }
            }
        }
    }

    public static void renderTooltip(GuiGraphics guiGraphics, BaseMachineScreen<?> screen, Font font, int mouseX, int mouseY) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack storageStack = AdvancedUpgradeStorageItem.findStorage(player);
        if (storageStack.isEmpty()) {
            return;
        }

        int upgradeSlots = UpgradeSlotStorage.getUpgradeSlots(screen.getMenu());
        int[] pos = calculatePosition(screen, upgradeSlots);
        int panelX = pos[0];
        int panelY = pos[1];

        if (!expanded) {
            if (MiscTools.inBounds(panelX, panelY, TAB_SIZE, TAB_SIZE, mouseX, mouseY)) {
                guiGraphics.renderTooltip(font, Component.translatable("jdte.screen.advanced_upgrade_storage.expand"), mouseX, mouseY);
            }
            return;
        }

        int closeX = panelX + PANEL_WIDTH - 14;
        int closeY = panelY + 3;
        if (MiscTools.inBounds(closeX, closeY, 10, 10, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, Component.translatable("jdte.screen.advanced_upgrade_storage.collapse"), mouseX, mouseY);
            return;
        }

        AdvancedUpgradeStorageHandler storageHandler = new AdvancedUpgradeStorageHandler(storageStack);
        BaseMachineBE baseMachineBE = screen.getMenu().baseMachineBE;
        ItemStack carried = screen.getMenu().getCarried();
        boolean hasAllowedCarried = !carried.isEmpty() && UpgradeStorageItem.isAllowedUpgrade(carried);

        int startX = panelX + PANEL_PADDING;
        int startY = panelY + HEADER_HEIGHT;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                int slotIndex = r * COLUMNS + c;
                int slotX = startX + c * SLOT_SIZE;
                int slotY = startY + r * SLOT_SIZE;

                if (MiscTools.inBounds(slotX, slotY, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) {
                    ItemStack stack = storageHandler.getStackInSlot(slotIndex);
                    if (!stack.isEmpty()) {
                        List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), stack));
                        tooltip.add(Component.empty());

                        if (canInsertIntoMachine(baseMachineBE, stack)) {
                            int current = getUpgradeCount(baseMachineBE, stack);
                            int max = getUpgradeMax(baseMachineBE, stack);
                            if (max > 0) {
                                tooltip.add(Component.translatable("jdte.screen.advanced_upgrade_storage.compatible", current, max)
                                        .withStyle(ChatFormatting.GREEN));
                            } else {
                                tooltip.add(Component.translatable("jdte.screen.advanced_upgrade_storage.compatible_simple")
                                        .withStyle(ChatFormatting.GREEN));
                            }
                        } else if (isOppositeSpeedConflict(baseMachineBE, stack)) {
                            tooltip.add(Component.translatable("jdte.screen.advanced_upgrade_storage.conflict")
                                    .withStyle(ChatFormatting.RED));
                        } else if (isAtMaxLimit(baseMachineBE, stack)) {
                            int current = getUpgradeCount(baseMachineBE, stack);
                            int max = getUpgradeMax(baseMachineBE, stack);
                            tooltip.add(Component.translatable("jdte.screen.advanced_upgrade_storage.max_reached", current, max)
                                    .withStyle(ChatFormatting.RED));
                        } else {
                            tooltip.add(Component.translatable("jdte.screen.advanced_upgrade_storage.incompatible")
                                    .withStyle(ChatFormatting.DARK_GRAY));
                        }

                        tooltip.add(Component.translatable("jdte.screen.advanced_upgrade_storage.action.left_click")
                                .withStyle(ChatFormatting.YELLOW));
                        tooltip.add(Component.translatable("jdte.screen.advanced_upgrade_storage.action.shift_left_click")
                                .withStyle(ChatFormatting.YELLOW));
                        tooltip.add(Component.translatable("jdte.screen.advanced_upgrade_storage.action.right_click")
                                .withStyle(ChatFormatting.YELLOW));
                        tooltip.add(Component.translatable("jdte.screen.advanced_upgrade_storage.action.shift_right_click")
                                .withStyle(ChatFormatting.YELLOW));

                        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                        return;
                    } else if (hasAllowedCarried) {
                        List<Component> tooltip = List.of(
                                Component.translatable("jdte.screen.advanced_upgrade_storage.action.deposit_all")
                                        .withStyle(ChatFormatting.GREEN),
                                Component.translatable("jdte.screen.advanced_upgrade_storage.action.deposit_one")
                                        .withStyle(ChatFormatting.GREEN)
                        );
                        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                        return;
                    }
                }
            }
        }
    }

    public static boolean mouseClicked(BaseMachineScreen<?> screen, double mouseX, double mouseY, int button) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        ItemStack storageStack = AdvancedUpgradeStorageItem.findStorage(player);
        if (storageStack.isEmpty()) {
            return false;
        }

        int upgradeSlots = UpgradeSlotStorage.getUpgradeSlots(screen.getMenu());
        int[] pos = calculatePosition(screen, upgradeSlots);
        int panelX = pos[0];
        int panelY = pos[1];

        if (!expanded) {
            if (MiscTools.inBounds(panelX, panelY, TAB_SIZE, TAB_SIZE, mouseX, mouseY)) {
                expanded = true;
                playClickSound();
                return true;
            }
            return false;
        }

        int closeX = panelX + PANEL_WIDTH - 14;
        int closeY = panelY + 3;
        if (MiscTools.inBounds(closeX, closeY, 10, 10, mouseX, mouseY)) {
            expanded = false;
            playClickSound();
            return true;
        }

        int startX = panelX + PANEL_PADDING;
        int startY = panelY + HEADER_HEIGHT;

        if (MiscTools.inBounds(startX, startY, COLUMNS * SLOT_SIZE, ROWS * SLOT_SIZE, mouseX, mouseY)) {
            int col = (int) (mouseX - startX) / SLOT_SIZE;
            int row = (int) (mouseY - startY) / SLOT_SIZE;
            if (col >= 0 && col < COLUMNS && row >= 0 && row < ROWS) {
                int slotIndex = row * COLUMNS + col;
                AdvancedUpgradeStorageHandler storageHandler = new AdvancedUpgradeStorageHandler(storageStack);
                ItemStack carried = screen.getMenu().getCarried();
                ItemStack slotStack = storageHandler.getStackInSlot(slotIndex);

                if (!carried.isEmpty() && UpgradeStorageItem.isAllowedUpgrade(carried)) {
                    int action = (button == 1)
                            ? AdvancedUpgradeStorageActionPayload.ACTION_DEPOSIT_CURSOR_ONE
                            : AdvancedUpgradeStorageActionPayload.ACTION_DEPOSIT_CURSOR;
                    PacketDistributor.sendToServer(new AdvancedUpgradeStorageActionPayload(action, slotIndex));
                    playClickSound();
                    return true;
                }

                if (carried.isEmpty() && !slotStack.isEmpty()) {
                    int action;
                    if (Screen.hasShiftDown()) {
                        action = (button == 1)
                                ? AdvancedUpgradeStorageActionPayload.ACTION_WITHDRAW_INVENTORY
                                : AdvancedUpgradeStorageActionPayload.ACTION_INSERT_MAX;
                    } else {
                        action = (button == 1)
                                ? AdvancedUpgradeStorageActionPayload.ACTION_PICKUP_CURSOR
                                : AdvancedUpgradeStorageActionPayload.ACTION_INSERT_ONE;
                    }
                    PacketDistributor.sendToServer(new AdvancedUpgradeStorageActionPayload(action, slotIndex));
                    playClickSound();
                    return true;
                }
            }
            return true;
        }

        return MiscTools.inBounds(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, mouseX, mouseY);
    }

    private static void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static boolean canInsertIntoMachine(BaseMachineBE machine, ItemStack stack) {
        if (stack.isEmpty() || machine == null) {
            return false;
        }

        if (machine instanceof BioCrusherBE crusher) {
            if (stack.getItem() instanceof LootingUpgradeItem) {
                int count = 0;
                for (int s = 0; s < crusher.getLootingHandler().getSlots(); s++) {
                    count += crusher.getLootingHandler().getStackInSlot(s).getCount();
                }
                return count < JDTEConfig.COMMON.maxLootingUpgrades.get();
            }
            if (stack.getItem() instanceof SharpnessUpgradeItem) {
                int count = 0;
                for (int s = 0; s < crusher.getSharpnessHandler().getSlots(); s++) {
                    count += crusher.getSharpnessHandler().getStackInSlot(s).getCount();
                }
                return count < JDTEConfig.COMMON.maxSharpnessUpgrades.get();
            }
        }

        UpgradeItemStackHandler handler = UpgradeHelper.getUpgradeHandler(machine);
        if (handler == null) {
            return false;
        }

        for (int s = 0; s < handler.getSlots(); s++) {
            if (handler.insertItem(s, stack.copyWithCount(1), true).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOppositeSpeedConflict(BaseMachineBE machine, ItemStack stack) {
        if (machine == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof UpgradeCardItem card) {
            UpgradeType type = card.getType();
            if (type == UpgradeType.OVERCLOCK) {
                return UpgradeHelper.countUpgrades(machine, UpgradeType.UNDERCLOCK) > 0;
            }
            if (type == UpgradeType.UNDERCLOCK) {
                return UpgradeHelper.countUpgrades(machine, UpgradeType.OVERCLOCK) > 0;
            }
        }
        return false;
    }

    public static boolean isAtMaxLimit(BaseMachineBE machine, ItemStack stack) {
        if (machine == null || stack.isEmpty()) {
            return false;
        }
        int max = getUpgradeMax(machine, stack);
        if (max <= 0) {
            return false;
        }
        int current = getUpgradeCount(machine, stack);
        return current >= max;
    }

    public static int getUpgradeCount(BaseMachineBE machine, ItemStack stack) {
        if (machine == null || stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() instanceof UpgradeCardItem card) {
            return UpgradeHelper.countUpgrades(machine, card.getType());
        }
        if (stack.getItem() instanceof EnergyOverloadUpgradeItem) {
            return UpgradeHelper.hasEnergyOverloadUpgrade(machine) ? 1 : 0;
        }
        if (stack.getItem() instanceof EnergyBrewingUpgradeItem) {
            return (machine instanceof AdvancedPotionBrewerBE brewer && brewer.hasEnergyBrewingUpgrade()) ? 1 : 0;
        }
        if (stack.getItem() instanceof LootingUpgradeItem) {
            if (machine instanceof BioCrusherBE crusher) {
                int count = 0;
                for (int s = 0; s < crusher.getLootingHandler().getSlots(); s++) {
                    count += crusher.getLootingHandler().getStackInSlot(s).getCount();
                }
                return count;
            }
            if (machine instanceof LootFabricatorBE fabricator) {
                return fabricator.getLootingLevel();
            }
            if (machine instanceof BioFactoryBE) {
                return UpgradeHelper.countUpgrades(machine, UpgradeType.FORTUNE);
            }
        }
        if (stack.getItem() instanceof SharpnessUpgradeItem && machine instanceof BioCrusherBE crusher) {
            int count = 0;
            for (int s = 0; s < crusher.getSharpnessHandler().getSlots(); s++) {
                count += crusher.getSharpnessHandler().getStackInSlot(s).getCount();
            }
            return count;
        }
        return 0;
    }

    public static int getUpgradeMax(BaseMachineBE machine, ItemStack stack) {
        if (machine == null || stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() instanceof UpgradeCardItem card) {
            return UpgradeHelper.getMaxUpgrades(machine, card.getType());
        }
        if (stack.getItem() instanceof EnergyOverloadUpgradeItem) {
            return UpgradeHelper.isEnergyTransmitter(machine) ? 1 : 0;
        }
        if (stack.getItem() instanceof EnergyBrewingUpgradeItem) {
            return (machine instanceof AdvancedPotionBrewerBE) ? 1 : 0;
        }
        if (stack.getItem() instanceof LootingUpgradeItem) {
            if (machine instanceof BioCrusherBE) {
                return JDTEConfig.COMMON.maxLootingUpgrades.get();
            }
            if (machine instanceof LootFabricatorBE) {
                return LootFabricatorUpgradeItemStackHandler.MAX_LOOTING;
            }
            if (machine instanceof BioFactoryBE) {
                return 8;
            }
        }
        if (stack.getItem() instanceof SharpnessUpgradeItem && machine instanceof BioCrusherBE) {
            return JDTEConfig.COMMON.maxSharpnessUpgrades.get();
        }
        return 0;
    }
}
