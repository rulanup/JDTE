package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class FactoryPackerConfig {
    public final ModConfigSpec.IntValue factoryPackerEnergyCapacity;
    public final ModConfigSpec.DoubleValue factoryPackerBaseRadius;
    public final ModConfigSpec.IntValue factoryPackerEnergyPerBlock;
    public final ModConfigSpec.IntValue factoryPackerBlocksPerTick;
    public final ModConfigSpec.IntValue factoryPackerSourceChangeRetries;
    public final ModConfigSpec.IntValue factoryPackerMaxAxis;
    public final ModConfigSpec.IntValue factoryPackerMaxVolume;
    public final ModConfigSpec.IntValue factoryPackerMaxCompressedBytes;
    public final ModConfigSpec.IntValue factoryPackerMaxUncompressedBytes;
    public final ModConfigSpec.IntValue factoryPackerPreviewMaxBlocks;
    public final ModConfigSpec.IntValue factoryPackerMaxEntities;
    public final ModConfigSpec.IntValue factoryPackerEnergyPerEntity;
    public final ModConfigSpec.BooleanValue factoryPackerMoveEntities;
    public final ModConfigSpec.BooleanValue factoryPackerMoveScheduledTicks;
    public final ModConfigSpec.BooleanValue factoryPackerRemapInternalLinks;
    public final ModConfigSpec.BooleanValue factoryPackerUseModMoveStrategies;
    public final ModConfigSpec.BooleanValue factoryPackerChatNotifications;

    public FactoryPackerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Factory Packer Settings")
                .translation("config.jdte.jdte.factoryPacker")
                .push("factoryPacker");
        factoryPackerEnergyCapacity = builder
                .translation("config.jdte.jdte.factoryPacker.energyCapacity")
                .defineInRange("energyCapacity", 10_000_000, 1000, Integer.MAX_VALUE);
        factoryPackerBaseRadius = builder
                .comment("Base X/Y/Z radius of the Factory Packer; Range Upgrades multiply it by powers of two")
                .translation("config.jdte.jdte.factoryPacker.baseRadius")
                .defineInRange("baseRadius", 10.0D, 1.0D, 64.0D);
        factoryPackerEnergyPerBlock = builder
                .comment("FE consumed for every non-air block captured")
                .translation("config.jdte.jdte.factoryPacker.energyPerBlock")
                .defineInRange("energyPerBlock", 100, 0, 1_000_000);
        factoryPackerBlocksPerTick = builder
                .comment("Maximum capture, cut, placement, or update operations per server tick; Overclock and Creative use four times this budget")
                .translation("config.jdte.jdte.factoryPacker.blocksPerTick")
                .defineInRange("blocksPerTick", 512, 1, 8192);
        factoryPackerSourceChangeRetries = builder
                .comment("Number of bounded recapture attempts when the source topology changes during packing")
                .translation("config.jdte.jdte.factoryPacker.sourceChangeRetries")
                .defineInRange("sourceChangeRetries", 3, 0, 16);
        factoryPackerMaxAxis = builder
                .comment("Hard per-axis safety limit; legacy values below 65 are corrected to the new default")
                .translation("config.jdte.jdte.factoryPacker.maxAxis")
                .defineInRange("maxAxis", 128, 65, 256);
        factoryPackerMaxVolume = builder
                .comment("Hard selected-volume safety limit; legacy values below 65536 are corrected to the new default")
                .translation("config.jdte.jdte.factoryPacker.maxVolume")
                .defineInRange("maxVolume", 1_000_000, 65_536, 16_777_216);
        factoryPackerMaxCompressedBytes = builder
                .translation("config.jdte.jdte.factoryPacker.maxCompressedBytes")
                .defineInRange("maxCompressedBytes", 33_554_432, 1_048_576, Integer.MAX_VALUE);
        factoryPackerMaxUncompressedBytes = builder
                .translation("config.jdte.jdte.factoryPacker.maxUncompressedBytes")
                .defineInRange("maxUncompressedBytes", 268_435_456, 1_048_576, Integer.MAX_VALUE);
        factoryPackerPreviewMaxBlocks = builder
                .comment("Maximum block positions sent to one client for a held package structure preview")
                .translation("config.jdte.jdte.factoryPacker.previewMaxBlocks")
                .defineInRange("previewMaxBlocks", 2048, 0, 16384);
        factoryPackerMaxEntities = builder
                .comment("Maximum root entities, including their passenger trees, stored in one package")
                .translation("config.jdte.jdte.factoryPacker.maxEntities")
                .defineInRange("maxEntities", 4096, 0, 65536);
        factoryPackerEnergyPerEntity = builder
                .translation("config.jdte.jdte.factoryPacker.energyPerEntity")
                .defineInRange("energyPerEntity", 100, 0, 1_000_000);
        factoryPackerMoveEntities = builder
                .translation("config.jdte.jdte.factoryPacker.moveEntities")
                .define("moveEntities", true);
        factoryPackerMoveScheduledTicks = builder
                .translation("config.jdte.jdte.factoryPacker.moveScheduledTicks")
                .define("moveScheduledTicks", true);
        factoryPackerRemapInternalLinks = builder
                .comment("Relocate recognized absolute block positions that point inside the packed area")
                .translation("config.jdte.jdte.factoryPacker.remapInternalLinks")
                .define("remapInternalLinks", true);
        factoryPackerUseModMoveStrategies = builder
                .comment("Use public mod move strategies, including AE2 block entity move hooks, when available")
                .translation("config.jdte.jdte.factoryPacker.useModMoveStrategies")
                .define("useModMoveStrategies", true);
        factoryPackerChatNotifications = builder
                .comment("Send operation phases, retries, detailed source changes, and results to the initiating player's chat")
                .translation("config.jdte.jdte.factoryPacker.chatNotifications")
                .define("chatNotifications", true);
        builder.pop();
    }
}
