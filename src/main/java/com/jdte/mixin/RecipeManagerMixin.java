package com.jdte.mixin;

import com.google.gson.JsonElement;
import com.jdte.common.content.JDTEContentControl;
import com.jdte.common.content.JDTERecipeFilter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Applies the content policy before vanilla decodes recipe JSON.
 *
 * <p>Filtering at this boundary keeps disabled recipes out of the server's
 * recipe index and, consequently, out of the recipe sync sent to clients.</p>
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    private void jdte$filterDisabledRecipes(Map<ResourceLocation, JsonElement> recipes,
                                              ResourceManager resourceManager,
                                              ProfilerFiller profiler,
                                              CallbackInfo ci) {
        JDTERecipeFilter.filterDisabledRecipes(recipes, JDTEContentControl.current());
    }
}
