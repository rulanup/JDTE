package com.jdte.common.integrations;

import com.jdte.common.recipes.FluidMixerRecipe;
import com.jdte.common.recipes.RecipeCacheSignal;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import java.util.*;

/** Optional Forge 1.20 gas-infuser bridge. Only same-ID registered fluid equivalents are accepted. */
public final class MekanismFluidMixerIntegration {
    private record Cache(long generation,List<FluidMixerRecipe> recipes) {}
    private static final Map<net.minecraft.world.item.crafting.RecipeManager,Cache> CACHES=new WeakHashMap<>();
    private MekanismFluidMixerIntegration() {}
    public static synchronized List<FluidMixerRecipe> getCachedRecipes(Level level) {
        if(level==null)return List.of();
        var manager=level.getRecipeManager();var cached=CACHES.get(manager);
        if(cached==null || cached.generation()!=RecipeCacheSignal.generation()) {
            List<FluidMixerRecipe> result=new ArrayList<>();
            for(var recipe:MekanismRecipeType.CHEMICAL_INFUSING.get().getRecipes(level)) {
                try {
                    var left=recipe.getLeftInput().getRepresentations();var right=recipe.getRightInput().getRepresentations();var outputs=recipe.getOutputDefinition();
                    if(left.isEmpty() || right.isEmpty() || outputs.isEmpty())continue;
                    var a=chemicalToFluid(left.get(0));var b=chemicalToFluid(right.get(0));var out=chemicalToFluid(outputs.get(0));
                    if(a.isEmpty() || b.isEmpty() || out.isEmpty())continue;
                    result.add(new FluidMixerRecipe(a,b,Optional.empty(),out,100,5000,
                            ResourceLocation.fromNamespaceAndPath("jdte","mekanism/"+recipe.getId().getNamespace()+"/"+recipe.getId().getPath())));
                }catch(RuntimeException error){com.mojang.logging.LogUtils.getLogger().warn("Cannot adapt Mekanism infuser recipe {}",recipe.getId(),error);}
            }
            cached=new Cache(RecipeCacheSignal.generation(),List.copyOf(result));CACHES.put(manager,cached);
        }
        return cached.recipes();
    }
    private static FluidStack chemicalToFluid(GasStack gas) {
        if(gas==null || gas.isEmpty())return FluidStack.EMPTY;
        var id=gas.getTypeRegistryName();
        if(id==null || !BuiltInRegistries.FLUID.containsKey(id))return FluidStack.EMPTY;
        return new FluidStack(BuiltInRegistries.FLUID.get(id),(int)Math.min(Integer.MAX_VALUE,gas.getAmount()));
    }
}
