package com.jdte.common.recipes;

import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import java.util.List;

public record LargePortableContainerRecipe(Item sourceItem, Item resultItem) implements CraftingRecipe {
    private static final Item ECLIPSE_ALLOY_INGOT = item("justdirethings", "eclipsealloy_ingot");

    public LargePortableContainerRecipe {
        if (sourceItem == Items.AIR || resultItem == Items.AIR) {
            throw new IllegalArgumentException("Large portable container recipes require real items");
        }
        if (!isSupportedUpgrade(sourceItem, resultItem)) {
            throw new IllegalArgumentException("Large portable container recipe must pair a portable container with its matching large variant");
        }
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int sourceCount = 0;
        int ingotCount = 0;
        int catalystCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(sourceItem)) {
                sourceCount += stack.getCount();
            } else if (stack.is(ECLIPSE_ALLOY_INGOT)) {
                ingotCount += stack.getCount();
            } else if (stack.is(JDTEItems.TIME_FLUID_CATALYST.get())) {
                catalystCount += stack.getCount();
            } else {
                return false;
            }
        }

        return sourceCount == 1 && ingotCount == 4 && catalystCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(sourceItem) && stack.getCount() == 1) {
                return stack.transmuteCopy(resultItem, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public PlacementInfo placementInfo() {
        Ingredient ingot = Ingredient.of(ECLIPSE_ALLOY_INGOT);
        return PlacementInfo.create(List.of(Ingredient.of(sourceItem), ingot, ingot, ingot, ingot,
                Ingredient.of(JDTEItems.TIME_FLUID_CATALYST.get())));
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                placementInfo().ingredients().stream().map(Ingredient::display).toList(),
                new SlotDisplay.ItemSlotDisplay(resultItem), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @Override public boolean showNotification() { return true; }
    @Override public String group() { return ""; }

    @Override
    public RecipeSerializer<LargePortableContainerRecipe> getSerializer() {
        return JDTERecipes.LARGE_PORTABLE_CONTAINER_RECIPE_SERIALIZER.get();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    private static boolean isSupportedUpgrade(Item sourceItem, Item resultItem) {
        return sourceItem == item("justdirethings", "pocket_generator") && resultItem == JDTEItems.LARGE_POCKET_GENERATOR.get()
                || sourceItem == item("justdirethings", "potion_canister") && resultItem == JDTEItems.LARGE_POTION_CANISTER.get()
                || sourceItem == item("justdirethings", "fuel_canister") && resultItem == JDTEItems.LARGE_FUEL_CANISTER.get();
    }

    private static Item item(String namespace, String path) {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(namespace, path));
    }

    public static final class Serializer {
        public static RecipeSerializer<LargePortableContainerRecipe> create() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }

        private static final MapCodec<RecipeData> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("source_item").forGetter(RecipeData::sourceId),
                Identifier.CODEC.fieldOf("result").forGetter(RecipeData::resultId)
        ).apply(instance, RecipeData::new));
        private static final MapCodec<LargePortableContainerRecipe> CODEC = RAW_CODEC.flatXmap(
                data -> validated(data.sourceId(), data.resultId()),
                recipe -> DataResult.success(new RecipeData(
                        BuiltInRegistries.ITEM.getKey(recipe.sourceItem()),
                        BuiltInRegistries.ITEM.getKey(recipe.resultItem()))));

        public static MapCodec<LargePortableContainerRecipe> codec() {
            return CODEC;
        }

        public static StreamCodec<RegistryFriendlyByteBuf, LargePortableContainerRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public LargePortableContainerRecipe decode(RegistryFriendlyByteBuf buffer) {
                    return validated(
                            Identifier.STREAM_CODEC.decode(buffer),
                            Identifier.STREAM_CODEC.decode(buffer)
                    ).getOrThrow();
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, LargePortableContainerRecipe recipe) {
                    Identifier.STREAM_CODEC.encode(buffer, BuiltInRegistries.ITEM.getKey(recipe.sourceItem()));
                    Identifier.STREAM_CODEC.encode(buffer, BuiltInRegistries.ITEM.getKey(recipe.resultItem()));
                }
            };
        }

        private static DataResult<LargePortableContainerRecipe> validated(Identifier sourceId,
                                                                          Identifier resultId) {
            Item sourceItem = BuiltInRegistries.ITEM.getValue(sourceId);
            if (sourceItem == Items.AIR) {
                return DataResult.error(() -> "Unknown source_item: " + sourceId);
            }

            Item resultItem = BuiltInRegistries.ITEM.getValue(resultId);
            if (resultItem == Items.AIR) {
                return DataResult.error(() -> "Unknown result item: " + resultId);
            }

            if (!isSupportedUpgrade(sourceItem, resultItem)) {
                return DataResult.error(() ->
                        "Large portable container recipe must pair matching source and result items: "
                                + sourceId + " -> " + resultId);
            }

            return DataResult.success(new LargePortableContainerRecipe(sourceItem, resultItem));
        }

        private record RecipeData(Identifier sourceId, Identifier resultId) {
        }
    }
}
