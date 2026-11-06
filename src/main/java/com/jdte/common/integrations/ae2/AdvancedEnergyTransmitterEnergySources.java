package com.jdte.common.integrations.ae2;

import com.jdte.common.blockentities.AdvancedEnergyTransmitterBE;
import net.minecraftforge.fml.ModList;

public final class AdvancedEnergyTransmitterEnergySources {
    private static final boolean AVAILABLE = ModList.get().isLoaded("ae2")
            && ModList.get().isLoaded("appflux");

    private AdvancedEnergyTransmitterEnergySources() {
    }

    public static AdvancedEnergyTransmitterEnergySource create(AdvancedEnergyTransmitterBE owner) {
        return AVAILABLE
                ? new AdvancedEnergyTransmitterAE2Integration(owner)
                : AdvancedEnergyTransmitterEnergySource.NONE;
    }

}
