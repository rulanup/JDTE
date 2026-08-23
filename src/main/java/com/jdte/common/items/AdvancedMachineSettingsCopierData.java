package com.jdte.common.items;

import com.jdte.common.autoioconfig.AutoIoConfigData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;

public final class AdvancedMachineSettingsCopierData {
    public static final String ROOT_KEY = "jdteAutoIoConfig";

    public record Masks(int inputMask, int outputMask) {}

    public static void write(CompoundTag copiedData, int inputMask, int outputMask) {
        CompoundTag autoIoConfig = new CompoundTag();
        autoIoConfig.putInt("inputMask", inputMask & AutoIoConfigData.ALL_SIDES_MASK);
        autoIoConfig.putInt("outputMask", outputMask & AutoIoConfigData.ALL_SIDES_MASK);
        copiedData.put(ROOT_KEY, autoIoConfig);
    }

    public static Optional<Masks> read(CompoundTag copiedData) {
        if (!copiedData.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }

        CompoundTag autoIoConfig = copiedData.getCompound(ROOT_KEY);
        if (!autoIoConfig.contains("inputMask", Tag.TAG_INT)
                || !autoIoConfig.contains("outputMask", Tag.TAG_INT)) {
            return Optional.empty();
        }

        return Optional.of(new Masks(
                autoIoConfig.getInt("inputMask") & AutoIoConfigData.ALL_SIDES_MASK,
                autoIoConfig.getInt("outputMask") & AutoIoConfigData.ALL_SIDES_MASK));
    }

    public static void remove(CompoundTag copiedData) {
        copiedData.remove(ROOT_KEY);
    }
}
