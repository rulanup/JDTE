package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RangeBlockerConfig {
    public final ForgeConfigSpec.IntValue rangeBlockerEnergyCapacity;
    public final ForgeConfigSpec.IntValue rangeBlockerContainmentEnergyPerTick;
    public final ForgeConfigSpec.IntValue rangeBlockerDemagnetizationEnergyPerTick;
    public final ForgeConfigSpec.IntValue rangeBlockerSilenceEnergyPerTick;
    public final ForgeConfigSpec.BooleanValue rangeBlockerProtectNamed;
    public final ForgeConfigSpec.BooleanValue rangeBlockerProtectTamed;
    public final ForgeConfigSpec.BooleanValue rangeBlockerProtectBosses;
    public final ForgeConfigSpec.BooleanValue rangeBlockerMekanismIntegration;
    public final ForgeConfigSpec.BooleanValue rangeBlockerContainProjectiles;
    public final ForgeConfigSpec.BooleanValue rangeBlockerContainOwnerlessProjectiles;
    public final ForgeConfigSpec.BooleanValue rangeBlockerContainProjectileExplosions;

    public RangeBlockerConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Range Blocker Settings")
                .translation("config.jdte.jdte.rangeBlocker")
                .push("rangeBlocker");
        rangeBlockerEnergyCapacity = builder
                .translation("config.jdte.jdte.rangeBlocker.energyCapacity")
                .defineInRange("energyCapacity", 200000, 1000, 100000000);
        rangeBlockerContainmentEnergyPerTick = builder
                .translation("config.jdte.jdte.rangeBlocker.containmentEnergyPerTick")
                .defineInRange("containmentEnergyPerTick", 250, 0, 1000000);
        rangeBlockerDemagnetizationEnergyPerTick = builder
                .comment("FE consumed per active Demagnetization tick")
                .translation("config.jdte.jdte.rangeBlocker.demagnetizationEnergyPerTick")
                .defineInRange("demagnetizationEnergyPerTick", 1, 0, 1000000);
        rangeBlockerSilenceEnergyPerTick = builder
                .comment("FE consumed every tick while Silence mode is active")
                .translation("config.jdte.jdte.rangeBlocker.silenceEnergyPerTick")
                .defineInRange("silenceEnergyPerTick", 1, 0, 1000000);
        rangeBlockerProtectNamed = builder
                .translation("config.jdte.jdte.rangeBlocker.protectNamed")
                .define("protectNamed", true);
        rangeBlockerProtectTamed = builder
                .translation("config.jdte.jdte.rangeBlocker.protectTamed")
                .define("protectTamed", true);
        rangeBlockerProtectBosses = builder
                .translation("config.jdte.jdte.rangeBlocker.protectBosses")
                .define("protectBosses", true);
        rangeBlockerMekanismIntegration = builder
                .comment("Make Mekanism's MekaSuit magnetic attraction respect demagnetization fields")
                .translation("config.jdte.jdte.rangeBlocker.mekanismIntegration")
                .define("mekanismIntegration", true);
        rangeBlockerContainProjectiles = builder
                .comment("Destroy non-player projectiles before they cross a Containment field boundary")
                .translation("config.jdte.jdte.rangeBlocker.containProjectiles")
                .define("containProjectiles", true);
        rangeBlockerContainOwnerlessProjectiles = builder
                .comment("Contain projectiles with no owner, including modded projectiles that do not expose one")
                .translation("config.jdte.jdte.rangeBlocker.containOwnerlessProjectiles")
                .define("containOwnerlessProjectiles", true);
        rangeBlockerContainProjectileExplosions = builder
                .comment("Prevent explosions from contained projectiles from affecting blocks and entities outside the field")
                .translation("config.jdte.jdte.rangeBlocker.containProjectileExplosions")
                .define("containProjectileExplosions", true);
        builder.pop();
    }
}
