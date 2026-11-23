package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SolarPanelConfig {
    public final ModConfigSpec.IntValue concentratedGeneration;
    public final ModConfigSpec.IntValue concentratedCapacity;
    public final ModConfigSpec.IntValue singularityGeneration;
    public final ModConfigSpec.IntValue singularityCapacity;
    public final ModConfigSpec.IntValue stellarFusionGeneration;
    public final ModConfigSpec.IntValue stellarFusionCapacity;
    public final ModConfigSpec.IntValue dimensionalCollapseGeneration;
    public final ModConfigSpec.IntValue dimensionalCollapseCapacity;

    public SolarPanelConfig(ModConfigSpec.Builder builder) {
        builder.comment("Tiered Solar Panel Settings")
                .translation("config.jdte.jdte.solarPanel")
                .push("solarPanel");
        concentratedGeneration = intValue(builder, "concentratedGeneration", 46_080);
        concentratedCapacity = intValue(builder, "concentratedCapacity", 4_608_000);
        singularityGeneration = intValue(builder, "singularityGeneration", 184_320);
        singularityCapacity = intValue(builder, "singularityCapacity", 18_432_000);
        stellarFusionGeneration = intValue(builder, "stellarFusionGeneration", 737_280);
        stellarFusionCapacity = intValue(builder, "stellarFusionCapacity", 73_728_000);
        dimensionalCollapseGeneration = intValue(builder, "dimensionalCollapseGeneration", 2_949_120);
        dimensionalCollapseCapacity = intValue(builder, "dimensionalCollapseCapacity", 294_912_000);
        builder.pop();
    }

    private static ModConfigSpec.IntValue intValue(ModConfigSpec.Builder builder, String name, int defaultValue) {
        return builder.translation("config.jdte.jdte.solarPanel." + name)
                .defineInRange(name, defaultValue, 1, Integer.MAX_VALUE);
    }
}
