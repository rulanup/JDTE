package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class AdvancedPotionBrewerConfig {
    public final ModConfigSpec.BooleanValue potionBrewerRejectPatternProviderFuelInput;

    public AdvancedPotionBrewerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Advanced Potion Brewer Settings")
                .translation("config.jdte.jdte.advancedPotionBrewer")
                .push("advancedPotionBrewer");
        potionBrewerRejectPatternProviderFuelInput = builder
                .comment("Reject Blaze Powder insertion into the fuel slot from adjacent AE2 crafting providers")
                .translation("config.jdte.jdte.advancedPotionBrewer.rejectPatternProviderFuelInput")
                .define("rejectPatternProviderFuelInput", true);
        builder.pop();
    }
}
