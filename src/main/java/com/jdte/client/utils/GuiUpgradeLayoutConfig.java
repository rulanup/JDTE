package com.jdte.client.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;

/**
 * Reads GUI layout configuration from assets/jdte/gui_layout.json.
 * Provides soft-coded positions for upgrade panels.
 * Falls back to hardcoded defaults if the JSON file cannot be loaded.
 */
public class GuiUpgradeLayoutConfig {
    private static final ResourceLocation CONFIG_LOCATION = ResourceLocation.fromNamespaceAndPath("jdte", "gui_layout.json");
    private static GuiUpgradeLayoutConfig INSTANCE;

    // Right panel (upgrade_panel_4): used by 4-slot machines and as right panel for 8-slot machines
    private final int firstSlotX;
    private final int firstSlotY;
    private final int slotSpacing;
    private final int slotSize;
    private final int columns;
    private final int rows;
    private final int panelPadding;

    // Left panel (upgrade_panel_8_left): used as left panel for 8-slot machines
    private final int leftFirstSlotX;
    private final int leftFirstSlotY;

    // Gel generator machine slot positions (gel_generator_slots)
    private final int gelGenGelX;
    private final int gelGenGelY;
    private final int gelGenFoodX;
    private final int gelGenFoodY;
    private final int gelGenInputStartX;
    private final int gelGenInputStartY;
    private final int gelGenInputSpacing;
    private final int gelGenInputCount;
    private final int gelGenInputFluidX;
    private final int gelGenInputFluidY;
    private final int gelGenOutputStartX;
    private final int gelGenOutputStartY;
    private final int gelGenOutputSpacing;
    private final int gelGenOutputCount;
    private final int gelGenOutputFluidX;
    private final int gelGenOutputFluidY;

    // Gel generator button positions (gel_generator_buttons)
    private final int gelGenProgressArrowX;
    private final int gelGenProgressArrowY;
    private final int gelGenSpeedButtonX;
    private final int gelGenSpeedButtonY;
    private final int gelGenAllowlistButtonX;
    private final int gelGenAllowlistButtonY;
    private final int gelGenCompareNBTButtonX;
    private final int gelGenCompareNBTButtonY;
    private final int gelGenRedstoneButtonX;
    private final int gelGenRedstoneButtonY;
    private final int gelGenAutoBalanceX;
    private final int gelGenAutoBalanceY;

    // Item sender button positions (item_sender_buttons)
    private final int itemSenderAllowlistX;
    private final int itemSenderAllowlistY;
    private final int itemSenderCompareNBTX;
    private final int itemSenderCompareNBTY;
    private final int itemSenderRedstoneX;
    private final int itemSenderRedstoneY;
    private final int itemSenderRenderAreaX;
    private final int itemSenderRenderAreaY;
    private final int itemSenderSpeedButtonX;
    private final int itemSenderSpeedButtonY;

    // Item sender machine slot positions (item_sender_slots)
    private final int itemSenderSlotStartX;
    private final int itemSenderSlotStartY;
    private final int itemSenderSlotSpacing;
    private final int itemSenderSlotCount;

    // Basic item sender button positions (basic_item_sender_buttons)
    private final int basicItemSenderAllowlistX;
    private final int basicItemSenderAllowlistY;
    private final int basicItemSenderCompareNBTX;
    private final int basicItemSenderCompareNBTY;
    private final int basicItemSenderRedstoneX;
    private final int basicItemSenderRedstoneY;
    private final int basicItemSenderRenderAreaX;
    private final int basicItemSenderRenderAreaY;
    private final int basicItemSenderSpeedButtonX;
    private final int basicItemSenderSpeedButtonY;

    // Basic item sender machine slot positions (basic_item_sender_slots)
    private final int basicItemSenderSlotStartX;
    private final int basicItemSenderSlotStartY;
    private final int basicItemSenderSlotSpacing;
    private final int basicItemSenderSlotCount;

    // Item receiver button positions (item_receiver_buttons)
    private final int itemReceiverAllowlistX;
    private final int itemReceiverAllowlistY;
    private final int itemReceiverCompareNBTX;
    private final int itemReceiverCompareNBTY;
    private final int itemReceiverRedstoneX;
    private final int itemReceiverRedstoneY;
    private final int itemReceiverRenderAreaX;
    private final int itemReceiverRenderAreaY;
    private final int itemReceiverSpeedButtonX;
    private final int itemReceiverSpeedButtonY;

    // Item receiver machine slot positions (item_receiver_slots)
    private final int itemReceiverSlotStartX;
    private final int itemReceiverSlotStartY;
    private final int itemReceiverSlotSpacing;
    private final int itemReceiverSlotCount;

    // Basic item receiver button positions (basic_item_receiver_buttons)
    private final int basicItemReceiverAllowlistX;
    private final int basicItemReceiverAllowlistY;
    private final int basicItemReceiverCompareNBTX;
    private final int basicItemReceiverCompareNBTY;
    private final int basicItemReceiverRedstoneX;
    private final int basicItemReceiverRedstoneY;
    private final int basicItemReceiverRenderAreaX;
    private final int basicItemReceiverRenderAreaY;
    private final int basicItemReceiverSpeedButtonX;
    private final int basicItemReceiverSpeedButtonY;

    // Basic item receiver machine slot positions (basic_item_receiver_slots)
    private final int basicItemReceiverSlotStartX;
    private final int basicItemReceiverSlotStartY;
    private final int basicItemReceiverSlotSpacing;
    private final int basicItemReceiverSlotCount;

    // Life extractor mode button positions (life_extractor_buttons)
    private final int lifeExtractorModeButtonX;
    private final int lifeExtractorModeButtonY;

    // Bio crusher mode button positions (bio_crusher_buttons)
    private final int bioCrusherModeButtonX;
    private final int bioCrusherModeButtonY;

    private GuiUpgradeLayoutConfig(JsonObject json) {
        JsonObject panel = json.getAsJsonObject("upgrade_panel_4");
        this.firstSlotX = getInt(panel, "first_slot_x", 183);
        this.firstSlotY = getInt(panel, "first_slot_y", 85);
        this.slotSpacing = getInt(panel, "slot_spacing", 18);
        this.slotSize = getInt(panel, "slot_size", 18);
        this.columns = getInt(panel, "columns", 1);
        this.rows = getInt(panel, "rows", 4);
        this.panelPadding = getInt(panel, "panel_padding", 8);

        JsonObject leftPanel = json.getAsJsonObject("upgrade_panel_8_left");
        if (leftPanel != null) {
            this.leftFirstSlotX = getInt(leftPanel, "first_slot_x", -22);
            this.leftFirstSlotY = getInt(leftPanel, "first_slot_y", this.firstSlotY);
        } else {
            this.leftFirstSlotX = -22;
            this.leftFirstSlotY = this.firstSlotY;
        }

        JsonObject gelGen = json.getAsJsonObject("gel_generator_slots");
        if (gelGen != null) {
            this.gelGenGelX = getInt(gelGen, "gel_slot_x", 10);
            this.gelGenGelY = getInt(gelGen, "gel_slot_y", -12);
            this.gelGenFoodX = getInt(gelGen, "food_slot_x", 10);
            this.gelGenFoodY = getInt(gelGen, "food_slot_y", 24);
            this.gelGenInputStartX = getInt(gelGen, "input_start_x", 44);
            this.gelGenInputStartY = getInt(gelGen, "input_start_y", -21);
            this.gelGenInputSpacing = getInt(gelGen, "input_spacing", 18);
            this.gelGenInputCount = getInt(gelGen, "input_count", 4);
            this.gelGenInputFluidX = getInt(gelGen, "input_fluid_x", this.gelGenInputStartX + 18);
            this.gelGenInputFluidY = getInt(gelGen, "input_fluid_y", this.gelGenInputStartY);
            this.gelGenOutputStartX = getInt(gelGen, "output_start_x", 116);
            this.gelGenOutputStartY = getInt(gelGen, "output_start_y", -21);
            this.gelGenOutputSpacing = getInt(gelGen, "output_spacing", 18);
            this.gelGenOutputCount = getInt(gelGen, "output_count", 4);
            this.gelGenOutputFluidX = getInt(gelGen, "output_fluid_x", this.gelGenOutputStartX + 18);
            this.gelGenOutputFluidY = getInt(gelGen, "output_fluid_y", this.gelGenOutputStartY);
        } else {
            this.gelGenGelX = 10;
            this.gelGenGelY = -12;
            this.gelGenFoodX = 10;
            this.gelGenFoodY = 24;
            this.gelGenInputStartX = 44;
            this.gelGenInputStartY = -21;
            this.gelGenInputSpacing = 18;
            this.gelGenInputCount = 4;
            this.gelGenInputFluidX = 62;
            this.gelGenInputFluidY = -22;
            this.gelGenOutputStartX = 116;
            this.gelGenOutputStartY = -21;
            this.gelGenOutputSpacing = 18;
            this.gelGenOutputCount = 4;
            this.gelGenOutputFluidX = 134;
            this.gelGenOutputFluidY = -22;
        }

        JsonObject gelGenButtons = json.getAsJsonObject("gel_generator_buttons");
        if (gelGenButtons != null) {
            this.gelGenProgressArrowX = getInt(gelGenButtons, "progress_arrow_x", 88);
            this.gelGenProgressArrowY = getInt(gelGenButtons, "progress_arrow_y", 8);
            this.gelGenSpeedButtonX = getInt(gelGenButtons, "speed_button_x", 88);
            this.gelGenSpeedButtonY = getInt(gelGenButtons, "speed_button_y", 38);
            this.gelGenAllowlistButtonX = getInt(gelGenButtons, "allowlist_button_x", 170);
            this.gelGenAllowlistButtonY = getInt(gelGenButtons, "allowlist_button_y", -3);
            this.gelGenCompareNBTButtonX = getInt(gelGenButtons, "compare_nbt_button_x", 170);
            this.gelGenCompareNBTButtonY = getInt(gelGenButtons, "compare_nbt_button_y", 15);
            this.gelGenRedstoneButtonX = getInt(gelGenButtons, "redstone_button_x", 170);
            this.gelGenRedstoneButtonY = getInt(gelGenButtons, "redstone_button_y", 33);
            this.gelGenAutoBalanceX = getInt(gelGenButtons, "auto_balance_x", 170);
            this.gelGenAutoBalanceY = getInt(gelGenButtons, "auto_balance_y", -21);
        } else {
            this.gelGenProgressArrowX = 88;
            this.gelGenProgressArrowY = 8;
            this.gelGenSpeedButtonX = 88;
            this.gelGenSpeedButtonY = 38;
            this.gelGenAllowlistButtonX = 170;
            this.gelGenAllowlistButtonY = -3;
            this.gelGenCompareNBTButtonX = 170;
            this.gelGenCompareNBTButtonY = 15;
            this.gelGenRedstoneButtonX = 170;
            this.gelGenRedstoneButtonY = 33;
            this.gelGenAutoBalanceX = 170;
            this.gelGenAutoBalanceY = -21;
        }

        JsonObject itemSenderButtons = json.getAsJsonObject("item_sender_buttons");
        if (itemSenderButtons != null) {
            this.itemSenderAllowlistX = getInt(itemSenderButtons, "allowlist_button_x", 8);
            this.itemSenderAllowlistY = getInt(itemSenderButtons, "allowlist_button_y", 42);
            this.itemSenderCompareNBTX = getInt(itemSenderButtons, "compare_nbt_button_x", 26);
            this.itemSenderCompareNBTY = getInt(itemSenderButtons, "compare_nbt_button_y", 42);
            this.itemSenderRedstoneX = getInt(itemSenderButtons, "redstone_button_x", 134);
            this.itemSenderRedstoneY = getInt(itemSenderButtons, "redstone_button_y", 42);
            this.itemSenderRenderAreaX = getInt(itemSenderButtons, "render_area_button_x", 152);
            this.itemSenderRenderAreaY = getInt(itemSenderButtons, "render_area_button_y", 42);
            this.itemSenderSpeedButtonX = getInt(itemSenderButtons, "speed_button_x", 78);
            this.itemSenderSpeedButtonY = getInt(itemSenderButtons, "speed_button_y", 44);
        } else {
            this.itemSenderAllowlistX = 8;
            this.itemSenderAllowlistY = 42;
            this.itemSenderCompareNBTX = 26;
            this.itemSenderCompareNBTY = 42;
            this.itemSenderRedstoneX = 134;
            this.itemSenderRedstoneY = 42;
            this.itemSenderRenderAreaX = 152;
            this.itemSenderRenderAreaY = 42;
            this.itemSenderSpeedButtonX = 78;
            this.itemSenderSpeedButtonY = 44;
        }

        JsonObject itemSenderSlots = json.getAsJsonObject("item_sender_slots");
        if (itemSenderSlots != null) {
            this.itemSenderSlotStartX = getInt(itemSenderSlots, "machine_start_x", 8);
            this.itemSenderSlotStartY = getInt(itemSenderSlots, "machine_start_y", 36);
            this.itemSenderSlotSpacing = getInt(itemSenderSlots, "machine_spacing", 18);
            this.itemSenderSlotCount = getInt(itemSenderSlots, "machine_count", 9);
        } else {
            this.itemSenderSlotStartX = 8;
            this.itemSenderSlotStartY = 36;
            this.itemSenderSlotSpacing = 18;
            this.itemSenderSlotCount = 9;
        }

        JsonObject basicItemSenderButtons = json.getAsJsonObject("basic_item_sender_buttons");
        if (basicItemSenderButtons != null) {
            this.basicItemSenderAllowlistX = getInt(basicItemSenderButtons, "allowlist_button_x", 8);
            this.basicItemSenderAllowlistY = getInt(basicItemSenderButtons, "allowlist_button_y", 42);
            this.basicItemSenderCompareNBTX = getInt(basicItemSenderButtons, "compare_nbt_button_x", 26);
            this.basicItemSenderCompareNBTY = getInt(basicItemSenderButtons, "compare_nbt_button_y", 42);
            this.basicItemSenderRedstoneX = getInt(basicItemSenderButtons, "redstone_button_x", 134);
            this.basicItemSenderRedstoneY = getInt(basicItemSenderButtons, "redstone_button_y", 42);
            this.basicItemSenderRenderAreaX = getInt(basicItemSenderButtons, "render_area_button_x", 152);
            this.basicItemSenderRenderAreaY = getInt(basicItemSenderButtons, "render_area_button_y", 42);
            this.basicItemSenderSpeedButtonX = getInt(basicItemSenderButtons, "speed_button_x", 78);
            this.basicItemSenderSpeedButtonY = getInt(basicItemSenderButtons, "speed_button_y", 44);
        } else {
            this.basicItemSenderAllowlistX = 8;
            this.basicItemSenderAllowlistY = 42;
            this.basicItemSenderCompareNBTX = 26;
            this.basicItemSenderCompareNBTY = 42;
            this.basicItemSenderRedstoneX = 134;
            this.basicItemSenderRedstoneY = 42;
            this.basicItemSenderRenderAreaX = 152;
            this.basicItemSenderRenderAreaY = 42;
            this.basicItemSenderSpeedButtonX = 78;
            this.basicItemSenderSpeedButtonY = 44;
        }

        JsonObject basicItemSenderSlots = json.getAsJsonObject("basic_item_sender_slots");
        if (basicItemSenderSlots != null) {
            this.basicItemSenderSlotStartX = getInt(basicItemSenderSlots, "machine_start_x", 8);
            this.basicItemSenderSlotStartY = getInt(basicItemSenderSlots, "machine_start_y", 36);
            this.basicItemSenderSlotSpacing = getInt(basicItemSenderSlots, "machine_spacing", 18);
            this.basicItemSenderSlotCount = getInt(basicItemSenderSlots, "machine_count", 9);
        } else {
            this.basicItemSenderSlotStartX = 8;
            this.basicItemSenderSlotStartY = 36;
            this.basicItemSenderSlotSpacing = 18;
            this.basicItemSenderSlotCount = 9;
        }

        // --- Item receiver buttons ---
        JsonObject itemReceiverButtons = json.getAsJsonObject("item_receiver_buttons");
        if (itemReceiverButtons != null) {
            this.itemReceiverAllowlistX = getInt(itemReceiverButtons, "allowlist_button_x", 8);
            this.itemReceiverAllowlistY = getInt(itemReceiverButtons, "allowlist_button_y", 42);
            this.itemReceiverCompareNBTX = getInt(itemReceiverButtons, "compare_nbt_button_x", 26);
            this.itemReceiverCompareNBTY = getInt(itemReceiverButtons, "compare_nbt_button_y", 42);
            this.itemReceiverRedstoneX = getInt(itemReceiverButtons, "redstone_button_x", 134);
            this.itemReceiverRedstoneY = getInt(itemReceiverButtons, "redstone_button_y", 42);
            this.itemReceiverRenderAreaX = getInt(itemReceiverButtons, "render_area_button_x", 152);
            this.itemReceiverRenderAreaY = getInt(itemReceiverButtons, "render_area_button_y", 42);
            this.itemReceiverSpeedButtonX = getInt(itemReceiverButtons, "speed_button_x", 78);
            this.itemReceiverSpeedButtonY = getInt(itemReceiverButtons, "speed_button_y", 44);
        } else {
            this.itemReceiverAllowlistX = 8;
            this.itemReceiverAllowlistY = 42;
            this.itemReceiverCompareNBTX = 26;
            this.itemReceiverCompareNBTY = 42;
            this.itemReceiverRedstoneX = 134;
            this.itemReceiverRedstoneY = 42;
            this.itemReceiverRenderAreaX = 152;
            this.itemReceiverRenderAreaY = 42;
            this.itemReceiverSpeedButtonX = 78;
            this.itemReceiverSpeedButtonY = 44;
        }

        JsonObject itemReceiverSlots = json.getAsJsonObject("item_receiver_slots");
        if (itemReceiverSlots != null) {
            this.itemReceiverSlotStartX = getInt(itemReceiverSlots, "machine_start_x", 8);
            this.itemReceiverSlotStartY = getInt(itemReceiverSlots, "machine_start_y", 36);
            this.itemReceiverSlotSpacing = getInt(itemReceiverSlots, "machine_spacing", 18);
            this.itemReceiverSlotCount = getInt(itemReceiverSlots, "machine_count", 9);
        } else {
            this.itemReceiverSlotStartX = 8;
            this.itemReceiverSlotStartY = 36;
            this.itemReceiverSlotSpacing = 18;
            this.itemReceiverSlotCount = 9;
        }

        // --- Basic item receiver buttons ---
        JsonObject basicItemReceiverButtons = json.getAsJsonObject("basic_item_receiver_buttons");
        if (basicItemReceiverButtons != null) {
            this.basicItemReceiverAllowlistX = getInt(basicItemReceiverButtons, "allowlist_button_x", 8);
            this.basicItemReceiverAllowlistY = getInt(basicItemReceiverButtons, "allowlist_button_y", 42);
            this.basicItemReceiverCompareNBTX = getInt(basicItemReceiverButtons, "compare_nbt_button_x", 26);
            this.basicItemReceiverCompareNBTY = getInt(basicItemReceiverButtons, "compare_nbt_button_y", 42);
            this.basicItemReceiverRedstoneX = getInt(basicItemReceiverButtons, "redstone_button_x", 134);
            this.basicItemReceiverRedstoneY = getInt(basicItemReceiverButtons, "redstone_button_y", 42);
            this.basicItemReceiverRenderAreaX = getInt(basicItemReceiverButtons, "render_area_button_x", 152);
            this.basicItemReceiverRenderAreaY = getInt(basicItemReceiverButtons, "render_area_button_y", 42);
            this.basicItemReceiverSpeedButtonX = getInt(basicItemReceiverButtons, "speed_button_x", 78);
            this.basicItemReceiverSpeedButtonY = getInt(basicItemReceiverButtons, "speed_button_y", 44);
        } else {
            this.basicItemReceiverAllowlistX = 8;
            this.basicItemReceiverAllowlistY = 42;
            this.basicItemReceiverCompareNBTX = 26;
            this.basicItemReceiverCompareNBTY = 42;
            this.basicItemReceiverRedstoneX = 134;
            this.basicItemReceiverRedstoneY = 42;
            this.basicItemReceiverRenderAreaX = 152;
            this.basicItemReceiverRenderAreaY = 42;
            this.basicItemReceiverSpeedButtonX = 78;
            this.basicItemReceiverSpeedButtonY = 44;
        }

        JsonObject basicItemReceiverSlots = json.getAsJsonObject("basic_item_receiver_slots");
        if (basicItemReceiverSlots != null) {
            this.basicItemReceiverSlotStartX = getInt(basicItemReceiverSlots, "machine_start_x", 8);
            this.basicItemReceiverSlotStartY = getInt(basicItemReceiverSlots, "machine_start_y", 36);
            this.basicItemReceiverSlotSpacing = getInt(basicItemReceiverSlots, "machine_spacing", 18);
            this.basicItemReceiverSlotCount = getInt(basicItemReceiverSlots, "machine_count", 9);
        } else {
            this.basicItemReceiverSlotStartX = 8;
            this.basicItemReceiverSlotStartY = 36;
            this.basicItemReceiverSlotSpacing = 18;
            this.basicItemReceiverSlotCount = 9;
        }

        // --- Life extractor buttons ---
        JsonObject lifeExtractorButtons = json.getAsJsonObject("life_extractor_buttons");
        if (lifeExtractorButtons != null) {
            this.lifeExtractorModeButtonX = getInt(lifeExtractorButtons, "mode_button_x", 80);
            this.lifeExtractorModeButtonY = getInt(lifeExtractorButtons, "mode_button_y", 62);
        } else {
            this.lifeExtractorModeButtonX = 80;
            this.lifeExtractorModeButtonY = 62;
        }

        // --- Bio crusher buttons ---
        JsonObject bioCrusherButtons = json.getAsJsonObject("bio_crusher_buttons");
        if (bioCrusherButtons != null) {
            this.bioCrusherModeButtonX = getInt(bioCrusherButtons, "mode_button_x", 80);
            this.bioCrusherModeButtonY = getInt(bioCrusherButtons, "mode_button_y", 62);
        } else {
            this.bioCrusherModeButtonX = 80;
            this.bioCrusherModeButtonY = 62;
        }
    }

    private GuiUpgradeLayoutConfig() {
        // Hardcoded fallback defaults
        this.firstSlotX = 183;
        this.firstSlotY = 85;
        this.slotSpacing = 18;
        this.slotSize = 18;
        this.columns = 1;
        this.rows = 4;
        this.panelPadding = 8;
        this.leftFirstSlotX = -22;
        this.leftFirstSlotY = 85;
        this.gelGenGelX = 10;
        this.gelGenGelY = -12;
        this.gelGenFoodX = 10;
        this.gelGenFoodY = 24;
        this.gelGenInputStartX = 44;
        this.gelGenInputStartY = -21;
        this.gelGenInputSpacing = 18;
        this.gelGenInputCount = 4;
        this.gelGenInputFluidX = 62;
        this.gelGenInputFluidY = -22;
        this.gelGenOutputStartX = 116;
        this.gelGenOutputStartY = -21;
        this.gelGenOutputSpacing = 18;
        this.gelGenOutputCount = 4;
        this.gelGenOutputFluidX = 134;
        this.gelGenOutputFluidY = -22;
        this.gelGenProgressArrowX = 88;
        this.gelGenProgressArrowY = 8;
        this.gelGenSpeedButtonX = 88;
        this.gelGenSpeedButtonY = 38;
        this.gelGenAllowlistButtonX = 170;
        this.gelGenAllowlistButtonY = -3;
        this.gelGenCompareNBTButtonX = 170;
        this.gelGenCompareNBTButtonY = 15;
        this.gelGenRedstoneButtonX = 170;
        this.gelGenRedstoneButtonY = 33;
        this.gelGenAutoBalanceX = 170;
        this.gelGenAutoBalanceY = -21;
        this.itemSenderAllowlistX = 8;
        this.itemSenderAllowlistY = 42;
        this.itemSenderCompareNBTX = 26;
        this.itemSenderCompareNBTY = 42;
        this.itemSenderRedstoneX = 134;
        this.itemSenderRedstoneY = 42;
        this.itemSenderRenderAreaX = 152;
        this.itemSenderRenderAreaY = 42;
        this.itemSenderSpeedButtonX = 78;
        this.itemSenderSpeedButtonY = 44;
        this.itemSenderSlotStartX = 8;
        this.itemSenderSlotStartY = 36;
        this.itemSenderSlotSpacing = 18;
        this.itemSenderSlotCount = 9;
        this.basicItemSenderAllowlistX = 8;
        this.basicItemSenderAllowlistY = 42;
        this.basicItemSenderCompareNBTX = 26;
        this.basicItemSenderCompareNBTY = 42;
        this.basicItemSenderRedstoneX = 134;
        this.basicItemSenderRedstoneY = 42;
        this.basicItemSenderRenderAreaX = 152;
        this.basicItemSenderRenderAreaY = 42;
        this.basicItemSenderSpeedButtonX = 78;
        this.basicItemSenderSpeedButtonY = 44;
        this.basicItemSenderSlotStartX = 8;
        this.basicItemSenderSlotStartY = 36;
        this.basicItemSenderSlotSpacing = 18;
        this.basicItemSenderSlotCount = 9;
        this.itemReceiverAllowlistX = 8;
        this.itemReceiverAllowlistY = 42;
        this.itemReceiverCompareNBTX = 26;
        this.itemReceiverCompareNBTY = 42;
        this.itemReceiverRedstoneX = 134;
        this.itemReceiverRedstoneY = 42;
        this.itemReceiverRenderAreaX = 152;
        this.itemReceiverRenderAreaY = 42;
        this.itemReceiverSpeedButtonX = 78;
        this.itemReceiverSpeedButtonY = 44;
        this.itemReceiverSlotStartX = 8;
        this.itemReceiverSlotStartY = 36;
        this.itemReceiverSlotSpacing = 18;
        this.itemReceiverSlotCount = 9;
        this.basicItemReceiverAllowlistX = 8;
        this.basicItemReceiverAllowlistY = 42;
        this.basicItemReceiverCompareNBTX = 26;
        this.basicItemReceiverCompareNBTY = 42;
        this.basicItemReceiverRedstoneX = 134;
        this.basicItemReceiverRedstoneY = 42;
        this.basicItemReceiverRenderAreaX = 152;
        this.basicItemReceiverRenderAreaY = 42;
        this.basicItemReceiverSpeedButtonX = 78;
        this.basicItemReceiverSpeedButtonY = 44;
        this.basicItemReceiverSlotStartX = 8;
        this.basicItemReceiverSlotStartY = 36;
        this.basicItemReceiverSlotSpacing = 18;
        this.basicItemReceiverSlotCount = 9;
        this.lifeExtractorModeButtonX = 80;
        this.lifeExtractorModeButtonY = 62;
        this.bioCrusherModeButtonX = 80;
        this.bioCrusherModeButtonY = 62;
    }

    /**
     * Get the singleton config instance. Loads from JSON on first access.
     */
    public static GuiUpgradeLayoutConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static GuiUpgradeLayoutConfig load() {
        try {
            var mc = Minecraft.getInstance();
            if (mc == null) {
                return new GuiUpgradeLayoutConfig();
            }
            var resourceOpt = mc.getResourceManager().getResource(CONFIG_LOCATION);
            if (resourceOpt.isPresent()) {
                try (var reader = new InputStreamReader(resourceOpt.get().open())) {
                    JsonObject json = new Gson().fromJson(reader, JsonObject.class);
                    if (json != null && json.has("upgrade_panel_4")) {
                        return new GuiUpgradeLayoutConfig(json);
                    }
                }
            }
        } catch (Exception e) {
            // Fall back to defaults on any error
        }
        return new GuiUpgradeLayoutConfig();
    }

    private static int getInt(JsonObject obj, String key, int defaultValue) {
        return obj.has(key) ? obj.get(key).getAsInt() : defaultValue;
    }

    // --- Right panel getters ---

    public int getFirstSlotX() {
        return firstSlotX;
    }

    public int getFirstSlotY() {
        return firstSlotY;
    }

    public int getSlotSpacing() {
        return slotSpacing;
    }

    public int getSlotSize() {
        return slotSize;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public int getPanelPadding() {
        return panelPadding;
    }

    public int getPanelWidth() {
        return columns * slotSize + 2 * panelPadding;
    }

    public int getPanelHeight() {
        return rows * slotSize + 2 * panelPadding;
    }

    // --- Left panel getters (only for 8-slot machines) ---

    public int getLeftFirstSlotX() {
        return leftFirstSlotX;
    }

    public int getLeftFirstSlotY() {
        return leftFirstSlotY;
    }

    // --- Gel generator machine slot getters ---

    public int getGelGenGelX() {
        return gelGenGelX;
    }

    public int getGelGenGelY() {
        return gelGenGelY;
    }

    public int getGelGenFoodX() {
        return gelGenFoodX;
    }

    public int getGelGenFoodY() {
        return gelGenFoodY;
    }

    public int getGelGenInputStartX() {
        return gelGenInputStartX;
    }

    public int getGelGenInputStartY() {
        return gelGenInputStartY;
    }

    public int getGelGenInputSpacing() {
        return gelGenInputSpacing;
    }

    public int getGelGenInputCount() {
        return gelGenInputCount;
    }

    public int getGelGenInputFluidX() {
        return gelGenInputFluidX;
    }

    public int getGelGenInputFluidY() {
        return gelGenInputFluidY;
    }

    public int getGelGenOutputStartX() {
        return gelGenOutputStartX;
    }

    public int getGelGenOutputStartY() {
        return gelGenOutputStartY;
    }

    public int getGelGenOutputSpacing() {
        return gelGenOutputSpacing;
    }

    public int getGelGenOutputCount() {
        return gelGenOutputCount;
    }

    public int getGelGenOutputFluidX() {
        return gelGenOutputFluidX;
    }

    public int getGelGenOutputFluidY() {
        return gelGenOutputFluidY;
    }

    // --- Gel generator button getters ---

    public int getGelGenProgressArrowX() {
        return gelGenProgressArrowX;
    }

    public int getGelGenProgressArrowY() {
        return gelGenProgressArrowY;
    }

    public int getGelGenSpeedButtonX() {
        return gelGenSpeedButtonX;
    }

    public int getGelGenSpeedButtonY() {
        return gelGenSpeedButtonY;
    }

    public int getGelGenAllowlistButtonX() {
        return gelGenAllowlistButtonX;
    }

    public int getGelGenAllowlistButtonY() {
        return gelGenAllowlistButtonY;
    }

    public int getGelGenCompareNBTButtonX() {
        return gelGenCompareNBTButtonX;
    }

    public int getGelGenCompareNBTButtonY() {
        return gelGenCompareNBTButtonY;
    }

    public int getGelGenRedstoneButtonX() {
        return gelGenRedstoneButtonX;
    }

    public int getGelGenRedstoneButtonY() {
        return gelGenRedstoneButtonY;
    }

    public int getGelGenAutoBalanceX() {
        return gelGenAutoBalanceX;
    }

    public int getGelGenAutoBalanceY() {
        return gelGenAutoBalanceY;
    }

    // --- Item sender button getters ---

    public int getItemSenderAllowlistX() {
        return itemSenderAllowlistX;
    }

    public int getItemSenderAllowlistY() {
        return itemSenderAllowlistY;
    }

    public int getItemSenderCompareNBTX() {
        return itemSenderCompareNBTX;
    }

    public int getItemSenderCompareNBTY() {
        return itemSenderCompareNBTY;
    }

    public int getItemSenderRedstoneX() {
        return itemSenderRedstoneX;
    }

    public int getItemSenderRedstoneY() {
        return itemSenderRedstoneY;
    }

    public int getItemSenderRenderAreaX() {
        return itemSenderRenderAreaX;
    }

    public int getItemSenderRenderAreaY() {
        return itemSenderRenderAreaY;
    }

    public int getItemSenderSpeedButtonX() {
        return itemSenderSpeedButtonX;
    }

    public int getItemSenderSpeedButtonY() {
        return itemSenderSpeedButtonY;
    }

    // --- Item sender slot getters ---

    public int getItemSenderSlotStartX() {
        return itemSenderSlotStartX;
    }

    public int getItemSenderSlotStartY() {
        return itemSenderSlotStartY;
    }

    public int getItemSenderSlotSpacing() {
        return itemSenderSlotSpacing;
    }

    public int getItemSenderSlotCount() {
        return itemSenderSlotCount;
    }

    // --- Basic item sender button getters ---

    public int getBasicItemSenderAllowlistX() {
        return basicItemSenderAllowlistX;
    }

    public int getBasicItemSenderAllowlistY() {
        return basicItemSenderAllowlistY;
    }

    public int getBasicItemSenderCompareNBTX() {
        return basicItemSenderCompareNBTX;
    }

    public int getBasicItemSenderCompareNBTY() {
        return basicItemSenderCompareNBTY;
    }

    public int getBasicItemSenderRedstoneX() {
        return basicItemSenderRedstoneX;
    }

    public int getBasicItemSenderRedstoneY() {
        return basicItemSenderRedstoneY;
    }

    public int getBasicItemSenderRenderAreaX() {
        return basicItemSenderRenderAreaX;
    }

    public int getBasicItemSenderRenderAreaY() {
        return basicItemSenderRenderAreaY;
    }

    public int getBasicItemSenderSpeedButtonX() {
        return basicItemSenderSpeedButtonX;
    }

    public int getBasicItemSenderSpeedButtonY() {
        return basicItemSenderSpeedButtonY;
    }

    // --- Basic item sender slot getters ---

    public int getBasicItemSenderSlotStartX() {
        return basicItemSenderSlotStartX;
    }

    public int getBasicItemSenderSlotStartY() {
        return basicItemSenderSlotStartY;
    }

    public int getBasicItemSenderSlotSpacing() {
        return basicItemSenderSlotSpacing;
    }

    public int getBasicItemSenderSlotCount() {
        return basicItemSenderSlotCount;
    }

    // --- Item receiver button getters ---

    public int getItemReceiverAllowlistX() {
        return itemReceiverAllowlistX;
    }

    public int getItemReceiverAllowlistY() {
        return itemReceiverAllowlistY;
    }

    public int getItemReceiverCompareNBTX() {
        return itemReceiverCompareNBTX;
    }

    public int getItemReceiverCompareNBTY() {
        return itemReceiverCompareNBTY;
    }

    public int getItemReceiverRedstoneX() {
        return itemReceiverRedstoneX;
    }

    public int getItemReceiverRedstoneY() {
        return itemReceiverRedstoneY;
    }

    public int getItemReceiverRenderAreaX() {
        return itemReceiverRenderAreaX;
    }

    public int getItemReceiverRenderAreaY() {
        return itemReceiverRenderAreaY;
    }

    public int getItemReceiverSpeedButtonX() {
        return itemReceiverSpeedButtonX;
    }

    public int getItemReceiverSpeedButtonY() {
        return itemReceiverSpeedButtonY;
    }

    // --- Item receiver slot getters ---

    public int getItemReceiverSlotStartX() {
        return itemReceiverSlotStartX;
    }

    public int getItemReceiverSlotStartY() {
        return itemReceiverSlotStartY;
    }

    public int getItemReceiverSlotSpacing() {
        return itemReceiverSlotSpacing;
    }

    public int getItemReceiverSlotCount() {
        return itemReceiverSlotCount;
    }

    // --- Basic item receiver button getters ---

    public int getBasicItemReceiverAllowlistX() {
        return basicItemReceiverAllowlistX;
    }

    public int getBasicItemReceiverAllowlistY() {
        return basicItemReceiverAllowlistY;
    }

    public int getBasicItemReceiverCompareNBTX() {
        return basicItemReceiverCompareNBTX;
    }

    public int getBasicItemReceiverCompareNBTY() {
        return basicItemReceiverCompareNBTY;
    }

    public int getBasicItemReceiverRedstoneX() {
        return basicItemReceiverRedstoneX;
    }

    public int getBasicItemReceiverRedstoneY() {
        return basicItemReceiverRedstoneY;
    }

    public int getBasicItemReceiverRenderAreaX() {
        return basicItemReceiverRenderAreaX;
    }

    public int getBasicItemReceiverRenderAreaY() {
        return basicItemReceiverRenderAreaY;
    }

    public int getBasicItemReceiverSpeedButtonX() {
        return basicItemReceiverSpeedButtonX;
    }

    public int getBasicItemReceiverSpeedButtonY() {
        return basicItemReceiverSpeedButtonY;
    }

    // --- Basic item receiver slot getters ---

    public int getBasicItemReceiverSlotStartX() {
        return basicItemReceiverSlotStartX;
    }

    public int getBasicItemReceiverSlotStartY() {
        return basicItemReceiverSlotStartY;
    }

    public int getBasicItemReceiverSlotSpacing() {
        return basicItemReceiverSlotSpacing;
    }

    public int getBasicItemReceiverSlotCount() {
        return basicItemReceiverSlotCount;
    }

    // --- Life extractor mode button getters ---

    public int getLifeExtractorModeButtonX() {
        return lifeExtractorModeButtonX;
    }

    public int getLifeExtractorModeButtonY() {
        return lifeExtractorModeButtonY;
    }

    // --- Bio crusher mode button getters ---

    public int getBioCrusherModeButtonX() {
        return bioCrusherModeButtonX;
    }

    public int getBioCrusherModeButtonY() {
        return bioCrusherModeButtonY;
    }
}
