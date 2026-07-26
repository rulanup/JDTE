package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class LootFabricatorConfig {
    public final ModConfigSpec.IntValue lootFabricatorLifeFluidCost;
    public final ModConfigSpec.IntValue lootFabricatorBaseTimeFluidCost;
    public final ModConfigSpec.IntValue lootFabricatorLootingFluidCostIncreasePercent;

    public LootFabricatorConfig(ModConfigSpec.Builder builder) {
        builder.comment("Loot Fabricator Settings").translation("config.jdte.jdte.lootFabricator").push("lootFabricator");
        lootFabricatorLifeFluidCost = builder
                .comment("Life Fluid consumed per successful loot fabrication operation (mB)")
                .translation("config.jdte.jdte.lootFabricator.lifeFluidCost")
                .defineInRange("lifeFluidCost", 100, 1, 1_000_000);
        lootFabricatorBaseTimeFluidCost = builder
                .comment("Base Time Fluid consumed per successful loot fabrication operation (mB). Faster machine speeds multiply this integer cost.")
                .translation("config.jdte.jdte.lootFabricator.baseTimeFluidCost")
                .defineInRange("baseTimeFluidCost", 1, 1, 1_000_000);
        lootFabricatorLootingFluidCostIncreasePercent = builder
                .comment("Additional Life Fluid and Time Fluid cost per Looting Upgrade installed in a Loot Fabricator, in percent.")
                .translation("config.jdte.jdte.lootFabricator.lootingFluidCostIncreasePercent")
                .defineInRange("lootingFluidCostIncreasePercent", 50, 0, 10_000);
        builder.pop();
    }
}
