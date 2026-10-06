package com.jdte.client.jei.mineralextractor;

import com.jdte.JDTE;
import com.jdte.client.MineralSurveyClientCache;
import com.jdte.common.content.JDTEContentControl;
import com.jdte.common.items.MineralSurveyItem;
import com.jdte.common.minerals.MineralEntry;
import com.jdte.common.minerals.MineralSurveyData;
import com.jdte.common.minerals.MineralExtractorFluidRoles;
import com.jdte.common.recipes.MineralExtractorResourceResolver;
import com.jdte.setup.JDTEBlocks;
import com.jdte.setup.JDTEConfig;
import com.jdte.setup.JDTEDataComponents;
import com.jdte.setup.JDTEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record MineralExtractorJeiRecipe(
        ResourceLocation biomeId,
        ResourceLocation dimensionId,
        ItemStack surveyStack,
        List<DisplayMineral> minerals,
        int outputPage,
        int totalPages,
        ResourceLocation fortuneFluid,
        ResourceLocation accelerationFluid,
        int experienceFluidCost,
        int timeFluidCost,
        int fortuneBonusPercent,
        int energyCost
) {
    public static final int PAGE_SIZE = 16;

    public record DisplayMineral(ItemStack stack, MineralEntry entry, String chancePercent) { }

    public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath("jdte",
                "jei/mineral_extractor/" + biomeId.getNamespace() + "/" + biomeId.getPath()
                        + (totalPages > 1 ? "/" + outputPage : ""));
    }

    public static List<MineralExtractorJeiRecipe> getRecipes() {
        JDTEContentControl control = JDTEContentControl.current();
        if (!control.isBlockEnabled(JDTE.id("mineral_extractor"))
                && !control.isBlockEnabled(JDTE.id("large_mineral_extractor"))) {
            return List.of();
        }

        List<MineralSurveyData> surveys = MineralSurveyClientCache.get();
        if (surveys.isEmpty()) {
            return List.of();
        }

        RecipeManager recipeManager = getRecipeManager();
        MineralExtractorFluidRoles roles = MineralExtractorResourceResolver.resolve(recipeManager);
        int experienceFluidCost = getExperienceFluidPerCycle();
        int timeFluidCost = getTimeFluidPerAcceleratedCycle();
        int fortuneBonusPercent = getFortuneBonusPercent();
        int energyCost = getEnergyPerCycle();

        List<MineralExtractorJeiRecipe> list = new ArrayList<>();
        for (MineralSurveyData survey : surveys) {
            ResourceLocation biomeId = survey.biomeId();
            ResourceLocation dimensionId = survey.dimensionId();
            long totalWeight = survey.totalWeight();

            List<DisplayMineral> validMinerals = new ArrayList<>();
            for (MineralEntry entry : survey.entries()) {
                ItemStack ore = oreStack(entry.oreId());
                if (ore.isEmpty()) continue;
                String chance = MineralSurveyItem.formatPercent(entry.weight(), totalWeight);
                validMinerals.add(new DisplayMineral(ore, entry, chance));
            }
            if (validMinerals.isEmpty()) continue;

            ItemStack surveyStack = new ItemStack(JDTEItems.MINERAL_SURVEY.get());
            surveyStack.set(JDTEDataComponents.MINERAL_SURVEY.get(), survey);

            int totalPages = Math.max(1, (validMinerals.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            for (int page = 0; page < totalPages; page++) {
                int start = page * PAGE_SIZE;
                int end = Math.min(start + PAGE_SIZE, validMinerals.size());
                List<DisplayMineral> pageMinerals = List.copyOf(validMinerals.subList(start, end));
                MineralExtractorJeiRecipe recipe = new MineralExtractorJeiRecipe(
                        biomeId, dimensionId, surveyStack, pageMinerals, page, totalPages,
                        roles.fortuneFluid(), roles.accelerationFluid(),
                        experienceFluidCost, timeFluidCost, fortuneBonusPercent, energyCost);
                if (control.isRecipeEnabled(recipe.id())) {
                    list.add(recipe);
                }
            }
        }

        list.sort(Comparator.comparing(r -> r.biomeId().toString()));
        return list;
    }

    public static List<ItemStack> getMachines() {
        JDTEContentControl control = JDTEContentControl.current();
        List<ItemStack> machines = new ArrayList<>();
        if (control.isBlockEnabled(JDTE.id("mineral_extractor"))) {
            machines.add(new ItemStack(JDTEBlocks.MINERAL_EXTRACTOR.get()));
        }
        if (control.isBlockEnabled(JDTE.id("large_mineral_extractor"))) {
            machines.add(new ItemStack(JDTEBlocks.LARGE_MINERAL_EXTRACTOR.get()));
        }
        return machines;
    }

    private static ItemStack oreStack(ResourceLocation id) {
        return BuiltInRegistries.BLOCK.getOptional(id)
                .map(block -> new ItemStack(block.asItem()))
                .orElse(ItemStack.EMPTY);
    }

    private static RecipeManager getRecipeManager() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return null;
            if (mc.level != null) return mc.level.getRecipeManager();
            if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
                return mc.getSingleplayerServer().getRecipeManager();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static int getExperienceFluidPerCycle() {
        try {
            return JDTEConfig.COMMON.mineralExtractor.experienceFluidPerCycle.get();
        } catch (Throwable ignored) {
            return 25;
        }
    }

    private static int getTimeFluidPerAcceleratedCycle() {
        try {
            return JDTEConfig.COMMON.mineralExtractor.timeFluidPerAcceleratedCycle.get();
        } catch (Throwable ignored) {
            return 5;
        }
    }

    private static int getFortuneBonusPercent() {
        try {
            return JDTEConfig.COMMON.mineralExtractor.fortuneBonusPercent.get();
        } catch (Throwable ignored) {
            return 100;
        }
    }

    private static int getEnergyPerCycle() {
        try {
            return JDTEConfig.COMMON.mineralExtractor.energyPerCycle.get();
        } catch (Throwable ignored) {
            return 5000;
        }
    }
}
