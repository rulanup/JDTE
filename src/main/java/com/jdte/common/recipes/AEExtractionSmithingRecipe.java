package com.jdte.common.recipes;

import appeng.api.ids.AEComponents;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.common.integrations.ae2.AEExtractionNetwork;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.energy.IEnergyStorage;
import com.mojang.serialization.MapCodec;

public final class AEExtractionSmithingRecipe implements SmithingRecipe {
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
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        if (!matches(input, null)) return ItemStack.EMPTY;
        ItemStack result = input.base().copyWithCount(1);
        AEExtractionNetwork.copyLink(input.template(), result);
        result.set(JDTEDataComponents.AE_EXTRACTION_ENABLED.get(), true);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return JDTERecipes.AE_EXTRACTION_SMITHING_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.is(JDTEItems.AE_EXTRACTION_UPGRADE.get()) && AEExtractionNetwork.isLinked(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        if (stack.isEmpty() || !isJdtItem(stack) || stack.has(JDTEDataComponents.AE_EXTRACTION_ENABLED.get())) return false;
        return stack.getCapability(Capabilities.EnergyStorage.ITEM) instanceof IEnergyStorage
                || stack.getCapability(Capabilities.FluidHandler.ITEM) instanceof IFluidHandlerItem;
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return false;
    }

    private static boolean isJdtItem(ItemStack stack) {
        ResourceLocation key = stack.getItem().builtInRegistryHolder().key().location();
        return key.getNamespace().equals("justdirethings") || key.getNamespace().equals("jdte");
    }

    public static final class Serializer implements RecipeSerializer<AEExtractionSmithingRecipe> {
        private static final MapCodec<AEExtractionSmithingRecipe> CODEC = MapCodec.unit(new AEExtractionSmithingRecipe());
        private static final StreamCodec<RegistryFriendlyByteBuf, AEExtractionSmithingRecipe> STREAM_CODEC =
                StreamCodec.unit(new AEExtractionSmithingRecipe());

        @Override
        public MapCodec<AEExtractionSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AEExtractionSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
