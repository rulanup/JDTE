package com.jdte.setup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class AEOutputConfig {
    public final ModConfigSpec.IntValue aeOutputReturnInterval;

    public AEOutputConfig(ModConfigSpec.Builder builder) {
        builder.comment("AE Output Upgrade Settings")
                .translation("config.jdte.jdte.aeOutput")
                .push("aeOutput");
        aeOutputReturnInterval = builder
                .comment("Ticks between AE return flushes on machines with the AE Output Upgrade installed")
                .translation("config.jdte.jdte.aeOutput.returnInterval")
                .defineInRange("returnInterval", 5, 1, 1200);
        builder.pop();
    }
}
