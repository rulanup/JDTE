package com.jdte.setup;

import com.jdte.JDTE;
import com.jdte.common.recipes.InfusionRecipe;
import com.jdte.common.recipes.GreenhouseRecipe;
import com.jdte.common.recipes.BioFactoryRecipe;
import com.jdte.common.recipes.LifeSynthesisRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class JDTERecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, JDTE.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, JDTE.MODID);

    public static final RegistryObject<RecipeType<InfusionRecipe>> INFUSION_RECIPE_TYPE = RECIPE_TYPES.register(
            "infusion", () -> RecipeType.simple(com.jdte.JDTE.id("infusion")));

    public static final Supplier<InfusionRecipe.Serializer> INFUSION_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "infusion", InfusionRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<GreenhouseRecipe>> GREENHOUSE_RECIPE_TYPE = RECIPE_TYPES.register(
            "greenhouse", () -> RecipeType.simple(com.jdte.JDTE.id("greenhouse")));

    public static final Supplier<GreenhouseRecipe.Serializer> GREENHOUSE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "greenhouse", GreenhouseRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<BioFactoryRecipe>> BIO_FACTORY_RECIPE_TYPE = RECIPE_TYPES.register(
            "bio_factory", () -> RecipeType.simple(com.jdte.JDTE.id("bio_factory")));

    public static final Supplier<BioFactoryRecipe.Serializer> BIO_FACTORY_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "bio_factory", BioFactoryRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<LifeSynthesisRecipe>> LIFE_SYNTHESIS_RECIPE_TYPE = RECIPE_TYPES.register(
            "life_synthesis", () -> RecipeType.simple(com.jdte.JDTE.id("life_synthesis")));

    public static final Supplier<LifeSynthesisRecipe.Serializer> LIFE_SYNTHESIS_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "life_synthesis", LifeSynthesisRecipe.Serializer::new);
}
