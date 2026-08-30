package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class RepairTalismanConfig {
    public final ModConfigSpec.IntValue repairTalismanAmountPerCycle;
    public final ModConfigSpec.IntValue repairTalismanCycleInterval;
    public final ModConfigSpec.IntValue repairTalismanEnergyPerDurability;

    public RepairTalismanConfig(ModConfigSpec.Builder builder) {
        builder.comment("Repair Talisman Settings")
                .translation("config.jdte.jdte.repairTalisman")
                .push("repairTalisman");
        repairTalismanAmountPerCycle = builder
                .comment("Durability points repaired per cycle for every damaged item while a Repair Talisman is in the inventory")
                .translation("config.jdte.jdte.repairTalisman.amountPerCycle")
                .defineInRange("amountPerCycle", 5, 1, 1000);
        repairTalismanCycleInterval = builder
                .comment("Ticks between repair cycles; 0 disables the talisman")
                .translation("config.jdte.jdte.repairTalisman.cycleInterval")
                .defineInRange("cycleInterval", 4, 0, 1200);
        repairTalismanEnergyPerDurability = builder
                .comment("FE consumed per durability point repaired, drawn from charged energy items in the player's inventory; 0 makes repairs free")
                .translation("config.jdte.jdte.repairTalisman.energyPerDurability")
                .defineInRange("energyPerDurability", 10000, 0, 100000000);
        builder.pop();
    }
}
