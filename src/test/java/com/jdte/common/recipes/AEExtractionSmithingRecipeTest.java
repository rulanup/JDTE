package com.jdte.common.recipes;

import appeng.api.ids.AEComponents;
import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AEExtractionSmithingRecipeTest {
    private static final RegistryAccess REGISTRY_ACCESS =
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    @Test
    void boundUpgradePreservesResourcesAndWritesLink() {
        GlobalPos link = GlobalPos.of(Level.OVERWORLD, new BlockPos(3, 64, 5));
        ItemStack template = boundUpgrade(link);
        ItemStack base = new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());
        base.set(DataComponents.CUSTOM_NAME, Component.literal("Linked Wand"));
        base.set(JustDireDataComponents.FORGE_ENERGY, 12_345);
        SmithingRecipeInput input = new SmithingRecipeInput(template, base, ItemStack.EMPTY);

        AEExtractionSmithingRecipe recipe = new AEExtractionSmithingRecipe();
        assertTrue(recipe.matches(input, null));

        ItemStack result = recipe.assemble(input, REGISTRY_ACCESS);

        assertEquals(link, result.get(AEComponents.WIRELESS_LINK_TARGET));
        assertEquals(12_345, result.get(JustDireDataComponents.FORGE_ENERGY));
        assertEquals(Component.literal("Linked Wand"), result.get(DataComponents.CUSTOM_NAME));
        assertTrue(result.getOrDefault(JDTEDataComponents.AE_EXTRACTION_ENABLED.get(), false));
    }

    @Test
    void rejectsUnboundTemplatesFilledAdditionUnsupportedItemsAndAlreadyEnhancedBases() {
        AEExtractionSmithingRecipe recipe = new AEExtractionSmithingRecipe();
        ItemStack bound = boundUpgrade(GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO));
        ItemStack capable = new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());
        ItemStack alreadyEnhanced = capable.copy();
        alreadyEnhanced.set(JDTEDataComponents.AE_EXTRACTION_ENABLED.get(), true);

        assertFalse(recipe.matches(new SmithingRecipeInput(
                new ItemStack(JDTEItems.AE_EXTRACTION_UPGRADE.get()), capable, ItemStack.EMPTY), null));
        assertFalse(recipe.matches(new SmithingRecipeInput(bound, capable, new ItemStack(Items.REDSTONE)), null));
        assertFalse(recipe.matches(new SmithingRecipeInput(bound, new ItemStack(Items.DIAMOND), ItemStack.EMPTY), null));
        assertFalse(recipe.matches(new SmithingRecipeInput(bound,
                new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get()), ItemStack.EMPTY), null));
        assertFalse(recipe.matches(new SmithingRecipeInput(bound, alreadyEnhanced, ItemStack.EMPTY), null));
    }

    @Test
    void exposesOnlyTheBoundUpgradeAndSupportedEnergyBaseIngredients() {
        AEExtractionSmithingRecipe recipe = new AEExtractionSmithingRecipe();
        ItemStack template = boundUpgrade(GlobalPos.of(ResourceKey.create(Registries.DIMENSION, JDTEItems.ITEMS.getRegistryKey().location()), BlockPos.ZERO));
        ItemStack capable = new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());

        assertTrue(recipe.isTemplateIngredient(template));
        assertFalse(recipe.isTemplateIngredient(new ItemStack(Items.DIAMOND)));
        assertTrue(recipe.isBaseIngredient(capable));
        assertFalse(recipe.isBaseIngredient(new ItemStack(JDTEItems.TIME_FLUID_CATALYST.get())));
        assertFalse(recipe.isAdditionIngredient(ItemStack.EMPTY));
        assertFalse(recipe.isAdditionIngredient(new ItemStack(Items.REDSTONE)));
    }

    private static ItemStack boundUpgrade(GlobalPos link) {
        ItemStack template = new ItemStack(JDTEItems.AE_EXTRACTION_UPGRADE.get());
        template.set(AEComponents.WIRELESS_LINK_TARGET, link);
        return template;
    }
}
