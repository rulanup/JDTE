package com.jdte.common.recipes;

import com.direwolf20.justdirethings.common.items.FuelCanister;
import com.direwolf20.justdirethings.common.items.PotionCanister;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LargePortableContainerRecipeTest {
    private static final RegistryAccess REGISTRY_ACCESS =
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    @Test
    void upgradesPocketGeneratorWithoutDroppingStoredEnergyOrFuelData() {
        ItemStack source = new ItemStack(item("justdirethings", "pocket_generator"));
        source.set(JustDireDataComponents.FORGE_ENERGY, 12_345);
        source.set(JustDireDataComponents.POCKETGEN_COUNTER, 67);
        source.set(JustDireDataComponents.POCKETGEN_MAXBURN, 89);
        source.set(JustDireDataComponents.POCKETGEN_FUELMULT, 3);
        source.set(JustDireDataComponents.ITEMSTACK_HANDLER,
                ItemContainerContents.fromItems(List.of(new ItemStack(Items.COAL, 4))));
        source.set(DataComponents.CUSTOM_NAME, Component.literal("Primed Pocket"));

        LargePortableContainerRecipe recipe =
                new LargePortableContainerRecipe(item("justdirethings", "pocket_generator"),
                        JDTEItems.LARGE_POCKET_GENERATOR.get());

        CraftingInput input = craftingInput(source,
                new ItemStack(Items.AIR),
                new ItemStack(item("justdirethings", "eclipsealloy_ingot"), 4),
                new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get()));

        assertTrue(recipe.matches(input, null));

        ItemStack result = recipe.assemble(input, REGISTRY_ACCESS);

        assertTrue(result.is(JDTEItems.LARGE_POCKET_GENERATOR.get()));
        assertEquals(12_345, result.get(JustDireDataComponents.FORGE_ENERGY));
        assertEquals(67, result.get(JustDireDataComponents.POCKETGEN_COUNTER));
        assertEquals(89, result.get(JustDireDataComponents.POCKETGEN_MAXBURN));
        assertEquals(3, result.get(JustDireDataComponents.POCKETGEN_FUELMULT));
        assertEquals(source.get(JustDireDataComponents.ITEMSTACK_HANDLER),
                result.get(JustDireDataComponents.ITEMSTACK_HANDLER));
        assertEquals(source.get(DataComponents.CUSTOM_NAME), result.get(DataComponents.CUSTOM_NAME));
        assertTrue(source.is(item("justdirethings", "pocket_generator")));
    }

    @Test
    void upgradesPotionCanisterWithoutDroppingStoredPotionContents() {
        ItemStack source = new ItemStack(item("justdirethings", "potion_canister"));
        PotionContents healing = PotionContents.createItemStack(Items.POTION, Potions.HEALING)
                .getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        PotionCanister.setPotionContents(source, healing);
        PotionCanister.setPotionAmount(source, 1_000);

        LargePortableContainerRecipe recipe =
                new LargePortableContainerRecipe(item("justdirethings", "potion_canister"),
                        JDTEItems.LARGE_POTION_CANISTER.get());

        ItemStack result = recipe.assemble(craftingInput(source,
                new ItemStack(item("justdirethings", "eclipsealloy_ingot"), 4),
                new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get())), REGISTRY_ACCESS);

        assertTrue(result.is(JDTEItems.LARGE_POTION_CANISTER.get()));
        assertEquals(healing, PotionCanister.getPotionContents(result));
        assertEquals(1_000, PotionCanister.getPotionAmount(result));
    }

    @Test
    void upgradesFuelCanisterWithoutDroppingFuelLevelOrBurnSpeed() {
        ItemStack source = new ItemStack(item("justdirethings", "fuel_canister"));
        FuelCanister.setFuelLevel(source, 7_654_321);
        FuelCanister.setBurnSpeed(source, 4.25D);

        LargePortableContainerRecipe recipe =
                new LargePortableContainerRecipe(item("justdirethings", "fuel_canister"),
                        JDTEItems.LARGE_FUEL_CANISTER.get());

        ItemStack result = recipe.assemble(craftingInput(source,
                new ItemStack(item("justdirethings", "eclipsealloy_ingot"), 4),
                new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get())), REGISTRY_ACCESS);

        assertTrue(result.is(JDTEItems.LARGE_FUEL_CANISTER.get()));
        assertEquals(7_654_321, FuelCanister.getFuelLevel(result));
        assertEquals(4.25D, FuelCanister.getBurnSpeed(result));
    }

    @Test
    void rejectsWrongSourceMissingMaterialsAndMultiplePortableSources() {
        LargePortableContainerRecipe pocketRecipe =
                new LargePortableContainerRecipe(item("justdirethings", "pocket_generator"),
                        JDTEItems.LARGE_POCKET_GENERATOR.get());

        assertFalse(pocketRecipe.matches(craftingInput(
                new ItemStack(item("justdirethings", "fuel_canister")),
                new ItemStack(item("justdirethings", "eclipsealloy_ingot"), 4),
                new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get())), null));
        assertFalse(pocketRecipe.matches(craftingInput(
                new ItemStack(item("justdirethings", "pocket_generator")),
                new ItemStack(item("justdirethings", "eclipsealloy_ingot"), 3),
                new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get())), null));
        assertFalse(pocketRecipe.matches(craftingInput(
                new ItemStack(item("justdirethings", "pocket_generator")),
                new ItemStack(item("justdirethings", "pocket_generator")),
                new ItemStack(item("justdirethings", "eclipsealloy_ingot"), 4),
                new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get())), null));
    }

    @Test
    void registersLargePortableContainerRecipeSerializerAndUsesVanillaCraftingType() {
        LargePortableContainerRecipe recipe =
                new LargePortableContainerRecipe(item("justdirethings", "pocket_generator"),
                        JDTEItems.LARGE_POCKET_GENERATOR.get());

        assertEquals(RecipeType.CRAFTING, recipe.getType());
        assertEquals("large_portable_container",
                BuiltInRegistries.RECIPE_SERIALIZER.getKey(
                        JDTERecipes.LARGE_PORTABLE_CONTAINER_RECIPE_SERIALIZER.get()).getPath());
    }

    @Test
    void recipeManagerExposesLargePortableUpgradeAsVanillaCraftingRecipe() {
        RecipeManager recipeManager = new RecipeManager(REGISTRY_ACCESS);
        RecipeHolder<?> recipeHolder = ExposedRecipeManager.fromJson(
                ResourceLocation.fromNamespaceAndPath("jdte", "large_pocket_generator"),
                json("data/jdte/recipe/large_pocket_generator.json"),
                REGISTRY_ACCESS);
        recipeManager.replaceRecipes(List.of(recipeHolder));

        CraftingInput input = craftingInput(
                new ItemStack(item("justdirethings", "pocket_generator")),
                new ItemStack(item("justdirethings", "eclipsealloy_ingot"), 4),
                new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get()));

        assertTrue(recipeManager.getRecipeFor(RecipeType.CRAFTING, input, null).isPresent());
        RecipeHolder<CraftingRecipe> craftingHolder = recipeManager.getRecipeFor(RecipeType.CRAFTING, input, null)
                .orElseThrow();
        assertEquals(ResourceLocation.fromNamespaceAndPath("jdte", "large_pocket_generator"), craftingHolder.id());
        assertEquals(LargePortableContainerRecipe.class, craftingHolder.value().getClass());
        assertEquals(1, recipeManager.getAllRecipesFor(RecipeType.CRAFTING).size());
    }

    @Test
    void curiosItemTagsOnlyContainTheirMatchingLargePortableContainer() {
        assertTagValues("data/curios/tags/item/large_pocket_generator.json", "jdte:large_pocket_generator");
        assertTagValues("data/curios/tags/item/large_potion_canister.json", "jdte:large_potion_canister");
        assertTagValues("data/curios/tags/item/large_fuel_canister.json", "jdte:large_fuel_canister");
    }

    @Test
    void curiosSlotAndEntityResourcesExposeThreeIndependentSingleSlots() {
        assertSlotResource("data/curios/curios/slots/large_pocket_generator.json");
        assertSlotResource("data/curios/curios/slots/large_potion_canister.json");
        assertSlotResource("data/curios/curios/slots/large_fuel_canister.json");

        assertEntitySlots("data/jdte/curios/entities/large_pocket_generator.json", "large_pocket_generator");
        assertEntitySlots("data/jdte/curios/entities/large_potion_canister.json", "large_potion_canister");
        assertEntitySlots("data/jdte/curios/entities/large_fuel_canister.json", "large_fuel_canister");
    }

    @Test
    void recipeAndModelResourcesUseTheExpectedPortableContainerIds() {
        assertRecipeResource("large_pocket_generator", "justdirethings:pocket_generator", "jdte:large_pocket_generator");
        assertRecipeResource("large_potion_canister", "justdirethings:potion_canister", "jdte:large_potion_canister");
        assertRecipeResource("large_fuel_canister", "justdirethings:fuel_canister", "jdte:large_fuel_canister");

        JsonObject pocketModel = json("assets/jdte/models/item/large_pocket_generator.json");
        assertTrue(pocketModel.getAsJsonArray("overrides").toString().contains("justdirethings:enabled"));

        JsonObject potionModel = json("assets/jdte/models/item/large_potion_canister.json");
        assertTrue(potionModel.getAsJsonArray("overrides").toString().contains("justdirethings:potion_fullness"));

        JsonObject fuelModel = json("assets/jdte/models/item/large_fuel_canister.json");
        assertTrue(fuelModel.getAsJsonArray("overrides").toString().contains("justdirethings:fullness"));
    }

    @Test
    void englishAndChineseExposePortableContainerNamesAndCuriosSlotLabels() {
        JsonObject english = json("assets/jdte/lang/en_us.json");
        JsonObject chinese = json("assets/jdte/lang/zh_cn.json");
        List<String> keys = List.of(
                "item.jdte.large_pocket_generator",
                "item.jdte.large_potion_canister",
                "item.jdte.large_fuel_canister",
                "curios.identifier.large_pocket_generator",
                "curios.identifier.large_potion_canister",
                "curios.identifier.large_fuel_canister");

        for (String key : keys) {
            assertTrue(english.has(key), () -> "Missing en_us key: " + key);
            assertTrue(chinese.has(key), () -> "Missing zh_cn key: " + key);
            assertFalse(english.get(key).getAsString().isBlank(), () -> "Blank en_us key: " + key);
            assertFalse(chinese.get(key).getAsString().isBlank(), () -> "Blank zh_cn key: " + key);
        }
    }

    private static void assertRecipeResource(String recipeName, String sourceId, String resultId) {
        JsonObject recipe = json("data/jdte/recipe/" + recipeName + ".json");
        assertEquals("jdte:large_portable_container", recipe.get("type").getAsString());
        assertEquals(sourceId, recipe.get("source_item").getAsString());
        assertEquals(resultId, recipe.get("result").getAsString());
    }

    private static void assertTagValues(String path, String expectedItemId) {
        JsonArray values = json(path).getAsJsonArray("values");
        assertNotNull(values, () -> "Missing values array in " + path);
        assertEquals(List.of(expectedItemId), values.asList().stream().map(element -> element.getAsString()).toList());
    }

    private static void assertSlotResource(String path) {
        JsonObject slot = json(path);
        assertEquals(1, slot.get("size").getAsInt());
    }

    private static void assertEntitySlots(String path, String expectedSlotId) {
        JsonObject entityBinding = json(path);
        assertEquals(List.of("minecraft:player"),
                entityBinding.getAsJsonArray("entities").asList().stream().map(element -> element.getAsString()).toList());
        assertEquals(List.of(expectedSlotId),
                entityBinding.getAsJsonArray("slots").asList().stream().map(element -> element.getAsString()).toList());
    }

    private static CraftingInput craftingInput(ItemStack... stacks) {
        List<ItemStack> grid = java.util.Arrays.asList(
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
        );
        for (int i = 0; i < stacks.length; i++) {
            grid.set(i, stacks[i]);
        }
        return CraftingInput.of(3, 3, grid);
    }

    private static Item item(String namespace, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        Item item = BuiltInRegistries.ITEM.get(id);
        assertFalse(item == Items.AIR, () -> "Missing registered item: " + id);
        return item;
    }

    private static JsonObject json(String path) {
        InputStream resource = LargePortableContainerRecipeTest.class.getClassLoader().getResourceAsStream(path);
        assertNotNull(resource, () -> "Missing resource: " + path);
        try (InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            throw new AssertionError("Unable to parse resource: " + path, e);
        }
    }

    private static final class ExposedRecipeManager extends RecipeManager {
        private ExposedRecipeManager() {
            super(REGISTRY_ACCESS);
        }

        protected static RecipeHolder<?> fromJson(ResourceLocation id, JsonObject recipeJson,
                                                  RegistryAccess registries) {
            return RecipeManager.fromJson(id, recipeJson, registries);
        }
    }
}
