package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class UpgradeItemsConfig {
    public final ModConfigSpec.IntValue maxSharpnessUpgrades;
    public final ModConfigSpec.IntValue sharpnessDamagePerUpgrade;
    public final ModConfigSpec.IntValue maxLootingUpgrades;

    public UpgradeItemsConfig(ModConfigSpec.Builder builder) {
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
