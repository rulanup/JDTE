package com.jdte.setup.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SenderReceiverConfig {
    public final ForgeConfigSpec.IntValue senderStorageSlots;
    public final ForgeConfigSpec.IntValue advancedItemSenderEnergyCapacity;
    public final ForgeConfigSpec.IntValue advancedItemSenderEnergyCost;
    public final ForgeConfigSpec.IntValue extendedItemSenderEnergyCapacity;
    public final ForgeConfigSpec.IntValue advancedFluidSenderEnergyCapacity;
    public final ForgeConfigSpec.IntValue advancedFluidSenderEnergyCost;
    public final ForgeConfigSpec.IntValue extendedFluidSenderEnergyCapacity;
    public final ForgeConfigSpec.IntValue fluidSenderFluidCapacity;
    public final ForgeConfigSpec.BooleanValue fluidSenderUnlimitedTransfer;
    public final ForgeConfigSpec.IntValue autoIoItemTransferRate;
    public final ForgeConfigSpec.IntValue autoIoFluidTransferRate;
    public final ForgeConfigSpec.IntValue senderReceiverItemTransferRate;
    public final ForgeConfigSpec.IntValue senderReceiverOverclockItemTransferRate;
    public final ForgeConfigSpec.IntValue senderReceiverFluidTransferRate;
    public final ForgeConfigSpec.IntValue senderReceiverOverclockFluidTransferRate;
    public final ForgeConfigSpec.IntValue transferFailureBackoffStart;
    public final ForgeConfigSpec.IntValue transferFailureBackoffMax;

    public SenderReceiverConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Sender/Receiver Settings").translation("config.jdte.jdte.senderReceiver").push("senderReceiver");
        senderStorageSlots = builder
                .comment("Internal storage slots for item sender/receiver")
                .translation("config.jdte.jdte.senderReceiver.senderStorageSlots")
                .defineInRange("senderStorageSlots", 9, 1, 54);
        advancedItemSenderEnergyCapacity = builder
                .comment("Advanced item sender energy capacity")
                .translation("config.jdte.jdte.senderReceiver.advancedItemSenderEnergyCapacity")
                .defineInRange("advancedItemSenderEnergyCapacity", 50000, 1000, 10000000);
        advancedItemSenderEnergyCost = builder
                .comment("Advanced item sender energy cost per cycle")
                .translation("config.jdte.jdte.senderReceiver.advancedItemSenderEnergyCost")
                .defineInRange("advancedItemSenderEnergyCost", 500, 10, 100000);
        extendedItemSenderEnergyCapacity = builder
                .comment("Extended item sender energy capacity")
                .translation("config.jdte.jdte.senderReceiver.extendedItemSenderEnergyCapacity")
                .defineInRange("extendedItemSenderEnergyCapacity", 100000, 1000, 10000000);
        advancedFluidSenderEnergyCapacity = builder
                .comment("Advanced fluid sender energy capacity")
                .translation("config.jdte.jdte.senderReceiver.advancedFluidSenderEnergyCapacity")
                .defineInRange("advancedFluidSenderEnergyCapacity", 50000, 1000, 10000000);
        advancedFluidSenderEnergyCost = builder
                .comment("Advanced fluid sender energy cost per cycle")
                .translation("config.jdte.jdte.senderReceiver.advancedFluidSenderEnergyCost")
                .defineInRange("advancedFluidSenderEnergyCost", 500, 10, 100000);
        extendedFluidSenderEnergyCapacity = builder
                .comment("Extended fluid sender energy capacity")
                .translation("config.jdte.jdte.senderReceiver.extendedFluidSenderEnergyCapacity")
                .defineInRange("extendedFluidSenderEnergyCapacity", 100000, 1000, 10000000);
        fluidSenderFluidCapacity = builder
                .comment("Fluid sender/receiver base fluid capacity (mB)")
                .translation("config.jdte.jdte.senderReceiver.fluidSenderFluidCapacity")
                .defineInRange("fluidSenderFluidCapacity", 8000, 100, 1000000);
        fluidSenderUnlimitedTransfer = builder
                .comment("When enabled, Fluid Senders move all available internal fluid per operation instead of using the configured fluid batch limits")
                .translation("config.jdte.jdte.senderReceiver.fluidSenderUnlimitedTransfer")
                .define("fluidSenderUnlimitedTransfer", true);
        autoIoItemTransferRate = builder
                .comment("Maximum items transferred per side and auto I/O operation. Default matches Logistics Network's Netherite tier.")
                .translation("config.jdte.jdte.senderReceiver.autoIoItemTransferRate")
                .defineInRange("autoIoItemTransferRate", 10000, 1, 1000000);
        autoIoFluidTransferRate = builder
                .comment("Maximum fluid transferred per side and auto I/O operation in mB. Default matches Logistics Network's Netherite tier.")
                .translation("config.jdte.jdte.senderReceiver.autoIoFluidTransferRate")
                .defineInRange("autoIoFluidTransferRate", 1000000, 1, Integer.MAX_VALUE);
        senderReceiverItemTransferRate = builder
                .comment("Maximum items moved per sender/receiver operation without Overclock. Default matches Logistics Network's Diamond tier.")
                .translation("config.jdte.jdte.senderReceiver.senderReceiverItemTransferRate")
                .defineInRange("senderReceiverItemTransferRate", 64, 1, 1000000);
        senderReceiverOverclockItemTransferRate = builder
                .comment("Maximum items moved per sender/receiver operation with Overclock or Creative. Default matches Logistics Network's Netherite tier.")
                .translation("config.jdte.jdte.senderReceiver.senderReceiverOverclockItemTransferRate")
                .defineInRange("senderReceiverOverclockItemTransferRate", 10000, 1, 1000000);
        senderReceiverFluidTransferRate = builder
                .comment("Maximum fluid moved per sender/receiver operation without Overclock in mB. Default matches Logistics Network's Diamond tier.")
                .translation("config.jdte.jdte.senderReceiver.senderReceiverFluidTransferRate")
                .defineInRange("senderReceiverFluidTransferRate", 20000, 1, Integer.MAX_VALUE);
        senderReceiverOverclockFluidTransferRate = builder
                .comment("Maximum fluid moved per sender/receiver operation with Overclock or Creative in mB. Default matches Logistics Network's Netherite tier.")
                .translation("config.jdte.jdte.senderReceiver.senderReceiverOverclockFluidTransferRate")
                .defineInRange("senderReceiverOverclockFluidTransferRate", 1000000, 1, Integer.MAX_VALUE);
        transferFailureBackoffStart = builder
                .comment("Initial idle retry delay for auto I/O and sender/receiver transfers in ticks")
                .translation("config.jdte.jdte.senderReceiver.transferFailureBackoffStart")
                .defineInRange("transferFailureBackoffStart", 10, 1, 200);
        transferFailureBackoffMax = builder
                .comment("Maximum idle retry delay for auto I/O and sender/receiver transfers in ticks")
                .translation("config.jdte.jdte.senderReceiver.transferFailureBackoffMax")
                .defineInRange("transferFailureBackoffMax", 40, 1, 1200);
        builder.pop();
    }
}
