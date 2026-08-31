package com.jdte.setup.config;

import com.jdte.common.content.ContentSelection;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ContentConfig {
    public final ModConfigSpec.ConfigValue<List<? extends String>> disabledBlocks;
    public final ModConfigSpec.ConfigValue<List<? extends String>> disabledRecipes;
    public final ModConfigSpec.BooleanValue greenhouseRecipeGenerationEnabled;
    public final ModConfigSpec.BooleanValue lootFabricatorRecipeGenerationEnabled;
    public final ModConfigSpec.BooleanValue bioFactoryRecipeGenerationEnabled;
    public final ModConfigSpec.BooleanValue timeAcceleratorEnabled;
    public final ModConfigSpec.BooleanValue timeFreezerEnabled;

    public ContentConfig(ModConfigSpec.Builder builder, GreenhouseConfig greenhouse,
                         LootFabricatorConfig lootFabricator, BioFactoryConfig bioFactory) {
        builder.comment("Content Settings")
                .translation("config.jdte.jdte.content")
                .push("content");
        disabledBlocks = builder
                .comment("Block ids or directory prefixes to disable. Missing namespaces default to jdte, so greenhouse means jdte:greenhouse.")
                .translation("config.jdte.jdte.content.disabledBlocks")
                .defineListAllowEmpty("disabledBlocks", List.of(), entry -> entry instanceof String value
                        && ContentSelection.isValid(value));
        disabledRecipes = builder
                .comment("Recipe ids or directory prefixes to disable. Missing namespaces default to jdte, so greenhouse means jdte:greenhouse.")
                .translation("config.jdte.jdte.content.disabledRecipes")
                .defineListAllowEmpty("disabledRecipes", List.of(), entry -> entry instanceof String value
                        && ContentSelection.isValid(value));
        timeAcceleratorEnabled = builder
                .comment("When disabled, all Time Accelerator blocks and their bundled crafting recipes remain registered but cannot be used.")
                .translation("config.jdte.jdte.content.timeAcceleratorEnabled")
                .define("timeAcceleratorEnabled", true);
        timeFreezerEnabled = builder
                .comment("When disabled, all Time Freezer blocks and their bundled crafting recipes remain registered but cannot be used.")
                .translation("config.jdte.jdte.content.timeFreezerEnabled")
                .define("timeFreezerEnabled", true);
        builder.pop();

        greenhouseRecipeGenerationEnabled = greenhouse.greenhouseRecipeGenerationEnabled;
        lootFabricatorRecipeGenerationEnabled = lootFabricator.lootFabricatorRecipeGenerationEnabled;
        bioFactoryRecipeGenerationEnabled = bioFactory.bioFactoryRecipeGenerationEnabled;
    }
}
