package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class GeneratorUpgradeConfig {
    public final ForgeConfigSpec.IntValue generatorUpgradeEnergyMultiplier;
    public final ForgeConfigSpec.IntValue generatorUpgradeFluidCost;

    public GeneratorUpgradeConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Generator Upgrade Settings").translation("config.jdte.jdte.generatorUpgrade").push("generatorUpgrade");
        generatorUpgradeEnergyMultiplier = builder
                .comment("Generator upgrade energy output multiplier")
                .translation("config.jdte.jdte.generatorUpgrade.generatorUpgradeEnergyMultiplier")
                .defineInRange("generatorUpgradeEnergyMultiplier", 3, 1, 10);
        generatorUpgradeFluidCost = builder
                .comment("Generator upgrade fluid cost per tick (mB)")
                .translation("config.jdte.jdte.generatorUpgrade.generatorUpgradeFluidCost")
                .defineInRange("generatorUpgradeFluidCost", 2, 1, 100);
        builder.pop();
    }
}
