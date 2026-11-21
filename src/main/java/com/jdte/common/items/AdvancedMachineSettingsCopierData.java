package com.jdte.common.items;

import com.jdte.common.autoioconfig.AutoIoConfigData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public final class AdvancedMachineSettingsCopierData {
    public static final String ROOT_KEY = "jdteAutoIoConfig";
    public static final String UPGRADES_KEY = "upgrades";
    public static final String UPGRADE_HANDLERS_KEY = "jdteUpgradeHandlers";
    public static final String MACHINE_SETTINGS_KEY = "jdteMachineSettings";
    private static final String MACHINE_TYPE_KEY = "machineType";
    private static final String INPUT_MASK_KEY = "inputMask";
    private static final String OUTPUT_MASK_KEY = "outputMask";

    public record Masks(int inputMask, int outputMask) {}

    public static void write(CompoundTag copiedData, Identifier machineType, int inputMask, int outputMask) {
        writeMachineType(copiedData, machineType);
        writeMasks(copiedData, inputMask, outputMask);
    }

    public static void writeMachineType(CompoundTag copiedData, Identifier machineType) {
        getOrCreateRoot(copiedData).putString(MACHINE_TYPE_KEY, machineType.toString());
    }

    public static Optional<Identifier> readMachineType(CompoundTag copiedData) {
        if (!(copiedData.get(ROOT_KEY) instanceof net.minecraft.nbt.CompoundTag)) {
            return Optional.empty();
        }

        CompoundTag autoIoConfig = copiedData.getCompoundOrEmpty(ROOT_KEY);
        if (!(autoIoConfig.get(MACHINE_TYPE_KEY) instanceof net.minecraft.nbt.StringTag)) {
            return Optional.empty();
        }

        return Optional.ofNullable(Identifier.tryParse(autoIoConfig.getStringOr(MACHINE_TYPE_KEY, "")));
    }

    public static void writeMasks(CompoundTag copiedData, int inputMask, int outputMask) {
        CompoundTag autoIoConfig = getOrCreateRoot(copiedData);
        autoIoConfig.putInt(INPUT_MASK_KEY, inputMask & AutoIoConfigData.ALL_SIDES_MASK);
        autoIoConfig.putInt(OUTPUT_MASK_KEY, outputMask & AutoIoConfigData.ALL_SIDES_MASK);
    }

    public static Optional<Masks> readMasks(CompoundTag copiedData) {
        if (!(copiedData.get(ROOT_KEY) instanceof net.minecraft.nbt.CompoundTag)) {
            return Optional.empty();
        }

        CompoundTag autoIoConfig = copiedData.getCompoundOrEmpty(ROOT_KEY);
        if (!(autoIoConfig.get(INPUT_MASK_KEY) instanceof net.minecraft.nbt.IntTag)
                || !(autoIoConfig.get(OUTPUT_MASK_KEY) instanceof net.minecraft.nbt.IntTag)) {
            return Optional.empty();
        }

        return Optional.of(new Masks(
                autoIoConfig.getIntOr(INPUT_MASK_KEY, 0) & AutoIoConfigData.ALL_SIDES_MASK,
                autoIoConfig.getIntOr(OUTPUT_MASK_KEY, 0) & AutoIoConfigData.ALL_SIDES_MASK));
    }

    public static void clearMasks(CompoundTag copiedData) {
        if (!(copiedData.get(ROOT_KEY) instanceof net.minecraft.nbt.CompoundTag)) {
            return;
        }

        CompoundTag autoIoConfig = copiedData.getCompoundOrEmpty(ROOT_KEY);
        autoIoConfig.remove(INPUT_MASK_KEY);
        autoIoConfig.remove(OUTPUT_MASK_KEY);
        if (autoIoConfig.isEmpty()) {
            copiedData.remove(ROOT_KEY);
        } else {
            copiedData.put(ROOT_KEY, autoIoConfig);
        }
    }

    public static void remove(CompoundTag copiedData) {
        copiedData.remove(ROOT_KEY);
    }

    public static void writeUpgrades(CompoundTag copiedData, CompoundTag upgrades) {
        copiedData.put(UPGRADES_KEY, upgrades.copy());
    }

    public static Optional<CompoundTag> readUpgrades(CompoundTag copiedData) {
        return copiedData.getCompound(UPGRADES_KEY);
    }

    public static void clearUpgrades(CompoundTag copiedData) {
        copiedData.remove(UPGRADES_KEY);
    }

    public static void writeUpgradeHandlers(CompoundTag copiedData, CompoundTag handlers) {
        copiedData.put(UPGRADE_HANDLERS_KEY, handlers.copy());
    }

    public static Optional<CompoundTag> readUpgradeHandlers(CompoundTag copiedData) {
        return copiedData.getCompound(UPGRADE_HANDLERS_KEY);
    }

    public static void clearUpgradeHandlers(CompoundTag copiedData) {
        copiedData.remove(UPGRADE_HANDLERS_KEY);
    }

    public static void clearMachineSettings(CompoundTag copiedData) {
        copiedData.remove(MACHINE_SETTINGS_KEY);
    }

    private static CompoundTag getOrCreateRoot(CompoundTag copiedData) {
        if ((copiedData.get(ROOT_KEY) instanceof net.minecraft.nbt.CompoundTag)) {
            return copiedData.getCompoundOrEmpty(ROOT_KEY);
        }

        CompoundTag autoIoConfig = new CompoundTag();
        copiedData.put(ROOT_KEY, autoIoConfig);
        return autoIoConfig;
    }
}
