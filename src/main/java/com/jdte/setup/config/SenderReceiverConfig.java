package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SenderReceiverConfig {
    public final ModConfigSpec.IntValue senderStorageSlots;
    public final ModConfigSpec.IntValue advancedItemSenderEnergyCapacity;
    public final ModConfigSpec.IntValue advancedItemSenderEnergyCost;
    public final ModConfigSpec.IntValue extendedItemSenderEnergyCapacity;
    public final ModConfigSpec.IntValue advancedFluidSenderEnergyCapacity;
    public final ModConfigSpec.IntValue advancedFluidSenderEnergyCost;
    public final ModConfigSpec.IntValue extendedFluidSenderEnergyCapacity;
    public final ModConfigSpec.IntValue fluidSenderFluidCapacity;
    public final ModConfigSpec.BooleanValue fluidSenderUnlimitedTransfer;
    public final ModConfigSpec.IntValue autoIoItemTransferRate;
    public final ModConfigSpec.IntValue autoIoFluidTransferRate;
    public final ModConfigSpec.IntValue senderReceiverItemTransferRate;
    public final ModConfigSpec.IntValue senderReceiverOverclockItemTransferRate;
    public final ModConfigSpec.IntValue senderReceiverFluidTransferRate;
    public final ModConfigSpec.IntValue senderReceiverOverclockFluidTransferRate;
    public final ModConfigSpec.IntValue transferFailureBackoffStart;
    public final ModConfigSpec.IntValue transferFailureBackoffMax;

    public SenderReceiverConfig(ModConfigSpec.Builder builder) {
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
