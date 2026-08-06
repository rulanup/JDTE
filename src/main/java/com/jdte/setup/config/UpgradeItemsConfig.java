package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class UpgradeItemsConfig {
    public final ForgeConfigSpec.IntValue maxSharpnessUpgrades;
    public final ForgeConfigSpec.IntValue sharpnessDamagePerUpgrade;
    public final ForgeConfigSpec.IntValue maxLootingUpgrades;

    public UpgradeItemsConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Upgrade Item Settings").translation("config.jdte.jdte.upgradeItems").push("upgradeItems");
        maxSharpnessUpgrades = builder
                .comment("Max sharpness upgrades stackable")
                .translation("config.jdte.jdte.upgradeItems.maxSharpnessUpgrades")
                .defineInRange("maxSharpnessUpgrades", 6, 1, 64);
        sharpnessDamagePerUpgrade = builder
                .comment("Damage added per sharpness upgrade")
                .translation("config.jdte.jdte.upgradeItems.sharpnessDamagePerUpgrade")
                .defineInRange("sharpnessDamagePerUpgrade", 5, 1, 100);
        maxLootingUpgrades = builder
                .comment("Max looting upgrades stackable")
                .translation("config.jdte.jdte.upgradeItems.maxLootingUpgrades")
                .defineInRange("maxLootingUpgrades", 6, 1, 64);
        builder.pop();
    }
}
