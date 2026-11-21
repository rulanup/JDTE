package com.jdte.common.recipes;

import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import com.jdte.setup.JDTERecipes;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public final class EntityAccelerationSmithingRecipe extends SimpleSmithingRecipe {
    public EntityAccelerationSmithingRecipe() {
        super(new CommonInfo(true));
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return input.template().getCount() == 1
                && input.base().getCount() == 1
                && input.addition().getCount() == 1
                && input.template().is(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get())
                && input.base().is(JDTEItems.ULTIMATE_TIME_WAND.get())
                && !input.base().getOrDefault(JDTEDataComponents.ULTIMATE_TIME_WAND_ENTITY_ACCELERATION.get(), false)
                && input.addition().is(JDTEItems.TIME_FLUID_CATALYST.get());
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        if (!matches(input, null)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = input.base().copyWithCount(1);
        result.set(JDTEDataComponents.ULTIMATE_TIME_WAND_ENTITY_ACCELERATION.get(), true);
        return result;
    }

    @Override
    public List<RecipeDisplay> display() {
        ItemStack stack = new ItemStack(JDTEItems.ULTIMATE_TIME_WAND.get());
        stack.set(JDTEDataComponents.ULTIMATE_TIME_WAND_ENTITY_ACCELERATION.get(), true);
        return List.of(new SmithingRecipeDisplay(templateIngredient().orElseThrow().display(),
                baseIngredient().display(), additionIngredient().orElseThrow().display(),
                new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack)),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }

    @Override
    public RecipeSerializer<EntityAccelerationSmithingRecipe> getSerializer() {
        return JDTERecipes.ENTITY_ACCELERATION_SMITHING_RECIPE_SERIALIZER.get();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(List.of(templateIngredient(), Optional.of(baseIngredient()), additionIngredient()));
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return Optional.of(Ingredient.of(JDTEItems.ENTITY_ACCELERATION_UPGRADE.get()));
    }

    @Override
    public Ingredient baseIngredient() {
        return Ingredient.of(JDTEItems.ULTIMATE_TIME_WAND.get());
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return Optional.of(Ingredient.of(JDTEItems.TIME_FLUID_CATALYST.get()));
    }

    public static final class Serializer {
        public static RecipeSerializer<EntityAccelerationSmithingRecipe> create() {
            return new RecipeSerializer<>(codec(), streamCodec());
        }

        private static final EntityAccelerationSmithingRecipe INSTANCE = new EntityAccelerationSmithingRecipe();
        private static final MapCodec<EntityAccelerationSmithingRecipe> CODEC = MapCodec.unit(INSTANCE);
        private static final StreamCodec<RegistryFriendlyByteBuf, EntityAccelerationSmithingRecipe> STREAM_CODEC =
                StreamCodec.unit(INSTANCE);

        public static MapCodec<EntityAccelerationSmithingRecipe> codec() {
            return CODEC;
        }

        public static StreamCodec<RegistryFriendlyByteBuf, EntityAccelerationSmithingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
