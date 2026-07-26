package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class LifeExtractorConfig {
    public final ModConfigSpec.DoubleValue lifeExtractorFluidPerHealth;
    public final ModConfigSpec.DoubleValue lifeExtractorHighHealthLossPercent;

    public LifeExtractorConfig(ModConfigSpec.Builder builder) {
        builder.comment("Life Extractor Settings").translation("config.jdte.jdte.lifeExtractor").push("lifeExtractor");
        lifeExtractorFluidPerHealth = builder
                .comment("Life Fluid produced per point of the entity's current health (mB)")
                .translation("config.jdte.jdte.lifeExtractor.fluidPerHealth")
                .defineInRange("fluidPerHealth", 0.1D, 0.001D, 100000.0D);
        lifeExtractorHighHealthLossPercent = builder
                .comment("Marginal yield loss for each complete 100-health band above the first 100 health")
                .translation("config.jdte.jdte.lifeExtractor.highHealthLossPercent")
                .defineInRange("highHealthLossPercent", 10.0D, 0.0D, 100.0D);
        builder.pop();
    }
}
