package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class LootFabricatorConfig {
    public final ForgeConfigSpec.IntValue lootFabricatorLifeFluidCost;
    public final ForgeConfigSpec.IntValue lootFabricatorBaseTimeFluidCost;
    public final ForgeConfigSpec.IntValue lootFabricatorLootingFluidCostIncreasePercent;

    public LootFabricatorConfig(ForgeConfigSpec.Builder builder) {
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
