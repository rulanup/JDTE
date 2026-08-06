package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class AdvancedItemCollectorConfig {
    public final ModConfigSpec.BooleanValue advancedItemCollectorPreDrainEnabled;
    public final ModConfigSpec.IntValue advancedItemCollectorPreDrainThreshold;
    public final ModConfigSpec.BooleanValue advancedItemCollectorMeDirectTransferEnabled;
    public final ModConfigSpec.BooleanValue advancedItemCollectorExistingItemScanEnabled;
    public final ModConfigSpec.IntValue advancedItemCollectorExistingItemScanInterval;
    public final ModConfigSpec.IntValue advancedItemCollectorExistingItemScanLimit;

    public AdvancedItemCollectorConfig(ModConfigSpec.Builder builder) {
        builder.comment("Advanced Item Collector Settings")
                .translation("config.jdte.jdte.advancedItemCollector")
                .push("advancedItemCollector");
        advancedItemCollectorPreDrainEnabled = builder
                .comment("Directly transfer oversized container slots before player block breaking creates item entities")
                .translation("config.jdte.jdte.advancedItemCollector.preDrainEnabled")
                .define("preDrainEnabled", true);
        advancedItemCollectorPreDrainThreshold = builder
                .comment("Minimum item count in one source slot that enables pre-break direct transfer")
                .translation("config.jdte.jdte.advancedItemCollector.preDrainThreshold")
                .defineInRange("preDrainThreshold", 10_000_000, 65, Integer.MAX_VALUE);
        advancedItemCollectorMeDirectTransferEnabled = builder
                .comment("Use direct AE2 ME storage insertion when the attached interface item handler cannot accept a complete collected stack")
                .translation("config.jdte.jdte.advancedItemCollector.meDirectTransferEnabled")
                .define("meDirectTransferEnabled", true);
        advancedItemCollectorExistingItemScanEnabled = builder
                .comment("Periodically collect item entities that already exist in configured collector areas")
                .translation("config.jdte.jdte.advancedItemCollector.existingItemScanEnabled")
                .define("existingItemScanEnabled", true);
        advancedItemCollectorExistingItemScanInterval = builder
                .comment("Minimum ticks between existing-item scans for each collector")
                .translation("config.jdte.jdte.advancedItemCollector.existingItemScanInterval")
                .defineInRange("existingItemScanInterval", 10, 1, 1200);
        advancedItemCollectorExistingItemScanLimit = builder
                .comment("Maximum existing item entities processed by one collector scan")
                .translation("config.jdte.jdte.advancedItemCollector.existingItemScanLimit")
                .defineInRange("existingItemScanLimit", 256, 1, 4096);
        builder.pop();
    }
}
