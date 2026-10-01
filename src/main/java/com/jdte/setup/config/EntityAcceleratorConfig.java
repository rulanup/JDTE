package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class EntityAcceleratorConfig {
    public static final int ADVANCED_BASE_ENERGY_CAPACITY = 200000;
    public static final int EXTENDED_BASE_ENERGY_CAPACITY = 500000;
    public static final int BASE_FLUID_CAPACITY = 10000;
    public static final int ADVANCED_DEFAULT_MULTIPLIER = 4;
    public static final int ADVANCED_MAX_MULTIPLIER = 64;
    public static final int ADVANCED_OVERCLOCK_MULTIPLIER = 128;
    public static final int EXTENDED_MAX_MULTIPLIER = 512;
    public static final int EXTENDED_OVERCLOCK_MULTIPLIER = 1024;
    public static final double BASE_FLUID_COST_MULTIPLIER = 1.0D;
    public static final double ADVANCED_TIER_FLUID_COST_MULTIPLIER = 2.0D;
    public static final double EXTENDED_TIER_FLUID_COST_MULTIPLIER = 5.0D;
    public static final int ADVANCED_MAX_ENTITIES_PER_TICK = 32;
    public static final int EXTENDED_MAX_ENTITIES_PER_TICK = 64;

    public final ModConfigSpec.IntValue advancedEnergyCapacity;
    public final ModConfigSpec.IntValue extendedEnergyCapacity;
    public final ModConfigSpec.IntValue baseFluidCapacity;
    public final ModConfigSpec.IntValue advancedDefaultMultiplier;
    public final ModConfigSpec.IntValue advancedMaxMultiplier;
    public final ModConfigSpec.IntValue advancedOverclockMultiplier;
    public final ModConfigSpec.IntValue extendedMaxMultiplier;
    public final ModConfigSpec.IntValue extendedOverclockMultiplier;
    public final ModConfigSpec.DoubleValue fluidCostMultiplier;
    public final ModConfigSpec.DoubleValue advancedTierFluidCostMultiplier;
    public final ModConfigSpec.DoubleValue extendedTierFluidCostMultiplier;
    public final ModConfigSpec.IntValue advancedMaxEntitiesPerTick;
    public final ModConfigSpec.IntValue extendedMaxEntitiesPerTick;

    public EntityAcceleratorConfig(ModConfigSpec.Builder builder) {
        builder.comment("Entity Accelerator Settings")
                .translation("config.jdte.jdte.entityAccelerator")
                .push("entityAccelerator");

        advancedEnergyCapacity = builder
                .translation("config.jdte.jdte.entityAccelerator.advancedEnergyCapacity")
                .defineInRange("advancedEnergyCapacity", ADVANCED_BASE_ENERGY_CAPACITY, 1000, 100000000);

        extendedEnergyCapacity = builder
                .translation("config.jdte.jdte.entityAccelerator.extendedEnergyCapacity")
                .defineInRange("extendedEnergyCapacity", EXTENDED_BASE_ENERGY_CAPACITY, 1000, 100000000);

        baseFluidCapacity = builder
                .translation("config.jdte.jdte.entityAccelerator.baseFluidCapacity")
                .defineInRange("baseFluidCapacity", BASE_FLUID_CAPACITY, 100, 100000);

        advancedDefaultMultiplier = builder
                .translation("config.jdte.jdte.entityAccelerator.advancedDefaultMultiplier")
                .defineInRange("advancedDefaultMultiplier", ADVANCED_DEFAULT_MULTIPLIER, 1, 64);

        advancedMaxMultiplier = builder
                .translation("config.jdte.jdte.entityAccelerator.advancedMaxMultiplier")
                .defineInRange("advancedMaxMultiplier", ADVANCED_MAX_MULTIPLIER, 1, 64);

        advancedOverclockMultiplier = builder
                .translation("config.jdte.jdte.entityAccelerator.advancedOverclockMultiplier")
                .defineInRange("advancedOverclockMultiplier", ADVANCED_OVERCLOCK_MULTIPLIER, 1, 256);

        extendedMaxMultiplier = builder
                .translation("config.jdte.jdte.entityAccelerator.extendedMaxMultiplier")
                .defineInRange("extendedMaxMultiplier", EXTENDED_MAX_MULTIPLIER, 1, 512);

        extendedOverclockMultiplier = builder
                .translation("config.jdte.jdte.entityAccelerator.extendedOverclockMultiplier")
                .defineInRange("extendedOverclockMultiplier", EXTENDED_OVERCLOCK_MULTIPLIER, 1, 1024);

        fluidCostMultiplier = builder
                .translation("config.jdte.jdte.entityAccelerator.fluidCostMultiplier")
                .defineInRange("fluidCostMultiplier", BASE_FLUID_COST_MULTIPLIER, 0.0D, 100.0D);

        advancedTierFluidCostMultiplier = builder
                .translation("config.jdte.jdte.entityAccelerator.advancedTierFluidCostMultiplier")
                .defineInRange("advancedTierFluidCostMultiplier", ADVANCED_TIER_FLUID_COST_MULTIPLIER, 0.1D, 100.0D);

        extendedTierFluidCostMultiplier = builder
                .translation("config.jdte.jdte.entityAccelerator.extendedTierFluidCostMultiplier")
                .defineInRange("extendedTierFluidCostMultiplier", EXTENDED_TIER_FLUID_COST_MULTIPLIER, 0.1D, 100.0D);

        advancedMaxEntitiesPerTick = builder
                .translation("config.jdte.jdte.entityAccelerator.advancedMaxEntitiesPerTick")
                .defineInRange("advancedMaxEntitiesPerTick", ADVANCED_MAX_ENTITIES_PER_TICK, 1, 512);

        extendedMaxEntitiesPerTick = builder
                .translation("config.jdte.jdte.entityAccelerator.extendedMaxEntitiesPerTick")
                .defineInRange("extendedMaxEntitiesPerTick", EXTENDED_MAX_ENTITIES_PER_TICK, 1, 512);

        builder.pop();
    }
}
