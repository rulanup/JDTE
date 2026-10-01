package com.jdte.common.items;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jdte.common.entities.EntityAccelerationData;
import com.jdte.common.manager.EntityAccelerationManager;
import com.jdte.common.recipes.EntityAccelerationSmithingRecipe;
import com.jdte.common.recipes.UltimateTimeWandUpgradeRecipe;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import com.direwolf20.justdirethings.setup.Registration;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityAccelerationUpgradeTest {

    @Test
    void resourcesAndContractsHonored() throws IOException {
        // 1. Crafting Recipe
        JsonObject craft = read("src/main/resources/data/jdte/recipe/entity_acceleration_upgrade.json");
        assertEquals("minecraft:crafting_shaped", craft.get("type").getAsString());
        assertEquals("jdte:entity_acceleration_upgrade", craft.getAsJsonObject("result").get("id").getAsString());

        // 2. Smithing Recipe
        JsonObject smith = read("src/main/resources/data/jdte/recipe/entity_acceleration_upgrade_apply.json");
        assertEquals("jdte:entity_acceleration_smithing", smith.get("type").getAsString());

        // 3. Crafting Upgrade Recipe
        JsonObject craftUpgrade = read("src/main/resources/data/jdte/recipe/ultimate_time_wand_entity_upgrade.json");
        assertEquals("jdte:ultimate_time_wand_entity_upgrade", craftUpgrade.get("type").getAsString());

        // 4. Model and Texture
        Path modelPath = source("src/main/resources/assets/jdte/models/item/entity_acceleration_upgrade.json");
        assertTrue(Files.isRegularFile(modelPath), "Model file missing");
        Path texturePath = source("src/main/resources/assets/jdte/textures/item/entity_acceleration_upgrade.png");
        assertTrue(Files.isRegularFile(texturePath), "Texture file missing");
        assertEquals(16, ImageIO.read(texturePath.toFile()).getWidth());
        assertEquals(16, ImageIO.read(texturePath.toFile()).getHeight());

        // 5. Tags
        JsonObject tags = read("src/main/resources/data/jdte/tags/item/upgrades.json");
        assertTrue(tags.getAsJsonArray("values").toString().contains("jdte:entity_acceleration_upgrade"));

        // 6. Translations
        for (String locale : List.of("en_us", "zh_cn")) {
            JsonObject lang = read("src/main/resources/assets/jdte/lang/" + locale + ".json");
            assertTrue(lang.has("item.jdte.entity_acceleration_upgrade"), locale + " missing item name");
            assertTrue(lang.has("tooltip.jdte.entity_acceleration_upgrade"), locale + " missing tooltip");
            assertTrue(lang.has("tooltip.jdte.ultimate_time_wand.entity_acceleration_installed"), locale + " missing installed tooltip");
            assertTrue(lang.has("message.jdte.ultimate_time_wand.requires_entity_acceleration"), locale + " missing message");

            String guide = Files.readString(source("src/main/resources/assets/jdte/guides/jdte/guide/" +
                    (locale.equals("en_us") ? "_en_us/" : "") + "upgrades.md"));
            assertTrue(guide.contains("entity_acceleration_upgrade"), locale + " guide missing item reference");
        }

        // 7. Creative Tab
        String creativeTab = Files.readString(source("src/main/java/com/jdte/setup/JDTECreativeTabs.java"));
        assertTrue(creativeTab.contains("JDTEItems.ENTITY_ACCELERATION_UPGRADE.get()"));
    }

    @Test
    void upgradeStorageAllowsEntityAccelerationUpgrade() {
        ItemStack upgradeStack = new ItemStack(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get());
        assertTrue(UpgradeStorageItem.isAllowedUpgrade(upgradeStack));
    }

    @Test
    void wandComponentStateHelpers() {
        ItemStack wand = new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());
        assertFalse(UltimateTimeWandItem.hasEntityAcceleration(wand));

        UltimateTimeWandItem.setEntityAcceleration(wand, true);
        assertTrue(UltimateTimeWandItem.hasEntityAcceleration(wand));

        UltimateTimeWandItem.setEntityAcceleration(wand, false);
        assertFalse(UltimateTimeWandItem.hasEntityAcceleration(wand));
    }

    @Test
    void dragAndDropInstallationOnlyAllowsUltimateTimeWandWithoutUpgrade() {
        EntityAccelerationUpgradeItem upgradeItem = (EntityAccelerationUpgradeItem) JDTEItems.ENTITY_ACCELERATION_UPGRADE.get();
        UltimateTimeWandItem wandItem = (UltimateTimeWandItem) JDTEItems.ULTIMATE_TIME_WAND.get();

        ItemStack upgradeStack = new ItemStack(upgradeItem, 2);
        ItemStack wandStack = new ItemStack(wandItem);
        ItemStack basicWandStack = new ItemStack(Registration.TimeWand.get());
        ItemStack otherStack = new ItemStack(Items.DIAMOND);

        // 1. Dragging upgrade onto un-upgraded ultimate wand -> succeeds
        assertTrue(upgradeItem.overrideStackedOnOther(upgradeStack, new DummySlot(wandStack), ClickAction.SECONDARY, null));
        assertTrue(UltimateTimeWandItem.hasEntityAcceleration(wandStack));
        assertEquals(1, upgradeStack.getCount());

        // 2. Dragging upgrade onto already-upgraded ultimate wand -> rejects
        assertFalse(upgradeItem.overrideStackedOnOther(upgradeStack, new DummySlot(wandStack), ClickAction.SECONDARY, null));
        assertEquals(1, upgradeStack.getCount());

        // 3. Dragging upgrade onto basic Time Wand -> strictly rejects!
        assertFalse(upgradeItem.overrideStackedOnOther(upgradeStack, new DummySlot(basicWandStack), ClickAction.SECONDARY, null));
        assertEquals(1, upgradeStack.getCount());

        // 4. Dragging upgrade onto random item -> rejects
        assertFalse(upgradeItem.overrideStackedOnOther(upgradeStack, new DummySlot(otherStack), ClickAction.SECONDARY, null));
        assertEquals(1, upgradeStack.getCount());

        // 5. Reverse: dragging upgrade onto wand in slot via overrideOtherStackedOnMe
        ItemStack freshWand = new ItemStack(wandItem);
        ItemStack singleUpgrade = new ItemStack(upgradeItem, 1);
        assertTrue(wandItem.overrideOtherStackedOnMe(freshWand, singleUpgrade, new DummySlot(freshWand), ClickAction.SECONDARY, null, net.minecraft.world.entity.SlotAccess.NULL));
        assertTrue(UltimateTimeWandItem.hasEntityAcceleration(freshWand));
        assertEquals(0, singleUpgrade.getCount());

        // Reverse on already upgraded -> rejects
        ItemStack anotherUpgrade = new ItemStack(upgradeItem, 1);
        assertFalse(wandItem.overrideOtherStackedOnMe(freshWand, anotherUpgrade, new DummySlot(freshWand), ClickAction.SECONDARY, null, net.minecraft.world.entity.SlotAccess.NULL));
        assertEquals(1, anotherUpgrade.getCount());
    }

    @Test
    void smithingRecipeMatchesOnlyUltimateTimeWandWithoutUpgrade() {
        EntityAccelerationSmithingRecipe recipe = new EntityAccelerationSmithingRecipe();

        ItemStack template = new ItemStack(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get());
        ItemStack baseUnupgraded = new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());
        ItemStack baseUpgraded = new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());
        UltimateTimeWandItem.setEntityAcceleration(baseUpgraded, true);
        ItemStack baseBasicWand = new ItemStack(Registration.TimeWand.get());
        ItemStack dummyAddition = ItemStack.EMPTY;

        // Valid input
        SmithingRecipeInput validInput = new SmithingRecipeInput(template, baseUnupgraded, dummyAddition);
        assertTrue(recipe.matches(validInput, null));

        ItemStack result = recipe.assemble(validInput, null);
        assertEquals(JDTEItems.ULTIMATE_TIME_WAND.get(), result.getItem());
        assertTrue(UltimateTimeWandItem.hasEntityAcceleration(result));

        // Already upgraded ultimate wand -> false
        assertFalse(recipe.matches(new SmithingRecipeInput(template, baseUpgraded, dummyAddition), null));

        // Basic Time Wand -> strictly false
        assertFalse(recipe.matches(new SmithingRecipeInput(template, baseBasicWand, dummyAddition), null));

        // Invalid template -> false
        assertFalse(recipe.matches(new SmithingRecipeInput(new ItemStack(Items.STICK), baseUnupgraded, dummyAddition), null));
    }

    @Test
    void craftingRecipeMatchesOnlyUltimateTimeWandWithoutUpgrade() {
        UltimateTimeWandUpgradeRecipe recipe = new UltimateTimeWandUpgradeRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);

        ItemStack upgrade = new ItemStack(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get());
        ItemStack baseUnupgraded = new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());
        ItemStack baseUpgraded = new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());
        UltimateTimeWandItem.setEntityAcceleration(baseUpgraded, true);
        ItemStack baseBasicWand = new ItemStack(Registration.TimeWand.get());

        // Grid with upgrade + unupgraded wand -> true
        CraftingInput validInput = CraftingInput.of(2, 1, List.of(upgrade, baseUnupgraded));
        assertTrue(recipe.matches(validInput, null));

        ItemStack result = recipe.assemble(validInput, null);
        assertEquals(JDTEItems.ULTIMATE_TIME_WAND.get(), result.getItem());
        assertTrue(UltimateTimeWandItem.hasEntityAcceleration(result));

        // Grid with upgrade + already upgraded wand -> false
        CraftingInput alreadyUpgradedInput = CraftingInput.of(2, 1, List.of(upgrade, baseUpgraded));
        assertFalse(recipe.matches(alreadyUpgradedInput, null));

        // Grid with upgrade + basic time wand -> strictly false!
        CraftingInput basicWandInput = CraftingInput.of(2, 1, List.of(upgrade, baseBasicWand));
        assertFalse(recipe.matches(basicWandInput, null));

        // Grid with multiple wands or multiple upgrades -> false
        CraftingInput multipleInput = CraftingInput.of(3, 1, List.of(upgrade, baseUnupgraded, upgrade));
        assertFalse(recipe.matches(multipleInput, null));
    }

    @Test
    void entityAccelerationDataCodecAndMethods() {
        EntityAccelerationData data = new EntityAccelerationData(4, 600, 300);
        assertEquals(4, data.getExponent());
        assertEquals(600, data.getTotalTime());
        assertEquals(300, data.getRemainingTime());
        assertTrue(data.isActive());

        data.decrementRemainingTime();
        assertEquals(299, data.getRemainingTime());
        assertEquals(4, data.getExponent());
    }

    @Test
    void aiSuppressionDefaultAndSafety() {
        assertFalse(EntityAccelerationManager.isSuppressingAi());
    }

    private static class DummySlot extends Slot {
        private ItemStack stack;

        public DummySlot(ItemStack initial) {
            super(new net.minecraft.world.SimpleContainer(1), 0, 0, 0);
            this.stack = initial;
        }

        @Override
        public ItemStack getItem() {
            return stack;
        }

        @Override
        public void set(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public boolean hasItem() {
            return !stack.isEmpty();
        }

        @Override
        public void setChanged() {
        }
    }

    private static JsonObject read(String path) throws IOException {
        try (Reader reader = Files.newBufferedReader(source(path), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static Path source(String path) {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve(path);
            if (Files.isRegularFile(candidate)) return candidate;
            current = current.getParent();
        }
        return Path.of(path);
    }
}
