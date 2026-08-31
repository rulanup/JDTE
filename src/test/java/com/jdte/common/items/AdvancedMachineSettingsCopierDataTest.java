package com.jdte.common.items;

import com.direwolf20.justdirethings.common.items.MachineSettingsCopier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedMachineSettingsCopierDataTest {
    private static final ResourceLocation MACHINE_TYPE = ResourceLocation.parse("jdte:test_machine");

    @Test
    void writesAndReadsTheTwoAutoIoMasksUnderTheRootKey() {
        CompoundTag copiedData = new CompoundTag();
        AdvancedMachineSettingsCopierData.write(copiedData, MACHINE_TYPE, 0b11_1111, 0b10_1010);

        assertEquals(Optional.of(MACHINE_TYPE), AdvancedMachineSettingsCopierData.readMachineType(copiedData));
        assertEquals(Optional.of(new AdvancedMachineSettingsCopierData.Masks(0b11_1111, 0b10_1010)),
                AdvancedMachineSettingsCopierData.readMasks(copiedData));
        assertEquals(Set.of("jdteAutoIoConfig"), copiedData.getAllKeys());
        assertEquals(Set.of("machineType", "inputMask", "outputMask"),
                copiedData.getCompound(AdvancedMachineSettingsCopierData.ROOT_KEY).getAllKeys());
    }

    @Test
    void remainsAJdtMachineSettingsCopier() {
        assertTrue(MachineSettingsCopier.class.isAssignableFrom(AdvancedMachineSettingsCopierItem.class));
        assertEquals(MachineSettingsCopier.class, AdvancedMachineSettingsCopierItem.class.getSuperclass());
    }

    @Test
    void clipsMasksToTheSixValidDirections() {
        CompoundTag copiedData = new CompoundTag();
        AdvancedMachineSettingsCopierData.write(copiedData, MACHINE_TYPE,
                0b11_1111 | (1 << 8), 0b10_1010 | (1 << 7));

        assertEquals(Optional.of(new AdvancedMachineSettingsCopierData.Masks(0b11_1111, 0b10_1010)),
                AdvancedMachineSettingsCopierData.readMasks(copiedData));
    }

    @Test
    void returnsEmptyWhenTheRootTagIsMissing() {
        assertEquals(Optional.empty(), AdvancedMachineSettingsCopierData.readMachineType(new CompoundTag()));
        assertEquals(Optional.empty(), AdvancedMachineSettingsCopierData.readMasks(new CompoundTag()));
    }

    @Test
    void returnsEmptyWhenTheMachineTypeIsMissing() {
        CompoundTag copiedData = new CompoundTag();
        CompoundTag autoIoConfig = new CompoundTag();
        autoIoConfig.putInt("inputMask", 0b11_1111);
        autoIoConfig.putInt("outputMask", 0b10_1010);
        copiedData.put(AdvancedMachineSettingsCopierData.ROOT_KEY, autoIoConfig);

        assertEquals(Optional.empty(), AdvancedMachineSettingsCopierData.readMachineType(copiedData));
    }

    @Test
    void returnsEmptyWhenTheMachineTypeIsInvalid() {
        CompoundTag copiedData = new CompoundTag();
        CompoundTag autoIoConfig = new CompoundTag();
        autoIoConfig.putString("machineType", "not a resource location");
        copiedData.put(AdvancedMachineSettingsCopierData.ROOT_KEY, autoIoConfig);

        assertEquals(Optional.empty(), AdvancedMachineSettingsCopierData.readMachineType(copiedData));
    }

    @Test
    void returnsEmptyWhenTheInputMaskIsMissing() {
        CompoundTag copiedData = new CompoundTag();
        CompoundTag autoIoConfig = new CompoundTag();
        autoIoConfig.putString("machineType", MACHINE_TYPE.toString());
        autoIoConfig.putInt("outputMask", 0b10_1010);
        copiedData.put(AdvancedMachineSettingsCopierData.ROOT_KEY, autoIoConfig);

        assertEquals(Optional.empty(), AdvancedMachineSettingsCopierData.readMasks(copiedData));
    }

    @Test
    void returnsEmptyWhenTheOutputMaskIsMissing() {
        CompoundTag copiedData = new CompoundTag();
        CompoundTag autoIoConfig = new CompoundTag();
        autoIoConfig.putString("machineType", MACHINE_TYPE.toString());
        autoIoConfig.putInt("inputMask", 0b11_1111);
        copiedData.put(AdvancedMachineSettingsCopierData.ROOT_KEY, autoIoConfig);

        assertEquals(Optional.empty(), AdvancedMachineSettingsCopierData.readMasks(copiedData));
    }

    @Test
    void clearMasksKeepsTheTypeFingerprint() {
        CompoundTag copiedData = new CompoundTag();
        AdvancedMachineSettingsCopierData.write(copiedData, MACHINE_TYPE, 0b11_1111, 0b10_1010);

        AdvancedMachineSettingsCopierData.clearMasks(copiedData);

        assertEquals(Optional.of(MACHINE_TYPE), AdvancedMachineSettingsCopierData.readMachineType(copiedData));
        assertEquals(Optional.empty(), AdvancedMachineSettingsCopierData.readMasks(copiedData));
    }

    @Test
    void removeDeletesTheRootTag() {
        CompoundTag copiedData = new CompoundTag();
        AdvancedMachineSettingsCopierData.write(copiedData, MACHINE_TYPE, 0b11_1111, 0b10_1010);

        AdvancedMachineSettingsCopierData.remove(copiedData);

        assertEquals(Set.of(), copiedData.getAllKeys());
    }

    @Test
    void storesAndClearsSerializedUpgradeData() {
        CompoundTag copiedData = new CompoundTag();
        CompoundTag upgrades = new CompoundTag();
        upgrades.putInt("Size", 4);

        AdvancedMachineSettingsCopierData.writeUpgrades(copiedData, upgrades);

        assertEquals(Optional.of(upgrades), AdvancedMachineSettingsCopierData.readUpgrades(copiedData));
        AdvancedMachineSettingsCopierData.clearUpgrades(copiedData);
        assertEquals(Optional.empty(), AdvancedMachineSettingsCopierData.readUpgrades(copiedData));
    }
}
