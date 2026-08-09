package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class UltimatePortalGunConfig {
    public final ForgeConfigSpec.IntValue ultimatePortalGunFluidCapacity;
    public final ForgeConfigSpec.IntValue ultimatePortalGunEnergyCapacity;
    public final ForgeConfigSpec.IntValue energyCost;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> teleportDimensionBlacklist;

    public UltimatePortalGunConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Ultimate Portal Gun Settings")
                .translation("config.jdte.jdte.ultimatePortalGun")
                .push("ultimatePortalGun");
        ultimatePortalGunFluidCapacity = builder
                .comment("Portal Fluid capacity in mB (1000 B default)")
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
                .comment("Dimensions excluded from the teleport dimension picker")
                .translation("config.jdte.jdte.ultimatePortalGun.teleportDimensionBlacklist")
                .defineListAllowEmpty("teleportDimensionBlacklist", List.of(), entry -> entry instanceof String);
        builder.pop();
    }
}
