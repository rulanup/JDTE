package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class GelGeneratorConfig {
    public final ModConfigSpec.IntValue gelGeneratorInputSlots;
    public final ModConfigSpec.IntValue gelGeneratorOutputSlots;
    public final ModConfigSpec.IntValue gelGeneratorFluidCapacity;
    public final ModConfigSpec.IntValue gelGeneratorEnergyCapacity;
    public final ModConfigSpec.IntValue gelGeneratorFluidConversionAmount;
    public final ModConfigSpec.IntValue gelGeneratorFuelUsesPerItem;
    public final ModConfigSpec.IntValue gelGeneratorEnergyCost;

    public GelGeneratorConfig(ModConfigSpec.Builder builder) {
        builder.comment("Gel Generator Settings").translation("config.jdte.jdte.gelGenerator").push("gelGenerator");
        gelGeneratorInputSlots = builder
                .comment("Number of input slots")
                .translation("config.jdte.jdte.gelGenerator.gelGeneratorInputSlots")
                .defineInRange("gelGeneratorInputSlots", 4, 1, 27);
        gelGeneratorOutputSlots = builder
                .comment("Number of output slots")
                .translation("config.jdte.jdte.gelGenerator.gelGeneratorOutputSlots")
                .defineInRange("gelGeneratorOutputSlots", 4, 1, 27);
        gelGeneratorFluidCapacity = builder
                .comment("Base fluid capacity (mB)")
                .translation("config.jdte.jdte.gelGenerator.gelGeneratorFluidCapacity")
                .defineInRange("gelGeneratorFluidCapacity", 4000, 100, 1000000);
        gelGeneratorEnergyCapacity = builder
                .comment("Base energy capacity")
                .translation("config.jdte.jdte.gelGenerator.gelGeneratorEnergyCapacity")
                .defineInRange("gelGeneratorEnergyCapacity", 100000, 1000, 10000000);
        gelGeneratorFluidConversionAmount = builder
                .comment("Fluid conversion amount per operation (mB)")
                .translation("config.jdte.jdte.gelGenerator.gelGeneratorFluidConversionAmount")
                .defineInRange("gelGeneratorFluidConversionAmount", 1000, 1, 100000);
        gelGeneratorFuelUsesPerItem = builder
                .comment("Number of uses per food item")
                .translation("config.jdte.jdte.gelGenerator.gelGeneratorFuelUsesPerItem")
                .defineInRange("gelGeneratorFuelUsesPerItem", 2, 1, 100);
        gelGeneratorEnergyCost = builder
                .comment("Base energy cost per conversion")
                .translation("config.jdte.jdte.gelGenerator.gelGeneratorEnergyCost")
                .defineInRange("gelGeneratorEnergyCost", 1000, 10, 100000);
        builder.pop();
    }
}
