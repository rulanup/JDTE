package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class RangeBlockerConfig {
    public final ModConfigSpec.IntValue rangeBlockerEnergyCapacity;
    public final ModConfigSpec.IntValue rangeBlockerContainmentEnergyPerTick;
    public final ModConfigSpec.IntValue rangeBlockerDemagnetizationEnergyPerTick;
    public final ModConfigSpec.IntValue rangeBlockerSilenceEnergyPerTick;
    public final ModConfigSpec.BooleanValue rangeBlockerProtectNamed;
    public final ModConfigSpec.BooleanValue rangeBlockerProtectTamed;
    public final ModConfigSpec.BooleanValue rangeBlockerProtectBosses;
    public final ModConfigSpec.BooleanValue rangeBlockerMekanismIntegration;
    public final ModConfigSpec.BooleanValue rangeBlockerContainProjectiles;
    public final ModConfigSpec.BooleanValue rangeBlockerContainOwnerlessProjectiles;
    public final ModConfigSpec.BooleanValue rangeBlockerContainProjectileExplosions;

    public RangeBlockerConfig(ModConfigSpec.Builder builder) {
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
