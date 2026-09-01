package com.jdte.common.items.machinesettings;

import com.direwolf20.justdirethings.common.blockentities.basebe.BaseMachineBE;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public interface MachineSettingsCodec {
    CompoundTag encode(BaseMachineBE machine, HolderLookup.Provider registries);

    Optional<PreparedSettings> decode(CompoundTag custom, HolderLookup.Provider registries);

    interface PreparedSettings {
        void apply(BaseMachineBE machine);
    }
}
