package com.jdte.client.screens.util;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import com.direwolf20.justdirethings.common.containers.basecontainers.BaseMachineContainer;
import com.jdte.JDTE;
import com.jdte.common.blockentities.AdvancedPotionBrewerBE;
import com.jdte.common.containers.AdvancedPotionBrewerContainer;
import com.jdte.common.containers.BioCrusherContainer;
import com.jdte.common.containers.DynamicFilterSlot;
import com.jdte.common.upgrades.BioFactoryUpgradeItemStackHandler;
import com.jdte.common.upgrades.UpgradeHelper;
import com.jdte.common.upgrades.UpgradeSlot;
import com.jdte.common.upgrades.UpgradeType;
import com.jdte.common.utils.GuiUpgradeLayoutConfig;
import com.jdte.mixin.SlotAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class UpgradeSlotLayoutHelper {
    private static final ResourceLocation UPGRADE_SLOT_PANEL = ResourceLocation.fromNamespaceAndPath(JDTE.MODID, "textures/gui/upgrade_slot_panel.png");
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int PANEL_WIDTH = 32;
    private static final int PANEL_HEIGHT = 86;
    private static final Map<Slot, int[]> ORIGINAL_SLOT_POSITIONS = new WeakHashMap<>();

    private UpgradeSlotLayoutHelper() {
    }

    public static void layoutSlots(BaseMachineContainer container, int upgradeSlots) {
        int upgradeSlotIndex = 0;
        var config = GuiUpgradeLayoutConfig.getInstance();
        boolean isEightSlot = upgradeSlots > 4;

        for (Slot slot : container.slots) {
            int[] original = getOriginalSlotPosition(slot);
            SlotAccessor slotAccessor = (SlotAccessor) slot;

            if (slot instanceof DynamicFilterSlot filterSlot && !filterSlot.isActive()) {
                slotAccessor.jdte$setX(-10000);
                slotAccessor.jdte$setY(-10000);
            } else if (slot instanceof UpgradeSlot) {
                if (isEightSlot && upgradeSlotIndex >= 4) {
                    // Left panel (slots 4-7)
                    int row = upgradeSlotIndex - 4;
                    slotAccessor.jdte$setX(config.getLeftFirstSlotX() + 1);
                    slotAccessor.jdte$setY(config.getLeftFirstSlotY() + row * config.getSlotSpacing() + 1);
                } else {
                    // Right panel (slots 0-3)
                    int row = upgradeSlotIndex % 4;
                    slotAccessor.jdte$setX(config.getFirstSlotX() + 1);
                    slotAccessor.jdte$setY(config.getFirstSlotY() + row * config.getSlotSpacing() + 1);
                }
                upgradeSlotIndex++;
            } else if (container instanceof AdvancedPotionBrewerContainer && layoutPotionBrewerSlot(container, slot, slotAccessor, config)) {
                continue;
            } else if (container instanceof BioCrusherContainer && layoutBioCrusherSlot(slot, slotAccessor, config)) {
                continue;
            } else if (isPlayerInventorySlot(slot)) {
                slotAccessor.jdte$setX(original[0]);
                slotAccessor.jdte$setY(original[1]);
            } else {
                slotAccessor.jdte$setX(original[0]);
                slotAccessor.jdte$setY(original[1]);
            }
        }
    }

    private static boolean layoutPotionBrewerSlot(BaseMachineContainer container, Slot slot, SlotAccessor slotAccessor, GuiUpgradeLayoutConfig config) {
        if (container.slots.get(AdvancedPotionBrewerBE.BOTTLE_SLOT_0) == slot) {
            slotAccessor.jdte$setX(config.getPotionBrewerBottleSlot0X());
            slotAccessor.jdte$setY(config.getPotionBrewerBottleSlot0Y());
            return true;
        }
        if (container.slots.get(AdvancedPotionBrewerBE.BOTTLE_SLOT_1) == slot) {
            slotAccessor.jdte$setX(config.getPotionBrewerBottleSlot1X());
            slotAccessor.jdte$setY(config.getPotionBrewerBottleSlot1Y());
            return true;
        }
        if (container.slots.get(AdvancedPotionBrewerBE.BOTTLE_SLOT_2) == slot) {
            slotAccessor.jdte$setX(config.getPotionBrewerBottleSlot2X());
            slotAccessor.jdte$setY(config.getPotionBrewerBottleSlot2Y());
            return true;
        }
        if (container.slots.get(AdvancedPotionBrewerBE.INGREDIENT_SLOT) == slot) {
            slotAccessor.jdte$setX(config.getPotionBrewerIngredientSlotX());
            slotAccessor.jdte$setY(config.getPotionBrewerIngredientSlotY());
            return true;
        }
        if (container.slots.get(AdvancedPotionBrewerBE.FUEL_SLOT) == slot) {
            slotAccessor.jdte$setX(config.getPotionBrewerFuelSlotX());
            slotAccessor.jdte$setY(config.getPotionBrewerFuelSlotY());
            return true;
        }
        int extraIngredientCount = Math.min(AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_COUNT, config.getPotionBrewerExtraIngredientCount());
        for (int i = 0; i < extraIngredientCount; i++) {
            if (container.slots.get(AdvancedPotionBrewerBE.EXTRA_INGREDIENT_SLOT_START + i) == slot) {
                slotAccessor.jdte$setX(config.getPotionBrewerExtraIngredientStartX() + i * config.getPotionBrewerExtraIngredientSpacing());
                slotAccessor.jdte$setY(config.getPotionBrewerExtraIngredientStartY());
                return true;
            }
        }
        int outputCount = Math.min(AdvancedPotionBrewerBE.OUTPUT_SLOT_COUNT, config.getPotionBrewerOutputCount());
        for (int i = 0; i < outputCount; i++) {
            if (container.slots.get(AdvancedPotionBrewerBE.OUTPUT_SLOT_START + i) == slot) {
                slotAccessor.jdte$setX(config.getPotionBrewerOutputStartX());
                slotAccessor.jdte$setY(config.getPotionBrewerOutputStartY() + i * config.getPotionBrewerOutputSpacing());
                return true;
            }
        }
        return false;
    }

    private static boolean layoutBioCrusherSlot(Slot slot, SlotAccessor slotAccessor, GuiUpgradeLayoutConfig config) {
        if (!(slot instanceof BioCrusherContainer.BioCrusherUpgradeSlot upgradeSlot)) {
            return false;
        }

        if (upgradeSlot.getKind() == BioCrusherContainer.UpgradeKind.SHARPNESS) {
            slotAccessor.jdte$setX(config.getBioCrusherSharpnessSlotX());
            slotAccessor.jdte$setY(config.getBioCrusherSharpnessSlotY());
            return true;
        }

        slotAccessor.jdte$setX(config.getBioCrusherLootingSlotX());
        slotAccessor.jdte$setY(config.getBioCrusherLootingSlotY());
        return true;
    }

    private static boolean isPlayerInventorySlot(Slot slot) {
        return slot instanceof SlotItemHandler slotItemHandler && slotItemHandler.getItemHandler() instanceof InvWrapper;
    }

    private static int[] getOriginalSlotPosition(Slot slot) {
        return ORIGINAL_SLOT_POSITIONS.computeIfAbsent(slot, key -> new int[]{key.x, key.y});
    }

    public static void renderFixedUpgradePanels(GuiGraphics guiGraphics, int totalSlots, int guiLeft, int guiTop) {
        var config = GuiUpgradeLayoutConfig.getInstance();
        int half = 4;
        boolean hasLeftPanel = totalSlots > 4;

        // Right panel (slots 0-3)
        drawSlotPanel(guiGraphics, config, guiLeft, guiTop, config.getFirstSlotX(), config.getFirstSlotY(), Math.min(half, totalSlots));

        // Left panel (slots 4-7, only for 8-slot machines)
        if (hasLeftPanel) {
            drawSlotPanel(guiGraphics, config, guiLeft, guiTop, config.getLeftFirstSlotX(), config.getLeftFirstSlotY(), totalSlots - half);
        }
    }

    private static void drawSlotPanel(GuiGraphics guiGraphics, GuiUpgradeLayoutConfig config, int guiLeft, int guiTop, int originX, int originY, int slotCount) {
        int panelX = guiLeft + originX - (PANEL_WIDTH - config.getSlotSize()) / 2;
        int panelY = guiTop + originY - (PANEL_HEIGHT - config.getRows() * config.getSlotSize()) / 2;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(UPGRADE_SLOT_PANEL, panelX, panelY, 0, 0,
                PANEL_WIDTH, PANEL_HEIGHT,
                PANEL_WIDTH, PANEL_HEIGHT);

        for (int i = 0; i < slotCount; i++) {
            int sx = guiLeft + originX;
            int sy = guiTop + originY + i * config.getSlotSpacing();
            GuiSpriteCompat.blitSprite(guiGraphics, SLOT_SPRITE, sx, sy, config.getSlotSize(), config.getSlotSize());
        }
    }

    public static List<FormattedText> buildEmptyUpgradeSlotTooltip(BaseMachineBE baseMachineBE) {
        List<FormattedText> lines = new ArrayList<>();
        lines.add(Component.translatable("jdte.upgrade.slot.empty"));
        lines.add(Component.empty());

        for (UpgradeType type : UpgradeType.values()) {
            if (!UpgradeHelper.isUpgradeCompatible(baseMachineBE, type)) continue;

            int current = UpgradeHelper.countUpgrades(baseMachineBE, type);
            int max = UpgradeHelper.getMaxUpgrades(baseMachineBE, type);
            boolean canAdd = current < max && !hasOppositeSpeedUpgrade(baseMachineBE, type);

            Component name = Component.translatable("item.jdte." + type.getSerializedName() + "_upgrade");
            lines.add(Component.literal("  ")
                    .append(name)
                    .append(Component.literal(": " + current + "/" + max))
                    .copy()
                    .withStyle(canAdd ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY));
        }

        if (baseMachineBE instanceof com.jdte.common.blockentities.LootFabricatorBE fabricator) {
            int current = fabricator.getLootingLevel();
            lines.add(Component.literal("  ")
                    .append(Component.translatable("item.jdte.looting_upgrade"))
                    .append(Component.literal(": " + current + "/3"))
                    .copy()
                    .withStyle(current < 3 ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY));
        }

        if (baseMachineBE instanceof com.jdte.common.blockentities.BioFactoryBE factory
                && net.minecraftforge.fml.ModList.get().isLoaded("productivebees")) {
            int productivityTotal = factory.getUpgradeHandler().getProductivityCount();
            for (ResourceLocation id : BioFactoryUpgradeItemStackHandler.getProductivityUpgradeIds()) {
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> {
                    ItemStack upgrade = new ItemStack(item);
                    int tier = BioFactoryUpgradeItemStackHandler.getProductivityTier(upgrade);
                    int current = factory.getUpgradeHandler().countProductivityTier(tier);
                    lines.add(Component.literal("  ")
                            .append(upgrade.getHoverName())
                            .append(Component.literal(": " + current + " (" + productivityTotal + "/4)"))
                            .copy()
                            .withStyle(productivityTotal < 4 ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY));
                });
            }
        }

        if (baseMachineBE instanceof com.jdte.common.blockentities.BioFactoryBE factory) {
            int current = factory.getUpgradeHandler().getLootingCount();
            lines.add(Component.literal("  ")
                    .append(Component.translatable("item.jdte.looting_upgrade"))
                    .append(Component.literal(": " + current + "/4"))
                    .copy()
                    .withStyle(current < 4 ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY));
        }

        return lines;
    }

    private static boolean hasOppositeSpeedUpgrade(BaseMachineBE baseMachineBE, UpgradeType type) {
        if (type == UpgradeType.OVERCLOCK) {
            return UpgradeHelper.countUpgrades(baseMachineBE, UpgradeType.UNDERCLOCK) > 0;
        }
        if (type == UpgradeType.UNDERCLOCK) {
            return UpgradeHelper.countUpgrades(baseMachineBE, UpgradeType.OVERCLOCK) > 0;
        }
        return false;
    }
}
