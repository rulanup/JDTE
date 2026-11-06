package com.jdte.common.recipes;

import com.google.gson.JsonObject;
import com.jdte.common.integrations.ae2.AEExtractionNetwork;
import com.jdte.common.integrations.ae2.AEWirelessLinkData;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTERecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/** Source two-input smithing operation: bound extraction template plus JDT/JDTE equipment. */
public final class AEExtractionSmithingRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    public AEExtractionSmithingRecipe(ResourceLocation id) { this.id = id; }
    @Override public boolean matches(Container input, Level level) {
        return input.getContainerSize() >= 3 && isTemplateIngredient(input.getItem(0))
                && input.getItem(0).getCount() == 1 && input.getItem(1).getCount() == 1
                && isBaseIngredient(input.getItem(1)) && input.getItem(2).isEmpty();
    }
    @Override public ItemStack assemble(Container input, RegistryAccess registries) {
        if (!matches(input, null)) return ItemStack.EMPTY;
        ItemStack result = input.getItem(1).copyWithCount(1);
        AEExtractionNetwork.copyLink(input.getItem(0), result);
        AEWirelessLinkData.setExtractionEnabled(result);
        return result;
    }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override public ItemStack getResultItem(RegistryAccess registries) { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return JDTERecipes.AE_EXTRACTION_SMITHING_RECIPE_SERIALIZER.get(); }
    @Override public boolean isTemplateIngredient(ItemStack stack) { return stack.is(JDTEItems.AE_EXTRACTION_UPGRADE.get()) && AEExtractionNetwork.isLinked(stack); }
    @Override public boolean isBaseIngredient(ItemStack stack) {
        if (stack.isEmpty() || AEWirelessLinkData.extractionEnabled(stack)) return false;
        String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
        return (namespace.equals("justdirethings") || namespace.equals("jdte"))
                && (stack.getCapability(ForgeCapabilities.ENERGY).isPresent() || stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent());
    }
    @Override public boolean isAdditionIngredient(ItemStack stack) { return false; }
    public static final class Serializer implements RecipeSerializer<AEExtractionSmithingRecipe> {
        @Override public AEExtractionSmithingRecipe fromJson(ResourceLocation id, JsonObject json) { return new AEExtractionSmithingRecipe(id); }
        @Override public AEExtractionSmithingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) { return new AEExtractionSmithingRecipe(id); }
        @Override public void toNetwork(FriendlyByteBuf buffer, AEExtractionSmithingRecipe recipe) {}
    }
}
