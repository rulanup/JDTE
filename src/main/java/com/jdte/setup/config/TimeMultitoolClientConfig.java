package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TimeMultitoolClientConfig {
    public final ModConfigSpec.IntValue continuousMiningHoldDelayMillis;

    public TimeMultitoolClientConfig(ModConfigSpec.Builder builder) {
        builder.translation("config.jdte.jdte.localTimeMultitool").push("timeMultitool");
        continuousMiningHoldDelayMillis = builder
                .comment("Hold attack this long before switching to additional blocks. 0 restores continuous mining immediately.")
                .translation("config.jdte.jdte.localTimeMultitool.continuousMiningHoldDelayMillis")
                .defineInRange("continuousMiningHoldDelayMillis", 250, 0, 2000);
        builder.pop();
    }
}
