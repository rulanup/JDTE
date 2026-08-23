package com.jdte.common.items;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdvancedMachineSettingsCopierDataTest {
    @Test
    void writesAndReadsTheTwoAutoIoMasksUnderTheRootKey() {
        CompoundTag copiedData = new CompoundTag();
        AdvancedMachineSettingsCopierData.write(copiedData, 0b11_1111, 0b10_1010);

        assertEquals(Optional.of(new AdvancedMachineSettingsCopierData.Masks(0b11_1111, 0b10_1010)),
                AdvancedMachineSettingsCopierData.read(copiedData));
        assertEquals(Set.of("jdteAutoIoConfig"), copiedData.getAllKeys());
    }

    @Test
    void clipsMasksToTheSixValidDirections() {
        CompoundTag copiedData = new CompoundTag();
        AdvancedMachineSettingsCopierData.write(copiedData, 0b11_1111 | (1 << 8), 0b10_1010 | (1 << 7));

        assertEquals(Optional.of(new AdvancedMachineSettingsCopierData.Masks(0b11_1111, 0b10_1010)),
                AdvancedMachineSettingsCopierData.read(copiedData));
    }

    @Test
    void returnsEmptyWhenTheRootTagIsMissing() {
        assertEquals(Optional.empty(), AdvancedMachineSettingsCopierData.read(new CompoundTag()));
    }

    @Test
    void returnsEmptyWhenTheInputMaskIsMissing() {
        CompoundTag copiedData = new CompoundTag();
        CompoundTag autoIoConfig = new CompoundTag();
        autoIoConfig.putInt("outputMask", 0b10_1010);
        copiedData.put(AdvancedMachineSettingsCopierData.ROOT_KEY, autoIoConfig);

        assertEquals(Optional.empty(), AdvancedMachineSettingsCopierData.read(copiedData));
    }

    @Test
    void returnsEmptyWhenTheOutputMaskIsMissing() {
        CompoundTag copiedData = new CompoundTag();
        CompoundTag autoIoConfig = new CompoundTag();
        autoIoConfig.putInt("inputMask", 0b11_1111);
        copiedData.put(AdvancedMachineSettingsCopierData.ROOT_KEY, autoIoConfig);

        assertEquals(Optional.empty(), AdvancedMachineSettingsCopierData.read(copiedData));
    }

    @Test
    void removeDeletesTheRootTag() {
        CompoundTag copiedData = new CompoundTag();
        AdvancedMachineSettingsCopierData.write(copiedData, 0b11_1111, 0b10_1010);

        AdvancedMachineSettingsCopierData.remove(copiedData);

        assertEquals(Set.of(), copiedData.getAllKeys());
    }
}
