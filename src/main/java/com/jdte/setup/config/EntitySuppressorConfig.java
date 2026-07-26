package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class EntitySuppressorConfig {
    public final ModConfigSpec.IntValue entitySuppressorEnergyCapacity;
    public final ModConfigSpec.IntValue entitySuppressorEnergyPerTick;
    public final ModConfigSpec.BooleanValue entitySuppressorProtectNamed;
    public final ModConfigSpec.BooleanValue entitySuppressorProtectTamed;
    public final ModConfigSpec.BooleanValue entitySuppressorProtectBosses;
    public final ModConfigSpec.BooleanValue entitySuppressorRemoveExisting;

    public EntitySuppressorConfig(ModConfigSpec.Builder builder) {
        builder.comment("Entity Suppressor Settings")
                .translation("config.jdte.jdte.entitySuppressor")
                .push("entitySuppressor");
        entitySuppressorEnergyCapacity = builder
                .translation("config.jdte.jdte.entitySuppressor.energyCapacity")
                .defineInRange("energyCapacity", 200000, 1000, 100000000);
        entitySuppressorEnergyPerTick = builder
                .translation("config.jdte.jdte.entitySuppressor.energyPerTick")
                .defineInRange("energyPerTick", 250, 0, 1000000);
        entitySuppressorProtectNamed = builder
                .translation("config.jdte.jdte.entitySuppressor.protectNamed")
                .define("protectNamed", true);
        entitySuppressorProtectTamed = builder
                .translation("config.jdte.jdte.entitySuppressor.protectTamed")
                .define("protectTamed", true);
        entitySuppressorProtectBosses = builder
                .translation("config.jdte.jdte.entitySuppressor.protectBosses")
                .define("protectBosses", true);
        entitySuppressorRemoveExisting = builder
                .comment("Periodically remove matching existing entities while Block Entities mode is active")
                .translation("config.jdte.jdte.entitySuppressor.removeExisting")
                .define("removeExistingEntities", false);
        builder.pop();
    }
}
