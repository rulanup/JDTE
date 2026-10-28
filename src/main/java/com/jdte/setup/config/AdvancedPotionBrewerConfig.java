package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class AdvancedPotionBrewerConfig {
    public final ForgeConfigSpec.BooleanValue potionBrewerRejectPatternProviderFuelInput;
    public final ForgeConfigSpec.IntValue potionBrewerEnergyPerBlazePowder;

    public AdvancedPotionBrewerConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Advanced Potion Brewer Settings")
                .translation("config.jdte.jdte.advancedPotionBrewer")
                .push("advancedPotionBrewer");
        potionBrewerRejectPatternProviderFuelInput = builder
                .comment("Reject Blaze Powder insertion into the fuel slot from adjacent AE2 crafting providers")
                .translation("config.jdte.jdte.advancedPotionBrewer.rejectPatternProviderFuelInput")
                .define("rejectPatternProviderFuelInput", true);
        potionBrewerEnergyPerBlazePowder = builder
                .comment("FE charged per blaze-powder-equivalent fuel charge (20 brews) when the Energy Brewing Upgrade is installed; 0 makes the energy fuel free")
                .translation("config.jdte.jdte.advancedPotionBrewer.energyPerBlazePowder")
                .defineInRange("energyPerBlazePowder", 5000, 0, 1000000);
        builder.pop();
    }
}
