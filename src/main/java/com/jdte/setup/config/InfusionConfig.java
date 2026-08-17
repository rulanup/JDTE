package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class InfusionConfig {
    public final ModConfigSpec.ConfigValue<List<? extends String>> spawnEggModBlacklist;

    public InfusionConfig(ModConfigSpec.Builder builder) {
        builder.comment("Infusion Machine Settings")
                .translation("config.jdte.jdte.infusion")
                .push("infusion");
        spawnEggModBlacklist = builder
                .comment("Mod ids (resource namespaces) whose spawn eggs are excluded from the Infusion Machine's "
                        + "dynamic spawn egg recipes, e.g. \"occultism\". As a result no infusion recipe is generated "
                        + "for those spawn eggs and they can no longer be produced from mob loot. Full item or entity "
                        + "resource location ids (e.g. \"occultism:debug_familiar_spawn_egg\") are also accepted.")
                .translation("config.jdte.jdte.infusion.spawnEggModBlacklist")
                .defineListAllowEmpty("spawnEggModBlacklist", List.of("occultism"),
                        entry -> entry instanceof String);
        builder.pop();
    }
}