package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class UpgradesConfig {
    public final ForgeConfigSpec.IntValue filterSlotsPerUpgrade;
    public final ForgeConfigSpec.DoubleValue underclockEnergyMultiplier;
    public final ForgeConfigSpec.IntValue overclockEnergyMultiplier;
    public final ForgeConfigSpec.IntValue underclockTickSpeed;
    public final ForgeConfigSpec.IntValue overclockTickSpeed;
    public final ForgeConfigSpec.DoubleValue baseAreaRadius;
    public final ForgeConfigSpec.IntValue baseAreaOffset;

    public UpgradesConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Upgrade System Settings").translation("config.jdte.jdte.upgrades").push("upgrades");
        filterSlotsPerUpgrade = builder
                .comment("Number of extra filter slots per filter upgrade card")
                .translation("config.jdte.jdte.upgrades.filterSlotsPerUpgrade")
                .defineInRange("filterSlotsPerUpgrade", 9, 1, 27);
        underclockEnergyMultiplier = builder
                .comment("Underclock energy cost multiplier (0.2 = 20% of original)")
                .translation("config.jdte.jdte.upgrades.underclockEnergyMultiplier")
                .defineInRange("underclockEnergyMultiplier", 0.2D, 0.01D, 1.0D);
        overclockEnergyMultiplier = builder
                .comment("Overclock energy cost multiplier (3 = 3x original)")
                .translation("config.jdte.jdte.upgrades.overclockEnergyMultiplier")
                .defineInRange("overclockEnergyMultiplier", 3, 1, 10);
        underclockTickSpeed = builder
                .comment("Underclock tick speed (locks machine to this tick interval)")
                .translation("config.jdte.jdte.upgrades.underclockTickSpeed")
                .defineInRange("underclockTickSpeed", 40, 1, 100);
        overclockTickSpeed = builder
                .comment("Overclock tick speed (locks machine to this tick interval)")
                .translation("config.jdte.jdte.upgrades.overclockTickSpeed")
                .defineInRange("overclockTickSpeed", 1, 1, 10);
        baseAreaRadius = builder
                .comment("Base area radius for range upgrade")
                .translation("config.jdte.jdte.upgrades.baseAreaRadius")
                .defineInRange("baseAreaRadius", 5.0D, 1.0D, 50.0D);
        baseAreaOffset = builder
                .comment("Base area offset for range upgrade")
                .translation("config.jdte.jdte.upgrades.baseAreaOffset")
                .defineInRange("baseAreaOffset", 9, 0, 50);
        builder.pop();
    }
}
