package com.jdte.common.recipes;

import com.direwolf20.justdirethings.setup.Registration;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record LargePortableContainerRecipe(Item sourceItem, Item resultItem) implements Recipe<CraftingInput> {
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
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(sourceItem) && stack.getCount() == 1) {
                return stack.transmuteCopy(resultItem, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 6;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return new ItemStack(resultItem);
    }

    @Override
    public RecipeType<?> getType() {
        return JDTERecipes.LARGE_PORTABLE_CONTAINER_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return JDTERecipes.LARGE_PORTABLE_CONTAINER_RECIPE_SERIALIZER.get();
    }

    private static boolean isSupportedUpgrade(Item sourceItem, Item resultItem) {
        return sourceItem == item("justdirethings", "pocket_generator") && resultItem == JDTEItems.LARGE_POCKET_GENERATOR.get()
                || sourceItem == item("justdirethings", "potion_canister") && resultItem == JDTEItems.LARGE_POTION_CANISTER.get()
                || sourceItem == item("justdirethings", "fuel_canister") && resultItem == JDTEItems.LARGE_FUEL_CANISTER.get();
    }

    private static Item item(String namespace, String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static final class Serializer implements RecipeSerializer<LargePortableContainerRecipe> {
        private static final MapCodec<RecipeData> RAW_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("source_item").forGetter(RecipeData::sourceId),
                ResourceLocation.CODEC.fieldOf("result").forGetter(RecipeData::resultId)
        ).apply(instance, RecipeData::new));
        private static final MapCodec<LargePortableContainerRecipe> CODEC = RAW_CODEC.flatXmap(
                data -> validated(data.sourceId(), data.resultId()),
                recipe -> DataResult.success(new RecipeData(
                        BuiltInRegistries.ITEM.getKey(recipe.sourceItem()),
                        BuiltInRegistries.ITEM.getKey(recipe.resultItem()))));

        @Override
        public MapCodec<LargePortableContainerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LargePortableContainerRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public LargePortableContainerRecipe decode(RegistryFriendlyByteBuf buffer) {
                    return validated(
                            ResourceLocation.STREAM_CODEC.decode(buffer),
                            ResourceLocation.STREAM_CODEC.decode(buffer)
                    ).getOrThrow();
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, LargePortableContainerRecipe recipe) {
                    ResourceLocation.STREAM_CODEC.encode(buffer, BuiltInRegistries.ITEM.getKey(recipe.sourceItem()));
                    ResourceLocation.STREAM_CODEC.encode(buffer, BuiltInRegistries.ITEM.getKey(recipe.resultItem()));
                }
            };
        }

        private static DataResult<LargePortableContainerRecipe> validated(ResourceLocation sourceId,
                                                                          ResourceLocation resultId) {
            Item sourceItem = BuiltInRegistries.ITEM.get(sourceId);
            if (sourceItem == Items.AIR) {
                return DataResult.error(() -> "Unknown source_item: " + sourceId);
            }

            Item resultItem = BuiltInRegistries.ITEM.get(resultId);
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

        private record RecipeData(ResourceLocation sourceId, ResourceLocation resultId) {
        }
    }
}
