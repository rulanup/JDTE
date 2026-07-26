package com.jdte.common.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads GUI layout configuration from assets/jdte/gui_layout.json.
 * Provides soft-coded positions for upgrade panels.
 * Falls back to hardcoded defaults if the JSON file cannot be loaded.
 */
public class GuiUpgradeLayoutConfig {
    private static final String CONFIG_RESOURCE = "/assets/jdte/gui_layout.json";
    private static GuiUpgradeLayoutConfig INSTANCE;

    private final Map<String, JsonObject> sections = new HashMap<>();

    private GuiUpgradeLayoutConfig(JsonObject json) {
        if (json != null) {
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    sections.put(entry.getKey(), entry.getValue().getAsJsonObject());
                }
            }
        }
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
        try (InputStream stream = GuiUpgradeLayoutConfig.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (stream != null) {
                try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    JsonObject json = new Gson().fromJson(reader, JsonObject.class);
                    if (json != null && json.has("upgrade_panel_4")) {
                        return new GuiUpgradeLayoutConfig(json);
                    }
                }
            }
        } catch (Exception e) {
            // Keep menus usable with built-in defaults if the packaged layout is unavailable or invalid.
        }
        return new GuiUpgradeLayoutConfig(null);
    }

    private int get(String section, String key, int defaultValue) {
        JsonObject obj = sections.get(section);
        return obj != null && obj.has(key) ? obj.get(key).getAsInt() : defaultValue;
    }

    // --- Right panel getters ---

    public int getFirstSlotX() { return get("upgrade_panel_4", "first_slot_x", 183); }
    public int getFirstSlotY() { return get("upgrade_panel_4", "first_slot_y", 85); }
    public int getSlotSpacing() { return get("upgrade_panel_4", "slot_spacing", 18); }
    public int getSlotSize() { return get("upgrade_panel_4", "slot_size", 18); }
    public int getColumns() { return get("upgrade_panel_4", "columns", 1); }
    public int getRows() { return get("upgrade_panel_4", "rows", 4); }
    public int getPanelPadding() { return get("upgrade_panel_4", "panel_padding", 8); }
    public int getPanelWidth() { return getColumns() * getSlotSize() + 2 * getPanelPadding(); }
    public int getPanelHeight() { return getRows() * getSlotSize() + 2 * getPanelPadding(); }

    // --- Left panel getters (only for 8-slot machines) ---

    public int getLeftFirstSlotX() { return get("upgrade_panel_8_left", "first_slot_x", -22); }
    public int getLeftFirstSlotY() { return get("upgrade_panel_8_left", "first_slot_y", getFirstSlotY()); }

    // --- Gel generator machine slot getters ---

    public int getGelGenGelX() { return get("gel_generator_slots", "gel_slot_x", 10); }
    public int getGelGenGelY() { return get("gel_generator_slots", "gel_slot_y", -12); }
    public int getGelGenFoodX() { return get("gel_generator_slots", "food_slot_x", 10); }
    public int getGelGenFoodY() { return get("gel_generator_slots", "food_slot_y", 24); }
    public int getGelGenInputStartX() { return get("gel_generator_slots", "input_start_x", 44); }
    public int getGelGenInputStartY() { return get("gel_generator_slots", "input_start_y", -21); }
    public int getGelGenInputSpacing() { return get("gel_generator_slots", "input_spacing", 18); }
    public int getGelGenInputCount() { return get("gel_generator_slots", "input_count", 4); }
    public int getGelGenInputFluidX() { return get("gel_generator_slots", "input_fluid_x", getGelGenInputStartX() + 18); }
    public int getGelGenInputFluidY() { return get("gel_generator_slots", "input_fluid_y", getGelGenInputStartY()); }
    public int getGelGenOutputStartX() { return get("gel_generator_slots", "output_start_x", 116); }
    public int getGelGenOutputStartY() { return get("gel_generator_slots", "output_start_y", -21); }
    public int getGelGenOutputSpacing() { return get("gel_generator_slots", "output_spacing", 18); }
    public int getGelGenOutputCount() { return get("gel_generator_slots", "output_count", 4); }
    public int getGelGenOutputFluidX() { return get("gel_generator_slots", "output_fluid_x", getGelGenOutputStartX() + 18); }
    public int getGelGenOutputFluidY() { return get("gel_generator_slots", "output_fluid_y", getGelGenOutputStartY()); }

    // --- Gel generator button getters ---

    public int getGelGenProgressArrowX() { return get("gel_generator_buttons", "progress_arrow_x", 88); }
    public int getGelGenProgressArrowY() { return get("gel_generator_buttons", "progress_arrow_y", 8); }
    public int getGelGenSpeedButtonX() { return get("gel_generator_buttons", "speed_button_x", 88); }
    public int getGelGenSpeedButtonY() { return get("gel_generator_buttons", "speed_button_y", 38); }
    public int getGelGenAllowlistButtonX() { return get("gel_generator_buttons", "allowlist_button_x", 170); }
    public int getGelGenAllowlistButtonY() { return get("gel_generator_buttons", "allowlist_button_y", -3); }
    public int getGelGenCompareNBTButtonX() { return get("gel_generator_buttons", "compare_nbt_button_x", 170); }
    public int getGelGenCompareNBTButtonY() { return get("gel_generator_buttons", "compare_nbt_button_y", 15); }
    public int getGelGenRedstoneButtonX() { return get("gel_generator_buttons", "redstone_button_x", 170); }
    public int getGelGenRedstoneButtonY() { return get("gel_generator_buttons", "redstone_button_y", 33); }
    public int getGelGenAutoBalanceX() { return get("gel_generator_buttons", "auto_balance_x", 170); }
    public int getGelGenAutoBalanceY() { return get("gel_generator_buttons", "auto_balance_y", -21); }

    // --- Item sender button getters ---

    public int getItemSenderAllowlistX() { return get("item_sender_buttons", "allowlist_button_x", 8); }
    public int getItemSenderAllowlistY() { return get("item_sender_buttons", "allowlist_button_y", 42); }
    public int getItemSenderCompareNBTX() { return get("item_sender_buttons", "compare_nbt_button_x", 26); }
    public int getItemSenderCompareNBTY() { return get("item_sender_buttons", "compare_nbt_button_y", 42); }
    public int getItemSenderRedstoneX() { return get("item_sender_buttons", "redstone_button_x", 134); }
    public int getItemSenderRedstoneY() { return get("item_sender_buttons", "redstone_button_y", 42); }
    public int getItemSenderRenderAreaX() { return get("item_sender_buttons", "render_area_button_x", 152); }
    public int getItemSenderRenderAreaY() { return get("item_sender_buttons", "render_area_button_y", 42); }
    public int getItemSenderSpeedButtonX() { return get("item_sender_buttons", "speed_button_x", 78); }
    public int getItemSenderSpeedButtonY() { return get("item_sender_buttons", "speed_button_y", 44); }

    // --- Item sender slot getters ---

    public int getItemSenderSlotStartX() { return get("item_sender_slots", "machine_start_x", 8); }
    public int getItemSenderSlotStartY() { return get("item_sender_slots", "machine_start_y", 36); }
    public int getItemSenderSlotSpacing() { return get("item_sender_slots", "machine_spacing", 18); }
    public int getItemSenderSlotCount() { return get("item_sender_slots", "machine_count", 9); }

    // --- Basic item sender button getters ---

    public int getBasicItemSenderAllowlistX() { return get("basic_item_sender_buttons", "allowlist_button_x", 8); }
    public int getBasicItemSenderAllowlistY() { return get("basic_item_sender_buttons", "allowlist_button_y", 42); }
    public int getBasicItemSenderCompareNBTX() { return get("basic_item_sender_buttons", "compare_nbt_button_x", 26); }
    public int getBasicItemSenderCompareNBTY() { return get("basic_item_sender_buttons", "compare_nbt_button_y", 42); }
    public int getBasicItemSenderRedstoneX() { return get("basic_item_sender_buttons", "redstone_button_x", 134); }
    public int getBasicItemSenderRedstoneY() { return get("basic_item_sender_buttons", "redstone_button_y", 42); }
    public int getBasicItemSenderRenderAreaX() { return get("basic_item_sender_buttons", "render_area_button_x", 152); }
    public int getBasicItemSenderRenderAreaY() { return get("basic_item_sender_buttons", "render_area_button_y", 42); }
    public int getBasicItemSenderSpeedButtonX() { return get("basic_item_sender_buttons", "speed_button_x", 78); }
    public int getBasicItemSenderSpeedButtonY() { return get("basic_item_sender_buttons", "speed_button_y", 44); }

    // --- Basic item sender slot getters ---

    public int getBasicItemSenderSlotStartX() { return get("basic_item_sender_slots", "machine_start_x", 8); }
    public int getBasicItemSenderSlotStartY() { return get("basic_item_sender_slots", "machine_start_y", 36); }
    public int getBasicItemSenderSlotSpacing() { return get("basic_item_sender_slots", "machine_spacing", 18); }
    public int getBasicItemSenderSlotCount() { return get("basic_item_sender_slots", "machine_count", 9); }

    // --- Item receiver button getters ---

    public int getItemReceiverAllowlistX() { return get("item_receiver_buttons", "allowlist_button_x", 8); }
    public int getItemReceiverAllowlistY() { return get("item_receiver_buttons", "allowlist_button_y", 42); }
    public int getItemReceiverCompareNBTX() { return get("item_receiver_buttons", "compare_nbt_button_x", 26); }
    public int getItemReceiverCompareNBTY() { return get("item_receiver_buttons", "compare_nbt_button_y", 42); }
    public int getItemReceiverRedstoneX() { return get("item_receiver_buttons", "redstone_button_x", 134); }
    public int getItemReceiverRedstoneY() { return get("item_receiver_buttons", "redstone_button_y", 42); }
    public int getItemReceiverRenderAreaX() { return get("item_receiver_buttons", "render_area_button_x", 152); }
    public int getItemReceiverRenderAreaY() { return get("item_receiver_buttons", "render_area_button_y", 42); }
    public int getItemReceiverSpeedButtonX() { return get("item_receiver_buttons", "speed_button_x", 78); }
    public int getItemReceiverSpeedButtonY() { return get("item_receiver_buttons", "speed_button_y", 44); }

    // --- Item receiver slot getters ---

    public int getItemReceiverSlotStartX() { return get("item_receiver_slots", "machine_start_x", 8); }
    public int getItemReceiverSlotStartY() { return get("item_receiver_slots", "machine_start_y", 36); }
    public int getItemReceiverSlotSpacing() { return get("item_receiver_slots", "machine_spacing", 18); }
    public int getItemReceiverSlotCount() { return get("item_receiver_slots", "machine_count", 9); }

    // --- Basic item receiver button getters ---

    public int getBasicItemReceiverAllowlistX() { return get("basic_item_receiver_buttons", "allowlist_button_x", 8); }
    public int getBasicItemReceiverAllowlistY() { return get("basic_item_receiver_buttons", "allowlist_button_y", 42); }
    public int getBasicItemReceiverCompareNBTX() { return get("basic_item_receiver_buttons", "compare_nbt_button_x", 26); }
    public int getBasicItemReceiverCompareNBTY() { return get("basic_item_receiver_buttons", "compare_nbt_button_y", 42); }
    public int getBasicItemReceiverRedstoneX() { return get("basic_item_receiver_buttons", "redstone_button_x", 134); }
    public int getBasicItemReceiverRedstoneY() { return get("basic_item_receiver_buttons", "redstone_button_y", 42); }
    public int getBasicItemReceiverRenderAreaX() { return get("basic_item_receiver_buttons", "render_area_button_x", 152); }
    public int getBasicItemReceiverRenderAreaY() { return get("basic_item_receiver_buttons", "render_area_button_y", 42); }
    public int getBasicItemReceiverSpeedButtonX() { return get("basic_item_receiver_buttons", "speed_button_x", 78); }
    public int getBasicItemReceiverSpeedButtonY() { return get("basic_item_receiver_buttons", "speed_button_y", 44); }

    // --- Basic item receiver slot getters ---

    public int getBasicItemReceiverSlotStartX() { return get("basic_item_receiver_slots", "machine_start_x", 8); }
    public int getBasicItemReceiverSlotStartY() { return get("basic_item_receiver_slots", "machine_start_y", 36); }
    public int getBasicItemReceiverSlotSpacing() { return get("basic_item_receiver_slots", "machine_spacing", 18); }
    public int getBasicItemReceiverSlotCount() { return get("basic_item_receiver_slots", "machine_count", 9); }

    // --- Life extractor mode button getters ---

    public int getLifeExtractorModeButtonX() { return get("life_extractor_buttons", "mode_button_x", 80); }
    public int getLifeExtractorModeButtonY() { return get("life_extractor_buttons", "mode_button_y", 62); }

    // --- Bio crusher mode button getters ---

    public int getBioCrusherModeButtonX() { return get("bio_crusher_buttons", "mode_button_x", 80); }
    public int getBioCrusherModeButtonY() { return get("bio_crusher_buttons", "mode_button_y", 36); }
    public int getBioCrusherSharpnessSlotX() { return get("bio_crusher_buttons", "sharpness_slot_x", getBioCrusherModeButtonX() - 20); }
    public int getBioCrusherSharpnessSlotY() { return get("bio_crusher_buttons", "sharpness_slot_y", getBioCrusherModeButtonY()); }
    public int getBioCrusherLootingSlotX() { return get("bio_crusher_buttons", "looting_slot_x", getBioCrusherModeButtonX() + 18); }
    public int getBioCrusherLootingSlotY() { return get("bio_crusher_buttons", "looting_slot_y", getBioCrusherModeButtonY()); }

    // --- Advanced potion brewer layout getters ---

    public int getPotionBrewerBgSrcX() { return get("advanced_potion_brewer_background", "src_x", 15); }
    public int getPotionBrewerBgSrcY() { return get("advanced_potion_brewer_background", "src_y", 15); }
    public int getPotionBrewerBgX() { return get("advanced_potion_brewer_background", "x", 15); }
    public int getPotionBrewerBgY() { return get("advanced_potion_brewer_background", "y", 5); }
    public int getPotionBrewerBgWidth() { return get("advanced_potion_brewer_background", "width", 107); }
    public int getPotionBrewerBgHeight() { return get("advanced_potion_brewer_background", "height", 63); }
    public int getPotionBrewerFuelSlotX() { return get("advanced_potion_brewer_slots", "fuel_slot_x", 17); }
    public int getPotionBrewerFuelSlotY() { return get("advanced_potion_brewer_slots", "fuel_slot_y", 7); }
    public int getPotionBrewerIngredientSlotX() { return get("advanced_potion_brewer_slots", "ingredient_slot_x", 79); }
    public int getPotionBrewerIngredientSlotY() { return get("advanced_potion_brewer_slots", "ingredient_slot_y", 7); }
    public int getPotionBrewerBottleSlot0X() { return get("advanced_potion_brewer_slots", "bottle_slot_0_x", 56); }
    public int getPotionBrewerBottleSlot0Y() { return get("advanced_potion_brewer_slots", "bottle_slot_0_y", 41); }
    public int getPotionBrewerBottleSlot1X() { return get("advanced_potion_brewer_slots", "bottle_slot_1_x", 79); }
    public int getPotionBrewerBottleSlot1Y() { return get("advanced_potion_brewer_slots", "bottle_slot_1_y", 48); }
    public int getPotionBrewerBottleSlot2X() { return get("advanced_potion_brewer_slots", "bottle_slot_2_x", 102); }
    public int getPotionBrewerBottleSlot2Y() { return get("advanced_potion_brewer_slots", "bottle_slot_2_y", 41); }
    public int getPotionBrewerExtraIngredientStartX() { return get("advanced_potion_brewer_slots", "extra_ingredient_start_x", 43); }
    public int getPotionBrewerExtraIngredientStartY() { return get("advanced_potion_brewer_slots", "extra_ingredient_start_y", -21); }
    public int getPotionBrewerExtraIngredientSpacing() { return get("advanced_potion_brewer_slots", "extra_ingredient_spacing", 18); }
    public int getPotionBrewerExtraIngredientCount() { return get("advanced_potion_brewer_slots", "extra_ingredient_count", 5); }
    public int getPotionBrewerOutputStartX() { return get("advanced_potion_brewer_slots", "output_start_x", 128); }
    public int getPotionBrewerOutputStartY() { return get("advanced_potion_brewer_slots", "output_start_y", 13); }
    public int getPotionBrewerOutputSpacing() { return get("advanced_potion_brewer_slots", "output_spacing", 18); }
    public int getPotionBrewerOutputCount() { return get("advanced_potion_brewer_slots", "output_count", 3); }
    public int getPotionBrewerSpeedButtonX() { return get("advanced_potion_brewer_buttons", "speed_button_x", 148); }
    public int getPotionBrewerSpeedButtonY() { return get("advanced_potion_brewer_buttons", "speed_button_y", 49); }
    public int getPotionBrewerRedstoneButtonX() { return get("advanced_potion_brewer_buttons", "redstone_button_x", 150); }
    public int getPotionBrewerRedstoneButtonY() { return get("advanced_potion_brewer_buttons", "redstone_button_y", 31); }
    public int getPotionBrewerRecipeLockButtonX() { return get("advanced_potion_brewer_buttons", "recipe_lock_button_x", getPotionBrewerRedstoneButtonX()); }
    public int getPotionBrewerRecipeLockButtonY() { return get("advanced_potion_brewer_buttons", "recipe_lock_button_y", getPotionBrewerRedstoneButtonY() - 18); }
    public int getPotionBrewerFuelInputButtonX() { return get("advanced_potion_brewer_buttons", "fuel_input_button_x", getPotionBrewerRecipeLockButtonX()); }
    public int getPotionBrewerFuelInputButtonY() { return get("advanced_potion_brewer_buttons", "fuel_input_button_y", getPotionBrewerRecipeLockButtonY() - 18); }
    public int getPotionBrewerFuelBarX() { return get("advanced_potion_brewer_widgets", "fuel_bar_x", 60); }
    public int getPotionBrewerFuelBarBottomY() { return get("advanced_potion_brewer_widgets", "fuel_bar_bottom_y", 38); }
    public int getPotionBrewerBubblesX() { return get("advanced_potion_brewer_widgets", "bubbles_x", 63); }
    public int getPotionBrewerBubblesBottomY() { return get("advanced_potion_brewer_widgets", "bubbles_bottom_y", 33); }
    public int getPotionBrewerArrowX() { return get("advanced_potion_brewer_widgets", "arrow_x", 97); }
    public int getPotionBrewerArrowBottomY() { return get("advanced_potion_brewer_widgets", "arrow_bottom_y", 34); }
    public int getPotionBrewerWaterFluidX() { return get("advanced_potion_brewer_fluids", "water_fluid_x", -6); }
    public int getPotionBrewerWaterFluidY() { return get("advanced_potion_brewer_fluids", "water_fluid_y", -21); }
    public int getPotionBrewerTimeFluidX() { return get("advanced_potion_brewer_fluids", "time_fluid_x", 174); }
    public int getPotionBrewerTimeFluidY() { return get("advanced_potion_brewer_fluids", "time_fluid_y", -21); }

    // --- Loot fabricator getters ---

    public int getLootFabricatorExtraWidth() { return get("loot_fabricator_layout", "extra_width", 60); }
    public int getLootFabricatorExtraHeight() { return get("loot_fabricator_layout", "extra_height", 0); }
    public int getLootFabricatorInputStartX() { return get("loot_fabricator_slots", "input_start_x", 8); }
    public int getLootFabricatorInputStartY() { return get("loot_fabricator_slots", "input_start_y", -21); }
    public int getLootFabricatorInputSpacing() { return get("loot_fabricator_slots", "input_spacing", 18); }
    public int getLootFabricatorOutputStartX() { return get("loot_fabricator_slots", "output_start_x", 68); }
    public int getLootFabricatorOutputStartY() { return get("loot_fabricator_slots", "output_start_y", -21); }
    public int getLootFabricatorOutputSpacing() { return get("loot_fabricator_slots", "output_spacing", 18); }
    public int getLootFabricatorOutputColumns() { return get("loot_fabricator_slots", "output_columns", 4); }
    public int getLootFabricatorOutputRows() { return get("loot_fabricator_slots", "output_rows", 4); }
    public int getLootFabricatorLifeFluidX() { return get("loot_fabricator_fluids", "life_fluid_x", 162); }
    public int getLootFabricatorLifeFluidY() { return get("loot_fabricator_fluids", "life_fluid_y", -21); }
    public int getLootFabricatorTimeFluidX() { return get("loot_fabricator_fluids", "time_fluid_x", 182); }
    public int getLootFabricatorTimeFluidY() { return get("loot_fabricator_fluids", "time_fluid_y", -21); }
    public int getLootFabricatorProgressArrowX() { return get("loot_fabricator_widgets", "progress_arrow_x", 36); }
    public int getLootFabricatorProgressArrowY() { return get("loot_fabricator_widgets", "progress_arrow_y", 7); }
    public int getLootFabricatorSpeedButtonX() { return get("loot_fabricator_widgets", "speed_button_x", 36); }
    public int getLootFabricatorSpeedButtonY() { return get("loot_fabricator_widgets", "speed_button_y", 24); }
    public int getLootFabricatorRedstoneButtonX() { return get("loot_fabricator_widgets", "redstone_button_x", 142); }
    public int getLootFabricatorRedstoneButtonY() { return get("loot_fabricator_widgets", "redstone_button_y", 17); }
    public int getLootFabricatorOutputPrevX() { return get("loot_fabricator_widgets", "output_prev_x", 54); }
    public int getLootFabricatorOutputPrevY() { return get("loot_fabricator_widgets", "output_prev_y", 36); }
    public int getLootFabricatorOutputNextX() { return get("loot_fabricator_widgets", "output_next_x", 142); }
    public int getLootFabricatorOutputNextY() { return get("loot_fabricator_widgets", "output_next_y", 36); }
    public int getLootFabricatorOutputPageButtonSize() { return get("loot_fabricator_widgets", "output_page_button_size", 12); }
    public int getLootFabricatorOutputPageTextX() { return get("loot_fabricator_widgets", "output_page_text_x", 104); }
    public int getLootFabricatorOutputPageTextY() { return get("loot_fabricator_widgets", "output_page_text_y", 54); }
}
