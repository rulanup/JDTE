package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class UltimatePortalGunConfig {
    public final ModConfigSpec.IntValue ultimatePortalGunFluidCapacity;
    public final ModConfigSpec.IntValue ultimatePortalGunEnergyCapacity;
    public final ModConfigSpec.IntValue energyCost;
    public final ModConfigSpec.ConfigValue<List<? extends String>> teleportDimensionBlacklist;

    public UltimatePortalGunConfig(ModConfigSpec.Builder builder) {
        builder.comment("Ultimate Portal Gun Settings")
                .translation("config.jdte.jdte.ultimatePortalGun")
                .push("ultimatePortalGun");
        ultimatePortalGunFluidCapacity = builder
                .comment("Time Fluid capacity in mB (1000 B default)")
                .translation("config.jdte.jdte.ultimatePortalGun.fluidCapacity")
                .defineInRange("fluidCapacity", 1000000, 1000, 100000000);
        ultimatePortalGunEnergyCapacity = builder
                .translation("config.jdte.jdte.ultimatePortalGun.energyCapacity")
                .defineInRange("energyCapacity", 1000000, 1000, Integer.MAX_VALUE);
        energyCost = builder
                .comment("FE consumed per teleport")
                .translation("config.jdte.jdte.ultimatePortalGun.energyCost")
                .defineInRange("energyCost", 100, 1, 100000);
        teleportDimensionBlacklist = builder
                .comment("Dimensions excluded from the teleport dimension picker, using resource location ids (e.g. \"minecraft:the_end\")")
                .translation("config.jdte.jdte.ultimatePortalGun.teleportDimensionBlacklist")
                .defineListAllowEmpty("teleportDimensionBlacklist", List.of(),
                        entry -> entry instanceof String);
        builder.pop();
    }
}
