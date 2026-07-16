package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.recipes.InfusionRecipe;
import com.jdte.common.recipes.GreenhouseRecipe;
import com.jdte.common.recipes.BioFactoryRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class JDTERecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, JDTE.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, JDTE.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<InfusionRecipe>> INFUSION_RECIPE_TYPE = RECIPE_TYPES.register(
            "infusion", () -> RecipeType.simple(com.jdte.JDTE.id("infusion")));

    public static final Supplier<InfusionRecipe.Serializer> INFUSION_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "infusion", InfusionRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GreenhouseRecipe>> GREENHOUSE_RECIPE_TYPE = RECIPE_TYPES.register(
            "greenhouse", () -> RecipeType.simple(com.jdte.JDTE.id("greenhouse")));

    public static final Supplier<GreenhouseRecipe.Serializer> GREENHOUSE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "greenhouse", GreenhouseRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<BioFactoryRecipe>> BIO_FACTORY_RECIPE_TYPE = RECIPE_TYPES.register(
            "bio_factory", () -> RecipeType.simple(com.jdte.JDTE.id("bio_factory")));

    public static final Supplier<BioFactoryRecipe.Serializer> BIO_FACTORY_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "bio_factory", BioFactoryRecipe.Serializer::new);
}
