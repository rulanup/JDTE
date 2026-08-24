package com.jdte.setup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jdte.common.items.UltimateTimeWandItem;
import com.direwolf20.justdirethings.setup.Registration;
import com.direwolf20.justdirethings.util.MagicHelpers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UltimateTimeWandConfigLanguageContractTest {
    private static final String LANGUAGE_ROOT = "src/main/resources/assets/jdte/lang";
    private static final String ITEM_ID = "jdte:ultimate_time_wand";
    private static final String RECIPE_ID = "jdte:ultimate_time_wand";
    private static final Set<String> ALLOWED_INGREDIENT_NAMESPACES = Set.of("jdte", "justdirethings", "minecraft");
    private static final Set<String> REQUIRED_INGREDIENTS = Set.of(
            "justdirethings:eclipsealloy_paxel",
            "jdte:big_fluid_tank",
            "jdte:time_fluid_catalyst",
            "jdte:ae_acceleration_upgrade");
    private static final Path ENGLISH = sourceLanguage("en_us.json");
    private static final Path CHINESE = sourceLanguage("zh_cn.json");

    private static final List<String> REQUIRED_KEYS = List.of(
            "entity.jdte.ultimate_time_wand",
            "item.jdte.ultimate_time_wand",
            "tooltip.jdte.ultimate_time_wand.mode.normal",
            "tooltip.jdte.ultimate_time_wand.mode.x2",
            "tooltip.jdte.ultimate_time_wand.mode.x4",
            "tooltip.jdte.ultimate_time_wand.mode.max",
            "tooltip.jdte.ultimate_time_wand.fluid",
            "tooltip.jdte.ultimate_time_wand.energy",
            "message.jdte.ultimate_time_wand.mode",
            "message.jdte.ultimate_time_wand.switch",
            "message.jdte.ultimate_time_wand.insufficient_resources",
            "message.jdte.ultimate_time_wand.invalid_target",
            "message.jdte.ultimate_time_wand.max_multiplier",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandFluidCapacity",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandEnergyCapacity",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandDuration",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandBaseCostMultiplier",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandEnergyCostMultiplier",
            "config.jdte.jdte.timeAccelerator.ultimateTimeWandFractionalFluidSettlement");

    @Test
    void englishAndChineseContainEveryUltimateTimeWandLanguageKey() throws IOException {
        assertTrue(Files.isRegularFile(ENGLISH), "Missing English language file: " + ENGLISH);
        assertTrue(Files.isRegularFile(CHINESE), "Missing Chinese language file: " + CHINESE);

        JsonObject english = readJson(ENGLISH);
        JsonObject chinese = readJson(CHINESE);
        assertEquals(19, REQUIRED_KEYS.size(), "The contract must cover exactly 19 keys");

        for (String key : REQUIRED_KEYS) {
            assertTrue(english.has(key), () -> "Missing en_us key: " + key);
            assertTrue(chinese.has(key), () -> "Missing zh_cn key: " + key);
            assertFalse(english.get(key).getAsString().isBlank(), () -> "Blank en_us key: " + key);
            assertFalse(chinese.get(key).getAsString().isBlank(), () -> "Blank zh_cn key: " + key);
        }
    }

    @Test
    void itemAndRecipeUseJdteNamespaceWithoutDynaReferences() throws IOException {
        Path recipe = sourcePath("src/main/resources/data/jdte/recipe/ultimate_time_wand.json");
        assertTrue(Files.exists(recipe), "Missing Ultimate Time Wand recipe: " + recipe);
        String recipeSource = Files.readString(recipe);
        JsonObject recipeJson = JsonParser.parseString(recipeSource).getAsJsonObject();
        assertEquals(RECIPE_ID, recipeJson.getAsJsonObject("result").get("id").getAsString());

        JsonArray ingredients = recipeJson.getAsJsonArray("ingredients");
        assertEquals(4, ingredients.size(), "Ultimate Time Wand recipe must have exactly four ingredients");
        Set<String> ingredientIds = new HashSet<>();
        for (JsonElement ingredient : ingredients) {
            String itemId = ingredient.getAsJsonObject().get("item").getAsString();
            ingredientIds.add(itemId);
            int namespaceSeparator = itemId.indexOf(':');
            assertTrue(namespaceSeparator > 0, () -> "Ingredient must be namespaced: " + itemId);
            assertTrue(ALLOWED_INGREDIENT_NAMESPACES.contains(itemId.substring(0, namespaceSeparator)),
                    () -> "Unexpected ingredient namespace: " + itemId);
        }
        assertEquals(REQUIRED_INGREDIENTS, ingredientIds);

        String source = Files.readString(sourcePath("src/main/java/com/jdte/common/items/UltimateTimeWandItem.java"));
        assertContainsNoDynaReferences(recipeSource);
        assertContainsNoDynaReferences(source);
    }

    @Test
    void guideMeAndPatchouliResourcesDescribeAndRegisterUltimateTimeWand() throws IOException {
        assertGuideContract("guide/ultimate-time-wand.md");
        assertGuideContract("guide/_en_us/ultimate-time-wand.md");
        assertPatchouliContract("zh_cn");
        assertPatchouliContract("en_us");
    }

    @Test
    void tooltipMustShowCurrentFluidAndEnergyResourceValuesInOrder() {
        UltimateTimeWandItem wand = new UltimateTimeWandItem();
        ItemStack stack = new ItemStack(wand);
        IFluidHandlerItem fluid = stack.getCapability(Capabilities.FluidHandler.ITEM);
        assertTrue(fluid != null, "Ultimate Time Wand should expose a fluid capability");
        assertEquals(1_234, fluid.fill(new FluidStack(Registration.TIME_FLUID_SOURCE.get(), 1_234),
                IFluidHandler.FluidAction.EXECUTE));
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        assertTrue(energy != null, "Ultimate Time Wand should expose an energy capability");
        assertEquals(2_222, energy.receiveEnergy(2_222, false));

        List<Component> tooltip = new ArrayList<>();
        new UltimateTimeWandItem().appendHoverText(stack, null, tooltip, TooltipFlag.Default.NORMAL);
        assertEquals(2, tooltip.size());
        String fluidText = tooltip.get(0).getString();
        String energyText = tooltip.get(1).getString();
        String currentFluid = MagicHelpers.formatted(1_234);
        String maximumFluid = MagicHelpers.formatted(wand.getMaxMB());
        String currentEnergy = MagicHelpers.formatted(2_222);
        String maximumEnergy = MagicHelpers.formatted(wand.getMaxEnergy());
        assertTrue(fluidText.contains(currentFluid), () -> "Fluid tooltip must include the current value: " + fluidText);
        assertTrue(fluidText.contains(maximumFluid), () -> "Fluid tooltip must include the maximum value: " + fluidText);
        assertTrue(energyText.contains(currentEnergy), () -> "Energy tooltip must include the current value: " + energyText);
        assertTrue(energyText.contains(maximumEnergy), () -> "Energy tooltip must include the maximum value: " + energyText);
        assertTrue(fluidText.indexOf(currentFluid) < fluidText.indexOf(maximumFluid),
                () -> "Fluid tooltip must keep current before maximum: " + fluidText);
        assertTrue(energyText.indexOf(currentEnergy) < energyText.indexOf(maximumEnergy),
                () -> "Energy tooltip must keep current before maximum: " + energyText);
        assertEquals("tooltip.jdte.ultimate_time_wand.fluid",
                ((TranslatableContents) tooltip.get(0).getContents()).getKey());
        assertEquals("tooltip.jdte.ultimate_time_wand.energy",
                ((TranslatableContents) tooltip.get(1).getContents()).getKey());
    }

    private static void assertContainsNoDynaReferences(String source) {
        String normalized = source.toLowerCase(Locale.ROOT);
        assertFalse(normalized.contains("dyna"), "Dyna references are forbidden");
        assertFalse(normalized.contains("justdynthings"), "Just Dyna Things references are forbidden");
        assertFalse(normalized.contains("justdynathings"), "Just Dynathings references are forbidden");
    }

    private static void assertGuideContract(String relativeGuidePath) throws IOException {
        Path guide = sourcePath("src/main/resources/assets/jdte/guides/jdte/" + relativeGuidePath);
        assertTrue(Files.isRegularFile(guide), "Missing GuideME page: " + guide);
        String content = Files.readString(guide);
        for (String requiredValue : List.of(ITEM_ID, "800000", "10000000", "1024×")) {
            assertTrue(content.contains(requiredValue),
                    () -> "GuideME page " + guide + " is missing: " + requiredValue);
        }
    }

    private static void assertPatchouliContract(String locale) throws IOException {
        Path entry = sourcePath("src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook/"
                + locale + "/entries/jdte/ultimate-time-wand.json");
        assertTrue(Files.isRegularFile(entry), "Missing Patchouli entry: " + entry);
        JsonObject patchouli = readJson(entry);
        assertEquals("justdirethings:upgrades_tools", patchouli.get("category").getAsString());
        assertEquals(ITEM_ID, patchouli.get("icon").getAsString());
        assertTrue(patchouli.getAsJsonObject("extra_recipe_mappings").has(ITEM_ID),
                "Patchouli recipe mappings must include the Ultimate Time Wand");

        boolean hasRecipePage = false;
        for (JsonElement page : patchouli.getAsJsonArray("pages")) {
            JsonObject pageObject = page.getAsJsonObject();
            if ("patchouli:crafting".equals(pageObject.get("type").getAsString())
                    && pageObject.has("recipe")
                    && RECIPE_ID.equals(pageObject.get("recipe").getAsString())) {
                hasRecipePage = true;
                break;
            }
        }
        assertTrue(hasRecipePage, "Patchouli entry must include the Ultimate Time Wand recipe page");
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static Path sourceLanguage(String fileName) {
        return sourcePath(Path.of(LANGUAGE_ROOT, fileName).toString());
    }

    private static Path sourcePath(String sourcePath) {
        Path relativePath = Path.of(sourcePath);
        Path workingDirectory = Path.of("").toAbsolutePath();
        Path discovered = findFrom(workingDirectory, relativePath);
        if (discovered != null) {
            return discovered;
        }

        try {
            URI location = UltimateTimeWandConfigLanguageContractTest.class
                    .getProtectionDomain().getCodeSource().getLocation().toURI();
            discovered = findFrom(Path.of(location), relativePath);
        } catch (Exception ignored) {
            // The assertion below reports the useful source path if discovery fails.
        }
        return discovered == null ? relativePath : discovered;
    }

    private static Path findFrom(Path start, Path relativePath) {
        Path current = Files.isDirectory(start) ? start : start.getParent();
        while (current != null) {
            Path candidate = current.resolve(relativePath);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return null;
    }
}
