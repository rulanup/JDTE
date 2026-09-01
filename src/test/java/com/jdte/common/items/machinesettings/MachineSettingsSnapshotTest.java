package com.jdte.common.items.machinesettings;

import com.jdte.common.items.AdvancedMachineSettingsCopierData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineSettingsSnapshotTest {
    @Test
    void roundTripsCommonSettingsAndCustomSettings() {
        CompoundTag copied = new CompoundTag();
        MachineSettingsSnapshot.write(copied,
                ResourceLocation.parse("justdirethings:clicker_t2"),
                7, 4, new CompoundTag());

        MachineSettingsSnapshot snapshot = MachineSettingsSnapshot.read(copied).orElseThrow();

        assertEquals(1, snapshot.schemaVersion());
        assertEquals(7, snapshot.tickSpeed());
        assertEquals(4, snapshot.direction());
        assertEquals(ResourceLocation.parse("justdirethings:clicker_t2"), snapshot.machineType());
    }

    @Test
    void rejectsInvalidCommonValuesBeforeApplyingAnything() {
        CompoundTag copied = new CompoundTag();
        CompoundTag settings = new CompoundTag();
        settings.putInt("schemaVersion", 1);
        settings.putString("machineType", "justdirethings:clicker_t2");
        settings.putInt("tickSpeed", -1);
        settings.putInt("direction", 6);
        settings.put("custom", new CompoundTag());
        copied.put("jdteMachineSettings", settings);

        assertTrue(MachineSettingsSnapshot.read(copied).isEmpty());
    }

    @Test
    void acceptsLegacyCopiedDataWithoutTheNewSettingsNode() {
        CompoundTag copied = new CompoundTag();
        AdvancedMachineSettingsCopierData.writeMachineType(
                copied, ResourceLocation.parse("justdirethings:clicker_t1"));

        assertTrue(MachineSettingsSnapshot.read(copied).isEmpty());
        assertEquals(Optional.of(ResourceLocation.parse("justdirethings:clicker_t1")),
                AdvancedMachineSettingsCopierData.readMachineType(copied));
    }

    @Test
    void deepCopiesCustomSettingsWhenWritingAndReading() {
        CompoundTag custom = new CompoundTag();
        custom.putInt("Size", 4);

        CompoundTag copied = new CompoundTag();
        MachineSettingsSnapshot.write(copied,
                ResourceLocation.parse("justdirethings:clicker_t2"),
                7, 4, custom);

        custom.putInt("Size", 9);

        MachineSettingsSnapshot snapshot = MachineSettingsSnapshot.read(copied).orElseThrow();
        snapshot.custom().putInt("Size", 12);

        assertEquals(4, copied.getCompound("jdteMachineSettings").getCompound("custom").getInt("Size"));
    }
}
