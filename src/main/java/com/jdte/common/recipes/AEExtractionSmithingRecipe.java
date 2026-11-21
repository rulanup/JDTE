package com.jdte.common.recipes;

import com.jdte.common.integrations.ae2.AEExtractionNetwork;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import com.mojang.serialization.MapCodec;
import java.util.Optional;

public final class AEExtractionSmithingRecipe extends SimpleSmithingRecipe {
    public AEExtractionSmithingRecipe() { super(new CommonInfo(false)); }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return input.template().is(JDTEItems.AE_EXTRACTION_UPGRADE.get())
                && input.template().getCount() == 1
                && AEExtractionNetwork.isLinked(input.template())
                && input.base().getCount() == 1
                && isBaseIngredient(input.base())
                && input.addition().isEmpty();
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        if (!matches(input, null)) return ItemStack.EMPTY;
        ItemStack result = input.base().copyWithCount(1);
        AEExtractionNetwork.copyLink(input.template(), result);
        result.set(JDTEDataComponents.AE_EXTRACTION_ENABLED.get(), true);
        return result;
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return Optional.of(Ingredient.of(JDTEItems.AE_EXTRACTION_UPGRADE.get()));
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return Optional.empty();
    }

    @Override
    public RecipeSerializer<AEExtractionSmithingRecipe> getSerializer() {
        return JDTERecipes.AE_EXTRACTION_SMITHING_RECIPE_SERIALIZER.get();
    }

    @Override
    public Ingredient baseIngredient() {
        return Ingredient.of(BuiltInRegistries.ITEM.stream()
                .filter(item -> isJdtNamespace(BuiltInRegistries.ITEM.getKey(item))));
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    private boolean isBaseIngredient(ItemStack stack) {
        if (stack.isEmpty() || !isJdtNamespace(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                || stack.has(JDTEDataComponents.AE_EXTRACTION_ENABLED.get())) return false;
        var access = ItemAccess.forStack(stack);
        return access.getCapability(Capabilities.Energy.ITEM) != null
                || access.getCapability(Capabilities.Fluid.ITEM) != null;
    }

    private static boolean isJdtNamespace(Identifier key) {
        return key.getNamespace().equals("justdirethings") || key.getNamespace().equals("jdte");
    }

    public static final class Serializer {
        public static RecipeSerializer<AEExtractionSmithingRecipe> create() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }

        private static final AEExtractionSmithingRecipe INSTANCE = new AEExtractionSmithingRecipe();
        private static final MapCodec<AEExtractionSmithingRecipe> CODEC = MapCodec.unit(INSTANCE);
        private static final StreamCodec<RegistryFriendlyByteBuf, AEExtractionSmithingRecipe> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        public static MapCodec<AEExtractionSmithingRecipe> codec() {
            return CODEC;
        }

        public static StreamCodec<RegistryFriendlyByteBuf, AEExtractionSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
